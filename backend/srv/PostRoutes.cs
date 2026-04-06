using System.Security.Claims;
using villagets.Auth;

namespace srv
{
    public static class PostRoutes
    {
        public static void Map(WebApplication app, PostService postService)
        {
            var supabaseClient = srv.SupabaseService.GetClient();

            app.MapGet("/post/{id}", async (string id) =>
            {
                var post = await postService.GetById(id);
                if (post is null) return Results.NotFound("Publication not found");

                return Results.Ok(new
                {
                    id = post.Id,
                    titre = post.Nom,
                    contenu = post.Contenu,
                    utilisateurId = post.UtilisateurId,
                    datePublication = post.DatePublication
                });
            });


            app.MapPost("/post", async (sql.Publication publication, HttpContext ctx) =>
            {
                var principal = AuthHelper.GetPrincipalFromContext(ctx);
                if (principal == null) return Results.Unauthorized();

                var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value!;

                var created = await postService.Create(publication, userId);
                return Results.Ok(created);
            });

            app.MapDelete("/post/{id}", async (string id, HttpContext ctx) =>
            {
                var principal = AuthHelper.GetPrincipalFromContext(ctx);
                if (principal == null) return Results.Unauthorized();

                var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value!;

                try
                {
                    var deleted = await postService.Delete(id, userId);
                    return deleted ? Results.Ok() : Results.NotFound("Publication not found");
                }
                catch (UnauthorizedAccessException)
                {
                    return Results.Forbid();
                }
            });

            app.MapGet("/feed", async (HttpContext ctx) =>
            {
                var principal = AuthHelper.GetPrincipalFromContext(ctx);
                if (principal == null) return Results.Unauthorized();

                var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value!;

                var feed = await postService.GetFeed(userId);

                return Results.Ok(feed.Select(p => new
                {
                    id = p.Id,
                    titre = p.Nom,
                    contenu = p.Contenu,
                    utilisateurId = p.UtilisateurId,
                    datePublication = p.DatePublication
                }));
            });
        }
    }
};

