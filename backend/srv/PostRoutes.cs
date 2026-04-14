using Microsoft.AspNetCore.Mvc;
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
                var ret = await postService.GetById(id);
                if (ret.publication is null) return Results.NotFound("Publication not found");

                return Results.Ok(ret.publication.ToJson(ret.utilisateur));
            });

            app.MapPost("/post", async ([FromBody] sql.Publication publication, HttpContext ctx) =>
            {
                var principal = AuthHelper.GetPrincipalFromContext(ctx);
                if (principal == null) return Results.Unauthorized();

                var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value!;

                var created = await postService.Create(publication, userId);
                if (created is null) return Results.BadRequest("Failed to create post");

                return Results.Ok(new
                {
                    id = created.Id,
                    titre = created.Nom,
                    contenu = created.Contenu,
                    media = created.Media,
                    utilisateurId = created.UtilisateurId,
                    datePublication = created.DatePublication,
                    prix = created.Prix,
                    articleAVendre = created.ArticleAVendre
                });
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

            app.MapPost("/feed", async ([FromBody] FeedQuery query, HttpContext ctx) =>
            {
                var principal = AuthHelper.GetPrincipalFromContext(ctx);
                if (principal == null) return Results.Unauthorized();

                var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value!;

                var feed = await postService.GetFeed(userId, query);

                return Results.Ok(feed.posts.Select(p => {
                    feed.users.TryGetValue(p.UtilisateurId ?? "", out var user);
                    feed.reactions.TryGetValue(p.Id ?? "", out var reaction);
                    return p.ToJson(user, reaction);
                }));
            });
        }
    }
};

