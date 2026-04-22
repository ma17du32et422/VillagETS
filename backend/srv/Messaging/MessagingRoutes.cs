using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;
using villagets.Auth;

namespace srv.Messaging
{
    public static class MessagingRoutes
    {
        public static void Map(WebApplication app, MessagingService messagingService)
        {
            // 1. WebSocket Endpoint for Real-time Chat
            app.Map("/ws/chat", async (HttpContext ctx) =>
            {
                if (!ctx.WebSockets.IsWebSocketRequest)
                {
                    ctx.Response.StatusCode = StatusCodes.Status400BadRequest;
                    return;
                }

                var currentUser = await AuthHelper.GetAuthenticatedUserAsync(ctx);
                if (currentUser == null)
                {
                    ctx.Response.StatusCode = StatusCodes.Status401Unauthorized;
                    return;
                }

                using var webSocket = await ctx.WebSockets.AcceptWebSocketAsync();

                // Keep the connection alive and handle incoming/outgoing messages
                await messagingService.HandleConnectionAsync(currentUser.Id!, webSocket);
            });

            // 2. Get Chat History Endpoint
            app.MapGet("/chat/history/{targetUserId}", async (string targetUserId, HttpContext ctx) =>
            {
                var currentUser = await AuthHelper.GetAuthenticatedUserAsync(ctx);
                if (currentUser == null) return Results.Unauthorized();

                var history = await messagingService.GetHistoryAsync(currentUser.Id!, targetUserId);
                return Results.Ok(history);

            });

            app.MapGet("/chat/conversations", async (HttpContext ctx) =>
            {
                var currentUser = await AuthHelper.GetAuthenticatedUserAsync(ctx);
                if (currentUser == null) return Results.Unauthorized();

                var conversations = await messagingService.GetAllConversationsAsync(currentUser.Id!);
                return Results.Ok(conversations);
            });

            app.MapDelete("/chat/message/{messageId}", async (string messageId, HttpContext ctx) =>
            {
                var currentUser = await AuthHelper.GetAuthenticatedUserAsync(ctx);
                if (currentUser == null) return Results.Unauthorized();

                try
                {
                    var deleted = await messagingService.DeleteMessageAsync(messageId, currentUser.Id!);
                    return deleted ? Results.Ok() : Results.NotFound("Message not found");
                }
                catch (UnauthorizedAccessException)
                {
                    return Results.Forbid();
                }
            });
        }
    }
}
