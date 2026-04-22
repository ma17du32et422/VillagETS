package com.example.villagets_androidstudio.View;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.villagets_androidstudio.Model.Post;
import com.example.villagets_androidstudio.Model.User;
import com.example.villagets_androidstudio.R;
import com.example.villagets_androidstudio.View_Model.PostViewModel;
import com.example.villagets_androidstudio.View_Model.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class CreatePostActivity extends AppCompatActivity {

    private EditText etTitle, etContent, etPrice;
    private CheckBox switchMarketplace;
    private Button btnPost, btnCancel;
    private LinearLayout uploadPlaceholder;
    private FrameLayout btnUploadPhotos;
    private ImageView ivPostImagePreview;
    private TextView tvUserName;
    private PostViewModel postViewModel;
    private UserViewModel userViewModel;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivPostImagePreview.setImageURI(uri);
                    ivPostImagePreview.setVisibility(View.VISIBLE);
                    uploadPlaceholder.setVisibility(View.GONE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        etTitle = findViewById(R.id.etPostTitle);
        etContent = findViewById(R.id.etPostContent);
        etPrice = findViewById(R.id.etPostPrice);
        switchMarketplace = findViewById(R.id.switchMarketplace);
        btnPost = findViewById(R.id.btnPost);
        btnCancel = findViewById(R.id.btnCancel);
        btnUploadPhotos = findViewById(R.id.btnUploadPhotos);
        uploadPlaceholder = findViewById(R.id.uploadPlaceholder);
        ivPostImagePreview = findViewById(R.id.ivPostImagePreview);
        tvUserName = findViewById(R.id.userName);

        User currentUser = User.loadUser(this);
        if (currentUser != null && currentUser.getEmail() != null) {
            userViewModel.fetchUser(currentUser.getEmail());
        }

        userViewModel.getUserLiveData().observe(this, user -> {
            if (user != null && user.getPseudo() != null) {
                tvUserName.setText(user.getPseudo());
            }
        });

        switchMarketplace.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etPrice.setEnabled(isChecked);
            if (isChecked) {
                etPrice.setVisibility(View.VISIBLE);
            } else {
                etPrice.setVisibility(View.GONE);
                etPrice.setText("");
            }
        });

        btnCancel.setOnClickListener(v -> finish());

        btnUploadPhotos.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

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
            } else {
                newPost.setPrix(0.0);
            }

            postViewModel.creerPost(this, newPost, selectedImageUri);
        });

        postViewModel.getSaveSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Post published successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        postViewModel.getMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
