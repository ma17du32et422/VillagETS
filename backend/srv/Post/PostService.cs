using Microsoft.Data.SqlClient;
using Microsoft.Extensions.Configuration.UserSecrets;
using Npgsql;
using Newtonsoft.Json;
using Supabase.Postgrest;
using static Supabase.Postgrest.Constants;
namespace srv.Post
{
    public class PostService
    {
        private readonly Supabase.Client _supabase;
        private readonly ILogger<PostService> _logger;

        public PostService(ILogger<PostService> logger)
        {
            _supabase = SupabaseService.GetClient();
            _logger = logger;
        }

        public async Task<object?> GetById(int id)
        {
            var rows = await PerfLogger.TimeAsync(_logger, "PostService.GetById rpc get_post_by_id", () => _supabase.Rpc<List<PostDetailRpcRow>>(
                "get_post_by_id",
                new Dictionary<string, object?>
                {
                    { "p_publication_id", id }
                }));

            var row = rows?.FirstOrDefault();
            if (row == null)
                return null;

            return new
            {
                id = row.Id,
                titre = row.Titre,
                contenu = row.Contenu,
                media = row.Media ?? [],
                datePublication = row.DatePublication,
                prix = row.Prix,
                articleAVendre = row.ArticleAVendre,
                likes = row.Likes ?? 0,
                dislikes = row.Dislikes ?? 0,
                commentaires = row.Commentaires ?? 0,
                op = new
                {
                    id = row.OpId,
                    pseudo = row.OpPseudo,
                    photoProfil = row.OpPhotoProfil
                }
            };
        }
        public async Task<List<sql.Publication>> GetFeed(string userId)
        {
            var result = await PerfLogger.TimeAsync(_logger, "PostService.GetFeed publications", () => _supabase.From<sql.Publication>().Order("date_publication", Supabase.Postgrest.Constants.Ordering.Descending).Limit(20).Get());

            return result.Models.ToList();
        }
        public async Task<PostCreateResult?> Create(PostCreateRequest publication, string userId)
        {
            var mediaUrls = publication.Media?
                .Where(m => !string.IsNullOrWhiteSpace(m))
                .Select(m => m.Trim())
                .Distinct()
                .ToList() ?? [];

            var fichiers = await ResolveOwnedFilesByUrlsAsync(userId, mediaUrls, "PostService.Create fichier lookup");
            if (fichiers.Count != mediaUrls.Count)
                throw new ArgumentException("One or more post media uploads are invalid");

            var sanitized = new sql.Publication
            {
                UtilisateurId = userId,
                Nom = publication.Nom?.Trim(),
                Contenu = publication.Contenu?.Trim(),
                DatePublication = DateTime.UtcNow,
                Prix = publication.Prix,
                ArticleAVendre = publication.ArticleAVendre
            };

            if (string.IsNullOrWhiteSpace(sanitized.Nom))
                throw new ArgumentException("Post title is required");

            if (string.IsNullOrWhiteSpace(sanitized.Contenu))
                throw new ArgumentException("Post content is required");

            var result = await PerfLogger.TimeAsync(_logger, "PostService.Create insert publication", () => _supabase.From<sql.Publication>().Insert(sanitized));
            var saved = result.Model;
            if (saved is null) return null;

            if (fichiers.Count > 0)
            {
                var links = fichiers
                    .Where(f => f.Id.HasValue)
                    .Select(f => new sql.PublicationFichier
                    {
                        IdFichier = f.Id!.Value.ToString("D"),
                        IdPublication = saved.Id
                    }).ToList();
                if (links.Count > 0)
                    await PerfLogger.TimeAsync(_logger, "PostService.Create insert publication fichiers", () => _supabase.From<sql.PublicationFichier>().Insert(links));
            }

            return new PostCreateResult
            {
                Id = saved.Id,
                UtilisateurId = saved.UtilisateurId,
                Nom = saved.Nom,
                Contenu = saved.Contenu,
                Media = mediaUrls.ToArray(),
                DatePublication = saved.DatePublication,
                Prix = saved.Prix,
                ArticleAVendre = saved.ArticleAVendre
            };
        }


