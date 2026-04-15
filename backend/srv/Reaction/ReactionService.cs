using static Supabase.Postgrest.Constants;

namespace srv.Reaction
{
    public class ReactionService
    {
        private readonly Supabase.Client _supabase;

        public ReactionService()
        {
            _supabase = SupabaseService.GetClient();
        }

        private async Task UpdateCounts(string publicationId, int likeDelta, int dislikeDelta)
        {
            var pub = await _supabase
                .From<sql.Publication>()
                .Filter("id_publication", Operator.Equals, publicationId)
                .Get();

            var current = pub.Model;
            if (current == null) return;

            await _supabase
                .From<sql.Publication>()
                .Filter("id_publication", Operator.Equals, publicationId)
                .Set(p => p.Likes!, (current.Likes ?? 0) + likeDelta)
                .Set(p => p.Dislikes!, (current.Dislikes ?? 0) + dislikeDelta)
                .Update();
        }

        public async Task<(int likes, int dislikes, string? userReaction)> Toggle(string userId, string publicationId, string type)
        {
            var existing = await _supabase
                .From<sql.ReactionPublication>()
                .Filter("id_utilisateur", Operator.Equals, userId)
                .Filter("id_publication", Operator.Equals, publicationId)
                .Get();

            var current = existing.Models.FirstOrDefault();

            if (current == null)
            {
                await _supabase.From<sql.ReactionPublication>().Insert(new sql.ReactionPublication
                {
                    IdUtilisateur = userId,
                    IdPublication = publicationId,
                    Type = type
                });
                await UpdateCounts(publicationId, type == "like" ? 1 : 0, type == "dislike" ? 1 : 0);
            }
            else if (current.Type == type)
            {
                await _supabase
                    .From<sql.ReactionPublication>()
                    .Filter("id_utilisateur", Operator.Equals, userId)
                    .Filter("id_publication", Operator.Equals, publicationId)
                    .Delete();
                await UpdateCounts(publicationId, type == "like" ? -1 : 0, type == "dislike" ? -1 : 0);
            }
            else
            {
                await _supabase
                    .From<sql.ReactionPublication>()
                    .Filter("id_utilisateur", Operator.Equals, userId)
                    .Filter("id_publication", Operator.Equals, publicationId)
                    .Set(r => r.Type!, type)
                    .Update();
                await UpdateCounts(publicationId, type == "like" ? 1 : -1, type == "dislike" ? 1 : -1);
            }

            var pub = await _supabase
                .From<sql.Publication>()
                .Filter("id_publication", Operator.Equals, publicationId)
                .Get();

            var userReaction = await GetUserReaction(userId, publicationId);

            return (pub.Model?.Likes ?? 0, pub.Model?.Dislikes ?? 0, userReaction);
        }

        public async Task<string?> GetUserReaction(string userId, string publicationId)
        {
            var result = await _supabase
                .From<sql.ReactionPublication>()
                .Filter("id_utilisateur", Operator.Equals, userId)
                .Filter("id_publication", Operator.Equals, publicationId)
                .Get();

            return result.Models.FirstOrDefault()?.Type;
        }
    }
}