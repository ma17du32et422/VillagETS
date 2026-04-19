using Supabase.Postgrest;
using villagets.Auth;
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
            Console.WriteLine($"Fetching top-level comments for publication {publicationId}");
            var result = await PerfLogger.TimeAsync(_logger, "CommentService.GetByPublication comments", () => _supabase
                .From<sql.Commentaire>()
                .Where(c => c.PublicationId == publicationId)
                .Filter("parent_commentaire", Operator.Is, (string?)null)
                .Order("date_commentaire", Ordering.Ascending)
                .Get());
            Console.WriteLine($"Fetched {result.Models.Count} top-level comments for publication {publicationId}");
            return await EnrichWithUsers(result.Models.ToList());
        }

        public async Task<List<(sql.Commentaire comment, sql.Utilisateur? user)>> GetReplies(string parentCommentId)
        {
            var result = await PerfLogger.TimeAsync(_logger, "CommentService.GetReplies replies", () => _supabase
                .From<sql.Commentaire>().Where(c => c.ParentCommentaire == parentCommentId)
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
            // Ensure the publication exists
            var pubResult = await PerfLogger.TimeAsync(_logger, "CommentService.Create publication lookup", () => _supabase
                .From<sql.Publication>()
                .Where(p => p.Id == publicationId)
                .Get());

            if (pubResult.Model is null) return (null, null);

            if (parentCommentId != null)
            {
                var parentResult = await PerfLogger.TimeAsync(_logger, "CommentService.Create parent lookup", () => _supabase
                    .From<sql.Commentaire>()
                    .Where(c => c.Id == parentCommentId)
                    .Get());

                var parent = parentResult.Model;
                if (parent is null || parent.PublicationId != publicationId)
                    return (null, null);

                // Increment nb_reponses on the parent comment
                parent.NbReponses = (parent.NbReponses ?? 0) + 1;
                await PerfLogger.TimeAsync(_logger, "CommentService.Create update parent reply count", () => _supabase
                    .From<sql.Commentaire>()
                    .Where(c => c.Id == parentCommentId)
                    .Update(parent));
            }

            // Increment nb_commentaires on the publication
            var pub = pubResult.Model;
            if (pub != null)
            {
                await IncrementPostCount(pub);
            }

            var comment = new sql.Commentaire
            {
                UtilisateurId = userId,
                PublicationId = publicationId,
                ParentCommentaire = parentCommentId,
                DateCommentaire = DateTime.UtcNow,
                Contenu = contenu,
                NbReponses = 0
            };

            var insertResult = await PerfLogger.TimeAsync(_logger, "CommentService.Create insert comment", () => _supabase
                .From<sql.Commentaire>()
                .Insert(comment));

            var saved = insertResult.Model;
            if (saved is null) return (null, null);

            var userResult = await PerfLogger.TimeAsync(_logger, "CommentService.Create user lookup", () => _supabase
                .From<sql.Utilisateur>()
                .Where(u => u.Id == userId)
                .Get());

            return (saved, userResult.Model);
        }

        public async Task<bool> Delete(string commentId, string userId)
        {
            var result = await PerfLogger.TimeAsync(_logger, "CommentService.Delete comment lookup", () => _supabase
                .From<sql.Commentaire>()
                .Where(c => c.Id == commentId)
                .Get());

            var comment = result.Model;
            if (comment is null) return false;

            var userResult = await PerfLogger.TimeAsync(_logger, "CommentService.Delete user lookup", () => _supabase
                .From<sql.Utilisateur>()
                .Where(u => u.Id == userId)
                .Get());

            var currentUser = userResult.Model;
            var isAdmin = currentUser?.MainAdmin == true;

            if (comment.UtilisateurId != userId && !isAdmin)
                throw new UnauthorizedAccessException();

            // Count direct replies so we can adjust the publication's comment count
            var repliesResult = await PerfLogger.TimeAsync(_logger, "CommentService.Delete replies lookup", () => _supabase
                .From<sql.Commentaire>()
                .Where(c => c.ParentCommentaire == commentId)
                .Get());

            var replyCount = repliesResult.Models.Count;

            // Delete all direct replies
            await PerfLogger.TimeAsync(_logger, "CommentService.Delete delete replies", () => _supabase
                .From<sql.Commentaire>()
                .Where(c => c.ParentCommentaire == commentId)
                .Delete());

            // Delete the comment itself
            await PerfLogger.TimeAsync(_logger, "CommentService.Delete delete comment", () => _supabase
                .From<sql.Commentaire>()
                .Where(c => c.Id == commentId)
                .Delete());

            // If this was a reply, decrement the parent's nb_reponses
            if (comment.ParentCommentaire != null)
            {
                var parentResult = await PerfLogger.TimeAsync(_logger, "CommentService.Delete parent lookup", () => _supabase
                    .From<sql.Commentaire>()
                    .Where(c => c.Id == comment.ParentCommentaire)
                    .Get());

                var parent = parentResult.Model;
                if (parent != null)
                {
                    parent.NbReponses = Math.Max((parent.NbReponses ?? replyCount) - 1, 0);
                    await PerfLogger.TimeAsync(_logger, "CommentService.Delete update parent reply count", () => _supabase
                        .From<sql.Commentaire>()
                        .Where(c => c.Id == comment.ParentCommentaire)
                        .Update(parent));
                }
            }

            // Decrement nb_commentaires on the publication (comment + its replies)
            if (comment.PublicationId != null)
            {
                var pubResult = await PerfLogger.TimeAsync(_logger, "CommentService.Delete publication lookup", () => _supabase
                    .From<sql.Publication>()
                    .Where(p => p.Id == comment.PublicationId)
                    .Get());

                var pub = pubResult.Model;
                if (pub != null)
                {
                    await DecrementPostCount(pub);
                }
            }

            return true;
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

        private async Task IncrementPostCount(sql.Publication pub, int incrementBy = 1)
        {
            pub.CommentairesCount = (pub.CommentairesCount ?? 0) + incrementBy;
            await PerfLogger.TimeAsync(_logger, "CommentService.IncrementPostCount update publication", () => _supabase
                .From<sql.Publication>()
                .Where(p => p.Id == pub.Id)
                .Update(pub));
        }

        private async Task DecrementPostCount(sql.Publication pub, int decrementBy = 1)
        {
            pub.CommentairesCount = Math.Max((pub.CommentairesCount ?? decrementBy) - decrementBy, 0);
            await PerfLogger.TimeAsync(_logger, "CommentService.DecrementPostCount update publication", () => _supabase
                .From<sql.Publication>()
                .Where(p => p.Id == pub.Id)
                .Update(pub));
        }

    }
}

