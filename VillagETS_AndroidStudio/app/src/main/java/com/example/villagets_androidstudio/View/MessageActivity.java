package com.example.villagets_androidstudio.View;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.villagets_androidstudio.Model.Message;
import com.example.villagets_androidstudio.R;
import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private List<Message> messageList;
    private EditText etMessageInput;
    private ImageButton btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_message);

        String userName = getIntent().getStringExtra("userName");
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

        // placeholders
        messageList.add(new Message(userName, "Salut ! Est-ce que c'est toujours dispo ?", "10:00", "avatar"));
        messageList.add(new Message("Moi", "Oui, toujours dispo !", "10:05", "avatar"));
        messageList.add(new Message(userName, "Super, je peux passer quand ?", "10:06", "avatar"));

        adapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnSend.setOnClickListener(v -> {
            String text = etMessageInput.getText().toString().trim();
            if (!text.isEmpty()) {
                messageList.add(new Message("Moi", text, "Maintenant", "avatar"));
                adapter.notifyItemInserted(messageList.size() - 1);
                recyclerView.scrollToPosition(messageList.size() - 1);
                etMessageInput.setText("");
            }
        });
    }
}
