package com.example.villagets_androidstudio.View;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bumptech.glide.Glide;
import com.example.villagets_androidstudio.Model.Dao.PostDao;
import com.example.villagets_androidstudio.Model.User;
import com.example.villagets_androidstudio.R;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ItemDetailsActivity extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_details);

        View toolbarContainer = findViewById(R.id.toolbarContainer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailsMainLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            toolbarContainer.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String postId = getIntent().getStringExtra("postId");
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String price = getIntent().getStringExtra("price");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String posterName = getIntent().getStringExtra("posterName");
        String posterAvatarUrl = getIntent().getStringExtra("posterAvatarUrl");
        String posterId = getIntent().getStringExtra("posterId");

        TextView tvTitle = findViewById(R.id.tvItemTitle);
        TextView tvDescriptionContent = findViewById(R.id.tvItemDescriptionContent);
        TextView tvPrice = findViewById(R.id.tvItemPrice);
        ImageView ivPhoto = findViewById(R.id.ivItemPhoto);
        TextView tvPosterName = findViewById(R.id.tvPosterName);
        ImageView ivPosterAvatar = findViewById(R.id.ivPosterAvatar);
        AppCompatButton btnContactSeller = findViewById(R.id.btnContactSeller);
        ImageButton btnDeletePost = findViewById(R.id.btnDeletePost);
        User currentUser = User.loadUser(this);

        tvTitle.setText(title);
        tvDescriptionContent.setText(description);
        tvPrice.setText(price);
        tvPosterName.setText(posterName != null ? posterName : "User Name");

        boolean isAuthor = currentUser != null
                && currentUser.getUserId() != null
                && currentUser.getUserId().equals(posterId)
                && postId != null
                && !postId.trim().isEmpty();
        btnDeletePost.setVisibility(isAuthor ? View.VISIBLE : View.GONE);
        btnContactSeller.setVisibility(isAuthor ? View.GONE : View.VISIBLE);

        if (posterAvatarUrl != null && !posterAvatarUrl.isEmpty()) {
            String avatarUrl = posterAvatarUrl.replace("localhost", "10.0.2.2");
            Glide.with(this).load(avatarUrl).into(ivPosterAvatar);
        } else {
            ivPosterAvatar.setImageDrawable(null);
        }

        View.OnClickListener toProfile = v -> {
            if (posterId != null) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("userId", posterId);
                startActivity(intent);
            }
        };

        tvPosterName.setOnClickListener(toProfile);
        ivPosterAvatar.setOnClickListener(toProfile);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            String finalUrl = imageUrl;
            if (finalUrl.contains("localhost")) {
                finalUrl = finalUrl.replace("localhost", "10.0.2.2");
            }
            
            Glide.with(this)
                    .load(finalUrl)
                    .into(ivPhoto);
        } else {
            ivPhoto.setVisibility(View.GONE);
        }

        btnDeletePost.setOnClickListener(v -> confirmDeletePost(postId));

        btnContactSeller.setOnClickListener(v -> {
            if (posterId != null) {
                Intent intent = new Intent(this, MessageActivity.class);
                intent.putExtra("receiverId", posterId);
                intent.putExtra("userName", posterName);
                startActivity(intent);
            } else {
                Log.e("ItemDetailsActivity", "posterId is null, cannot start conversation");
            }
        });
    }

    private void confirmDeletePost(String postId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete post")
                .setMessage("Are you sure you want to delete this post?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deletePost(postId))
                .show();
    }

    private void deletePost(String postId) {
        if (postId == null || postId.trim().isEmpty()) {
            Toast.makeText(this, "Unable to delete this post", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            try {
                boolean deleted = PostDao.deletePost(postId);
                runOnUiThread(() -> {
                    if (deleted) {
                        Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Only the author can delete this post", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}
