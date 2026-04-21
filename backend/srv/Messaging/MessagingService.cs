using Microsoft.AspNetCore.Mvc;
using sql;
using Supabase.Postgrest;
using System.Buffers;
using System.Collections.Concurrent;
using System.Net.WebSockets;
using System.Text;
using System.Text.Json;

namespace srv.Messaging
{
    public class MessagingService
    {
        private readonly Supabase.Client _supabase;
        private readonly ILogger<MessagingService> _logger;
        private const int ReceiveBufferSize = 4 * 1024;
        private const int MaxMessageBytes = 64 * 1024;

        // Keeps track of currently connected users: Key = UserId, Value = WebSocket
        private readonly ConcurrentDictionary<string, WebSocket> _activeConnections = new();

        public MessagingService(ILogger<MessagingService> logger)
        {
            _supabase = SupabaseService.GetClient();
            _logger = logger;
        }

        public async Task HandleConnectionAsync(string userId, WebSocket webSocket)
        {
            _activeConnections.AddOrUpdate(userId, webSocket, (key, oldValue) => webSocket);

            try
            {
                while (webSocket.State == WebSocketState.Open)
                {
                    string? incomingMessage;
                    WebSocketReceiveResult receiveResult;
                    try
                    {
                        (incomingMessage, receiveResult) = await ReceiveTextMessageAsync(webSocket, CancellationToken.None);
                    }
                    catch (InvalidOperationException ex)
                    {
                        _logger.LogWarning(ex, "Closing websocket for user {UserId}: invalid message payload", userId);
                        if (webSocket.State == WebSocketState.Open)
                            await webSocket.CloseAsync(WebSocketCloseStatus.InvalidPayloadData, ex.Message, CancellationToken.None);
                        break;
                    }

                    if (receiveResult.MessageType == WebSocketMessageType.Close || receiveResult.CloseStatus.HasValue)
                    {
                        if (webSocket.State == WebSocketState.Open || webSocket.State == WebSocketState.CloseReceived)
                        {
                            await webSocket.CloseAsync(
                                receiveResult.CloseStatus ?? WebSocketCloseStatus.NormalClosure,
                                receiveResult.CloseStatusDescription,
                                CancellationToken.None);
                        }
                        break;
                    }

                    if (string.IsNullOrWhiteSpace(incomingMessage))
                        continue;

                    WsMessagePayload? payload;
                    try
                    {
                        payload = JsonSerializer.Deserialize<WsMessagePayload>(incomingMessage);
                    }
                    catch (JsonException ex)
                    {
                        _logger.LogWarning(ex, "Ignoring malformed websocket payload from user {UserId}", userId);
                        continue;
                    }

                    if (payload != null && !string.IsNullOrWhiteSpace(payload.receiverId))
                     {
                         var limited = IsRateLimited(userId);
                         _logger.LogInformation("Rate limit check for {UserId}: limited={Limited}", userId, limited);

                         if (limited)
                         {
                             var errorMsg = JsonSerializer.Serialize(new { error = "rate_limited", message = "Trop de messages." });
                             var errorBytes = Encoding.UTF8.GetBytes(errorMsg);
                             await webSocket.SendAsync(new ArraySegment<byte>(errorBytes), WebSocketMessageType.Text, true, CancellationToken.None);
                             continue;
                         }

                         var savedMsg = await SaveMessageAsync(userId, payload.receiverId, payload.contenu?.Trim());


                        if (savedMsg != null && _activeConnections.TryGetValue(payload.receiverId, out var receiverSocket))
                        {
                            if (receiverSocket.State == WebSocketState.Open)
                            {
                                var output = new MessageDTO
                                {
                                    id = savedMsg.Id,
                                    conversationId = savedMsg.ConversationId,
                                    envoyeurId = savedMsg.EnvoyeurId,
                                    receveurId = savedMsg.ReceveurId,
                                    contenu = savedMsg.Contenu,
                                    dateMsg = savedMsg.DateMsg
                                };

                                var outMsg = JsonSerializer.Serialize(output);
                                var outBytes = Encoding.UTF8.GetBytes(outMsg);
                                await receiverSocket.SendAsync(new ArraySegment<byte>(outBytes), WebSocketMessageType.Text, true, CancellationToken.None);
                            }
                        }
                    }
                }
            }
            finally
            {
                _activeConnections.TryRemove(userId, out _);
            }
        }

        private async Task<(string? message, WebSocketReceiveResult result)> ReceiveTextMessageAsync(WebSocket webSocket, CancellationToken cancellationToken)
        {
            var buffer = ArrayPool<byte>.Shared.Rent(ReceiveBufferSize);
            try
            {
                using var stream = new MemoryStream();
                while (true)
                {
                    var result = await webSocket.ReceiveAsync(new ArraySegment<byte>(buffer), cancellationToken);
                    if (result.MessageType == WebSocketMessageType.Close)
                        return (null, result);

                    stream.Write(buffer, 0, result.Count);
                    if (stream.Length > MaxMessageBytes)
                        throw new InvalidOperationException("Message exceeds the 64 KB limit.");

                    if (result.EndOfMessage)
                        return (Encoding.UTF8.GetString(stream.ToArray()), result);
                }
            }
            finally
            {
                ArrayPool<byte>.Shared.Return(buffer);
            }
        }

