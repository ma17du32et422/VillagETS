using Microsoft.AspNetCore.Mvc;
using sql;
using srv.Upload;
using Supabase.Postgrest;
using System.Buffers;
using System.Collections.Concurrent;
using System.Net.WebSockets;
using System.Text;
using System.Text.Json;

// okay lowkey im so olost i need to comment this entire class before i forget what the heck is going on in 3 months when i look at it again
namespace srv.Messaging
{
    public class MessagingService
    {
        private readonly Supabase.Client _supabase;
        private readonly ILogger<MessagingService> _logger;
        private readonly UploadService _uploadService;
        private const int ReceiveBufferSize = 4 * 1024;
        private const int MaxMessageBytes = 64 * 1024;

        private readonly ConcurrentDictionary<string, WebSocket> _activeConnections = new();

        public MessagingService(ILogger<MessagingService> logger, UploadService uploadService)
        {
            _supabase = SupabaseService.GetClient();
            _logger = logger;
            _uploadService = uploadService;
        }

        /// <summary>
        /// Registers the user's websocket, listens for incoming chat payloads, saves valid messages, and forwards them to connected recipients.
        /// </summary>
        /// <returns>A task that completes when the websocket closes or the connection loop exits.</returns>
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
                        
                        
                        var normalizedContent = payload.contenu?.Trim();
                        var normalizedMedia = payload.media
                            .Where(url => !string.IsNullOrWhiteSpace(url))
                            .Select(url => url.Trim())
                            .Distinct()
                            .ToArray() ?? [];

                        if (string.IsNullOrWhiteSpace(normalizedContent) && normalizedMedia.Length == 0)
                            continue;

                        MessageDTO? savedMsg;
                        try
                        {
                            savedMsg = await SaveMessageAsync(userId, payload.receiverId, normalizedContent, normalizedMedia);
                        }
                        catch (ArgumentException ex)
                        {
                            _logger.LogWarning(ex, "Ignoring invalid websocket payload from user {UserId}", userId);
                            continue;
                        }

