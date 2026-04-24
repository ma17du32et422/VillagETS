package com.example.villagets_androidstudio.View_Model;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.villagets_androidstudio.Model.Entity.ChatMessage;
import com.example.villagets_androidstudio.Model.Entity.Conversation;
import com.example.villagets_androidstudio.Model.Dao.ChatApi;
import com.example.villagets_androidstudio.Model.Dao.ChatWebSocketClient;
import com.example.villagets_androidstudio.Model.Entity.MessageDeletedEventDTO;
import com.example.villagets_androidstudio.Model.Dao.PostDao;
import com.example.villagets_androidstudio.Model.Dao.RetrofitClient;
import com.example.villagets_androidstudio.Model.Dao.SessionManager;
import com.example.villagets_androidstudio.Model.Entity.User;
import com.example.villagets_androidstudio.Utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class ChatViewModel extends ViewModel {
    private final MutableLiveData<List<Conversation>> conversationsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<ChatMessage>> chatHistoryLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final ChatApi chatApi;
    private final ChatWebSocketClient webSocketClient;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private volatile String activeChatUserId;
    private volatile String activeConversationId;

    public ChatViewModel() {
        this.chatApi = RetrofitClient.getInstance().create(ChatApi.class);
        this.webSocketClient = new ChatWebSocketClient();
    }

    public LiveData<List<Conversation>> getConversationsLiveData() {
        return conversationsLiveData;
    }

    public LiveData<List<ChatMessage>> getChatHistoryLiveData() {
        return chatHistoryLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // --- API Calls ---

    public void loadConversations(Context context) {
        User currentUser = User.loadUser(context);
        String currentUserId = (currentUser != null) ? currentUser.getUserId() : null;

        executorService.execute(() -> {
            try {
                Response<List<Conversation>> response = chatApi.getConversations().execute();
                if (response.isSuccessful() && response.body() != null) {
                    List<Conversation> allConversations = response.body();
                    List<Conversation> filteredConversations = new ArrayList<>();

                    for (Conversation conv : allConversations) {
                        if (conv.getOtherUser() != null) {
                            String otherUserId = conv.getOtherUser().getId_utilisateur();
                            // On n'ajoute la conversation que si l'autre utilisateur n'est pas soi-même
                            if (currentUserId == null || !currentUserId.equals(otherUserId)) {
                                filteredConversations.add(conv);
                            }
                        }
                    }
                    conversationsLiveData.postValue(filteredConversations);
                } else {
                    errorMessage.postValue("Erreur lors de la récupération des conversations");
                }
            } catch (IOException e) {
                errorMessage.postValue("Erreur réseau : " + e.getMessage());
            }
        });
    }

    public void loadChatHistory(String targetUserId) {
        activeChatUserId = targetUserId;
        activeConversationId = null;
        executorService.execute(() -> {
            try {
                Response<List<ChatMessage>> response = chatApi.getChatHistory(targetUserId).execute();
                if (response.isSuccessful() && response.body() != null) {
                    List<ChatMessage> chatHistory = response.body();
                    activeConversationId = extractConversationId(chatHistory);
                    chatHistoryLiveData.postValue(chatHistory);
                } else {
                    errorMessage.postValue("Erreur lors de la récupération de l'historique");
                }
            } catch (IOException e) {
                errorMessage.postValue("Erreur réseau : " + e.getMessage());
            }
        });
    }

    // --- WebSocket Logic ---

    public void connectToChat(SessionManager sessionManager) {
        webSocketClient.connect(sessionManager, new ChatWebSocketClient.ChatMessageListener() {
            @Override
            public void onMessageReceived(ChatMessage message) {
                if (!belongsToActiveConversation(message)) {
                    return;
                }

                if (activeConversationId == null && message.getConversationId() != null) {
                    activeConversationId = message.getConversationId();
                }

                List<ChatMessage> currentMessages = chatHistoryLiveData.getValue();
                if (currentMessages == null) currentMessages = new ArrayList<>();
                
                // Vérifier si le message existe déjà par son ID pour éviter les doublons
                boolean alreadyExists = false;
                if (message.getId() != null) {
                    for (ChatMessage cm : currentMessages) {
                        if (message.getId().equals(cm.getId())) {
                            alreadyExists = true;
                            break;
                        }
                    }
                }

                if (!alreadyExists) {
                    List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                    updatedMessages.add(message);
                    chatHistoryLiveData.postValue(updatedMessages);
                }
            }

            @Override
            public void onMessageDeleted(MessageDeletedEventDTO deletedEvent) {
                if (deletedEvent == null || deletedEvent.getId() == null || !shouldApplyDeletionEvent(deletedEvent)) {
                    return;
                }
                removeMessageById(deletedEvent.getId());
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("Erreur WebSocket : " + error);
            }
        });
    }

    public void sendMessage(String receiverId, String contenu) {
        webSocketClient.sendMessage(receiverId, contenu);
    }

    public void deleteMessage(String messageId) {
        executorService.execute(() -> {
            try {
                Response<Void> response = chatApi.deleteMessage(messageId).execute();
                if (response.isSuccessful()) {
                    removeMessageById(messageId);
                } else if (response.code() == 403) {
                    errorMessage.postValue("Vous ne pouvez supprimer que vos propres messages");
                } else if (response.code() == 404) {
                    errorMessage.postValue("Message introuvable");
                } else {
                    errorMessage.postValue("Erreur lors de la suppression du message");
                }
            } catch (IOException e) {
                errorMessage.postValue("Erreur réseau : " + e.getMessage());
            }
        });
    }

    public void sendImage(Context context, String receiverId, Uri imageUri) {
        executorService.execute(() -> {
            try {
                File file = FileUtils.getFileFromUri(context, imageUri);
                if (file != null) {
                    String mimeType = context.getContentResolver().getType(imageUri);
                    if (mimeType == null) mimeType = "image/jpeg";

                    String uploadedUrl = PostDao.uploadFile(file, file.getName(), mimeType);
                    if (uploadedUrl != null) {
                        // On envoie l'URL de l'image comme contenu du message via WebSocket
                        webSocketClient.sendMessage(receiverId, uploadedUrl);
                    } else {
                        errorMessage.postValue("Erreur lors de l'upload de l'image");
                    }
                }
            } catch (IOException e) {
                errorMessage.postValue("Erreur lors de l'envoi de l'image : " + e.getMessage());
            }
        });
    }

    private boolean belongsToActiveConversation(ChatMessage message) {
        if (message == null || activeChatUserId == null) {
            return true;
        }

        return activeChatUserId.equals(message.getEnvoyeurId())
                || activeChatUserId.equals(message.getReceveurId());
    }

    private boolean shouldApplyDeletionEvent(MessageDeletedEventDTO deletedEvent) {
        if (deletedEvent == null || deletedEvent.getId() == null) {
            return false;
        }

        if (activeConversationId != null && activeConversationId.equals(deletedEvent.getConversationId())) {
            return true;
        }

        List<ChatMessage> currentMessages = chatHistoryLiveData.getValue();
        if (currentMessages == null || currentMessages.isEmpty()) {
            return false;
        }

        for (ChatMessage currentMessage : currentMessages) {
            if (deletedEvent.getId().equals(currentMessage.getId())) {
                return true;
            }
        }

        return activeChatUserId != null
                && (activeChatUserId.equals(deletedEvent.getEnvoyeurId())
                || activeChatUserId.equals(deletedEvent.getReceveurId()));
    }

    private String extractConversationId(List<ChatMessage> messages) {
        if (messages == null) {
            return null;
        }

        for (ChatMessage message : messages) {
            if (message != null && message.getConversationId() != null && !message.getConversationId().trim().isEmpty()) {
                return message.getConversationId();
            }
        }

        return null;
    }

    private void removeMessageById(String messageId) {
        if (messageId == null) {
            return;
        }

        List<ChatMessage> currentMessages = chatHistoryLiveData.getValue();
        if (currentMessages == null || currentMessages.isEmpty()) {
            return;
        }

        List<ChatMessage> updatedMessages = new ArrayList<>();
        boolean removed = false;

        for (ChatMessage currentMessage : currentMessages) {
            if (!messageId.equals(currentMessage.getId())) {
                updatedMessages.add(currentMessage);
            } else {
                removed = true;
            }
        }

        if (removed) {
            chatHistoryLiveData.postValue(updatedMessages);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        webSocketClient.disconnect();
        executorService.shutdown();
    }
}
