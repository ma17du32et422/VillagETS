using BCrypt.Net;
using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;
using OpenTelemetry.Resources;
using OpenTelemetry.Trace;
using sql;
using srv;
using srv.Comment;
using srv.Messaging;
using srv.Post;
using srv.Reaction;
using srv.Upload;
using Supabase;
using Supabase.Postgrest.Attributes;
using Supabase.Postgrest.Models;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using System.Text.Json;
using villagets.Auth;

const bool EnablePerformanceLogging = true;
const bool EnableTracing = false;
const long MaxUploadBytes = 10 * 1024 * 1024;

//autorise les uploads a partir du site web
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
var blockedMessageUploadContentTypes = new HashSet<string>(StringComparer.OrdinalIgnoreCase)
{
    "text/html",
    "application/xhtml+xml",
    "image/svg+xml",
    "text/javascript",
    "application/javascript"
};
var blockedMessageUploadExtensions = new HashSet<string>(StringComparer.OrdinalIgnoreCase)
{
    ".html",
    ".htm",
    ".xhtml",
    ".svg",
    ".js",
    ".mjs"
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

var uploadFolder = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "uploads");
var uploadOptions = new UploadOptions
{
    UploadFolder = uploadFolder,
    MaxUploadBytes = MaxUploadBytes,
    AllowedUploadContentTypes = allowedUploadContentTypes,
    AllowedUploadExtensions = allowedUploadExtensions,
    AllowAnyMessageUpload = true,
    BlockedMessageUploadContentTypes = blockedMessageUploadContentTypes,
    BlockedMessageUploadExtensions = blockedMessageUploadExtensions
};

builder.Services.AddSingleton(uploadOptions);
builder.Services.AddSingleton<UploadService>();
builder.Services.AddSingleton<MessagingService>();

var tracingEnabled = EnableTracing;
if (tracingEnabled)
{
    builder.Services.AddOpenTelemetry()
        .ConfigureResource(resource => resource.AddService(serviceName: builder.Environment.ApplicationName))
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

var options = new SupabaseOptions
{
    AutoConnectRealtime = true
};

SupabaseService supabaseService = new(url, key, options);
await supabaseService.InitializeAsync();

var supabase = SupabaseService.GetClient();
var loggerFactory = app.Services.GetRequiredService<ILoggerFactory>();
var uploadService = app.Services.GetRequiredService<UploadService>();

UserService userService = new(loggerFactory.CreateLogger<UserService>());
srv.User.UserRoutes.MapUserRoutes(app, userService, isDevelopment);

PostService postService = new(loggerFactory.CreateLogger<PostService>(), uploadService);
PostRoutes.Map(app, postService);

ReactionService reactionService = new(loggerFactory.CreateLogger<ReactionService>());
ReactionRoutes.MapReactionRoutes(app, reactionService);

CommentService commentService = new(loggerFactory.CreateLogger<CommentService>());
CommentRoutes.Map(app, commentService);

MessagingRoutes.Map(app, app.Services.GetRequiredService<MessagingService>());
UploadRoutes.Map(app, uploadService);

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
            .From<CategoriePublication>()
            .Get();

        return Results.Ok(result.Models);
    });

    app.MapPost("/addCategorie", async (CategoriePublication categorie) =>
    {
        var response = await supabase
            .From<CategoriePublication>()
            .Insert(categorie);

        return Results.Ok(response.Models.FirstOrDefault());
    });
}

app.MapGet("/me", async (HttpContext ctx) =>
{
    var (user, isDeleted) = await AuthHelper.GetAuthenticatedUserAsync(ctx);
    if (isDeleted) return Results.Problem("Utilisateur supprimé.", null, statusCode: 410);
    if (user == null)
        return Results.Unauthorized();

    return Results.Ok(new
    {
        userId = user.Id,
        pseudo = user.GetDisplayPseudo(),
        nom = user.DeletedAt.HasValue ? null : user.Nom,
        prenom = user.DeletedAt.HasValue ? null : user.Prenom,
        email = user.Email,
        photoProfil = user.DeletedAt.HasValue ? null : user.PhotoProfil,
        deleted = user.DeletedAt.HasValue,
        mainAdmin = user.MainAdmin
    });
});

app.UseStaticFiles();

app.MapFallbackToFile("index.html");
app.Run();

record SignupRequest(string Email, string Password, string Pseudo, string Nom, string Prenom, string PhotoProfil);
record LoginRequest(string Email, string Password);

public record FeedQuery(
    string? SearchString,
    string[]? Tags,
    bool IsMarketplace,
    decimal? MinPrice = null,
    decimal? MaxPrice = null,
    int PageIndex = 0,
    string? SortMode = null
);
