using Newtonsoft.Json;
using Supabase.Postgrest;
using static Supabase.Postgrest.Constants;

namespace srv.Comment
{
    public class CommentService
    {
        private readonly Supabase.Client _supabase;
        private readonly ILogger<CommentService> _logger;

        public CommentService(ILogger<CommentService> logger)
        {
            _supabase = SupabaseService.GetClient();
            _logger = logger;
        }

        public async Task<List<(sql.Commentaire comment, sql.Utilisateur? user)>> GetByPublication(int publicationId)
        {
            var result = await PerfLogger.TimeAsync(_logger, "CommentService.GetByPublication comments", () => _supabase
                .From<sql.Commentaire>()
                .Where(c => c.PublicationId == publicationId)
                .Filter("parent_commentaire", Operator.Is, (string?)null)
                .Order("date_commentaire", Ordering.Ascending)
                .Get());

            return await EnrichWithUsers(result.Models.ToList());
        }

        public async Task<List<(sql.Commentaire comment, sql.Utilisateur? user)>> GetReplies(string parentCommentId)
        {
            var result = await PerfLogger.TimeAsync(_logger, "CommentService.GetReplies replies", () => _supabase
                .From<sql.Commentaire>()
                .Where(c => c.ParentCommentaire == parentCommentId)
                .Order("date_commentaire", Ordering.Ascending)
                .Get());

            return await EnrichWithUsers(result.Models.ToList());
        }

        public async Task<(sql.Commentaire? comment, sql.Utilisateur? user)> Create(
            int publicationId,
            string userId,
            string contenu,
            string? parentCommentId = null)
        {
            if (!Guid.TryParse(userId, out var rpcUserId))
                throw new ArgumentException("Invalid user id.", nameof(userId));

            Guid? rpcParentCommentId = null;
            if (!string.IsNullOrWhiteSpace(parentCommentId))
            {
                if (!Guid.TryParse(parentCommentId, out var parsedParentCommentId))
                    throw new ArgumentException("Invalid parent comment id.", nameof(parentCommentId));

                var parentComment = await GetById(parentCommentId);
                if (parentComment?.ParentCommentaire != null)
                    throw new InvalidOperationException("Replies to replies are not allowed.");

                rpcParentCommentId = parsedParentCommentId;
            }

            try
            {
                var rows = await PerfLogger.TimeAsync(_logger, "CommentService.Create rpc add_comment", () => _supabase.Rpc<List<AddCommentRpcRow>>(
                    "add_comment",
                    new Dictionary<string, object?>
                    {
                        { "p_publication_id", publicationId },
                        { "p_user_id", rpcUserId },
                        { "p_contenu", contenu },
                        { "p_parent_commentaire", rpcParentCommentId }
                    }));

                var saved = rows?.FirstOrDefault();
                if (saved == null)
                    return (null, null);

                var userResult = await PerfLogger.TimeAsync(_logger, "CommentService.Create user lookup", () => _supabase
                    .From<sql.Utilisateur>()
                    .Where(u => u.Id == userId)
                    .Get());

                return (saved.ToCommentaire(), userResult.Model);
            }
            catch (Exception ex) when (
                ex.Message.Contains("Publication introuvable", StringComparison.OrdinalIgnoreCase) ||
                ex.Message.Contains("Commentaire parent introuvable", StringComparison.OrdinalIgnoreCase))
            {
                _logger.LogWarning(ex, "Comment creation failed for publication {PublicationId}", publicationId);
                return (null, null);
            }
        }

        private async Task<sql.Commentaire?> GetById(string commentId)
        {
            var result = await PerfLogger.TimeAsync(_logger, "CommentService.GetById comment", () => _supabase
                .From<sql.Commentaire>()
                .Where(c => c.Id == commentId)
                .Limit(1)
                .Get());

            return result.Models.FirstOrDefault();
        }

        public async Task<int> Delete(string commentId, string userId)
        {
            if (!Guid.TryParse(commentId, out var rpcCommentId))
                return 0;

            if (!Guid.TryParse(userId, out var rpcUserId))
                throw new ArgumentException("Invalid user id.", nameof(userId));

            try
            {
                var rows = await PerfLogger.TimeAsync(_logger, "CommentService.Delete rpc delete_comment", () => _supabase.Rpc<List<DeleteCommentRpcRow>>(
                    "delete_comment",
                    new Dictionary<string, object?>
                    {
                        { "p_comment_id", rpcCommentId },
                        { "p_request_user_id", rpcUserId }
                    }));

                return rows?.FirstOrDefault()?.DeletedCount ?? 0;
            }
            catch (Exception ex) when (ex.Message.Contains("Suppression non autorisee", StringComparison.OrdinalIgnoreCase))
            {
                throw new UnauthorizedAccessException("Suppression non autorisee.", ex);
            }
        }

        private async Task<List<(sql.Commentaire, sql.Utilisateur?)>> EnrichWithUsers(List<sql.Commentaire> comments)
        {
            if (comments.Count == 0) return [];

            var userIds = comments
                .Select(c => c.UtilisateurId)
                .Distinct()
                .Where(id => id != null)
                .ToList();

            var users = await PerfLogger.TimeAsync(_logger, "CommentService.EnrichWithUsers users", () => _supabase
                .From<sql.Utilisateur>()
                .Filter("id_utilisateur", Operator.In, userIds)
                .Get());

            var userMap = users.Models.ToDictionary(u => u.Id!, u => u);

            return comments
                .Select(c => (c, userMap.TryGetValue(c.UtilisateurId ?? "", out var u) ? u : null))
                .ToList();
        }
    }

    public class AddCommentRpcRow
    {
        [JsonProperty("id_commentaire")]
        public string? IdCommentaire { get; set; }

        [JsonProperty("id_utilisateur")]
        public string? UtilisateurId { get; set; }

        [JsonProperty("id_publication")]
        public int? PublicationId { get; set; }

        [JsonProperty("parent_commentaire")]
        public string? ParentCommentaire { get; set; }

        [JsonProperty("date_commentaire")]
        public DateTime? DateCommentaire { get; set; }

        [JsonProperty("contenu")]
        public string? Contenu { get; set; }

        [JsonProperty("nb_reponses")]
        public int? NbReponses { get; set; }

        public sql.Commentaire ToCommentaire() => new()
        {
            Id = IdCommentaire,
            UtilisateurId = UtilisateurId,
            PublicationId = PublicationId,
            ParentCommentaire = ParentCommentaire,
            DateCommentaire = DateCommentaire,
            Contenu = Contenu,
            NbReponses = NbReponses
        };
    }

    public class DeleteCommentRpcRow
    {
        [JsonProperty("deleted_count")]
        public int? DeletedCount { get; set; }

        [JsonProperty("publication_id")]
        public int? PublicationId { get; set; }

        [JsonProperty("parent_commentaire")]
        public string? ParentCommentaire { get; set; }
    }
}
