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
                var principal = AuthHelper.GetClaimsFromContext(ctx);
                if (principal == null) return Results.Unauthorized();
                var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value!;

                if (req.Type != "like" && req.Type != "dislike")
                    return Results.BadRequest("Type must be 'like' or 'dislike'");

                var (likes, dislikes, userReaction) = await reactionService.Toggle(userId, id, req.Type);

                return Results.Ok(new { likes, dislikes, userReaction });
            });

            app.MapGet("/post/{id}/react", async (int id, HttpContext ctx) =>
            {
                var principal = AuthHelper.GetClaimsFromContext(ctx);
                if (principal == null) return Results.Unauthorized();
                var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value!;

                var reaction = await reactionService.GetUserReaction(userId, id);
                return Results.Ok(new { userReaction = reaction });
            });
        }
    }

    record ReactRequest(string Type);
}