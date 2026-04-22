package com.example.villagets_androidstudio.View;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.ui.GPHContentType;
import com.giphy.sdk.ui.GPHSettings;
import com.giphy.sdk.ui.views.GiphyDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private List<Message> messageList;
    private EditText etMessageInput;
    private ImageButton btnSend, btnAddImage, btnGiphy;

    private ChatViewModel chatViewModel;
    private SessionManager sessionManager;
    private String receiverId;
    private String userName;
    private User currentUser;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null && receiverId != null) {
                    chatViewModel.sendImage(this, receiverId, uri);
                }
            });

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
        btnAddImage = findViewById(R.id.btnAddImage);
        btnGiphy = findViewById(R.id.btnGiphy);
        
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

        chatViewModel.getChatHistoryLiveData().observe(this, chatMessages -> {
            messageList.clear();
            for (ChatMessage cm : chatMessages) {
                boolean isSent = (currentUser != null && cm.getEnvoyeurId() != null && cm.getEnvoyeurId().equals(currentUser.getUserId()));
                String sender = isSent ? "Moi" : userName;
                String time = cm.getDateMsg() != null ? formatTimestamp(cm.getDateMsg()) : "";
                messageList.add(new Message(sender, cm.getContenu(), time, "avatar", isSent));
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

        chatViewModel.connectToChat(sessionManager);
        if (receiverId != null) {
            chatViewModel.loadChatHistory(receiverId);
        }

        btnAddImage.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        btnGiphy.setOnClickListener(v -> {
            GiphyDialogFragment giphyDialog = GiphyDialogFragment.Companion.newInstance(new GPHSettings());
            giphyDialog.setGifSelectionListener(new GiphyDialogFragment.GifSelectionListener() {
                @Override
                public void onGifSelected(@NonNull Media media, @Nullable String searchTerm, @NonNull GPHContentType selectedContentType) {
                    if (media.getImages().getOriginal() != null && receiverId != null) {
                        String gifUrl = media.getImages().getOriginal().getGifUrl();
                        chatViewModel.sendMessage(receiverId, gifUrl);
                    }
                    giphyDialog.dismiss();
                }

                @Override
                public void onDismissed(@NonNull GPHContentType selectedContentType) {
                }

                @Override
                public void didSearchTerm(@NonNull String s) {
                }
            });
            giphyDialog.show(getSupportFragmentManager(), "giphy_dialog");
        });

        btnSend.setOnClickListener(v -> {
            String text = etMessageInput.getText().toString().trim();
            if (!text.isEmpty() && receiverId != null) {
                chatViewModel.sendMessage(receiverId, text);
                etMessageInput.setText("");
            } else if (receiverId == null) {
                Toast.makeText(this, "Destinataire inconnu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatTimestamp(String isoDate) {
        try {
            if (isoDate.contains("T")) {
                String timePart = isoDate.split("T")[1];
                return timePart.substring(0, 5);
            }
        } catch (Exception e) {
            return isoDate;
        }
        return isoDate;
    }
}
