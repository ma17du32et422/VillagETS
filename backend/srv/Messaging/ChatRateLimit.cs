public class ChatRateLimit
{
    private static readonly Dictionary<string, Queue<DateTime>> _timestamps = new();
    private static readonly Lock _lock = new();
    private const int MAX_MESSAGES = 10;
    private static readonly TimeSpan WINDOW = TimeSpan.FromMinutes(1);

    private readonly RequestDelegate _next;

    public ChatRateLimitMiddleware(RequestDelegate next)
    {
        _next = next;
    }

    public async Task InvokeAsync(HttpContext ctx)
    {
        if (ctx.Request.Path.StartsWithSegments("/ws/chat"))
        {
            await _next(ctx); // WebSocket géré autrement
            return;
        }

        var principal = AuthHelper.GetClaimsFromContext(ctx);
        var userId = principal?.FindFirst(ClaimTypes.NameIdentifier)?.Value;

        if (userId != null && ctx.Request.Method == "POST")
        {
            var now = DateTime.UtcNow;

            lock (_lock)
            {
                if (!_timestamps.ContainsKey(userId))
                    _timestamps[userId] = new Queue<DateTime>();

                var queue = _timestamps[userId];

                // Purger les vieux timestamps
                while (queue.Count > 0 && now - queue.Peek() > WINDOW)
                    queue.Dequeue();

                if (queue.Count >= MAX_MESSAGES)
                {
                    ctx.Response.StatusCode = 429;
                    ctx.Response.ContentType = "application/json";
                    await ctx.Response.WriteAsync("{\"error\":\"Trop de messages, réessayez dans une minute.\"}");
                    return;
                }

                queue.Enqueue(now);
            }
        }

        await _next(ctx);
    }
}