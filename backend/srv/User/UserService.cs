using sql;
using static Supabase.Postgrest.Constants;
namespace srv
{
    public class UserService
    {
        private readonly Supabase.Client _supabase;
        private readonly ILogger<UserService> _logger;

        public UserService(ILogger<UserService> logger)
        {
            _supabase = SupabaseService.GetClient();
            _logger = logger;
        }

        public async Task<Utilisateur?> Register(string email, string password, string pseudo, string nom, string prenom, string photoProfil)
        {
            email = email.Trim();
            pseudo = pseudo.Trim();
            nom = nom.Trim();
            prenom = prenom.Trim();

            if (string.IsNullOrWhiteSpace(email)) throw new ArgumentException("Email is required");
            if (pseudo.Length < 3) throw new ArgumentException("Username must be at least 3 characters");
            if (password.Length < 8) throw new ArgumentException("Password must be at least 8 characters");

            var existingEmail = await PerfLogger.TimeAsync(_logger, "UserService.Register existing email lookup", () => _supabase.From<Utilisateur>().Where(u => u.Email == email).Get());
            if (existingEmail.Models.Count > 0) throw new InvalidOperationException("Email already in use");

            var existingPseudo = await PerfLogger.TimeAsync(_logger, "UserService.Register existing pseudo lookup", () => _supabase.From<Utilisateur>().Where(u => u.Pseudo == pseudo).Get());
            if (existingPseudo.Models.Count > 0) throw new InvalidOperationException("Username already taken");

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

            var result = await PerfLogger.TimeAsync(_logger, "UserService.Register insert user", () => _supabase.From<Utilisateur>().Insert(user));
            return result.Model;
        }

        public async Task<Utilisateur?> Login(string email, string password)
        {
            if (string.IsNullOrWhiteSpace(email) || string.IsNullOrWhiteSpace(password))
                throw new ArgumentException("Email and password are required");

            var result = await PerfLogger.TimeAsync(_logger, "UserService.Login user lookup", () => _supabase.From<Utilisateur>().Where(u => u.Email == email).Get());
            var user = result.Models.FirstOrDefault();

            if (user == null || user.DeletedAt.HasValue || !BCrypt.Net.BCrypt.Verify(password, user.Password))
                throw new UnauthorizedAccessException("Invalid credentials");

            return user;
        }

        public async Task<Utilisateur?> GetById(string id)
        {
            var result = await PerfLogger.TimeAsync(_logger, "UserService.GetById user lookup", () => _supabase
                .From<Utilisateur>()
                .Where(u => u.Id == id)
                .Get());
            return result.Model;
        }

        public async Task<bool> UpdatePseudo(string userId, string pseudo)
        {
            pseudo = pseudo.Trim();
            if (pseudo.Length < 3) throw new ArgumentException("Username must be at least 3 characters");

            var currentUser = await GetRequiredUser(userId);
            if (string.Equals(currentUser.Pseudo, pseudo, StringComparison.Ordinal))
                return true;

            var existing = await PerfLogger.TimeAsync(_logger, "UserService.UpdatePseudo pseudo lookup", () => _supabase.From<Utilisateur>().Filter("pseudo", Operator.Equals, pseudo).Get());
            if (existing.Models.Any(u => u.Id != userId)) throw new InvalidOperationException("Username already taken");

            await PerfLogger.TimeAsync(_logger, "UserService.UpdatePseudo update pseudo", () => _supabase.From<Utilisateur>().Where(u => u.Id == userId).Set(u => u.Pseudo!, pseudo).Update());
            return true;
        }

        public async Task<bool> UpdatePassword(string userId, string currentPassword, string newPassword)
        {
            if (newPassword.Length < 8) throw new ArgumentException("Password must be at least 8 characters");

            var result = await PerfLogger.TimeAsync(_logger, "UserService.UpdatePassword user lookup", () => _supabase.From<Utilisateur>().Where(u => u.Id == userId).Get());
            var user = result.Model;
            if (user == null) throw new KeyNotFoundException("User not found");
            if (!BCrypt.Net.BCrypt.Verify(currentPassword, user.Password))
                throw new UnauthorizedAccessException("Current password is incorrect");

            var hashed = BCrypt.Net.BCrypt.HashPassword(newPassword);
            await PerfLogger.TimeAsync(_logger, "UserService.UpdatePassword update password", () => _supabase.From<Utilisateur>().Where(u => u.Id == userId).Set(u => u.Password!, hashed).Update());
            return true;
        }

