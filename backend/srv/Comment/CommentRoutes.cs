using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;
using villagets.Auth;

namespace srv.Comment
{
    public record CreateCommentBody(string Contenu, string? ParentCommentaireId);

    public static class CommentRoutes
    {
        public static void Map(WebApplication app, CommentService commentService)
        {
            app.MapGet("/post/{publicationId}/comments", async (int? publicationId) =>
            {
                if (publicationId == null) return Results.BadRequest("Publication ID is required.");

                var results = await commentService.GetByPublication(publicationId.Value);
                return Results.Ok(results.Select(r => r.comment.ToJson(r.user)));
            });

            app.MapGet("/comment/{commentId}/replies", async (string commentId) =>
            {
                var results = await commentService.GetReplies(commentId);
                return Results.Ok(results.Select(r => r.comment.ToJson(r.user)));
            });

            app.MapPost("/post/{publicationId}/comment", async (
                int publicationId,
                [FromBody] CreateCommentBody body,
                HttpContext ctx) =>
            {
                var currentUser = await AuthHelper.GetAuthenticatedUserAsync(ctx);
                if (currentUser == null) return Results.Unauthorized();

                if (string.IsNullOrWhiteSpace(body.Contenu))
                    return Results.BadRequest("Le contenu du commentaire est requis.");

                var (comment, user) = await commentService.Create(
                    publicationId,
                    currentUser.Id!,
                    body.Contenu,
                    body.ParentCommentaireId
                );

                if (comment is null)
                    return Results.NotFound("Publication ou commentaire parent introuvable.");

                return Results.Ok(comment.ToJson(user));
            });

            app.MapDelete("/comment/{commentId}", async (string commentId, HttpContext ctx) =>
            {
                var currentUser = await AuthHelper.GetAuthenticatedUserAsync(ctx);
                if (currentUser == null) return Results.Unauthorized();

                try
                {
                    var deletedCount = await commentService.Delete(commentId, currentUser.Id!);
                    return deletedCount > 0
                        ? Results.Ok(new { deletedCount })
                        : Results.NotFound("Commentaire introuvable.");
                }
                catch (UnauthorizedAccessException)
                {
                    return Results.Forbid();
                }
            });
        }
    }
}
