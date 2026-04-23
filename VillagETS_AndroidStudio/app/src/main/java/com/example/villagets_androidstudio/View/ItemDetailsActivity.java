package com.example.villagets_androidstudio.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.villagets_androidstudio.Model.Comment;
import com.example.villagets_androidstudio.Model.Post;
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
    private String[] mediaUrls;
    private int currentMediaIndex = 0;
    private ImageView ivPhoto;
    private TextView btnPreviousImage;
    private TextView btnNextImage;
    private TextView tvImageCounter;
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
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

            toolbarContainer.setPadding(0, systemBars.top, 0, 0);

            // Appliquer le padding en bas pour le clavier
            int bottomPadding = Math.max(systemBars.bottom, imeInsets.bottom);
            v.setPadding(0, 0, 0, bottomPadding);

            return insets;
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        postId = getIntent().getStringExtra("postId");
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String price = getIntent().getStringExtra("price");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        mediaUrls = getIntent().getStringArrayExtra("media");
        String posterName = getIntent().getStringExtra("posterName");
        String posterAvatarUrl = getIntent().getStringExtra("posterAvatarUrl");
        String posterId = getIntent().getStringExtra("posterId");
        boolean openComments = getIntent().getBooleanExtra("openComments", false);

        TextView tvTitle = findViewById(R.id.tvItemTitle);
        TextView tvDescriptionContent = findViewById(R.id.tvItemDescriptionContent);
        TextView tvPrice = findViewById(R.id.tvItemPrice);
        ivPhoto = findViewById(R.id.ivItemPhoto);
        TextView tvPosterName = findViewById(R.id.tvPosterName);
        ImageView ivPosterAvatar = findViewById(R.id.ivPosterAvatar);
        btnPreviousImage = findViewById(R.id.btnPreviousImage);
        btnNextImage = findViewById(R.id.btnNextImage);
        tvImageCounter = findViewById(R.id.tvImageCounter);
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

        if ((mediaUrls == null || mediaUrls.length == 0) && imageUrl != null && !imageUrl.isEmpty()) {
            mediaUrls = new String[]{imageUrl};
        }
        setupMediaCarousel();
        loadPostDetails();

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

    private void loadPostDetails() {
        if (postId == null || postId.trim().isEmpty()) {
            return;
        }

        executorService.execute(() -> {
            try {
                Post post = PostDao.getPostById(postId);
                if (post != null && post.getMedia() != null && post.getMedia().length > 0) {
                    runOnUiThread(() -> {
                        mediaUrls = post.getMedia();
                        currentMediaIndex = 0;
                        setupMediaCarousel();
                    });
                }
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Unable to load all post photos", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setupMediaCarousel() {
        if (mediaUrls == null || mediaUrls.length == 0) {
            ivPhoto.setVisibility(View.GONE);
            btnPreviousImage.setVisibility(View.GONE);
            btnNextImage.setVisibility(View.GONE);
            tvImageCounter.setVisibility(View.GONE);
            return;
        }

        ivPhoto.setVisibility(View.VISIBLE);
        showMedia(currentMediaIndex);

        btnPreviousImage.setOnClickListener(v -> {
            if (mediaUrls.length == 0) {
                return;
            }
            currentMediaIndex = (currentMediaIndex - 1 + mediaUrls.length) % mediaUrls.length;
            showMedia(currentMediaIndex);
        });

        btnNextImage.setOnClickListener(v -> {
            if (mediaUrls.length == 0) {
                return;
            }
            currentMediaIndex = (currentMediaIndex + 1) % mediaUrls.length;
            showMedia(currentMediaIndex);
        });
    }

    private void showMedia(int index) {
        if (mediaUrls == null || mediaUrls.length == 0 || index < 0 || index >= mediaUrls.length) {
            return;
        }

        String finalUrl = mediaUrls[index];
        if (finalUrl == null || finalUrl.trim().isEmpty()) {
            ivPhoto.setImageDrawable(null);
            return;
        }

        finalUrl = finalUrl.replace("localhost", "10.0.2.2");
        Glide.with(this)
                .load(finalUrl)
                .into(ivPhoto);

        boolean hasMultiplePhotos = mediaUrls.length > 1;
        btnPreviousImage.setVisibility(hasMultiplePhotos ? View.VISIBLE : View.GONE);
        btnNextImage.setVisibility(hasMultiplePhotos ? View.VISIBLE : View.GONE);
        tvImageCounter.setVisibility(hasMultiplePhotos ? View.VISIBLE : View.GONE);
        tvImageCounter.setText((index + 1) + " / " + mediaUrls.length);
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
            // Scroll to comment input when replying
            commentsSection.post(() -> detailsScrollView.smoothScrollTo(0, commentsSection.getTop()));
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

}
