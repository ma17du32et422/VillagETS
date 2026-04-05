using Microsoft.IdentityModel.Tokens;
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
        catch { return null; }
    }
    public static ClaimsPrincipal? GetPrincipalFromContext(HttpContext ctx)
    {
        var token = ctx.Request.Cookies["token"];
        if (token == null) return null;
        return ValidateToken(token);
    }
}
