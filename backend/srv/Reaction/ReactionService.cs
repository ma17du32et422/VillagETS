using static Supabase.Postgrest.Constants;
using System.Collections.Concurrent;

namespace srv.Reaction
{
    public class ReactionService
    {
        private readonly Supabase.Client _supabase;
        private readonly ILogger<ReactionService> _logger;
        private readonly ConcurrentDictionary<int, SemaphoreSlim> _publicationLocks = new();

        public ReactionService(ILogger<ReactionService> logger)
        {
            _supabase = SupabaseService.GetClient();
            _logger = logger;
        }

        private SemaphoreSlim GetPublicationLock(int publicationId) =>
            _publicationLocks.GetOrAdd(publicationId, _ => new SemaphoreSlim(1, 1));

        private async Task<(int likes, int dislikes)> SyncCounts(int publicationId)
        {
            var reactions = await PerfLogger.TimeAsync(_logger, "ReactionService.SyncCounts reactions", () => _supabase
                .From<sql.ReactionPublication>()
                .Filter("id_publication", Operator.Equals, publicationId)
                .Get());

            var likes = reactions.Models.Count(r => r.Type == "like");
            var dislikes = reactions.Models.Count(r => r.Type == "dislike");

            await PerfLogger.TimeAsync(_logger, "ReactionService.SyncCounts update publication", () => _supabase
                .From<sql.Publication>()
                .Filter("id_publication", Operator.Equals, publicationId)
                .Set(p => p.Likes!, likes)
                .Set(p => p.Dislikes!, dislikes)
                .Update());

            return (likes, dislikes);
        }

        public async Task<(int likes, int dislikes, string? userReaction)> Toggle(string userId, int publicationId, string type)
        {
            var gate = GetPublicationLock(publicationId);
            await gate.WaitAsync();
            try
            {
                var existing = await PerfLogger.TimeAsync(_logger, "ReactionService.Toggle reaction lookup", () => _supabase
                    .From<sql.ReactionPublication>()
                    .Filter("id_utilisateur", Operator.Equals, userId)
                    .Filter("id_publication", Operator.Equals, publicationId)
                    .Get());

                var current = existing.Models.FirstOrDefault();
                string? userReaction;

                if (current == null)
                {
                    await PerfLogger.TimeAsync(_logger, "ReactionService.Toggle insert reaction", () => _supabase.From<sql.ReactionPublication>().Insert(new sql.ReactionPublication
                    {
                        IdUtilisateur = userId,
                        IdPublication = publicationId,
                        Type = type
                    }));
                    userReaction = type;
                }
                else if (current.Type == type)
                {
                    await PerfLogger.TimeAsync(_logger, "ReactionService.Toggle delete reaction", () => _supabase
                        .From<sql.ReactionPublication>()
                        .Filter("id_utilisateur", Operator.Equals, userId)
                        .Filter("id_publication", Operator.Equals, publicationId)
                        .Delete());
                    userReaction = null;
                }
                else
                {
                    await PerfLogger.TimeAsync(_logger, "ReactionService.Toggle update reaction", () => _supabase
                        .From<sql.ReactionPublication>()
                        .Filter("id_utilisateur", Operator.Equals, userId)
                        .Filter("id_publication", Operator.Equals, publicationId)
                        .Set(r => r.Type!, type)
                        .Update());
                    userReaction = type;
                }

                var (likes, dislikes) = await SyncCounts(publicationId);
                return (likes, dislikes, userReaction);
            }
            finally
            {
                gate.Release();
            }
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
