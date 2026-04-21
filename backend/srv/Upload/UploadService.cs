using sql;
using System.Security.Claims;

namespace srv.Upload
{
    public class UploadOptions
    {
        public required string UploadFolder { get; init; }
        public required long MaxUploadBytes { get; init; }
        public required HashSet<string> AllowedUploadContentTypes { get; init; }
        public required HashSet<string> AllowedUploadExtensions { get; init; }
        public required HashSet<string> AllowedMessageUploadContentTypes { get; init; }
        public required HashSet<string> AllowedMessageUploadExtensions { get; init; }
    }

    public class UploadService
    {
        private readonly Supabase.Client _supabase;
        private readonly ILogger<UploadService> _logger;
        private readonly UploadOptions _options;

        public UploadService(ILogger<UploadService> logger, UploadOptions options)
        {
            _supabase = SupabaseService.GetClient();
            _logger = logger;
            _options = options;

            Directory.CreateDirectory(_options.UploadFolder);
        }

        public async Task<IResult> HandleUploadAsync(HttpContext ctx)
        {
            string? filePath = null;
            try
            {
                var principal = villagets.Auth.AuthHelper.GetClaimsFromContext(ctx);
                if (principal == null)
                    return JsonError(StatusCodes.Status401Unauthorized, "Unauthorized.");

                var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                if (string.IsNullOrWhiteSpace(userId))
                    return JsonError(StatusCodes.Status401Unauthorized, "Unauthorized.");

                if (!ctx.Request.HasFormContentType)
                    return JsonError(StatusCodes.Status400BadRequest, "Le formulaire d'envoi est invalide.");

                var form = await ctx.Request.ReadFormAsync();
                var file = form.Files["file"];
                var uploadScope = form["scope"].ToString();

                if (file is null || file.Length == 0)
                    return JsonError(StatusCodes.Status400BadRequest, "Aucun fichier fourni.");

                if (file.Length > _options.MaxUploadBytes)
                    return JsonError(StatusCodes.Status400BadRequest, "Le fichier depasse la taille maximale autorisee de 10 Mo.");

                var extension = Path.GetExtension(file.FileName);
                var allowMessageFiles = string.Equals(uploadScope, "message", StringComparison.OrdinalIgnoreCase);
                var allowedExtensions = allowMessageFiles ? _options.AllowedMessageUploadExtensions : _options.AllowedUploadExtensions;
                var allowedContentTypes = allowMessageFiles ? _options.AllowedMessageUploadContentTypes : _options.AllowedUploadContentTypes;

                if (!allowedExtensions.Contains(extension) || !allowedContentTypes.Contains(file.ContentType))
                {
                    var error = allowMessageFiles
                        ? "Ce type de fichier n'est pas autorise pour les messages."
                        : "Seuls les fichiers image JPG, PNG, WEBP et GIF sont acceptes.";
                    return JsonError(StatusCodes.Status400BadRequest, error);
                }

                var nom = Path.GetFileName(string.IsNullOrWhiteSpace(form["nom"]) ? file.FileName : form["nom"].ToString());
                var uniqueName = $"{Guid.NewGuid()}{extension}";
                filePath = Path.Combine(_options.UploadFolder, uniqueName);

                using var stream = File.Create(filePath);
                await file.CopyToAsync(stream);

                var baseUrl = $"{ctx.Request.Scheme}://{ctx.Request.Host}";
                var fileUrl = $"{baseUrl}/uploads/{uniqueName}";

                var fichier = new Fichier
                {
                    Nom = nom,
                    LienFichier = fileUrl,
                    Type = file.ContentType,
                    IdProprietaire = Guid.TryParse(userId, out var ownerId) ? ownerId : null
                };

                var response = await PerfLogger.TimeAsync(_logger, "UploadService.HandleUploadAsync fichier insert", () => _supabase
                    .From<Fichier>()
                    .Insert(fichier));

                if (response.Models.FirstOrDefault() == null)
                {
                    if (File.Exists(filePath))
                        File.Delete(filePath);

                    return JsonError(StatusCodes.Status400BadRequest, "Impossible d'enregistrer le fichier.");
                }

                return Results.Json(new UploadResponse(fileUrl), statusCode: StatusCodes.Status200OK);
            }
            catch (Exception ex)
            {
                if (filePath != null && File.Exists(filePath))
                    File.Delete(filePath);

                _logger.LogError(ex, "Upload failed");
                return JsonError(StatusCodes.Status400BadRequest, "Le televersement a echoue.");
            }
        }

        public void DeleteLocalFiles(IEnumerable<string?> fileUrls)
        {
            foreach (var fileUrl in fileUrls)
            {
                if (string.IsNullOrWhiteSpace(fileUrl))
                    continue;

                var fileName = Path.GetFileName(fileUrl);
                if (string.IsNullOrWhiteSpace(fileName))
                    continue;

                var filePath = Path.Combine(_options.UploadFolder, fileName);
                if (!File.Exists(filePath))
                    continue;

                try
                {
                    File.Delete(filePath);
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Failed to delete local upload file {FilePath}", filePath);
                }
            }
        }

        private static IResult JsonError(int statusCode, string error)
        {
            return Results.Json(new UploadErrorResponse(error), statusCode: statusCode);
        }
    }

    public record UploadResponse(string url);

    public record UploadErrorResponse(string error);
}
