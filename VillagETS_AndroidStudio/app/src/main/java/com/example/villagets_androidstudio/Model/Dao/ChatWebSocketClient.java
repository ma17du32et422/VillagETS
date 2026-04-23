package com.example.villagets_androidstudio.Model.Dao;

import android.util.Log;

import com.example.villagets_androidstudio.Model.Entity.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatWebSocketClient {
    private static final String WS_URL = "wss://apivillagets.lesageserveur.com/ws/chat";
    private final OkHttpClient client;
    private WebSocket webSocket;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ChatMessageListener listener;

    public interface ChatMessageListener {
        void onMessageReceived(ChatMessage message);
        void onError(String error);
    }

    public ChatWebSocketClient() {
        this.client = new OkHttpClient();
    }

    public void connect(SessionManager sessionManager, ChatMessageListener listener) {
        this.listener = listener;
        String token = sessionManager.getToken();

        Request request = new Request.Builder()
                .url(WS_URL)
                .addHeader("Cookie", token != null ? token : "")
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    ChatMessage message = objectMapper.readValue(text, ChatMessage.class);
                    if (listener != null) listener.onMessageReceived(message);
                } catch (Exception e) {
                    Log.e("WebSocket", "Error parsing message", e);
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                if (listener != null) listener.onError(t.getMessage());
            }
        });
    }

    public void sendMessage(String receiverId, String contenu) {
        if (webSocket != null) {
            try {
                String json = String.format("{\"receiverId\":\"%s\", \"contenu\":\"%s\"}", receiverId, contenu);
                webSocket.send(json);
            } catch (Exception e) {
                Log.e("WebSocket", "Error sending message", e);
            }
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "User disconnected");
        }
    }
}
