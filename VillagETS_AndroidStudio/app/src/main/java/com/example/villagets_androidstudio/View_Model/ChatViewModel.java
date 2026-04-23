package com.example.villagets_androidstudio.View_Model;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.villagets_androidstudio.Model.ChatMessage;
import com.example.villagets_androidstudio.Model.Conversation;
import com.example.villagets_androidstudio.Model.Dao.ChatApi;
import com.example.villagets_androidstudio.Model.Dao.ChatWebSocketClient;
import com.example.villagets_androidstudio.Model.Dao.PostDao;
import com.example.villagets_androidstudio.Model.Dao.RetrofitClient;
import com.example.villagets_androidstudio.Model.SessionManager;
import com.example.villagets_androidstudio.Model.User;
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
        executorService.execute(() -> {
            try {
                Response<List<ChatMessage>> response = chatApi.getChatHistory(targetUserId).execute();
                if (response.isSuccessful() && response.body() != null) {
                    chatHistoryLiveData.postValue(response.body());
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
            public void onError(String error) {
                errorMessage.postValue("Erreur WebSocket : " + error);
            }
        });
    }

    public void sendMessage(String receiverId, String contenu) {
        webSocketClient.sendMessage(receiverId, contenu);
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

    @Override
    protected void onCleared() {
        super.onCleared();
        webSocketClient.disconnect();
        executorService.shutdown();
    }
}
