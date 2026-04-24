package com.example.villagets_androidstudio.View.Message;

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
import androidx.recyclerview.widget.RecyclerView;

import com.example.villagets_androidstudio.Model.Entity.ChatMessage;
import com.example.villagets_androidstudio.Model.Entity.Message;
import com.example.villagets_androidstudio.Model.Dao.SessionManager;
import com.example.villagets_androidstudio.Model.Entity.User;
import com.example.villagets_androidstudio.R;
import com.example.villagets_androidstudio.View_Model.ChatViewModel;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.ui.GPHContentType;
import com.giphy.sdk.ui.GPHSettings;
import com.giphy.sdk.ui.views.GiphyDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
    private String photoUrl;
    private User currentUser;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null && receiverId != null) {
                    if (isTargetMe()) {
                        Toast.makeText(this, "You cannot send images to yourself", Toast.LENGTH_SHORT).show();
                        return;
                    }
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
        photoUrl = getIntent().getStringExtra("photoUrl");
        
        currentUser = User.loadUser(this);

        if (isTargetMe()) {
            Toast.makeText(this, "You cannot message yourself", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            
            toolbarContainer.setPadding(0, systemBars.top, 0, 0);
            
            // On utilise l'imeInsets.bottom si le clavier est ouvert, sinon systemBars.bottom
            int bottomPadding = Math.max(systemBars.bottom, imeInsets.bottom);
            inputContainer.setPadding(0, 0, 0, bottomPadding);
            
            return insets;
        });

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList, this::confirmDeleteMessage);
        recyclerView.setAdapter(adapter);

        sessionManager = new SessionManager(this);

        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        chatViewModel.getChatHistoryLiveData().observe(this, chatMessages -> {
            messageList.clear();
            for (ChatMessage cm : chatMessages) {
                boolean isSent = (currentUser != null && cm.getEnvoyeurId() != null && cm.getEnvoyeurId().equals(currentUser.getUserId()));
                String sender = isSent ? "Moi" : userName;
                String time = cm.getDateMsg() != null ? formatTimestamp(cm.getDateMsg()) : "";
                
                // Utilisation de la photoUrl pour les messages reçus
                String currentAvatarUrl = isSent ? (currentUser != null ? currentUser.getPhotoProfil() : null) : photoUrl;
                
                messageList.add(new Message(cm.getId(), sender, cm.getContenu(), time, currentAvatarUrl, isSent));
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
            if (isTargetMe()) {
                Toast.makeText(this, "You cannot send GIFs to yourself", Toast.LENGTH_SHORT).show();
                return;
            }
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
            if (isTargetMe()) {
                Toast.makeText(this, "You cannot message yourself", Toast.LENGTH_SHORT).show();
                return;
            }
            String text = etMessageInput.getText().toString().trim();
            if (!text.isEmpty() && receiverId != null) {
                chatViewModel.sendMessage(receiverId, text);
                etMessageInput.setText("");
            } else if (receiverId == null) {
                Toast.makeText(this, "Destinataire inconnu", Toast.LENGTH_SHORT).show();
            }
        });

        // Scroll to bottom when keyboard appears
        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom && !messageList.isEmpty()) {
                recyclerView.postDelayed(() -> recyclerView.smoothScrollToPosition(messageList.size() - 1), 100);
            }
        });
    }

    private boolean isTargetMe() {
        return currentUser != null && receiverId != null && receiverId.equals(currentUser.getUserId());
    }

    private void confirmDeleteMessage(Message message) {
        if (message == null || message.getId() == null) {
            Toast.makeText(this, "Impossible de supprimer ce message", Toast.LENGTH_SHORT).show();
            return;
        }
        chatViewModel.deleteMessage(message.getId());
    }

    private String formatTimestamp(String isoDate) {
        try {
            // Parser la date ISO en UTC
            SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            sdfInput.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdfInput.parse(isoDate);

            // Formater la date pour l'heure locale
            SimpleDateFormat sdfOutput = new SimpleDateFormat("HH:mm", Locale.getDefault());
            sdfOutput.setTimeZone(TimeZone.getDefault());
            return sdfOutput.format(date);
        } catch (Exception e) {
            // Fallback si le format est différent (sans millisecondes ou sans Z)
            try {
                SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                sdfInput.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = sdfInput.parse(isoDate);

                SimpleDateFormat sdfOutput = new SimpleDateFormat("HH:mm", Locale.getDefault());
                sdfOutput.setTimeZone(TimeZone.getDefault());
                return sdfOutput.format(date);
            } catch (Exception e2) {
                return isoDate;
            }
        }
    }
}
