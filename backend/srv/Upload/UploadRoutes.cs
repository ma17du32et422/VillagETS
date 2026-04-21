namespace srv.Upload
{
    public static class UploadRoutes
    {
        public static void Map(WebApplication app, UploadService uploadService)
        {
            app.MapPost("/upload", (Func<HttpContext, Task<IResult>>)uploadService.HandleUploadAsync);
        }
    }
}
