using Microsoft.Data.SqlClient;
using Microsoft.Extensions.Configuration.UserSecrets;
using Npgsql;
using Supabase.Postgrest;
using System.Security.Claims;
using villagets.Auth;
using static Supabase.Postgrest.Constants;
namespace srv.Post
{
    public class PostService
    {
        private readonly Supabase.Client _supabase;

        public PostService()
        {
            _supabase = SupabaseService.GetClient();
        }

        public async Task<(sql.Publication? publication, sql.Utilisateur? utilisateur)> GetById(int id)
        {
            var result = await _supabase
                .From<sql.Publication>()
                .Where(p => p.Id == id)
                .Get();

            var post = result.Model;
            if (post == null) return (null, null);

            var userResult = await _supabase
                .From<sql.Utilisateur>()
                .Where(u => u.Id == post.UtilisateurId)
                .Get();

            return (post, userResult.Model);
        }
        public async Task<List<sql.Publication>> GetFeed(string userId)
        {
            var result = await _supabase.From<sql.Publication>().Order("date_publication", Supabase.Postgrest.Constants.Ordering.Descending).Limit(20).Get();

            return result.Models.ToList();
        }
        public async Task<sql.Publication?> Create(sql.Publication publication, string userId)
        {
            publication.UtilisateurId = userId;
            publication.DatePublication = DateTime.UtcNow;
            var result = await _supabase.From<sql.Publication>().Insert(publication);
            var saved = result.Model;
            if (saved is null) return null;

            saved.Media = publication.Media;

            var urls = publication.Media?.Where(u => u != null).ToList();
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
                        IdFichier = f.Id!.Value.ToString("D"),
                        IdPublication = saved.Id
                    }).ToList();
                Console.WriteLine($"Links to insert: {links.Count}");
                if (links.Count > 0)
                    await _supabase.From<sql.PublicationFichier>().Insert(links);
            }

            return saved;
        }


        public async Task<bool> Delete(int id, string userId)
        {
            var existing = await _supabase
                .From<sql.Publication>()
                .Where(p => p.Id == id)
                .Get();

            var publication = existing.Models.FirstOrDefault();
            if (publication == null) return false;
            if (publication.UtilisateurId != userId)
                throw new UnauthorizedAccessException();

            // Get all fichier links for this publication
            var pubFichiers = await _supabase
                .From<sql.PublicationFichier>()
                .Filter("id_publication", Operator.Equals, id)
                .Get();

            var fichierIds = pubFichiers.Models
                .Where(pf => pf.IdFichier != null)
                .Select(pf => pf.IdFichier!)
                .ToList();

            if (fichierIds.Count > 0)
            {
                // Get the actual fichier records to find file paths
                var fichiers = await _supabase
                    .From<sql.Fichier>()
                    .Filter("id_fichier", Operator.In, fichierIds)
                    .Get();

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
                await _supabase
                    .From<sql.PublicationFichier>()
                    .Filter("id_publication", Operator.Equals, id)
                    .Delete();

                // Delete fichier records from DB
                await _supabase
                    .From<sql.Fichier>()
                    .Filter("id_fichier", Operator.In, fichierIds)
                    .Delete();


            }

            // Delete reactions
            await _supabase
                .From<sql.ReactionPublication>()
                .Filter("id_publication", Operator.Equals, id)
                .Delete();

            // Delete the publication itself
            await _supabase
                .From<sql.Publication>()
                .Where(p => p.Id == id)
                .Delete();

            return true;
        }

        public async Task<(List<sql.Publication> posts, Dictionary<string, sql.Utilisateur> users, Dictionary<int, string?> reactions)> GetFeed(string? userId, FeedQuery query)
        {

            var q = _supabase.From<sql.Publication>();

            if (query.IsMarketplace)
                q = (Supabase.Interfaces.ISupabaseTable<sql.Publication, Supabase.Realtime.RealtimeChannel>)q.Filter("article_a_vendre", Operator.Equals, "true");

            if (!string.IsNullOrWhiteSpace(query.SearchString))
                q = (Supabase.Interfaces.ISupabaseTable<sql.Publication, Supabase.Realtime.RealtimeChannel>)q.Filter("nom", Operator.ILike, $"%{query.SearchString}%");

            var result = await q
                .Order("date_publication", Ordering.Descending)
                .Limit(20)
                .Get();

            var posts = result.Models.ToList();

            var userIds = posts.Select(p => p.UtilisateurId).Distinct().Where(id => id != null).ToList();

            var users = await _supabase
                .From<sql.Utilisateur>()
                .Filter("id_utilisateur", Operator.In, userIds)
                .Get();

            var userMap = users.Models.ToDictionary(u => u.Id!, u => u);

            var postIds = posts.Select(p => p.Id).Where(id => id != null).ToList();

            if (userId == null)
            {
                return (posts, userMap, new Dictionary<int, string?>());
            }
            var reactions = await _supabase
                .From<sql.ReactionPublication>()
                .Filter("id_utilisateur", Operator.Equals, userId)
                .Filter("id_publication", Operator.In, postIds)
                .Get();

            var reactionMap = reactions.Models
                .ToDictionary(r => r.IdPublication!.Value, r => r.Type);

            return (posts, userMap, reactionMap);

        }

        public async Task<(List<sql.Publication> posts, Dictionary<string, sql.Utilisateur> users, Dictionary<int, string?> reactions)> GetPostsByUser(string? currentUserId, string targetUserId)
        {
            var result = await _supabase
                .From<sql.Publication>()
                .Filter("id_utilisateur", Operator.Equals, targetUserId)
                .Order("date_publication", Ordering.Descending)
                .Limit(20)
                .Get();

            var posts = result.Models.ToList();
            var userMap = new Dictionary<string, sql.Utilisateur>();

            if (posts.Count > 0)
            {
                var users = await _supabase
                    .From<sql.Utilisateur>()
                    .Filter("id_utilisateur", Operator.Equals, targetUserId)
                    .Get();

                foreach (var user in users.Models)
                {
                    if (user.Id != null)
                        userMap[user.Id] = user;
                }
            }

            if (currentUserId == null || posts.Count == 0)
            {
                return (posts, userMap, new Dictionary<int, string?>());
            }

            var postIds = posts.Select(p => p.Id).Where(id => id != null).ToList();

            var reactions = await _supabase
                .From<sql.ReactionPublication>()
                .Filter("id_utilisateur", Operator.Equals, currentUserId)
                .Filter("id_publication", Operator.In, postIds)
                .Get();

            var reactionMap = reactions.Models
                .ToDictionary(r => r.IdPublication!.Value, r => r.Type);

            return (posts, userMap, reactionMap);
        }

    }
}