                        if (savedMsg != null)
                        {
                            await SendJsonToUserAsync(payload.receiverId, savedMsg);
                            await SendJsonToUserAsync(userId, savedMsg);
                        }
                    }
                }
            }
            finally
            {
                _activeConnections.TryRemove(userId, out _);
            }
        }

        /// <summary>
        /// Reads a full text websocket message, reassembles fragmented frames, and stops early if the client closes the socket.
        /// </summary>
        /// <returns>A tuple containing the decoded message text and the final websocket receive result.</returns>
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

        /// <summary>
        /// Persists a new message for the sender and receiver after resolving or creating their conversation.
        /// </summary>
        /// <returns>The saved conversation message row, or <c>null</c> if nothing was returned from the insert.</returns>
        private async Task<MessageDTO?> SaveMessageAsync(string senderId, string receiverId, string? content = "", string[]? media = null)
        {
            if (!await IsActiveUserAsync(senderId))
                throw new ArgumentException("Sender account is unavailable.");

            if (!await IsActiveUserAsync(receiverId))
                throw new ArgumentException("Receiver account is unavailable.");

            var mediaUrls = media?
                .Where(url => !string.IsNullOrWhiteSpace(url))
                .Select(url => url.Trim())
                .Distinct()
                .ToList() ?? [];

            var fichiers = await ResolveOwnedFilesByUrlsAsync(senderId, mediaUrls);
            if (fichiers.Count != mediaUrls.Count)
                throw new ArgumentException("One or more message attachments are invalid.");

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
            var saved = result.Models.FirstOrDefault();
            if (saved == null)
                return null;

            if (fichiers.Count > 0)
            {
                var links = fichiers
                    .Where(file => file.Id.HasValue)
                    .Select(file => new sql.MessageFichier
                    {
                        MessageId = saved.Id,
                        FichierId = file.Id!.Value.ToString("D")
                    })
                    .ToList();

                if (links.Count > 0)
                {
                    await PerfLogger.TimeAsync(_logger, "MessagingService.SaveMessageAsync insert message fichiers", () => _supabase
                        .From<sql.MessageFichier>()
                        .Insert(links));
                }
            }

            return new MessageDTO
            {
                id = saved.Id,
                conversationId = saved.ConversationId,
                envoyeurId = saved.EnvoyeurId,
                receveurId = saved.ReceveurId,
                contenu = saved.Contenu,
                media = mediaUrls.ToArray(),
                dateMsg = saved.DateMsg
            };
        }

        public async Task<bool> DeleteMessageAsync(string messageId, string requestUserId)
        {
            var existing = await PerfLogger.TimeAsync(_logger, "MessagingService.DeleteMessageAsync lookup message", () => _supabase
                .From<ConversationMessage>()
                .Where(message => message.Id == messageId)
                .Get());

            var message = existing.Models.FirstOrDefault();
            if (message == null)
                return false;

            if (!string.Equals(message.EnvoyeurId, requestUserId, StringComparison.Ordinal))
                throw new UnauthorizedAccessException();

            var deletedFiles = await PerfLogger.TimeAsync(_logger, "MessagingService.DeleteMessageAsync rpc delete_message_with_files", () => _supabase.Rpc<List<DeletedFileRpcRow>>(
                "delete_message_with_files",
                new Dictionary<string, object?>
                {
                    { "p_message_id", Guid.Parse(messageId) }
                }));

            _uploadService.DeleteLocalFiles((deletedFiles ?? []).Select(file => file.LienFichier));

            var deletionEvent = new MessageDeletedEventDTO
            {
                type = "message_deleted",
                id = messageId,
                conversationId = message.ConversationId,
                envoyeurId = message.EnvoyeurId,
                receveurId = message.ReceveurId
            };

            if (!string.IsNullOrWhiteSpace(message.EnvoyeurId))
                await SendJsonToUserAsync(message.EnvoyeurId, deletionEvent);

            if (!string.IsNullOrWhiteSpace(message.ReceveurId) && !string.Equals(message.ReceveurId, message.EnvoyeurId, StringComparison.Ordinal))
                await SendJsonToUserAsync(message.ReceveurId, deletionEvent);

            return true;
        }

        /// <summary>
        /// Looks up an existing conversation between two users in either direction.
        /// </summary>
        /// <returns>The conversation id when one exists, or <c>null</c> when no conversation has been created yet.</returns>
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

        /// <summary>
        /// Returns the existing conversation id for two users or creates a new conversation when none exists.
        /// </summary>
        /// <returns>The existing or newly created conversation id.</returns>
        public async Task<string> GetOrCreateConversationAsync(string user1Id, string user2Id)
        {
            var existingId = await FindConversationIdAsync(user1Id, user2Id);
            if (!string.IsNullOrWhiteSpace(existingId))
                return existingId;

            var newConvo = new Conversation
            {
                User1Id = user1Id,
                User2Id = user2Id
            };

            var insertRes = await PerfLogger.TimeAsync(_logger, "MessagingService.GetOrCreateConversationAsync insert conversation", () => _supabase.From<Conversation>().Insert(newConvo));
            return insertRes.Models.First().Id!;
        }

        /// <summary>
        /// Loads the full message history for the conversation between two users in ascending send order.
        /// </summary>
        /// <returns>A list of message DTOs, or an empty list when the users do not have a conversation yet.</returns>
        public async Task<List<MessageDTO>> GetHistoryAsync(string userId1, string userId2)
        {
            if (!Guid.TryParse(userId1, out var currentUserId) || !Guid.TryParse(userId2, out var targetUserId))
                return [];

            var rows = await PerfLogger.TimeAsync(_logger, "MessagingService.GetHistoryAsync rpc get_chat_history", () => _supabase.Rpc<List<MessageHistoryRpcRow>>(
                "get_chat_history",
                new Dictionary<string, object?>
                {
                    { "p_user_id_1", currentUserId },
                    { "p_user_id_2", targetUserId }
                }));

            return (rows ?? []).Select(m => new MessageDTO
            {
                id = m.Id,
                conversationId = m.ConversationId,
                envoyeurId = m.EnvoyeurId,
                receveurId = m.ReceveurId,
                contenu = m.Contenu,
                media = m.Media ?? [],
                dateMsg = m.DateMsg
            }).ToList();
        }

        /// <summary>
        /// Builds the sidebar conversation list for the current user and attaches the other participant's public profile data.
        /// </summary>
        /// <returns>A list of sidebar conversation DTOs for every conversation involving the current user.</returns>
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

        private async Task SendJsonToUserAsync(string userId, object payload)
        {
            if (!_activeConnections.TryGetValue(userId, out var socket) || socket.State != WebSocketState.Open)
                return;

            var outMsg = JsonSerializer.Serialize(payload);
            var outBytes = Encoding.UTF8.GetBytes(outMsg);
            await socket.SendAsync(new ArraySegment<byte>(outBytes), WebSocketMessageType.Text, true, CancellationToken.None);
        }

        private async Task<List<sql.Fichier>> ResolveOwnedFilesByUrlsAsync(string ownerId, IReadOnlyCollection<string> urls)
        {
            if (urls.Count == 0)
                return [];

            var response = await PerfLogger.TimeAsync(_logger, "MessagingService.ResolveOwnedFilesByUrlsAsync fichier lookup", () => _supabase
                .From<sql.Fichier>()
                .Filter("lien_fichier", Supabase.Postgrest.Constants.Operator.In, urls.ToList())
                .Filter("id_proprietaire", Supabase.Postgrest.Constants.Operator.Equals, ownerId)
                .Get());

            return response.Models.ToList();
        }

        private async Task<bool> IsActiveUserAsync(string userId)
        {
            var response = await PerfLogger.TimeAsync(_logger, "MessagingService.IsActiveUserAsync user lookup", () => _supabase
                .From<Utilisateur>()
                .Where(u => u.Id == userId)
                .Get());

            return response.Model?.DeletedAt.HasValue == false;
        }

    }

    public class WsMessagePayload
    {
        public string? receiverId { get; set; }
        public string? contenu { get; set; }
        public string[] media { get; set; } = [];
    }

    public class MessageDTO
    {
        public string? id { get; set; }
        public string? conversationId { get; set; }
        public string? envoyeurId { get; set; }
        public string? receveurId { get; set; }
        public string? contenu { get; set; }
        public string[] media { get; set; } = [];
        public DateTime? dateMsg { get; set; }
    }

    public class ConversationDTO
    {
        public string? ConversationId { get; set; }
        public object? OtherUser { get; set; }
    }

    public class MessageHistoryRpcRow
    {
        [Newtonsoft.Json.JsonProperty("id")]
        public string? Id { get; set; }

        [Newtonsoft.Json.JsonProperty("conversation_id")]
        public string? ConversationId { get; set; }

        [Newtonsoft.Json.JsonProperty("envoyeur_id")]
        public string? EnvoyeurId { get; set; }

        [Newtonsoft.Json.JsonProperty("receveur_id")]
        public string? ReceveurId { get; set; }

        [Newtonsoft.Json.JsonProperty("contenu")]
        public string? Contenu { get; set; }

        [Newtonsoft.Json.JsonProperty("media")]
        public string[]? Media { get; set; }

        [Newtonsoft.Json.JsonProperty("date_msg")]
        public DateTime? DateMsg { get; set; }
    }

    public class MessageDeletedEventDTO
    {
        public string type { get; set; } = "message_deleted";
        public string? id { get; set; }
        public string? conversationId { get; set; }
        public string? envoyeurId { get; set; }
        public string? receveurId { get; set; }
    }

    public class DeletedFileRpcRow
    {
        [Newtonsoft.Json.JsonProperty("id_fichier")]
        public Guid? IdFichier { get; set; }

        [Newtonsoft.Json.JsonProperty("lien_fichier")]
        public string? LienFichier { get; set; }
    }
}
