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

        // Keeps track of currently connected users: Key = UserId, Value = WebSocket
        private readonly ConcurrentDictionary<string, WebSocket> _activeConnections = new();

        public MessagingService()
        {
            _supabase = SupabaseService.GetClient();
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

                    if (payload != null && !string.IsNullOrEmpty(payload.ReceiverId))
                    {
                        var savedMsg = await SaveMessageAsync(userId, payload.ReceiverId, payload.Content);

                        if (savedMsg != null && _activeConnections.TryGetValue(payload.ReceiverId, out var receiverSocket))
                        {
                            if (receiverSocket.State == WebSocketState.Open)
                            {
                                var output = new MessageDTO
                                {
                                    Id = savedMsg.Id,
                                    ConversationId = savedMsg.ConversationId,
                                    EnvoyeurId = savedMsg.EnvoyeurId,
                                    ReceveurId = savedMsg.ReceveurId,
                                    Contenu = savedMsg.Contenu,
                                    DateMsg = savedMsg.DateMsg
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


        private async Task<ConversationMessage?> SaveMessageAsync(string senderId, string receiverId, string content)
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

            var result = await _supabase.From<ConversationMessage>().Insert(msg);
            return result.Models.FirstOrDefault();
        }

        public async Task<string> GetOrCreateConversationAsync(string user1Id, string user2Id)
        {
            // Check if conversation exists
            var res1 = await _supabase.From<Conversation>()
                .Where(c => c.User1Id == user1Id && c.User2Id == user2Id)
                .Get();

            var existing = res1.Models.FirstOrDefault();

            if (existing == null)
            {
                // Check reverse 
                var res2 = await _supabase.From<Conversation>()
                    .Where(c => c.User1Id == user2Id && c.User2Id == user1Id)
                    .Get();
                existing = res2.Models.FirstOrDefault();
            }

            if (existing != null) return existing.Id!;

            // Create new if none exists
            var newConvo = new Conversation
            {
                User1Id = user1Id,
                User2Id = user2Id
            };

            var insertRes = await _supabase.From<Conversation>().Insert(newConvo);
            return insertRes.Models.First().Id!;
        }

        public async Task<List<MessageDTO>> GetHistoryAsync(string userId1, string userId2)
        {

            var conversationId = await GetOrCreateConversationAsync(userId1, userId2);

            var result = await _supabase.From<ConversationMessage>()
                .Where(m => m.ConversationId == conversationId)
                .Order("date_msg", Supabase.Postgrest.Constants.Ordering.Ascending)
                .Get();

            return result.Models.Select(m => new MessageDTO
            {
                Id = m.Id,
                ConversationId = m.ConversationId,
                EnvoyeurId = m.EnvoyeurId,
                ReceveurId = m.ReceveurId,
                Contenu = m.Contenu,
                DateMsg = m.DateMsg
            }).ToList();
        }

        public async Task<List<ConversationDTO>> GetAllConversationsAsync(string currentUserId)
        {
            var response = await _supabase.From<Conversation>()
                .Where(c => c.User1Id == currentUserId || c.User2Id == currentUserId)
                .Get();

            var conversations = response.Models;
            var sidebarList = new List<ConversationDTO>();

            if (!conversations.Any()) return sidebarList;

            var otherUserIds = conversations
                .Select(c => c.User1Id == currentUserId ? c.User2Id : c.User1Id)
                .Where(id => id != null)
                .Distinct()
                .ToList();

            var usersResponse = await _supabase.From<Utilisateur>()
                .Filter("id_utilisateur", Supabase.Postgrest.Constants.Operator.In, otherUserIds!)
                .Get();

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
        public string? ReceiverId { get; set; }
        public string? Content { get; set; }
    }

    public class MessageDTO
    {
        public string? Id { get; set; }
        public string? ConversationId { get; set; }
        public string? EnvoyeurId { get; set; }
        public string? ReceveurId { get; set; }
        public string? Contenu { get; set; }
        public DateTime? DateMsg { get; set; }
    }

    public class ConversationDTO
    {
        public string? ConversationId { get; set; }
        public object? OtherUser { get; set; }
    }
}
