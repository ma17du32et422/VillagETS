using Supabase;
using Supabase.Postgrest.Attributes;
using Supabase.Postgrest.Models;
using System.Text.Json;

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