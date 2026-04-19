using static Supabase.Postgrest.Constants;

namespace srv.Reaction
{
    public class ReactionService
    {
        private readonly Supabase.Client _supabase;
        private readonly ILogger<ReactionService> _logger;

        public ReactionService(ILogger<ReactionService> logger)
        {
            _supabase = SupabaseService.GetClient();
            _logger = logger;
        }

        private async Task UpdateCounts(int publicationId, int likeDelta, int dislikeDelta)
        {
            var pub = await PerfLogger.TimeAsync(_logger, "ReactionService.UpdateCounts publication lookup", () => _supabase
                .From<sql.Publication>()
                .Filter("id_publication", Operator.Equals, publicationId)
                .Get());

            var current = pub.Model;
            if (current == null) return;

            await PerfLogger.TimeAsync(_logger, "ReactionService.UpdateCounts update publication", () => _supabase
                .From<sql.Publication>()
                .Filter("id_publication", Operator.Equals, publicationId)
                .Set(p => p.Likes!, (current.Likes ?? 0) + likeDelta)
                .Set(p => p.Dislikes!, (current.Dislikes ?? 0) + dislikeDelta)
                .Update());
        }

        public async Task<(int likes, int dislikes, string? userReaction)> Toggle(string userId, int publicationId, string type)
        {
            var existing = await PerfLogger.TimeAsync(_logger, "ReactionService.Toggle reaction lookup", () => _supabase
                .From<sql.ReactionPublication>()
                .Filter("id_utilisateur", Operator.Equals, userId)
                .Filter("id_publication", Operator.Equals, publicationId)
                .Get());

            var current = existing.Models.FirstOrDefault();

            if (current == null)
            {
                await PerfLogger.TimeAsync(_logger, "ReactionService.Toggle insert reaction", () => _supabase.From<sql.ReactionPublication>().Insert(new sql.ReactionPublication
                {
                    IdUtilisateur = userId,
                    IdPublication = publicationId,
                    Type = type
                }));
                await UpdateCounts(publicationId, type == "like" ? 1 : 0, type == "dislike" ? 1 : 0);
            }
            else if (current.Type == type)
            {
                await PerfLogger.TimeAsync(_logger, "ReactionService.Toggle delete reaction", () => _supabase
                    .From<sql.ReactionPublication>()
                    .Filter("id_utilisateur", Operator.Equals, userId)
                    .Filter("id_publication", Operator.Equals, publicationId)
                    .Delete());
                await UpdateCounts(publicationId, type == "like" ? -1 : 0, type == "dislike" ? -1 : 0);
            }
            else
            {
                await PerfLogger.TimeAsync(_logger, "ReactionService.Toggle update reaction", () => _supabase
                    .From<sql.ReactionPublication>()
                    .Filter("id_utilisateur", Operator.Equals, userId)
                    .Filter("id_publication", Operator.Equals, publicationId)
                    .Set(r => r.Type!, type)
                    .Update());
                await UpdateCounts(publicationId, type == "like" ? 1 : -1, type == "dislike" ? 1 : -1);
            }

            var pub = await PerfLogger.TimeAsync(_logger, "ReactionService.Toggle publication lookup", () => _supabase
                .From<sql.Publication>()
                .Filter("id_publication", Operator.Equals, publicationId)
                .Get());

            var userReaction = await GetUserReaction(userId, publicationId);

            return (pub.Model?.Likes ?? 0, pub.Model?.Dislikes ?? 0, userReaction);
        }

        public async Task<string?> GetUserReaction(string userId, int publicationId)
        {
            var result = await PerfLogger.TimeAsync(_logger, "ReactionService.GetUserReaction reaction lookup", () => _supabase
                .From<sql.ReactionPublication>()
                .Filter("id_utilisateur", Operator.Equals, userId)
                .Filter("id_publication", Operator.Equals, publicationId)
                .Get());

            return result.Models.FirstOrDefault()?.Type;
        }
    }
}