        private async Task<ConversationMessage?> SaveMessageAsync(string senderId, string receiverId, string? content = "")
        {
            var conversationId = await GetOrCreateConversationAsync(senderId, receiverId);

            var msg = new ConversationMessage
            {
                ConversationId = conversationId,
                EnvoyeurId = senderId,
                ReceveurId = receiverId,
                Contenu = content,
                DateMsg = DateTime.UtcNow
            };

            var result = await PerfLogger.TimeAsync(_logger, "MessagingService.SaveMessageAsync insert message", () => _supabase.From<ConversationMessage>().Insert(msg));
            return result.Models.FirstOrDefault();
        }

        public async Task<string?> FindConversationIdAsync(string user1Id, string user2Id)
        {
            var res1 = await PerfLogger.TimeAsync(_logger, "MessagingService.FindConversationIdAsync lookup direct", () => _supabase.From<Conversation>()
                .Where(c => c.User1Id == user1Id && c.User2Id == user2Id)
                .Get());

            var existing = res1.Models.FirstOrDefault();

            if (existing == null)
            {
                var res2 = await PerfLogger.TimeAsync(_logger, "MessagingService.FindConversationIdAsync lookup reverse", () => _supabase.From<Conversation>()
                    .Where(c => c.User1Id == user2Id && c.User2Id == user1Id)
                    .Get());
                existing = res2.Models.FirstOrDefault();
            }

            return existing?.Id;
        }

        public async Task<string> GetOrCreateConversationAsync(string user1Id, string user2Id)
        {
            var existingId = await FindConversationIdAsync(user1Id, user2Id);
            if (!string.IsNullOrWhiteSpace(existingId))
                return existingId;

            // Create new if none exists
            var newConvo = new Conversation
            {
                User1Id = user1Id,
                User2Id = user2Id
            };

            var insertRes = await PerfLogger.TimeAsync(_logger, "MessagingService.GetOrCreateConversationAsync insert conversation", () => _supabase.From<Conversation>().Insert(newConvo));
            return insertRes.Models.First().Id!;
        }

        public async Task<List<MessageDTO>> GetHistoryAsync(string userId1, string userId2)
        {
            var conversationId = await FindConversationIdAsync(userId1, userId2);
            if (string.IsNullOrWhiteSpace(conversationId))
                return [];

            var result = await PerfLogger.TimeAsync(_logger, "MessagingService.GetHistoryAsync messages", () => _supabase.From<ConversationMessage>()
                .Where(m => m.ConversationId == conversationId)
                .Order("date_msg", Supabase.Postgrest.Constants.Ordering.Ascending)
                .Get());

            return result.Models.Select(m => new MessageDTO
            {
                id = m.Id,
                conversationId = m.ConversationId,
                envoyeurId = m.EnvoyeurId,
                receveurId = m.ReceveurId,
                contenu = m.Contenu,
                dateMsg = m.DateMsg
            }).ToList();
        }

        public async Task<List<ConversationDTO>> GetAllConversationsAsync(string currentUserId)
        {
            var response = await PerfLogger.TimeAsync(_logger, "MessagingService.GetAllConversationsAsync conversations", () => _supabase.From<Conversation>()
                .Where(c => c.User1Id == currentUserId || c.User2Id == currentUserId)
                .Get());

            var conversations = response.Models;
            var sidebarList = new List<ConversationDTO>();

            if (!conversations.Any()) return sidebarList;

            var otherUserIds = conversations
                .Select(c => c.User1Id == currentUserId ? c.User2Id : c.User1Id)
                .Where(id => id != null)
                .Distinct()
                .ToList();

            var usersResponse = await PerfLogger.TimeAsync(_logger, "MessagingService.GetAllConversationsAsync users", () => _supabase.From<Utilisateur>()
                .Filter("id_utilisateur", Supabase.Postgrest.Constants.Operator.In, otherUserIds!)
                .Get());

            var userMap = usersResponse.Models.ToDictionary(u => u.Id!, u => u);

            foreach (var con in conversations)
            {
                var otherId = con.User1Id == currentUserId ? con.User2Id : con.User1Id;

                if (otherId != null && userMap.TryGetValue(otherId, out var otherUser))
                {
                    sidebarList.Add(new ConversationDTO
                    {
                        ConversationId = con.Id,
                        OtherUser = otherUser.ToJson()
                    });
                }
            }

            return sidebarList;
        }

            private readonly ConcurrentDictionary<string, Queue<DateTime>> _rateLimits = new();
            private readonly object _rateLock = new();
            private const int MaxMessagesPerMinute = 10;

            private bool IsRateLimited(string userId)
            {
                var now = DateTime.UtcNow;
                lock (_rateLock)
                {
                    var queue = _rateLimits.GetOrAdd(userId, _ => new Queue<DateTime>());

                    while (queue.Count > 0 && now - queue.Peek() > TimeSpan.FromMinutes(1))
                        queue.Dequeue();

                    if (queue.Count >= MaxMessagesPerMinute) return true;

                    queue.Enqueue(now);
                    return false;
                }
            }
    }

    // DTOSSSSSSS FOR UHHH SENDING JSON SHOULD PROB BE IN .TOJSON() BUT WHATEVER
    public class WsMessagePayload
    {
        public string? receiverId { get; set; }
        public string? contenu { get; set; }
    }

    public class MessageDTO
    {
        public string? id { get; set; }
        public string? conversationId { get; set; }
        public string? envoyeurId { get; set; }
        public string? receveurId { get; set; }
        public string? contenu { get; set; }
        public DateTime? dateMsg { get; set; }
    }

    public class ConversationDTO
    {
        public string? ConversationId { get; set; }
        public object? OtherUser { get; set; }
    }


}