        public async Task<bool> Delete(int id, string userId)
        {
            var existing = await PerfLogger.TimeAsync(_logger, "PostService.Delete publication lookup", () => _supabase
                .From<sql.Publication>()
                .Where(p => p.Id == id)
                .Get());

            var publication = existing.Models.FirstOrDefault();
            if (publication == null) return false;

            var userResult = await PerfLogger.TimeAsync(_logger, "PostService.Delete user lookup", () => _supabase
                .From<sql.Utilisateur>()
                .Where(u => u.Id == userId)
                .Get());

            var currentUser = userResult.Model;
            var isAdmin = currentUser?.MainAdmin == true;

            if (publication.UtilisateurId != userId && !isAdmin)
                throw new UnauthorizedAccessException();

            await PerfLogger.TimeAsync(_logger, "PostService.Delete delete publication fichiers", () => _supabase
                .From<sql.PublicationFichier>()
                .Filter("id_publication", Operator.Equals, id)
                .Delete());

            // Delete reactions
            await PerfLogger.TimeAsync(_logger, "PostService.Delete delete reactions", () => _supabase
                .From<sql.ReactionPublication>()
                .Filter("id_publication", Operator.Equals, id)
                .Delete());

            // Delete the publication itself
            await PerfLogger.TimeAsync(_logger, "PostService.Delete delete publication", () => _supabase
                .From<sql.Publication>()
                .Where(p => p.Id == id)
                .Delete());

            var deletedFiles = await PerfLogger.TimeAsync(_logger, "PostService.Delete rpc cleanup_orphan_files", () => _supabase.Rpc<List<CleanupOrphanFileRpcRow>>(
                "cleanup_orphan_files",
                new Dictionary<string, object?>()));

            var uploadFolder = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "uploads");
            foreach (var file in deletedFiles ?? [])
            {
                if (string.IsNullOrWhiteSpace(file.LienFichier))
                    continue;

                var fileName = Path.GetFileName(file.LienFichier);
                if (string.IsNullOrWhiteSpace(fileName))
                    continue;

                var filePath = Path.Combine(uploadFolder, fileName);
                if (File.Exists(filePath))
                    File.Delete(filePath);
            }

            return true;
        }

        public async Task<List<object>> GetFeed(string? userId, FeedQuery query)
        {
            Guid? rpcUserId = null;
            if (!string.IsNullOrWhiteSpace(userId) && Guid.TryParse(userId, out var parsedUserId))
                rpcUserId = parsedUserId;

            var rows = await PerfLogger.TimeAsync(_logger, "PostService.GetFeed rpc get_feed", () => _supabase.Rpc<List<FeedRpcRow>>(
                "get_feed",
                new Dictionary<string, object?>
                {
                    { "p_user_id", rpcUserId },
                    { "p_search_query", string.IsNullOrWhiteSpace(query.SearchString) ? null : query.SearchString },
                    { "p_is_marketplace", query.IsMarketplace }
                }));

            return (rows ?? []).Select(row => (object)new
            {
                id = row.Id,
                titre = row.Titre,
                contenu = row.Contenu,
                media = row.Media ?? [],
                datePublication = row.DatePublication,
                prix = row.Prix,
                articleAVendre = row.ArticleAVendre,
                likes = row.Likes ?? 0,
                dislikes = row.Dislikes ?? 0,
                commentaires = row.Commentaires ?? 0,
                userReaction = row.UserReaction,
                op = new
                {
                    id = row.OpId,
                    pseudo = row.OpPseudo,
                    photoProfil = row.OpPhotoProfil
                }
            }).ToList();
        }

