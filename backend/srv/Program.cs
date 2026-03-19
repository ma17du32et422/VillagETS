using Supabase;
using Supabase.Postgrest.Attributes;
using Supabase.Postgrest.Models;
using System.Text.Json;
var builder = WebApplication.CreateBuilder(args);
var app = builder.Build();

var url = Environment.GetEnvironmentVariable("SUPABASE_URL");
var key = Environment.GetEnvironmentVariable("SUPABASE_KEY");
var options = new Supabase.SupabaseOptions
{
    AutoConnectRealtime = true
};
var supabase = new Supabase.Client(url, key, options);
await supabase.InitializeAsync();

app.MapGet("/", async () =>
{
    var result = await supabase
        .From<CategoriePublication>()
        .Get();

    return $"Count: {result.Models.Count} | Raw: {result.Content}";
});

app.MapGet("/Categorie", async () =>
{
    var result = await supabase
        .From<CategoriePublication>()
        .Get();

    return $"Count: {result.Models.Count} | Raw: {result.Content}";
});
// à tester
app.MapPost("/addCategorie", async (CategoriePublication categorie) =>
{
    var response = await supabase
        .From<CategoriePublication>()
        .Insert(categorie);

    return Results.Ok(response);
});

app.MapGet("/Utilisateur", async () =>
{
    var result = await supabase
        .From<Utilisateur>()
        .Get();

    return $"Count: {result.Models.Count} | Raw: {result.Content}";
});


app.UseStaticFiles();
app.MapFallbackToFile("index.html");
app.Run();

[Table("categorie_publication")]
class CategoriePublication : BaseModel
{
    [PrimaryKey("id_categorie_publication")]
    public string? id { get; set; }
    [Column("catagorie_base")]
    public string? catagorie_base { get; set; }
    [Column("nom")]
    public string? nom { get; set; }

};
[Table("utilisateur")]
class Utilisateur : BaseModel
{
    [PrimaryKey("id_utilisateur")]
    public string? id { get; set; }
    [Column("nom")]
    public string? nom { get; set; }
    [Column("prenom")]
    public string? prenom { get; set; }

};