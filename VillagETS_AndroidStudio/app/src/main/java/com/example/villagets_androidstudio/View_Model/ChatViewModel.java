package com.example.villagets_androidstudio.View_Model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.villagets_androidstudio.Model.ChatMessage;
import com.example.villagets_androidstudio.Model.Conversation;
import com.example.villagets_androidstudio.Model.Dao.ChatApi;
import com.example.villagets_androidstudio.Model.Dao.ChatWebSocketClient;
import com.example.villagets_androidstudio.Model.Dao.RetrofitClient;
import com.example.villagets_androidstudio.Model.SessionManager;

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

    public void loadConversations() {
        executorService.execute(() -> {
            try {
                Response<List<Conversation>> response = chatApi.getConversations().execute();
                if (response.isSuccessful() && response.body() != null) {
                    conversationsLiveData.postValue(response.body());
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
                // Ajouter le nouveau message à la liste actuelle
                List<ChatMessage> currentMessages = chatHistoryLiveData.getValue();
                if (currentMessages == null) currentMessages = new ArrayList<>();
                
                // On crée une nouvelle liste pour déclencher l'observation
                List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                updatedMessages.add(message);
                chatHistoryLiveData.postValue(updatedMessages);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("Erreur WebSocket : " + error);
            }
        });
    }

    public void sendMessage(String receiverId, String contenu) {
        webSocketClient.sendMessage(receiverId, contenu);
        // Note: Le message envoyé sera probablement reçu en retour via le WebSocket 
        // ou devra être ajouté manuellement ici si le serveur ne le renvoie pas.
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        webSocketClient.disconnect();
        executorService.shutdown();
    }
}
