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

//autorise les uploads à partir du site web
var builder = WebApplication.CreateBuilder(args);
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowFrontend", policy =>
    {
        policy.WithOrigins(
                "http://localhost:420",
                "https://villagets.lesageserveur.com"
              )
              .AllowAnyHeader()
              .AllowAnyMethod();
    });
});

var jwtSecret = Environment.GetEnvironmentVariable("JWT_SECRET") ?? throw new Exception("JWT_SECRET environment variable is not set");

villagets.Auth.JwtHelper.Initialize(jwtSecret);

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
var supabase = new Supabase.Client(url, key, options);
await supabase.InitializeAsync();



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
            AnneeNaissance = req.AnneeNaissance,

        };
        var response = await supabase.From<Utilisateur>().Insert(user);
        var newUser = response.Model!;
        var token = villagets.Auth.JwtHelper.GenerateToken(newUser.Id!, newUser.Email!, newUser.Pseudo!);

        ctx.Response.Cookies.Append("token", token, new CookieOptions
        {
            HttpOnly = true,
            Secure = true,
            SameSite = SameSiteMode.Strict,
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

        var token = villagets.Auth.JwtHelper.GenerateToken(user.Id!, user.Email!, user.Pseudo!);
        ctx.Response.Cookies.Append("token", token, new CookieOptions
        {
            HttpOnly = true,
            Secure = true,
            SameSite = SameSiteMode.Strict,
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
    var principal = villagets.Auth.JwtHelper.ValidateToken(token);
    if (principal == null) return Results.Unauthorized();

    return Results.Ok(new { userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value });
});

app.MapGet("/publication/{id}", async (int id) => {
    var response = await supabase
        .From<sql.Publication>()
        .Where(p => p.Id == id.ToString())
        .Get();
    return Results.Ok(response.Model);
});

app.MapPost("/publication", async (sql.Publication publication, HttpContext ctx) =>
{
    var token = ctx.Request.Cookies["token"];
    if (token == null) return Results.Unauthorized();
    var principal = villagets.Auth.JwtHelper.ValidateToken(token);
    if (principal == null) return Results.Unauthorized();

    var response = await supabase
        .From<sql.Publication>()
        .Insert(publication);
    return Results.Ok(response);
});

app.MapDelete("/publication/{id}", async (int id, HttpContext ctx) =>
{
    var token = ctx.Request.Cookies["token"];
    if (token == null) return Results.Unauthorized();
    var principal = villagets.Auth.JwtHelper.ValidateToken(token);
    if (principal == null) return Results.Unauthorized();

    var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value;

    var existing = await supabase
        .From<sql.Publication>()
        .Where(p => p.Id == id.ToString())
        .Get();

    var publication = existing.Models.FirstOrDefault();
    if (publication == null)
        return Results.NotFound("Publication not found");

    if (publication.UtilisateurId != userId)
        return Results.Forbid();

    await supabase
        .From<sql.Publication>()
        .Where(p => p.Id == id.ToString())
        .Delete();

    return Results.Ok();
});

//UPLOAD DE FICHIERS
var uploadFolder = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "uploads");
Directory.CreateDirectory(uploadFolder);
app.MapPost("/upload", async (IFormFile file) =>
{
    if (file is null || file.Length == 0)
        return Results.BadRequest("Aucun fichier fourni.");

    
    var extension = Path.GetExtension(file.FileName);
    var uniqueName = $"{Guid.NewGuid()}{extension}";
    var filePath = Path.Combine(uploadFolder, uniqueName);

    using var stream = File.Create(filePath);
    await file.CopyToAsync(stream);

    // Retourne l'URL publique
    var baseUrl = Environment.GetEnvironmentVariable("BASE_URL") ?? "http://localhost:5000";
    var fileUrl = $"{baseUrl}/uploads/{uniqueName}";

    return Results.Ok(new { url = fileUrl, fileName = uniqueName });
})
.DisableAntiforgery();

// Sert les fichiers statiques depuis wwwroot/uploads
app.UseStaticFiles();


app.MapFallbackToFile("index.html");
app.Run();

record SignupRequest(string Email, string Password, string Pseudo, string Nom, string Prenom, DateTime AnneeNaissance);
record LoginRequest(string Email, string Password);