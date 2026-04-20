using Supabase.Postgrest;
using static Supabase.Postgrest.Constants;
using System.Collections.Concurrent;

namespace srv.Comment
{
    public class CommentService
    {
        private readonly Supabase.Client _supabase;
        private readonly ILogger<CommentService> _logger;
        private readonly ConcurrentDictionary<int, SemaphoreSlim> _publicationLocks = new();

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
            sql.Commentaire? saved = null;

            await WithPublicationLock(publicationId, async () =>
            {
                var pubResult = await PerfLogger.TimeAsync(_logger, "CommentService.Create publication lookup", () => _supabase
                    .From<sql.Publication>()
                    .Where(p => p.Id == publicationId)
                    .Get());

                if (pubResult.Model is null)
                    return;

                if (parentCommentId != null)
                {
                    var parentResult = await PerfLogger.TimeAsync(_logger, "CommentService.Create parent lookup", () => _supabase
                        .From<sql.Commentaire>()
                        .Where(c => c.Id == parentCommentId)
                        .Get());

                    var parent = parentResult.Model;
                    if (parent is null || parent.PublicationId != publicationId)
                        return;
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

                saved = insertResult.Model;
                if (saved is null)
                    return;

                if (parentCommentId != null)
                    await RefreshReplyCount(parentCommentId);

                await RefreshPublicationCommentCount(publicationId);
            });

            if (saved is null)
                return (null, null);

            var userResult = await PerfLogger.TimeAsync(_logger, "CommentService.Create user lookup", () => _supabase
                .From<sql.Utilisateur>()
                .Where(u => u.Id == userId)
                .Get());

            return (saved, userResult.Model);
        }

        public async Task<int> Delete(string commentId, string userId)
        {
            var initialResult = await PerfLogger.TimeAsync(_logger, "CommentService.Delete comment lookup", () => _supabase
                .From<sql.Commentaire>()
                .Where(c => c.Id == commentId)
                .Get());

            var initialComment = initialResult.Model;
            if (initialComment?.PublicationId == null)
                return 0;

            return await WithPublicationLock(initialComment.PublicationId.Value, async () =>
            {
                var result = await PerfLogger.TimeAsync(_logger, "CommentService.Delete locked comment lookup", () => _supabase
                    .From<sql.Commentaire>()
                    .Where(c => c.Id == commentId)
                    .Get());

                var comment = result.Model;
                if (comment?.PublicationId == null)
                    return 0;

                var userResult = await PerfLogger.TimeAsync(_logger, "CommentService.Delete user lookup", () => _supabase
                    .From<sql.Utilisateur>()
                    .Where(u => u.Id == userId)
                    .Get());

                var currentUser = userResult.Model;
                var isAdmin = currentUser?.MainAdmin == true;

                if (comment.UtilisateurId != userId && !isAdmin)
                    throw new UnauthorizedAccessException();

                var deletedCount = await DeleteCommentTree(commentId);

                if (comment.ParentCommentaire != null)
                    await RefreshReplyCount(comment.ParentCommentaire);

                await RefreshPublicationCommentCount(comment.PublicationId.Value);
                return deletedCount;
            });
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

        private SemaphoreSlim GetPublicationLock(int publicationId) =>
            _publicationLocks.GetOrAdd(publicationId, _ => new SemaphoreSlim(1, 1));

        private async Task WithPublicationLock(int publicationId, Func<Task> action)
        {
            var gate = GetPublicationLock(publicationId);
            await gate.WaitAsync();
            try
            {
                await action();
            }
            finally
            {
                gate.Release();
            }
        }

        private async Task<T> WithPublicationLock<T>(int publicationId, Func<Task<T>> action)
        {
            var gate = GetPublicationLock(publicationId);
            await gate.WaitAsync();
            try
            {
                return await action();
            }
            finally
            {
                gate.Release();
            }
        }

        private async Task RefreshPublicationCommentCount(int publicationId)
        {
            var comments = await PerfLogger.TimeAsync(_logger, "CommentService.RefreshPublicationCommentCount comments", () => _supabase
                .From<sql.Commentaire>()
                .Filter("id_publication", Operator.Equals, publicationId)
                .Get());

            await PerfLogger.TimeAsync(_logger, "CommentService.RefreshPublicationCommentCount update publication", () => _supabase
                .From<sql.Publication>()
                .Filter("id_publication", Operator.Equals, publicationId)
                .Set(p => p.CommentairesCount!, comments.Models.Count)
                .Update());
        }

        private async Task RefreshReplyCount(string parentCommentId)
        {
            var replies = await PerfLogger.TimeAsync(_logger, "CommentService.RefreshReplyCount replies", () => _supabase
                .From<sql.Commentaire>()
                .Filter("parent_commentaire", Operator.Equals, parentCommentId)
                .Get());

            await PerfLogger.TimeAsync(_logger, "CommentService.RefreshReplyCount update parent", () => _supabase
                .From<sql.Commentaire>()
                .Filter("id_commentaire", Operator.Equals, parentCommentId)
                .Set(c => c.NbReponses!, replies.Models.Count)
                .Update());
        }

        private async Task<int> DeleteCommentTree(string commentId)
        {
            var repliesResult = await PerfLogger.TimeAsync(_logger, "CommentService.DeleteCommentTree replies lookup", () => _supabase
                .From<sql.Commentaire>()
                .Where(c => c.ParentCommentaire == commentId)
                .Get());

            var deletedCount = 1;
            foreach (var reply in repliesResult.Models.Where(r => !string.IsNullOrWhiteSpace(r.Id)))
                deletedCount += await DeleteCommentTree(reply.Id!);

            await PerfLogger.TimeAsync(_logger, "CommentService.DeleteCommentTree delete comment", () => _supabase
                .From<sql.Commentaire>()
                .Where(c => c.Id == commentId)
                .Delete());

            return deletedCount;
        }
    }
}
