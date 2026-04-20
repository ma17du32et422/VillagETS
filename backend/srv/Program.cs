using BCrypt.Net;
using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;
using sql;
using srv;
using srv.Comment;
using srv.Messaging;
using srv.Post;
using srv.Reaction;
using Supabase;
using Supabase.Postgrest.Attributes;
using Supabase.Postgrest.Models;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using System.Text.Json;
using OpenTelemetry.Resources;
using OpenTelemetry.Trace;
const bool EnablePerformanceLogging = true;
const bool EnableTracing = false;
const long MaxUploadBytes = 10 * 1024 * 1024;

//autorise les uploads à partir du site web
var builder = WebApplication.CreateBuilder(args);
var isDevelopment = builder.Environment.IsDevelopment();
var allowedUploadContentTypes = new HashSet<string>(StringComparer.OrdinalIgnoreCase)
{
    "image/jpeg",
    "image/png",
    "image/webp",
    "image/gif"
};
var allowedUploadExtensions = new HashSet<string>(StringComparer.OrdinalIgnoreCase)
{
    ".jpg",
    ".jpeg",
    ".png",
    ".webp",
    ".gif"
};
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

builder.Services.AddSingleton<MessagingService>();
var tracingEnabled = EnableTracing;
if (tracingEnabled)
{
    builder.Services.AddOpenTelemetry()
        .ConfigureResource(resource => resource
            .AddService(serviceName: builder.Environment.ApplicationName))
        .WithTracing(tracing => tracing
            .AddAspNetCoreInstrumentation()
            .AddHttpClientInstrumentation()
            .AddConsoleExporter());
}


var app = builder.Build();

PerfLogger.Enabled = EnablePerformanceLogging;

app.UseCors("AllowFrontend");
app.UseWebSockets();
app.Use(async (context, next) =>
{
    if (!PerfLogger.Enabled || context.WebSockets.IsWebSocketRequest)
    {
        await next();
        return;
    }

    var sw = System.Diagnostics.Stopwatch.StartNew();
    try
    {
        await next();
    }
    finally
    {
        sw.Stop();
        var elapsedMs = sw.ElapsedMilliseconds;
        if (elapsedMs >= 500)
        {
            app.Logger.LogWarning(
                "HTTP {Method} {Path} -> {StatusCode} in {ElapsedMs}ms",
                context.Request.Method,
                context.Request.Path,
                context.Response.StatusCode,
                elapsedMs);
        }
        else
        {
            app.Logger.LogInformation(
                "HTTP {Method} {Path} -> {StatusCode} in {ElapsedMs}ms",
                context.Request.Method,
                context.Request.Path,
                context.Response.StatusCode,
                elapsedMs);
        }
    }
});

var url = Environment.GetEnvironmentVariable("SUPABASE_URL");
if (string.IsNullOrWhiteSpace(url))
    throw new Exception("SUPABASE_URL environment variable is not set");

var key = Environment.GetEnvironmentVariable("SUPABASE_KEY");
if (string.IsNullOrWhiteSpace(key))
    throw new Exception("SUPABASE_KEY environment variable is not set");
var options = new Supabase.SupabaseOptions
{
    AutoConnectRealtime = true
};

SupabaseService supabaseService = new(url, key, options);
await supabaseService.InitializeAsync();

var supabase = SupabaseService.GetClient();

var loggerFactory = app.Services.GetRequiredService<ILoggerFactory>();

UserService userService = new UserService(loggerFactory.CreateLogger<UserService>());
srv.User.UserRoutes.MapUserRoutes(app, userService, isDevelopment);


PostService postService = new PostService(loggerFactory.CreateLogger<PostService>());
PostRoutes.Map(app, postService);

ReactionService reactionService = new ReactionService(loggerFactory.CreateLogger<ReactionService>());
ReactionRoutes.MapReactionRoutes(app, reactionService);

CommentService commentService = new CommentService(loggerFactory.CreateLogger<CommentService>());
CommentRoutes.Map(app, commentService);

MessagingRoutes.Map(app, app.Services.GetRequiredService<MessagingService>());
//ROUTES
app.MapGet("/", async () =>
{
    return Results.Ok(new { status = "ok" });
});

