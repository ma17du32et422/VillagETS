package com.example.villagets_androidstudio.View;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.villagets_androidstudio.Model.Post;
import com.example.villagets_androidstudio.R;
import com.example.villagets_androidstudio.View_Model.PostViewModel;

public class CreatePostActivity extends AppCompatActivity {

    private EditText etTitle, etTags, etContent;
    private Button btnPost, btnCancel;
    private LinearLayout btnUploadPhotos;
    private PostViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        viewModel = new ViewModelProvider(this).get(PostViewModel.class);

        etTitle = findViewById(R.id.etPostTitle);
        etTags = findViewById(R.id.etPostTags);
        etContent = findViewById(R.id.etPostContent);
        btnPost = findViewById(R.id.btnPost);
        btnCancel = findViewById(R.id.btnCancel);
        btnUploadPhotos = findViewById(R.id.btnUploadPhotos);

        btnCancel.setOnClickListener(v -> finish());

        btnUploadPhotos.setOnClickListener(v ->
            Toast.makeText(this, "Photo upload coming soon!", Toast.LENGTH_SHORT).show()
        );

        btnPost.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            
            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Please fill in title and content", Toast.LENGTH_SHORT).show();
                return;
            }

            Post newPost = new Post();
            newPost.setTitre(title);
            newPost.setContenu(content);
            newPost.setArticleAVendre(false); // Default

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
