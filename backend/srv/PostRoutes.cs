using System.Security.Claims;
using villagets.Auth;

namespace srv
{
    public static class PostRoutes
    {
        public static void Map(WebApplication app, PostService postService)
        {
            var supabaseClient = srv.SupabaseService.GetClient();

            app.MapGet("/publication/{id}", async (string id) => {
                var post = await postService.GetById(id);
                if (post is null) return Results.NotFound("Publication not found");

                return Results.Ok(new
                {
                    id = post.Id,
                    titre = post.Nom,
                    contenu = post.Contenu,
                    utilisateurId = post.UtilisateurId,
                    dateCreation = post.DatePublication
                });
            });


            app.MapPost("/publication", async (sql.Publication publication, HttpContext ctx) =>
            {
                var principal = AuthHelper.GetPrincipalFromContext(ctx);
                if (principal == null) return Results.Unauthorized();

                var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value!;

                var created = await postService.Create(publication, userId);
                return Results.Ok(created);
            });

            app.MapDelete("/publication/{id}", async (string id, HttpContext ctx) =>
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

        }
    }
}

