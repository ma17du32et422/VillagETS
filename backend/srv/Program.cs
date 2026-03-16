using Microsoft.Extensions.FileProviders;
using Microsoft.Data.SqlClient;

var builder = WebApplication.CreateBuilder(args);
var app = builder.Build();

app.MapGet("/", () => "Hello world");

app.MapGet("/test-db", async () =>
{
    var connStr = builder.Configuration.GetConnectionString("DefaultConnection");
    try
    {
        using var conn = new SqlConnection(connStr);
        await conn.OpenAsync();
        return Results.Ok("DB connection successful!");
    }
    catch (Exception ex)
    {
        return Results.Problem(ex.Message);
    }
});

app.UseStaticFiles();
app.MapFallbackToFile("index.html");
app.Run();