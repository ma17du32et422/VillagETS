using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;
using villagets.Auth;

namespace srv.Post
{
    public static class PostRoutes
    {
        public static void Map(WebApplication app, PostService postService)
        {
            var supabaseClient = srv.SupabaseService.GetClient();

            app.MapGet("/post/{id}", async (int id) =>
            {
                var ret = await postService.GetById(id);
                if (ret is null) return Results.NotFound("Publication not found");

                return Results.Ok(ret);
            });

            app.MapPost("/post", async ([FromBody] PostCreateRequest publication, HttpContext ctx) =>
            {
                var currentUser = await AuthHelper.GetAuthenticatedUserAsync(ctx);
                if (currentUser == null) return Results.Unauthorized();

                try
                {
                    var created = await postService.Create(publication, currentUser.Id!);
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
                }
                catch (ArgumentException ex)
                {
                    return Results.BadRequest(ex.Message);
                }
            });

            app.MapDelete("/post/{id}", async (int id, HttpContext ctx) =>
            {
                var currentUser = await AuthHelper.GetAuthenticatedUserAsync(ctx);
                if (currentUser == null) return Results.Unauthorized();

                try
                {
                    var deleted = await postService.Delete(id, currentUser.Id!);
                    return deleted ? Results.Ok() : Results.NotFound("Publication not found");
                }
                catch (UnauthorizedAccessException)
                {
                    return Results.Forbid();
                }
            });

            app.MapPost("/feed", async ([FromBody] FeedQuery query, HttpContext ctx) =>
            {
                var userId = await AuthHelper.GetAuthenticatedUserIdIfActiveAsync(ctx);

                var feed = await postService.GetFeed(userId, query);

                return Results.Ok(feed);
            });

            app.MapGet("/user/{id}/posts", async (string id, HttpContext ctx) =>
            {
                var currentUserId = await AuthHelper.GetAuthenticatedUserIdIfActiveAsync(ctx);

                var feed = await postService.GetPostsByUser(currentUserId, id);

                return Results.Ok(feed);
            });
        }
    }
};

