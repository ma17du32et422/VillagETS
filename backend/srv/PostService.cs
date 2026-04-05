using Microsoft.Extensions.Configuration.UserSecrets;
using System.Security.Claims;
using villagets.Auth;
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

        public async Task<sql.Publication?> Create(sql.Publication publication, string userId)
        {
            publication.UtilisateurId = userId;
            var result = await _supabase
                .From<sql.Publication>()
                .Insert(publication);
            return result.Model;
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
    }
}