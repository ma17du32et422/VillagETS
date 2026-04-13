using BCrypt.Net;
using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;
using sql;
using srv;
using Supabase;
using Supabase.Postgrest.Attributes;
using Supabase.Postgrest.Models;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using System.Text.Json;

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


var app = builder.Build();

app.UseCors("AllowFrontend");

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

UserService userService = new UserService();
UserRoutes.MapUserRoutes(app, userService, isDevelopment);

ReactionService reactionService = new ReactionService();
ReactionRoutes.MapReactionRoutes(app, reactionService);
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


app.MapGet("/me", async (HttpContext ctx) =>
{
    var token = ctx.Request.Cookies["token"];
    if (token == null) return Results.Unauthorized();

    var principal = villagets.Auth.AuthHelper.ValidateToken(token);
    if (principal == null) return Results.Unauthorized();

    var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value;

    var result = await supabase
        .From<sql.Utilisateur>()
        .Where(u => u.Id == userId)
        .Get();

    var user = result.Model;
    if (user == null) return Results.NotFound();

    return Results.Ok(new
    {
        userId = user.Id,
        pseudo = user.Pseudo,
        nom = user.Nom,
        prenom = user.Prenom,
        email = user.Email,
        photoProfil = user.PhotoProfil
    });
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
            url = fileUrl
        });
    }
    catch (Exception ex)
    {
        return Results.BadRequest(new { error = ex.Message });
    }
});

app.UseStaticFiles();


app.MapFallbackToFile("index.html");
app.Run();

record SignupRequest(string Email, string Password, string Pseudo, string Nom, string Prenom, string PhotoProfil);
record LoginRequest(string Email, string Password);

public record FeedQuery(
    string? SearchString,
    string[]? Tags,
    bool IsMarketplace
);

