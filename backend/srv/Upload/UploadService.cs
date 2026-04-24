using sql;
using villagets.Auth;

namespace srv.Upload
{
    public class UploadOptions
    {
        public required string UploadFolder { get; init; }
        public required long MaxUploadBytes { get; init; }
        public required HashSet<string> AllowedUploadContentTypes { get; init; }
        public required HashSet<string> AllowedUploadExtensions { get; init; }
        public required bool AllowAnyMessageUpload { get; init; }
        public required HashSet<string> BlockedMessageUploadContentTypes { get; init; }
        public required HashSet<string> BlockedMessageUploadExtensions { get; init; }
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
                var (currentUser, isDeleted) = await AuthHelper.GetAuthenticatedUserAsync(ctx);
                if (isDeleted) return JsonError(StatusCodes.Status410Gone, "User was deleted");
                if (currentUser == null)
                    return JsonError(StatusCodes.Status401Unauthorized, "Unauthorized.");

                var userId = currentUser.Id;
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

                if (allowMessageFiles && _options.AllowAnyMessageUpload)
                {
                    var normalizedExtension = string.IsNullOrWhiteSpace(extension) ? string.Empty : extension;
                    var normalizedContentType = string.IsNullOrWhiteSpace(file.ContentType) ? "application/octet-stream" : file.ContentType;

                    if (_options.BlockedMessageUploadExtensions.Contains(normalizedExtension)
                        || _options.BlockedMessageUploadContentTypes.Contains(normalizedContentType))
                    {
                        return JsonError(StatusCodes.Status400BadRequest, "Ce type de fichier n'est pas autorise pour les messages.");
                    }
                }
                else if (!_options.AllowedUploadExtensions.Contains(extension) || !_options.AllowedUploadContentTypes.Contains(file.ContentType))
                {
                    return JsonError(StatusCodes.Status400BadRequest, "Seuls les fichiers image JPG, PNG, WEBP et GIF sont acceptes.");
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
