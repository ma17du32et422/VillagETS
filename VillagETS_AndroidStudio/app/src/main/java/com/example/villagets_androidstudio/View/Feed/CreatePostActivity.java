package com.example.villagets_androidstudio.View.Feed;

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

import com.bumptech.glide.Glide;
import com.example.villagets_androidstudio.Model.Entity.Post;
import com.example.villagets_androidstudio.Model.Entity.User;
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
    private ImageView ivPostImagePreview, ivUserAvatar;
    private TextView tvUserName, tvUploadLabel, tvSelectedPhotoCount;
    private PostViewModel postViewModel;
    private UserViewModel userViewModel;
    private final List<Uri> selectedImageUris = new ArrayList<>();
    private boolean isPublishing;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(10), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    selectedImageUris.clear();
                    selectedImageUris.addAll(uris);
                    updateSelectedMediaPreview();
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
        ivUserAvatar = findViewById(R.id.userAvatar);
        tvUserName = findViewById(R.id.userName);
        tvUploadLabel = findViewById(R.id.tvUploadLabel);
        tvSelectedPhotoCount = findViewById(R.id.tvSelectedPhotoCount);

        User currentUser = User.loadUser(this);
        
        // Affichage immédiat des données locales si disponibles
        if (currentUser != null) {
            if (currentUser.getPseudo() != null) {
                tvUserName.setText(currentUser.getPseudo());
            }
            if (currentUser.getPhotoProfil() != null) {
                String photoUrl = currentUser.getPhotoProfil().replace("localhost", "10.0.2.2");
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.profile_placeholder)
                        .circleCrop()
                        .into(ivUserAvatar);
            }
        }

        // Rafraîchissement depuis le serveur
        if (currentUser != null && currentUser.getEmail() != null) {
            userViewModel.fetchUser(currentUser.getEmail());
        }

        userViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                if (user.getPseudo() != null) {
                    tvUserName.setText(user.getPseudo());
                }
                if (user.getPhotoProfil() != null) {
                    String photoUrl = user.getPhotoProfil().replace("localhost", "10.0.2.2");
                    Glide.with(this)
                            .load(photoUrl)
                            .placeholder(R.drawable.profile_placeholder)
                            .circleCrop()
                            .into(ivUserAvatar);
                }
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
            if (isPublishing) {
                return;
            }

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

            setPublishingState(true);
            postViewModel.creerPost(this, newPost, new ArrayList<>(selectedImageUris));
        });

        postViewModel.getSaveSuccess().observe(this, success -> {
            if (success == null) {
                return;
            }

            if (success) {
                Toast.makeText(this, "Post published successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                setPublishingState(false);
            }
        });

        postViewModel.getMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                setPublishingState(false);
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setPublishingState(boolean publishing) {
        isPublishing = publishing;
        btnPost.setEnabled(!publishing);
        btnPost.setClickable(!publishing);
        btnPost.setAlpha(publishing ? 0.55f : 1f);
    }

    private void updateSelectedMediaPreview() {
        if (selectedImageUris.isEmpty()) {
            ivPostImagePreview.setVisibility(View.GONE);
            uploadPlaceholder.setVisibility(View.VISIBLE);
            tvSelectedPhotoCount.setVisibility(View.GONE);
            tvUploadLabel.setText("Click or drag to add photos");
            return;
        }

        Uri previewUri = selectedImageUris.get(0);
        ivPostImagePreview.setImageURI(previewUri);
        ivPostImagePreview.setVisibility(View.VISIBLE);
        uploadPlaceholder.setVisibility(View.GONE);

        int count = selectedImageUris.size();
        tvSelectedPhotoCount.setText(count == 1 ? "1 photo" : count + " photos");
        tvSelectedPhotoCount.setVisibility(View.VISIBLE);
        tvUploadLabel.setText(count == 1 ? "1 photo selected" : count + " photos selected");
    }
}
