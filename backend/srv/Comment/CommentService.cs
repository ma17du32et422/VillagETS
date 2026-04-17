using Supabase.Postgrest;
using static Supabase.Postgrest.Constants;
using villagets.Auth;

namespace srv.Comment
{
    public class CommentService
    {
        private readonly Supabase.Client _supabase;

        public CommentService()
        {
            _supabase = SupabaseService.GetClient();
        }

        public async Task<List<(sql.Commentaire comment, sql.Utilisateur? user)>> GetByPublication(int publicationId)
        {
            Console.WriteLine($"Fetching top-level comments for publication {publicationId}");
            var result = await _supabase
                .From<sql.Commentaire>()
                .Where(c => c.PublicationId == publicationId && c.ParentCommentaire == null)
                .Order("date_commentaire", Ordering.Ascending)
                .Get();
            Console.WriteLine($"Fetched {result.Models.Count} top-level comments for publication {publicationId}");
            return await EnrichWithUsers(result.Models.ToList());
        }

        public async Task<List<(sql.Commentaire comment, sql.Utilisateur? user)>> GetReplies(string parentCommentId)
        {
            var result = await _supabase
                .From<sql.Commentaire>().Where(c => c.ParentCommentaire == parentCommentId)
                .Order("date_commentaire", Ordering.Ascending)
                .Get();

            return await EnrichWithUsers(result.Models.ToList());
        }

        public async Task<(sql.Commentaire? comment, sql.Utilisateur? user)> Create(
            int publicationId,
            string userId,
            string contenu,
            string? parentCommentId = null)
        {
            // Ensure the publication exists
            var pubResult = await _supabase
                .From<sql.Publication>()
                .Where(p => p.Id == publicationId)
                .Get();

            if (pubResult.Model is null) return (null, null);

            // If it's a reply, ensure the parent exists and belongs to the same publication
            if (parentCommentId != null)
            {
                var parentResult = await _supabase
                    .From<sql.Commentaire>()
                    .Where(c => c.Id == parentCommentId)
                    .Get();

                var parent = parentResult.Model;
                if (parent is null || parent.PublicationId != publicationId)
                    return (null, null);

                // Increment nb_reponses on the parent comment
                var updatedParent = new sql.Commentaire
                {
                    Id = parent.Id,
                    NbReponses = (parent.NbReponses ?? 0) + 1
                };
                await _supabase
                    .From<sql.Commentaire>()
                    .Where(c => c.Id == parentCommentId)
                    .Update(updatedParent);
            }

            // Increment nb_commentaires on the publication
            var pub = pubResult.Model;
            var updatedPub = new sql.Publication
            {
                Id = pub.Id,
                CommentairesCount = (pub.CommentairesCount ?? 0) + 1
            };
            await _supabase
                .From<sql.Publication>()
                .Where(p => p.Id == publicationId)
                .Update(updatedPub);

            var comment = new sql.Commentaire
            {
                UtilisateurId = userId,
                PublicationId = publicationId,
                ParentCommentaire = parentCommentId,
                DateCommentaire = DateTime.UtcNow,
                Contenu = contenu,
                NbReponses = 0
            };

            var insertResult = await _supabase
                .From<sql.Commentaire>()
                .Insert(comment);

            var saved = insertResult.Model;
            if (saved is null) return (null, null);

            var userResult = await _supabase
                .From<sql.Utilisateur>()
                .Where(u => u.Id == userId)
                .Get();

            return (saved, userResult.Model);
        }

        public async Task<bool> Delete(string commentId, string userId)
        {
            var result = await _supabase
                .From<sql.Commentaire>()
                .Where(c => c.Id == commentId)
                .Get();

            var comment = result.Model;
            if (comment is null) return false;
            if (comment.UtilisateurId != userId)
                throw new UnauthorizedAccessException();

            // Count direct replies so we can adjust the publication's comment count
            var repliesResult = await _supabase
                .From<sql.Commentaire>()
                .Where(c => c.ParentCommentaire == commentId)
                .Get();

            var replyCount = repliesResult.Models.Count;

            // Delete all direct replies
            await _supabase
                .From<sql.Commentaire>()
                .Where(c => c.ParentCommentaire == commentId)
                .Delete();

            // Delete the comment itself
            await _supabase
                .From<sql.Commentaire>()
                .Where(c => c.Id == commentId)
                .Delete();

            // If this was a reply, decrement the parent's nb_reponses
            if (comment.ParentCommentaire != null)
            {
                var parentResult = await _supabase
                    .From<sql.Commentaire>()
                    .Where(c => c.Id == comment.ParentCommentaire)
                    .Get();

                var parent = parentResult.Model;
                if (parent != null)
                {
                    var updatedParent = new sql.Commentaire
                    {
                        Id = parent.Id,
                        NbReponses = Math.Max((parent.NbReponses ?? 1) - 1, 0)
                    };
                    await _supabase
                        .From<sql.Commentaire>()
                        .Where(c => c.Id == comment.ParentCommentaire)
                        .Update(updatedParent);
                }
            }

            // Decrement nb_commentaires on the publication (comment + its replies)
            if (comment.PublicationId != null)
            {
                var pubResult = await _supabase
                    .From<sql.Publication>()
                    .Where(p => p.Id == comment.PublicationId)
                    .Get();

                var pub = pubResult.Model;
                if (pub != null)
                {
                    var totalRemoved = 1 + replyCount;
                    var updatedPub = new sql.Publication
                    {
                        Id = pub.Id,
                        CommentairesCount = Math.Max((pub.CommentairesCount ?? totalRemoved) - totalRemoved, 0)
                    };
                    await _supabase
                        .From<sql.Publication>()
                        .Where(p => p.Id == comment.PublicationId)
                        .Update(updatedPub);
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

            var users = await _supabase
                .From<sql.Utilisateur>()
                .Filter("id_utilisateur", Operator.In, userIds)
                .Get();

            var userMap = users.Models.ToDictionary(u => u.Id!, u => u);

            return comments
                .Select(c => (c, userMap.TryGetValue(c.UtilisateurId ?? "", out var u) ? u : null))
                .ToList();
        }
    }
}