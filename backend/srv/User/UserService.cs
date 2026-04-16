using sql;
using static Supabase.Postgrest.Constants;
namespace srv
{
    public class UserService
    {
        private readonly Supabase.Client _supabase;

        public UserService()
        {
            _supabase = SupabaseService.GetClient();
        }

        public async Task<Utilisateur?> Register(string email, string password, string pseudo, string nom, string prenom, string photoProfil)
        {
            if (pseudo.Length < 3) throw new ArgumentException("Username must be at least 3 characters");
            if (password.Length < 8) throw new ArgumentException("Password must be at least 8 characters");

            var existing = await _supabase.From<Utilisateur>().Where(u => u.Email == email).Get();
            if (existing.Models.Count > 0) throw new InvalidOperationException("Email already in use");

            var user = new Utilisateur
            {
                Pseudo = pseudo,
                Nom = nom,
                Prenom = prenom,
                Password = BCrypt.Net.BCrypt.HashPassword(password),
                PhotoProfil = photoProfil,
                Email = email,
                DateCreation = DateTime.UtcNow,
                AnneeNaissance = DateTime.UtcNow
            };

            var result = await _supabase.From<Utilisateur>().Insert(user);
            return result.Model;
        }

        public async Task<Utilisateur?> Login(string email, string password)
        {
            if (string.IsNullOrWhiteSpace(email) || string.IsNullOrWhiteSpace(password))
                throw new ArgumentException("Email and password are required");

            var result = await _supabase.From<Utilisateur>().Where(u => u.Email == email).Get();
            var user = result.Models.FirstOrDefault();

            if (user == null || !BCrypt.Net.BCrypt.Verify(password, user.Password))
                throw new UnauthorizedAccessException("Invalid credentials");

            return user;
        }

        public async Task<Utilisateur?> GetById(string id)
        {
            var result = await _supabase
                .From<Utilisateur>()
                .Where(u => u.Id == id)
                .Get();
            return result.Model;
        }

        public async Task<bool> UpdatePseudo(string userId, string pseudo)
        {
            if (pseudo.Trim().Length < 3) throw new ArgumentException("Username must be at least 3 characters");

            var existing = await _supabase.From<Utilisateur>().Filter("pseudo", Operator.Equals, pseudo).Get();
            if (existing.Models.Count > 0) throw new InvalidOperationException("Username already taken");

            await _supabase.From<Utilisateur>().Where(u => u.Id == userId).Set(u => u.Pseudo!, pseudo).Update();
            return true;
        }

        public async Task<bool> UpdatePassword(string userId, string currentPassword, string newPassword)
        {
            if (newPassword.Length < 8) throw new ArgumentException("Password must be at least 8 characters");

            var result = await _supabase.From<Utilisateur>().Where(u => u.Id == userId).Get();
            var user = result.Model;
            if (user == null) throw new KeyNotFoundException("User not found");
            if (!BCrypt.Net.BCrypt.Verify(currentPassword, user.Password))
                throw new UnauthorizedAccessException("Current password is incorrect");

            var hashed = BCrypt.Net.BCrypt.HashPassword(newPassword);
            await _supabase.From<Utilisateur>().Where(u => u.Id == userId).Set(u => u.Password!, hashed).Update();
            return true;
        }

        public async Task<bool> UpdateEmail(string userId, string email)
        {
            var existing = await _supabase.From<Utilisateur>().Where(u => u.Email == email).Get();
            if (existing.Models.Count > 0) throw new InvalidOperationException("Email already in use");

            await _supabase.From<Utilisateur>().Where(u => u.Id == userId).Set(u => u.Email!, email).Update();
            return true;
        }

        public async Task<bool> UpdateProfilePicture(string userId, string photoUrl)
        {
            await _supabase.From<Utilisateur>().Where(u => u.Id == userId).Set(u => u.PhotoProfil!, photoUrl).Update();
            return true;
        }

        public async Task<List<Utilisateur>> GetUserListFromPseudo(string query)
        {
            var result = await _supabase.From<Utilisateur>().Filter("pseudo", Operator.ILike, $"%{query}%").Get();
            return result.Models.ToList();
        }
    }
}