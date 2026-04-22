package com.example.villagets_androidstudio.View;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.villagets_androidstudio.Model.Comment;
import com.example.villagets_androidstudio.Model.Dao.PostDao;
import com.example.villagets_androidstudio.Model.User;
import com.example.villagets_androidstudio.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ItemDetailsActivity extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String postId;
    private EditText etComment;
    private TextView btnPostComment;
    private TextView tvCommentsTitle;
    private RecyclerView rvComments;
    private View commentsSection;
    private ScrollView detailsScrollView;
    private ShapeableImageView ivMyAvatar;

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

        postId = getIntent().getStringExtra("postId");
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String price = getIntent().getStringExtra("price");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String posterName = getIntent().getStringExtra("posterName");
        String posterAvatarUrl = getIntent().getStringExtra("posterAvatarUrl");
        String posterId = getIntent().getStringExtra("posterId");
        boolean openComments = getIntent().getBooleanExtra("openComments", false);

        TextView tvTitle = findViewById(R.id.tvItemTitle);
        TextView tvDescriptionContent = findViewById(R.id.tvItemDescriptionContent);
        TextView tvPrice = findViewById(R.id.tvItemPrice);
        ImageView ivPhoto = findViewById(R.id.ivItemPhoto);
        TextView tvPosterName = findViewById(R.id.tvPosterName);
        ImageView ivPosterAvatar = findViewById(R.id.ivPosterAvatar);
        AppCompatButton btnContactSeller = findViewById(R.id.btnContactSeller);
        ImageButton btnDeletePost = findViewById(R.id.btnDeletePost);
        detailsScrollView = findViewById(R.id.detailsScrollView);
        commentsSection = findViewById(R.id.commentsSection);
        tvCommentsTitle = findViewById(R.id.tvCommentsTitle);
        ivMyAvatar = findViewById(R.id.ivMyAvatar);
        etComment = findViewById(R.id.etComment);
        btnPostComment = findViewById(R.id.btnPostComment);
        rvComments = findViewById(R.id.rvComments);
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

        if (currentUser != null && currentUser.getPhotoProfil() != null && !currentUser.getPhotoProfil().trim().isEmpty()) {
            String myAvatar = currentUser.getPhotoProfil().replace("localhost", "10.0.2.2");
            Glide.with(this).load(myAvatar).placeholder(R.drawable.profile_placeholder).into(ivMyAvatar);
        }

        rvComments.setLayoutManager(new LinearLayoutManager(this));
        btnPostComment.setOnClickListener(v -> {
            String content = etComment.getText().toString().trim();
            if (!content.isEmpty()) {
                postComment(content, null);
            }
        });

        loadComments();

        if (openComments) {
            commentsSection.post(() -> detailsScrollView.smoothScrollTo(0, commentsSection.getTop()));
        }
    }

    private void loadComments() {
        if (postId == null || postId.trim().isEmpty()) {
            tvCommentsTitle.setText("Comments");
            return;
        }

        executorService.execute(() -> {
            try {
                List<Comment> comments = PostDao.getPostComments(postId);
                runOnUiThread(() -> {
                    setupCommentsRecyclerView(comments);
                    tvCommentsTitle.setText("Comments (" + estimateVisibleCommentCount(comments) + ")");
                });
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Unable to load comments", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setupCommentsRecyclerView(List<Comment> comments) {
        CommentAdapter adapter = new CommentAdapter(parentComment -> {
            etComment.requestFocus();
            etComment.setHint("Replying to " + (parentComment.getOp() != null ? parentComment.getOp().getPseudo() : "User") + "...");
            btnPostComment.setOnClickListener(v -> {
                String content = etComment.getText().toString().trim();
                if (!content.isEmpty()) {
                    postComment(content, parentComment.getId());
                }
            });
        });
        rvComments.setAdapter(adapter);
        adapter.setComments(comments);
    }

    private void postComment(String content, String parentId) {
        if (postId == null || postId.trim().isEmpty()) {
            Toast.makeText(this, "Unable to post comment", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            try {
                Comment newComment = PostDao.createComment(postId, content, parentId);
                if (newComment != null) {
                    runOnUiThread(() -> {
                        etComment.setText("");
                        etComment.setHint("Write a comment...");
                        btnPostComment.setOnClickListener(v -> {
                            String newContent = etComment.getText().toString().trim();
                            if (!newContent.isEmpty()) {
                                postComment(newContent, null);
                            }
                        });
                        loadComments();
                    });
                }
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Unable to post comment", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private int estimateVisibleCommentCount(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return 0;
        }

        int count = comments.size();
        for (Comment comment : comments) {
            if (comment == null) {
                continue;
            }

            if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
                count += estimateVisibleCommentCount(comment.getReplies());
            } else {
                count += Math.max(comment.getReplyCount(), 0);
            }
        }
        return count;
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