        public async Task<bool> UpdateEmail(string userId, string email)
        {
            email = email.Trim();
            if (string.IsNullOrWhiteSpace(email)) throw new ArgumentException("Email is required");

            var currentUser = await GetRequiredUser(userId);
            if (string.Equals(currentUser.Email, email, StringComparison.OrdinalIgnoreCase))
                return true;

            var existing = await PerfLogger.TimeAsync(_logger, "UserService.UpdateEmail email lookup", () => _supabase.From<Utilisateur>().Where(u => u.Email == email).Get());
            if (existing.Models.Any(u => u.Id != userId)) throw new InvalidOperationException("Email already in use");

            await PerfLogger.TimeAsync(_logger, "UserService.UpdateEmail update email", () => _supabase.From<Utilisateur>().Where(u => u.Id == userId).Set(u => u.Email!, email).Update());
            return true;
        }

        public async Task<bool> UpdateProfilePicture(string userId, string photoUrl)
        {
            await PerfLogger.TimeAsync(_logger, "UserService.UpdateProfilePicture update photo", () => _supabase.From<Utilisateur>().Where(u => u.Id == userId).Set(u => u.PhotoProfil!, photoUrl).Update());
            return true;
        }

        public async Task<List<Utilisateur>> GetUserListFromPseudo(string query)
        {
            var result = await PerfLogger.TimeAsync(_logger, "UserService.GetUserListFromPseudo user search", () => _supabase
                .From<Utilisateur>()
                .Filter("deleted_at", Operator.Is, (string?)null)
                .Filter("pseudo", Operator.ILike, $"%{query}%")
                .Get());
            return result.Models.ToList();
        }

        public async Task<List<DeletedFileRpcRow>> DeleteUserByAdmin(string targetUserId, string requestUserId)
        {
            var targetUser = await EnsureAdminCanManageUser(targetUserId, requestUserId);
            if (targetUser.DeletedAt.HasValue)
                throw new InvalidOperationException("User is already deleted.");

            if (!Guid.TryParse(targetUserId, out var rpcTargetUserId))
                throw new ArgumentException("Invalid user id.", nameof(targetUserId));

            var deletedEmail = $"d-{rpcTargetUserId:N}@del.ca";
            var deletedPassword = BCrypt.Net.BCrypt.HashPassword(Guid.NewGuid().ToString("N"));
            var deletedAt = DateTime.UtcNow;

            var deletedFiles = await PerfLogger.TimeAsync(_logger, "UserService.DeleteUserByAdmin rpc delete_user_with_posts", () => _supabase.Rpc<List<DeletedFileRpcRow>>(
                "delete_user_with_posts",
                new Dictionary<string, object?>
                {
                    { "p_user_id", rpcTargetUserId },
                    { "p_deleted_email", deletedEmail },
                    { "p_deleted_password", deletedPassword },
                    { "p_deleted_at", deletedAt }
                }));

            return deletedFiles ?? [];
        }

        public async Task<List<DeletedFileRpcRow>> WipeUserByAdmin(string targetUserId, string requestUserId)
        {
            _ = await EnsureAdminCanManageUser(targetUserId, requestUserId);

            if (!Guid.TryParse(targetUserId, out var rpcTargetUserId))
                throw new ArgumentException("Invalid user id.", nameof(targetUserId));

            var deletedFiles = await PerfLogger.TimeAsync(_logger, "UserService.WipeUserByAdmin rpc wipe_user_and_files", () => _supabase.Rpc<List<DeletedFileRpcRow>>(
                "wipe_user_and_files",
                new Dictionary<string, object?>
                {
                    { "p_user_id", rpcTargetUserId }
                }));

            return deletedFiles ?? [];
        }

        private async Task<Utilisateur> GetRequiredUser(string userId)
        {
            var result = await PerfLogger.TimeAsync(_logger, "UserService.GetRequiredUser user lookup", () => _supabase
                .From<Utilisateur>()
                .Where(u => u.Id == userId)
                .Get());

            return result.Model ?? throw new KeyNotFoundException("User not found");
        }

        private async Task<Utilisateur> EnsureAdminCanManageUser(string targetUserId, string requestUserId)
        {
            if (string.Equals(targetUserId, requestUserId, StringComparison.Ordinal))
                throw new InvalidOperationException("Admins cannot delete their own account.");

            var requestUser = await GetRequiredUser(requestUserId);
            if (requestUser.DeletedAt.HasValue || requestUser.MainAdmin != true)
                throw new UnauthorizedAccessException("Only admins can delete users.");

            return await GetRequiredUser(targetUserId);
        }
    }

    public class DeletedFileRpcRow
    {
        [Newtonsoft.Json.JsonProperty("id_fichier")]
        public Guid? IdFichier { get; set; }

        [Newtonsoft.Json.JsonProperty("lien_fichier")]
        public string? LienFichier { get; set; }
    }
}
