using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;
using villagets.Auth;

namespace srv.Reaction
{
    public static class ReactionRoutes
    {
        public static void MapReactionRoutes(this WebApplication app, ReactionService reactionService)
        {
            app.MapPost("/post/{id}/react", async (int id, [FromBody] ReactRequest req, HttpContext ctx) =>
            {
                var (currentUser, isDeleted) = await AuthHelper.GetAuthenticatedUserAsync(ctx);
                if (isDeleted) return Results.Problem("Utilisateur supprimé.", null, statusCode: 410);
                if (currentUser == null) return Results.Unauthorized();

                if (req.Type != "like" && req.Type != "dislike")
                    return Results.BadRequest("Type must be 'like' or 'dislike'");

                var (likes, dislikes, userReaction) = await reactionService.Toggle(currentUser.Id!, id, req.Type);

                return Results.Ok(new { likes, dislikes, userReaction });
            });

            app.MapGet("/post/{id}/react", async (int id, HttpContext ctx) =>
            {
                var (currentUser, isDeleted) = await AuthHelper.GetAuthenticatedUserAsync(ctx);
                if (isDeleted) return Results.Problem("Utilisateur supprimé.", null, statusCode: 410);
                if (currentUser == null) return Results.Unauthorized();

                var reaction = await reactionService.GetUserReaction(currentUser.Id!, id);
                return Results.Ok(new { userReaction = reaction });
            });
        }
    }

    record ReactRequest(string Type);
}
