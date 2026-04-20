using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;
using villagets.Auth;

namespace srv.User
{
    public static class UserRoutes
    {
        public static void MapUserRoutes(this WebApplication app, UserService userService, bool isDevelopment)
        {
            app.MapPost("/auth/signup", async ([FromBody] SignupRequest req, HttpContext ctx) =>
            {
                try
                {
                    var newUser = await userService.Register(req.Email, req.Password, req.Pseudo, req.Nom, req.Prenom, req.PhotoProfil);
                    if (newUser == null) return Results.BadRequest("Failed to create user");

                    var token = villagets.Auth.AuthHelper.GenerateToken(newUser.Id!, newUser.Email!, newUser.Pseudo!);
                    ctx.Response.Cookies.Append("token", token, new CookieOptions
                    {
                        HttpOnly = true,
                        Secure = !isDevelopment,
                        SameSite = isDevelopment ? SameSiteMode.Lax : SameSiteMode.Strict,
                        Expires = DateTimeOffset.UtcNow.AddDays(7)
                    });

                    return Results.Ok(new { userId = newUser.Id });
                }
                catch (ArgumentException ex) { return Results.BadRequest(ex.Message); }
                catch (InvalidOperationException ex) { return Results.BadRequest(ex.Message); }
                catch (Exception ex)
                {
                    if (IsUniqueConstraint(ex))
                        return Results.BadRequest("Email or username already in use");

                    return Results.BadRequest(ex.Message);
                }
            });

            app.MapPost("/auth/login", async ([FromBody] LoginRequest req, HttpContext ctx) =>
            {
                try
                {
                    var user = await userService.Login(req.Email, req.Password);
                    if (user == null) return Results.Unauthorized();

                    var token = villagets.Auth.AuthHelper.GenerateToken(user.Id!, user.Email!, user.Pseudo!);
                    ctx.Response.Cookies.Append("token", token, new CookieOptions
                    {
                        HttpOnly = true,
                        Secure = !isDevelopment,
                        SameSite = isDevelopment ? SameSiteMode.Lax : SameSiteMode.Strict,
                        Expires = DateTimeOffset.UtcNow.AddDays(7)
                    });

                    return Results.Ok(new { userId = user.Id });
                }
                catch (ArgumentException ex) { return Results.BadRequest(ex.Message); }
                catch (UnauthorizedAccessException) { return Results.Unauthorized(); }
            });

            app.MapPost("/auth/logout", (HttpContext ctx) =>
            {
                ctx.Response.Cookies.Delete("token");
                return Results.Ok();
            });

            app.MapGet("/user/{id}", async (string id) =>
            {
                var user = await userService.GetById(id);
                if (user == null) return Results.NotFound();

                return Results.Ok(new
                {
                    userId = user.Id,
                    pseudo = user.Pseudo,
                    nom = user.Nom,
                    prenom = user.Prenom,
                    photoProfil = user.PhotoProfil
                });
            });

            app.MapPatch("/user/pseudo", async ([FromBody] UpdatePseudoRequest req, HttpContext ctx) =>
            {
                var principal = AuthHelper.GetClaimsFromContext(ctx);
                if (principal == null) return Results.Unauthorized();
                var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value!;

                try
                {
                    await userService.UpdatePseudo(userId, req.Pseudo);
                    return Results.Ok();
                }
                catch (ArgumentException ex) { return Results.BadRequest(ex.Message); }
                catch (InvalidOperationException ex) { return Results.BadRequest(ex.Message); }
                catch (KeyNotFoundException ex) { return Results.NotFound(ex.Message); }
            });

            app.MapPatch("/user/password", async ([FromBody] UpdatePasswordRequest req, HttpContext ctx) =>
            {
                var principal = AuthHelper.GetClaimsFromContext(ctx);
                if (principal == null) return Results.Unauthorized();
                var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value!;

                try
                {
                    await userService.UpdatePassword(userId, req.CurrentPassword, req.NewPassword);
                    return Results.Ok();
                }
                catch (ArgumentException ex) { return Results.BadRequest(ex.Message); }
                catch (UnauthorizedAccessException ex) { return Results.BadRequest(ex.Message); }
                catch (KeyNotFoundException ex) { return Results.NotFound(ex.Message); }
            });

            app.MapPatch("/user/email", async ([FromBody] UpdateEmailRequest req, HttpContext ctx) =>
            {
                var principal = AuthHelper.GetClaimsFromContext(ctx);
                if (principal == null) return Results.Unauthorized();
                var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value!;

                try
                {
                    await userService.UpdateEmail(userId, req.Email);
                    return Results.Ok();
                }
                catch (ArgumentException ex) { return Results.BadRequest(ex.Message); }
                catch (InvalidOperationException ex) { return Results.BadRequest(ex.Message); }
                catch (KeyNotFoundException ex) { return Results.NotFound(ex.Message); }
            });

            app.MapPatch("/user/photo", async ([FromBody] UpdatePhotoRequest req, HttpContext ctx) =>
            {
                var principal = AuthHelper.GetClaimsFromContext(ctx);
                if (principal == null) return Results.Unauthorized();
                var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value!;

                await userService.UpdateProfilePicture(userId, req.PhotoUrl);
                return Results.Ok();
            });

            app.MapGet("/user/search", async (string query) =>
            {
                if (string.IsNullOrWhiteSpace(query))
                {
                    return Results.BadRequest("Search query cannot be empty.");
                }

                var result = await userService.GetUserListFromPseudo(query);
                var users = result.Select(u => u.ToJson()).ToList();

                return Results.Ok(users);
            });
        }

        private static bool IsUniqueConstraint(Exception ex) =>
            ex.Message.Contains("23505", StringComparison.OrdinalIgnoreCase) ||
            ex.Message.Contains("duplicate key", StringComparison.OrdinalIgnoreCase) ||
            ex.Message.Contains("unique constraint", StringComparison.OrdinalIgnoreCase);
    }

    record UpdatePseudoRequest(string Pseudo);
    record UpdatePasswordRequest(string CurrentPassword, string NewPassword);
    record UpdateEmailRequest(string Email);
    record UpdatePhotoRequest(string PhotoUrl);
}
