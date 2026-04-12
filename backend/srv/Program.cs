using sql;
using Supabase;
using Supabase.Postgrest.Attributes;
using Supabase.Postgrest.Models;
using System.Text.Json;
using BCrypt.Net;
using System.IdentityModel.Tokens.Jwt;
using Microsoft.IdentityModel.Tokens;
using System.Text;
using System.Security.Claims;
using srv;

//autorise les uploads à partir du site web
var builder = WebApplication.CreateBuilder(args);
var isDevelopment = builder.Environment.IsDevelopment();
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowFrontend", policy =>
    {
        if (isDevelopment)
        {
            policy.SetIsOriginAllowed(_ => true)
                  .AllowAnyHeader()
                  .AllowAnyMethod()
                  .AllowCredentials();
        }
        else
        {
            policy.WithOrigins(
                    "https://villagets.lesageserveur.com",
                    "https://apivillagets.lesageserveur.com",
                    "https://api.villagets.lesageserveur.com"
                  )
                  .AllowAnyHeader()
                  .AllowAnyMethod()
                  .AllowCredentials();
        }
    });
});

var jwtSecret = Environment.GetEnvironmentVariable("JWT_SECRET") ?? throw new Exception("JWT_SECRET environment variable is not set");

villagets.Auth.AuthHelper.Initialize(jwtSecret);

builder.Services.AddAntiforgery();

var app = builder.Build();

app.UseCors("AllowFrontend");
app.UseAntiforgery();

var url = Environment.GetEnvironmentVariable("SUPABASE_URL");
var key = Environment.GetEnvironmentVariable("SUPABASE_KEY");
var options = new Supabase.SupabaseOptions
{
    AutoConnectRealtime = true
};

SupabaseService supabaseService = new(url, key, options);
supabaseService.InitializeAsync().Wait();

var supabase = SupabaseService.GetClient();

PostService postService = new PostService();
PostRoutes.Map(app, postService);

//ROUTES
app.MapGet("/", async () =>
{
    var result = await supabase
        .From<sql.CategoriePublication>()
        .Get();

    return $"Count: {result.Models.Count} | Raw: {result.Content}";
});

app.MapGet("/Categorie", async () =>
{
    var result = await supabase
        .From<sql.CategoriePublication>()
        .Get();

    return $"Count: {result.Models.Count} | Raw: {result.Content}";
});
// à tester

app.MapPost("/addCategorie", async (sql.CategoriePublication categorie) =>
{
    var response = await supabase
        .From<sql.CategoriePublication>()
        .Insert(categorie);

    return Results.Ok(response);
});

app.MapGet("/Utilisateur", async () =>
{
    var result = await supabase
        .From<sql.Utilisateur>()
        .Get();

    return $"Count: {result.Models.Count} | Raw: {result.Content}";
});



app.MapPost("/auth/signup", async (SignupRequest req, HttpContext ctx) =>
{
    // TODO: add regex for email validation + make sure all fields are not empty

    if (req.Pseudo.Length < 3)
        return Results.BadRequest("Username must be at least 3 characters");

    if (req.Password.Length < 8)
        return Results.BadRequest("Password must be at least 8 characters");

    try
    {
        var existing = await supabase.From<sql.Utilisateur>().Where(u => u.Email == req.Email).Get();
        if (existing.Models.Count > 0)
            return Results.BadRequest("Email already in use");


        // Create utilisateur db
        var user = new Utilisateur
        {
            Pseudo = req.Pseudo,
            Nom = req.Nom,
            Prenom = req.Prenom,
            Password = BCrypt.Net.BCrypt.HashPassword(req.Password),
            Email = req.Email,
            DateCreation = DateTime.UtcNow,
            AnneeNaissance = DateTime.UtcNow

        };
        var response = await supabase.From<Utilisateur>().Insert(user);
        var newUser = response.Model!;
        var token = villagets.Auth.AuthHelper.GenerateToken(newUser.Id!, newUser.Email!, newUser.Pseudo!);

        ctx.Response.Cookies.Append("token", token, new CookieOptions
        {
            HttpOnly = true,
            Secure = !isDevelopment,
            SameSite = isDevelopment ? SameSiteMode.Lax : SameSiteMode.Strict,
            Expires = DateTimeOffset.UtcNow.AddDays(7)
        });
        // return token
        return Results.Ok(new
        {
            userId = newUser.Id
        });
    }
    catch (Exception ex)
    {
        return Results.BadRequest(ex.Message);
    }
});

