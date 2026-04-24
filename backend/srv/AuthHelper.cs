using Microsoft.IdentityModel.Tokens;
using sql;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace villagets.Auth;

public static class AuthHelper
{
    private static SymmetricSecurityKey? _key;

    public static void Initialize(string secret)
    {
        _key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(secret));
    }

    public static string GenerateToken(string userId, string email, string pseudo)
    {
        if (_key == null) throw new Exception("JwtHelper not initialized");

        var claims = new[]
        {
            new Claim(JwtRegisteredClaimNames.Sub, userId),
            new Claim(JwtRegisteredClaimNames.Email, email),
            new Claim("pseudo", pseudo),
            new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString())
        };

        var creds = new SigningCredentials(_key, SecurityAlgorithms.HmacSha256);
        var token = new JwtSecurityToken(
            issuer: "villagets",
            audience: "villagets",
            claims: claims,
            expires: DateTime.UtcNow.AddDays(7),
            signingCredentials: creds
        );

        return new JwtSecurityTokenHandler().WriteToken(token);
    }

    public static ClaimsPrincipal? ValidateToken(string token)
    {
        if (_key == null) throw new Exception("JwtHelper not initialized");

        try
        {
            var handler = new JwtSecurityTokenHandler();
            return handler.ValidateToken(token, new TokenValidationParameters
            {
                ValidateIssuerSigningKey = true,
                IssuerSigningKey = _key,
                ValidateIssuer = true,
                ValidIssuer = "villagets",
                ValidateAudience = true,
                ValidAudience = "villagets",
                ValidateLifetime = true
            }, out _);
        }
        catch
        {
            return null;
        }
    }

    public static ClaimsPrincipal? GetClaimsFromContext(HttpContext ctx)
    {
        var token = ctx.Request.Cookies["token"];
        if (token == null) return null;
        return ValidateToken(token);
    }

    public static async Task<(Utilisateur?, bool)> GetAuthenticatedUserAsync(HttpContext ctx)
    {
        var principal = GetClaimsFromContext(ctx);
        var userId = principal?.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrWhiteSpace(userId))
            return (null, false);

        var supabase = srv.SupabaseService.GetClient();
        var result = await supabase
            .From<Utilisateur>()
            .Where(u => u.Id == userId)
            .Get();

        var user = result.Model;
        return user == null || user.DeletedAt.HasValue ? (null, true) : (user, false);
    }

    public static async Task<string?> GetAuthenticatedUserIdIfActiveAsync(HttpContext ctx)
    {
        var (user, isDeleted) = await GetAuthenticatedUserAsync(ctx);
        return isDeleted ? null : user?.Id;
    }
}
