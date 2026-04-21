package com.example.villagets_androidstudio.Model.Dao;

import com.example.villagets_androidstudio.Model.ChatMessage;
import com.example.villagets_androidstudio.Model.Conversation;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ChatApi {
    @GET("/chat/history/{targetUserId}")
    Call<List<ChatMessage>> getChatHistory(@Path("targetUserId") String targetUserId);

    @GET("/chat/conversations")
    Call<List<Conversation>> getConversations();
}