if (isDevelopment)
{
    app.MapGet("/Categorie", async () =>
    {
        var result = await supabase
            .From<sql.CategoriePublication>()
            .Get();

        return Results.Ok(result.Models);
    });

    app.MapPost("/addCategorie", async (sql.CategoriePublication categorie) =>
    {
        var response = await supabase
            .From<sql.CategoriePublication>()
            .Insert(categorie);

        return Results.Ok(response.Models.FirstOrDefault());
    });
}


app.MapGet("/me", async (HttpContext ctx) =>
{
    var token = ctx.Request.Cookies["token"];
    if (token == null) return Results.Unauthorized();

    var principal = villagets.Auth.AuthHelper.ValidateToken(token);
    if (principal == null) return Results.Unauthorized();

    var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value;

    var result = await PerfLogger.TimeAsync(app.Logger, "Program./me user lookup", () => supabase
        .From<sql.Utilisateur>()
        .Where(u => u.Id == userId)
        .Get());

    var user = result.Model;
    if (user == null) return Results.NotFound();

    return Results.Ok(new
    {
        userId = user.Id,
        pseudo = user.Pseudo,
        nom = user.Nom,
        prenom = user.Prenom,
        email = user.Email,
        photoProfil = user.PhotoProfil,
        mainAdmin = user.MainAdmin
    });
});

//UPLOAD DE FICHIERS
var uploadFolder = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "uploads");
Directory.CreateDirectory(uploadFolder);


app.MapPost("/upload", async (HttpContext ctx) =>
{
    string? filePath = null;
    try
    {
        var principal = villagets.Auth.AuthHelper.GetClaimsFromContext(ctx);
        if (principal == null)
            return Results.Unauthorized();

        var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrWhiteSpace(userId))
            return Results.Unauthorized();

        if (!ctx.Request.HasFormContentType)
            return Results.BadRequest(new { error = "Le formulaire d'envoi est invalide." });

        var form = await ctx.Request.ReadFormAsync();
        var file = form.Files["file"];

        if (file is null || file.Length == 0)
            return Results.BadRequest(new { error = "Aucun fichier fourni." });

        if (file.Length > MaxUploadBytes)
            return Results.BadRequest(new { error = "Le fichier dépasse la taille maximale autorisée de 10 Mo." });

        var extension = Path.GetExtension(file.FileName);
        if (!allowedUploadExtensions.Contains(extension) || !allowedUploadContentTypes.Contains(file.ContentType))
            return Results.BadRequest(new { error = "Seuls les fichiers image JPG, PNG, WEBP et GIF sont acceptés." });

        var nom = Path.GetFileName(string.IsNullOrWhiteSpace(form["nom"]) ? file.FileName : form["nom"].ToString());
        var uniqueName = $"{Guid.NewGuid()}{extension}";
        filePath = Path.Combine(uploadFolder, uniqueName);

        using var stream = File.Create(filePath);
        await file.CopyToAsync(stream);

        var baseUrl = $"{ctx.Request.Scheme}://{ctx.Request.Host}";
        var fileUrl = $"{baseUrl}/uploads/{uniqueName}";

        var fichier = new sql.Fichier
        {
            Nom = nom,
            LienFichier = fileUrl,
            Type = file.ContentType,
            IdProprietaire = Guid.TryParse(userId, out var ownerId) ? ownerId : null
        };

        var response = await PerfLogger.TimeAsync(app.Logger, "Program./upload fichier insert", () => supabase
            .From<sql.Fichier>()
            .Insert(fichier));

        var savedFile = response.Models.FirstOrDefault();
        if (savedFile == null)
        {
            if (File.Exists(filePath))
                File.Delete(filePath);
            return Results.BadRequest(new { error = "Impossible d'enregistrer le fichier." });
        }

        return Results.Json(new
        {
            url = fileUrl
        });
    }
    catch (Exception ex)
    {
        if (filePath != null && File.Exists(filePath))
            File.Delete(filePath);

        app.Logger.LogError(ex, "Upload failed");
        return Results.BadRequest(new { error = "Le téléversement a échoué." });
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

