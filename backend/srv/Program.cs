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


app.UseStaticFiles();
app.MapFallbackToFile("index.html");
app.Run();

[Table("categorie_publication")]
class CategoriePublication : BaseModel
{
    [PrimaryKey("id_categorie_publication")]
    public string? Id { get; set; }
    [Column("nom")]
    public string? nom { get; set; }

};