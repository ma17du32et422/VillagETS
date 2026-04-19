using Microsoft.Data.SqlClient;
using Microsoft.Extensions.Configuration.UserSecrets;
using Npgsql;
using Newtonsoft.Json;
using Supabase.Postgrest;
using System.Security.Claims;
using villagets.Auth;
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

        public async Task<(sql.Publication? publication, sql.Utilisateur? utilisateur)> GetById(int id)
        {
            var result = await PerfLogger.TimeAsync(_logger, "PostService.GetById publication lookup", () => _supabase
                .From<sql.Publication>()
                .Where(p => p.Id == id)
                .Get());

            var post = result.Model;
            if (post == null) return (null, null);

            var userResult = await PerfLogger.TimeAsync(_logger, "PostService.GetById user lookup", () => _supabase
                .From<sql.Utilisateur>()
                .Where(u => u.Id == post.UtilisateurId)
                .Get());

            return (post, userResult.Model);
        }
        public async Task<List<sql.Publication>> GetFeed(string userId)
        {
            var result = await PerfLogger.TimeAsync(_logger, "PostService.GetFeed publications", () => _supabase.From<sql.Publication>().Order("date_publication", Supabase.Postgrest.Constants.Ordering.Descending).Limit(20).Get());

            return result.Models.ToList();
        }
        public async Task<sql.Publication?> Create(sql.Publication publication, string userId)
        {
            publication.UtilisateurId = userId;
            publication.DatePublication = DateTime.UtcNow;
            var result = await PerfLogger.TimeAsync(_logger, "PostService.Create insert publication", () => _supabase.From<sql.Publication>().Insert(publication));
            var saved = result.Model;
            if (saved is null) return null;

            saved.Media = publication.Media;

            var urls = publication.Media?.Where(u => u != null).ToList();
            if (urls is { Count: > 0 })
            {
                var fichiers = await PerfLogger.TimeAsync(_logger, "PostService.Create fichier lookup", () => _supabase
                    .From<sql.Fichier>()
                    .Filter("lien_fichier", Operator.In, urls)
                    .Get());

                var links = fichiers.Models
                    .Where(f => f.Id.HasValue)
                    .Select(f => new sql.PublicationFichier
                    {
                        IdFichier = f.Id!.Value.ToString("D"),
                        IdPublication = saved.Id
                    }).ToList();
                Console.WriteLine($"Links to insert: {links.Count}");
                if (links.Count > 0)
                    await PerfLogger.TimeAsync(_logger, "PostService.Create insert publication fichiers", () => _supabase.From<sql.PublicationFichier>().Insert(links));
            }

            return saved;
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

            // Get all fichier links for this publication
            var pubFichiers = await PerfLogger.TimeAsync(_logger, "PostService.Delete publication fichier lookup", () => _supabase
                .From<sql.PublicationFichier>()
                .Filter("id_publication", Operator.Equals, id)
                .Get());

            var fichierIds = pubFichiers.Models
                .Where(pf => pf.IdFichier != null)
                .Select(pf => pf.IdFichier!)
                .ToList();

            if (fichierIds.Count > 0)
            {
                // Get the actual fichier records to find file paths
                var fichiers = await PerfLogger.TimeAsync(_logger, "PostService.Delete fichier lookup", () => _supabase
                    .From<sql.Fichier>()
                    .Filter("id_fichier", Operator.In, fichierIds)
                    .Get());

                // Delete physical files from disk
                var uploadFolder = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "uploads");
                foreach (var fichier in fichiers.Models)
                {
                    if (fichier.LienFichier == null) continue;
                    var fileName = Path.GetFileName(fichier.LienFichier);
                    var filePath = Path.Combine(uploadFolder, fileName);
                    if (File.Exists(filePath))
                        File.Delete(filePath);
                }
                // Delete publication_fichier links
                await PerfLogger.TimeAsync(_logger, "PostService.Delete delete publication fichiers", () => _supabase
                    .From<sql.PublicationFichier>()
                    .Filter("id_publication", Operator.Equals, id)
                    .Delete());

                // Delete fichier records from DB
                await PerfLogger.TimeAsync(_logger, "PostService.Delete delete fichiers", () => _supabase
                    .From<sql.Fichier>()
                    .Filter("id_fichier", Operator.In, fichierIds)
                    .Delete());


            }

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
                media = row.Media,
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
                media = row.Media,
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
}
