using Microsoft.Data.SqlClient;
using Microsoft.Extensions.Configuration.UserSecrets;
using Npgsql;
using Supabase.Postgrest;
using System.Security.Claims;
using villagets.Auth;
using static Supabase.Postgrest.Constants;
namespace srv
{
    public class PostService
    {
        private readonly Supabase.Client _supabase;

        public PostService()
        {
            _supabase = SupabaseService.GetClient();
        }

        public async Task<sql.Publication?> GetById(string id)
        {
            var result = await _supabase
                .From<sql.Publication>()
                .Where(p => p.Id == id)
                .Get();
            return result.Model;
        }
        public async Task<List<sql.Publication>> GetFeed(string userId)
        {
            var result = await _supabase.From<sql.Publication>().Order("date_publication", Supabase.Postgrest.Constants.Ordering.Descending).Limit(20).Get();

            return result.Models.ToList();
        }
        public async Task<sql.Publication?> Create(sql.Publication publication, string userId)
        {
            publication.UtilisateurId = userId;

            var result = await _supabase
                .From<sql.Publication>()
                .Insert(publication);

            var saved = result.Model;
            if (saved is null) return null;

            var urls = publication.Media?.ToList();

            if (urls is { Count: > 0 })
            {
                var fichiers = await _supabase
                    .From<sql.Fichier>()
                    .Filter("lien_fichier", Operator.In, urls)
                    .Get();

                var links = fichiers.Models
                    .Where(f => f.Id.HasValue)
                    .Select(f => new sql.PublicationFichier
                    {
                        IdFichier = f.Id.ToString(),
                        IdPublication = saved.Id
                    }).ToList();

                if (links.Count > 0)
                    await _supabase
                        .From<sql.PublicationFichier>()
                        .Insert(links);
            }

            return saved;
        }

        public async Task<bool> Delete(string id, string userId)
        {
            var existing = await _supabase
                .From<sql.Publication>()
                .Where(p => p.Id == id)
                .Get();

            var publication = existing.Models.FirstOrDefault();
            if (publication == null) return false;
            if (publication.UtilisateurId != userId)
                throw new UnauthorizedAccessException();

            await _supabase
                .From<sql.Publication>()
                .Where(p => p.Id == id)
                .Delete();

            return true;
        }

        public async Task<List<sql.Publication>> GetFeed(string userId, FeedQuery query)
        {
            // Step 1: if tags provided, resolve them → publication IDs
            List<string>? taggedPublicationIds = null;
            if (query.Tags is { Length: > 0 })
            {
                var categories = await _supabase
                    .From<sql.CategoriePublication>()
                    .Filter("nom", Operator.In, query.Tags.ToList())
                    .Get();

                var categoryIds = categories.Models.Select(c => c.Id.ToString()).ToList();

                if (categoryIds.Count == 0)
                    return [];

                var catPubs = await _supabase
                    .From<sql.CatPubPublication>()
                    .Filter("id_categorie_publication", Operator.In, categoryIds)
                    .Get();

                taggedPublicationIds = catPubs.Models.Select(cp => cp.IdPublication).Distinct().ToList();

                if (taggedPublicationIds.Count == 0)
                    return [];
            }

            var q = _supabase.From<sql.Publication>();

            if (query.IsMarketplace)
                q = (Supabase.Interfaces.ISupabaseTable<sql.Publication, Supabase.Realtime.RealtimeChannel>)q.Filter("article_a_vendre", Operator.Equals, "true");

            if (!string.IsNullOrWhiteSpace(query.SearchString))
                q = (Supabase.Interfaces.ISupabaseTable<sql.Publication, Supabase.Realtime.RealtimeChannel>)q.Filter("nom", Operator.ILike, $"%{query.SearchString}%");

            if (taggedPublicationIds is not null)
                q = (Supabase.Interfaces.ISupabaseTable<sql.Publication, Supabase.Realtime.RealtimeChannel>)q.Filter("id_publication", Operator.In, taggedPublicationIds);

            var result = await q
                .Order("date_publication", Ordering.Descending)
                .Limit(20)
                .Get();

            return result.Models.ToList();
        }

    }
}