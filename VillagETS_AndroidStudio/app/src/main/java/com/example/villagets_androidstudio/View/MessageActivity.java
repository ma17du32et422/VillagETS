package com.example.villagets_androidstudio.View;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.villagets_androidstudio.Model.ChatMessage;
import com.example.villagets_androidstudio.Model.Message;
import com.example.villagets_androidstudio.Model.SessionManager;
import com.example.villagets_androidstudio.Model.User;
import com.example.villagets_androidstudio.R;
import com.example.villagets_androidstudio.View_Model.ChatViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private List<Message> messageList;
    private EditText etMessageInput;
    private ImageButton btnSend;

    private ChatViewModel chatViewModel;
    private SessionManager sessionManager;
    private String receiverId;
    private String userName;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_message);

        userName = getIntent().getStringExtra("userName");
        receiverId = getIntent().getStringExtra("receiverId");

        TextView tvUserName = findViewById(R.id.chatUserName);
        tvUserName.setText(userName != null ? userName : "Chat");

        findViewById(R.id.btnBackChat).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewMessages);
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSend = findViewById(R.id.btnSendMessage);
        View toolbarContainer = findViewById(R.id.toolbarContainer);
        View inputContainer = findViewById(R.id.inputContainer);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chatMainLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            toolbarContainer.setPadding(0, systemBars.top, 0, 0);
            inputContainer.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        sessionManager = new SessionManager(this);
        currentUser = User.loadUser(this);

        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        // Observer pour l'historique et les nouveaux messages
        chatViewModel.getChatHistoryLiveData().observe(this, chatMessages -> {
            messageList.clear();
            for (ChatMessage cm : chatMessages) {
                String sender = (currentUser != null && cm.getEnvoyeurId() != null && cm.getEnvoyeurId().equals(currentUser.getUserId())) ? "Moi" : userName;
                // On pourrait formater la date cm.getDateMsg() si elle est disponible et valide
                String time = cm.getDateMsg() != null ? cm.getDateMsg() : ""; 
                messageList.add(new Message(sender, cm.getContenu(), time, "avatar"));
            }
            adapter.notifyDataSetChanged();
            if (!messageList.isEmpty()) {
                recyclerView.scrollToPosition(messageList.size() - 1);
            }
        });

        chatViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Connexion au WebSocket et chargement de l'historique
        chatViewModel.connectToChat(sessionManager);
        if (receiverId != null) {
            chatViewModel.loadChatHistory(receiverId);
        }

        btnSend.setOnClickListener(v -> {
            String text = etMessageInput.getText().toString().trim();
            if (!text.isEmpty() && receiverId != null) {
                chatViewModel.sendMessage(receiverId, text);
                
                // Optionnel: Ajouter localement si le serveur ne renvoie pas l'écho
                // Dans le ChatViewModel actuel, on attend le message via WebSocket.
                
                etMessageInput.setText("");
            } else if (receiverId == null) {
                Toast.makeText(this, "Destinataire inconnu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