        public async Task<List<object>> GetPostsByUser(string? currentUserId, string targetUserId)
        {
            if (!Guid.TryParse(targetUserId, out var rpcTargetUserId))
                return [];

            Guid? rpcCurrentUserId = null;
            if (!string.IsNullOrWhiteSpace(currentUserId) && Guid.TryParse(currentUserId, out var parsedCurrentUserId))
                rpcCurrentUserId = parsedCurrentUserId;

            var rows = await PerfLogger.TimeAsync(_logger, "PostService.GetPostsByUser rpc get_posts_by_user", () => _supabase.Rpc<List<FeedRpcRow>>(
                "get_posts_by_user",
                new Dictionary<string, object?>
                {
                    { "p_current_user_id", rpcCurrentUserId },
                    { "p_target_user_id", rpcTargetUserId }
                }));

            return (rows ?? []).Select(row => (object)new
            {
                id = row.Id,
                titre = row.Titre,
                contenu = row.Contenu,
                media = row.Media ?? [],
                datePublication = row.DatePublication,
                prix = row.Prix,
                articleAVendre = row.ArticleAVendre,
                likes = row.Likes ?? 0,
                dislikes = row.Dislikes ?? 0,
                commentaires = row.Commentaires ?? 0,
                userReaction = row.UserReaction,
                op = new
                {
                    id = row.OpId,
                    pseudo = row.OpPseudo,
                    photoProfil = row.OpPhotoProfil
                }
            }).ToList();
        }

        private async Task<List<sql.Fichier>> ResolveOwnedFilesByUrlsAsync(string userId, IReadOnlyCollection<string> urls, string perfLabel)
        {
            if (urls.Count == 0)
                return [];

            var response = await PerfLogger.TimeAsync(_logger, perfLabel, () => _supabase
                .From<sql.Fichier>()
                .Filter("lien_fichier", Operator.In, urls.ToList())
                .Filter("id_proprietaire", Operator.Equals, userId)
                .Get());

            return response.Models.ToList();
        }

    }

    public class FeedRpcRow
    {
        [JsonProperty("id")]
        public int? Id { get; set; }

        [JsonProperty("titre")]
        public string? Titre { get; set; }

        [JsonProperty("contenu")]
        public string? Contenu { get; set; }

        [JsonProperty("media")]
        public string[]? Media { get; set; }

        [JsonProperty("date_publication")]
        public DateTime? DatePublication { get; set; }

        [JsonProperty("prix")]
        public decimal? Prix { get; set; }

        [JsonProperty("article_a_vendre")]
        public bool? ArticleAVendre { get; set; }

        [JsonProperty("likes")]
        public int? Likes { get; set; }

        [JsonProperty("dislikes")]
        public int? Dislikes { get; set; }

        [JsonProperty("commentaires")]
        public int? Commentaires { get; set; }

        [JsonProperty("op_id")]
        public string? OpId { get; set; }

        [JsonProperty("op_pseudo")]
        public string? OpPseudo { get; set; }

        [JsonProperty("op_photo_profil")]
        public string? OpPhotoProfil { get; set; }

        [JsonProperty("user_reaction")]
        public string? UserReaction { get; set; }
    }

    public class PostDetailRpcRow : FeedRpcRow
    {
    }

    public class CleanupOrphanFileRpcRow
    {
        [JsonProperty("id_fichier")]
        public Guid? IdFichier { get; set; }

        [JsonProperty("lien_fichier")]
        public string? LienFichier { get; set; }
    }

    public class PostCreateRequest
    {
        public string? Nom { get; set; }
        public string? Contenu { get; set; }
        public string[]? Media { get; set; }
        public decimal? Prix { get; set; }
        public bool? ArticleAVendre { get; set; }
    }

    public class PostCreateResult
    {
        public int? Id { get; set; }
        public string? UtilisateurId { get; set; }
        public string? Nom { get; set; }
        public string? Contenu { get; set; }
        public string[] Media { get; set; } = [];
        public DateTime? DatePublication { get; set; }
        public decimal? Prix { get; set; }
        public bool? ArticleAVendre { get; set; }
    }
}
