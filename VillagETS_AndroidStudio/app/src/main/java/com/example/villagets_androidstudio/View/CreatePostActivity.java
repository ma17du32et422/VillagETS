package com.example.villagets_androidstudio.View;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.villagets_androidstudio.Model.Post;
import com.example.villagets_androidstudio.Model.User;
import com.example.villagets_androidstudio.R;
import com.example.villagets_androidstudio.View_Model.PostViewModel;

public class CreatePostActivity extends AppCompatActivity {

    private EditText etTitle, etTags, etContent, etPrice;
    private SwitchCompat switchMarketplace;
    private Button btnPost, btnCancel;
    private LinearLayout btnUploadPhotos;
    private TextView tvUserName;
    private PostViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        viewModel = new ViewModelProvider(this).get(PostViewModel.class);

        etTitle = findViewById(R.id.etPostTitle);
        etTags = findViewById(R.id.etPostTags);
        etContent = findViewById(R.id.etPostContent);
        etPrice = findViewById(R.id.etPostPrice);
        switchMarketplace = findViewById(R.id.switchMarketplace);
        btnPost = findViewById(R.id.btnPost);
        btnCancel = findViewById(R.id.btnCancel);
        btnUploadPhotos = findViewById(R.id.btnUploadPhotos);
        tvUserName = findViewById(R.id.userName);

        User currentUser = User.loadUser(this);
        if (currentUser != null && currentUser.getPseudo() != null) {
            tvUserName.setText(currentUser.getPseudo());
        }

        switchMarketplace.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etPrice.setEnabled(isChecked);
            if (!isChecked) {
                etPrice.setText("");
            }
        });

        btnCancel.setOnClickListener(v -> finish());

        btnUploadPhotos.setOnClickListener(v ->
            Toast.makeText(this, "Photo upload coming soon!", Toast.LENGTH_SHORT).show()
        );

        btnPost.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            boolean isMarketplace = switchMarketplace.isChecked();
            String priceStr = etPrice.getText().toString().trim();
            
            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Please fill in title and content", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isMarketplace && priceStr.isEmpty()) {
                Toast.makeText(this, "Please enter a price for the marketplace", Toast.LENGTH_SHORT).show();
                return;
            }

            Post newPost = new Post();
            newPost.setTitre(title);
            newPost.setContenu(content);
            newPost.setArticleAVendre(isMarketplace);
            
            if (isMarketplace) {
                try {
                    newPost.setPrix(Double.parseDouble(priceStr));
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            viewModel.creerPost(newPost);
        });

        viewModel.getSaveSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Post published successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.getMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