app.MapPost("/auth/login", async (LoginRequest req, HttpContext ctx) =>
{
    if (string.IsNullOrWhiteSpace(req.Email) || string.IsNullOrWhiteSpace(req.Password))
        return Results.BadRequest("Email and password are required");

    try
    {
        var result = await supabase.From<Utilisateur>()
            .Where(u => u.Email == req.Email)
            .Get();

        var user = result.Models.FirstOrDefault();
        if (user == null || !BCrypt.Net.BCrypt.Verify(req.Password, user.Password))
            return Results.Unauthorized();

        var token = villagets.Auth.AuthHelper.GenerateToken(user.Id!, user.Email!, user.Pseudo!);
        ctx.Response.Cookies.Append("token", token, new CookieOptions
        {
            HttpOnly = true,
            Secure = !isDevelopment,
            SameSite = isDevelopment ? SameSiteMode.Lax : SameSiteMode.Strict,
            Expires = DateTimeOffset.UtcNow.AddDays(7)
        });
        
        return Results.Ok(new { userId = user.Id });
    }
    catch
    {
        return Results.Unauthorized();
    }
});
app.MapPost("/auth/logout", (HttpContext ctx) =>
{
    ctx.Response.Cookies.Delete("token");
    return Results.Ok();
});

app.MapGet("/me", (HttpContext ctx) =>
{
    var token = ctx.Request.Cookies["token"];
    if (token == null) return Results.Unauthorized();
    var principal = villagets.Auth.AuthHelper.ValidateToken(token);
    if (principal == null) return Results.Unauthorized();

    return Results.Ok(new { userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value });
});

//UPLOAD DE FICHIERS
var uploadFolder = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "uploads");
Directory.CreateDirectory(uploadFolder);


app.MapPost("/upload", async (HttpContext ctx) =>
{
    try
    {
        var form = await ctx.Request.ReadFormAsync();
        var file = form.Files["file"];
        var nom = form["nom"].ToString();
        var type = form["type"].ToString();

        if (file is null || file.Length == 0)
            return Results.BadRequest(new { error = "Aucun fichier fourni." });

        var extension = Path.GetExtension(file.FileName);
        var uniqueName = $"{Guid.NewGuid()}{extension}";
        var filePath = Path.Combine(uploadFolder, uniqueName);

        using var stream = File.Create(filePath);
        await file.CopyToAsync(stream);

        var baseUrl = Environment.GetEnvironmentVariable("BASE_URL") ?? "http://localhost:5000";
        var fileUrl = $"{baseUrl}/uploads/{uniqueName}";

        var fichier = new sql.Fichier
        {
            Nom = nom,
            LienFichier = fileUrl,
            Type = type
        };

        var response = await supabase
            .From<sql.Fichier>()
            .Insert(fichier);

        var savedFile = response.Models.FirstOrDefault();
        if (savedFile == null)
            return Results.BadRequest(new { error = "Impossible d'enregistrer le fichier." });

        return Results.Json(new
        {
            url = fileUrl,
            db = new
            {
                id = savedFile.Id?.ToString(), 
                nom = savedFile.Nom,
                lien_fichier = savedFile.LienFichier,
                type = savedFile.Type
            }
        });
    }
    catch (Exception ex)
    {
        return Results.BadRequest(new { error = ex.Message });
    }
})
.DisableAntiforgery();

app.UseStaticFiles();


app.MapFallbackToFile("index.html");
app.Run();

record SignupRequest(string Email, string Password, string Pseudo, string Nom, string Prenom);
record LoginRequest(string Email, string Password);

public record FeedQuery(
    string? SearchString,
    string[]? Tags,
    bool IsMarketplace
);

