package com.example.villagets_androidstudio.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.villagets_androidstudio.Model.Conversation;
import com.example.villagets_androidstudio.R;
import com.example.villagets_androidstudio.View_Model.ChatViewModel;

import java.util.List;

public class ConversationFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatViewModel chatViewModel;
    private ConversationAdapter adapter;
    private EditText etNewConversationId;
    private Button btnStartNewChat;

    public ConversationFragment() {
        super(R.layout.fragment_conversation);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewConversations);
        etNewConversationId = view.findViewById(R.id.etNewConversationId);
        btnStartNewChat = view.findViewById(R.id.btnStartNewChat);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        chatViewModel.getConversationsLiveData().observe(getViewLifecycleOwner(), conversations -> {
            if (conversations != null) {
                setupAdapter(conversations);
            }
        });

        chatViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        btnStartNewChat.setOnClickListener(v -> {
            String targetId = etNewConversationId.getText().toString().trim();
            if (!targetId.isEmpty()) {
                Intent intent = new Intent(getContext(), MessageActivity.class);
                intent.putExtra("receiverId", targetId);
                intent.putExtra("userName", "Utilisateur " + targetId);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Veuillez entrer un ID", Toast.LENGTH_SHORT).show();
            }
        });

        chatViewModel.loadConversations();
    }

    private void setupAdapter(List<Conversation> conversations) {
        int size = conversations.size();
        String[] userNames = new String[size];
        String[] lastMessages = new String[size];
        String[] times = new String[size];
        Integer[] avatarResIds = new Integer[size];
        String[] receiverIds = new String[size];

        for (int i = 0; i < size; i++) {
            Conversation conv = conversations.get(i);
            if (conv.getOtherUser() != null) {
                userNames[i] = conv.getOtherUser().getPrenom() + " " + conv.getOtherUser().getNom();
                receiverIds[i] = conv.getOtherUser().getId_utilisateur();
            } else {
                userNames[i] = "Inconnu";
                receiverIds[i] = "";
            }
            lastMessages[i] = "";
            times[i] = "";
            avatarResIds[i] = R.drawable.profile_placeholder;
        }

        adapter = new ConversationAdapter(userNames, lastMessages, times, avatarResIds, receiverIds);
        recyclerView.setAdapter(adapter);
    }
}
