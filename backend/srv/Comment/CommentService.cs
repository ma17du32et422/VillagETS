using Supabase.Postgrest;
using villagets.Auth;
using static Supabase.Postgrest.Constants;

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
                .Where(c => c.PublicationId == publicationId)
                .Filter("parent_commentaire", Operator.Is, (string?)null)
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
                parent.NbReponses = (parent.NbReponses ?? 0) + 1;
                await _supabase
                    .From<sql.Commentaire>()
                    .Where(c => c.Id == parentCommentId)
                    .Update(parent);
            }

            // Increment nb_commentaires on the publication
            var pub = pubResult.Model;
            if (pub != null)
            {
                IncrementPostCount(pub);
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
                    parent.NbReponses = Math.Max((parent.NbReponses ?? replyCount) - 1, 0);
                    await _supabase
                        .From<sql.Commentaire>()
                        .Where(c => c.Id == comment.ParentCommentaire)
                        .Update(parent);
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
                    DecrementPostCount(pub);
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

        private async void IncrementPostCount(sql.Publication pub, int incrementBy = 1)
        {
            pub.CommentairesCount = (pub.CommentairesCount ?? 0) + incrementBy;
            await _supabase
                        .From<sql.Publication>()
                        .Where(p => p.Id == pub.Id)
                        .Update(pub);
        }

        private async void DecrementPostCount(sql.Publication pub, int decrementBy = 1)
        {
            pub.CommentairesCount = Math.Max((pub.CommentairesCount ?? decrementBy) - decrementBy, 0);
            await _supabase
                        .From<sql.Publication>()
                        .Where(p => p.Id == pub.Id)
                        .Update(pub);
        }

    }
}

