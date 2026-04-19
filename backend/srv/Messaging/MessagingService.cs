using Microsoft.AspNetCore.Mvc;
using sql;
using Supabase.Postgrest;
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

            var buffer = new byte[1024 * 4];
            try
            {
                var result = await webSocket.ReceiveAsync(new ArraySegment<byte>(buffer), CancellationToken.None);

                while (!result.CloseStatus.HasValue)
                {
                    var incomingMessage = Encoding.UTF8.GetString(buffer, 0, result.Count);
                    var payload = JsonSerializer.Deserialize<WsMessagePayload>(incomingMessage);

                    if (payload != null && !string.IsNullOrEmpty(payload.receiverId))
                    {
                        var savedMsg = await SaveMessageAsync(userId, payload.receiverId, payload.contenu);

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

                    result = await webSocket.ReceiveAsync(new ArraySegment<byte>(buffer), CancellationToken.None);
                }

                await webSocket.CloseAsync(result.CloseStatus.Value, result.CloseStatusDescription, CancellationToken.None);
            }
            finally
            {
                _activeConnections.TryRemove(userId, out _);
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

        public async Task<string> GetOrCreateConversationAsync(string user1Id, string user2Id)
        {
            // Check if conversation exists
            var res1 = await PerfLogger.TimeAsync(_logger, "MessagingService.GetOrCreateConversationAsync lookup direct", () => _supabase.From<Conversation>()
                .Where(c => c.User1Id == user1Id && c.User2Id == user2Id)
                .Get());

            var existing = res1.Models.FirstOrDefault();

            if (existing == null)
            {
                // Check reverse 
                var res2 = await PerfLogger.TimeAsync(_logger, "MessagingService.GetOrCreateConversationAsync lookup reverse", () => _supabase.From<Conversation>()
                    .Where(c => c.User1Id == user2Id && c.User2Id == user1Id)
                    .Get());
                existing = res2.Models.FirstOrDefault();
            }

            if (existing != null) return existing.Id!;

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

            var conversationId = await GetOrCreateConversationAsync(userId1, userId2);

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
