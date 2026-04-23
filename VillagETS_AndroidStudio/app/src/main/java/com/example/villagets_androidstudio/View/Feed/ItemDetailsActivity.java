package com.example.villagets_androidstudio.View.Feed;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.villagets_androidstudio.Model.Entity.Comment;
import com.example.villagets_androidstudio.Model.Entity.Post;
import com.example.villagets_androidstudio.Model.Dao.PostDao;
import com.example.villagets_androidstudio.Model.Entity.User;
import com.example.villagets_androidstudio.R;
import com.example.villagets_androidstudio.Utils.FileUtils;
import com.example.villagets_androidstudio.View.Message.MessageActivity;
import com.example.villagets_androidstudio.View.Profile.ProfileActivity;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.ui.GPHContentType;
import com.giphy.sdk.ui.GPHSettings;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
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
    private ImageButton btnPostComment;
    private TextView tvCommentsTitle;
    private RecyclerView rvComments;
    private View commentsSection;
    private View inputContainer;
    private ScrollView detailsScrollView;
    private ShapeableImageView ivMyAvatar;
    private View btnContactSeller;
    private ImageButton btnCommentAddImage;
    private ImageButton btnCommentGiphy;
    private String replyParentCommentId;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickCommentMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    uploadAndPostCommentImage(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_details);

        View toolbarContainer = findViewById(R.id.toolbarContainer);
        inputContainer = findViewById(R.id.inputContainer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailsMainLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

            toolbarContainer.setPadding(0, systemBars.top, 0, 0);

            int bottomPadding = Math.max(systemBars.bottom, imeInsets.bottom);
            inputContainer.setPadding(8, 8, 8, bottomPadding + 8);

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
        boolean isMarketplace = getIntent().getBooleanExtra("isMarketplace", false);

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
        etComment = findViewById(R.id.etComment);
        btnPostComment = findViewById(R.id.btnPostComment);
        btnCommentAddImage = findViewById(R.id.btnCommentAddImage);
        btnCommentGiphy = findViewById(R.id.btnCommentGiphy);
        rvComments = findViewById(R.id.rvComments);
        btnContactSeller = findViewById(R.id.btnContactSeller);
        ivMyAvatar = findViewById(R.id.ivMyAvatar);
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

        // Marketplace logic
        if (isMarketplace && posterId != null && currentUser != null && !posterId.equals(currentUser.getUserId())) {
            btnContactSeller.setVisibility(View.VISIBLE);
            btnContactSeller.setOnClickListener(v -> {
                Intent intent = new Intent(this, MessageActivity.class);
                intent.putExtra("receiverId", posterId);
                intent.putExtra("userName", posterName);
                startActivity(intent);
            });
        } else {
            btnContactSeller.setVisibility(View.GONE);
        }

        rvComments.setLayoutManager(new LinearLayoutManager(this));
        btnPostComment.setOnClickListener(v -> {
            String content = etComment.getText().toString().trim();
            if (!content.isEmpty()) {
                postComment(content, replyParentCommentId);
            }
        });

        btnCommentAddImage.setOnClickListener(v -> pickCommentMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        btnCommentGiphy.setOnClickListener(v -> showGiphyPicker());

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
                if (post != null) {
                    runOnUiThread(() -> {
                        if (post.getMedia() != null && post.getMedia().length > 0) {
                            mediaUrls = post.getMedia();
                            currentMediaIndex = 0;
                            setupMediaCarousel();
                        }
                        
                        User currentUser = User.loadUser(this);
                        if (post.isArticleAVendre() && post.getOp() != null && currentUser != null 
                                && !post.getOp().getId().equals(currentUser.getUserId())) {
                            btnContactSeller.setVisibility(View.VISIBLE);
                            btnContactSeller.setOnClickListener(v -> {
                                Intent intent = new Intent(this, MessageActivity.class);
                                intent.putExtra("receiverId", post.getOp().getId());
                                intent.putExtra("userName", post.getOp().getPseudo());
                                startActivity(intent);
                            });
                        }
                    });
                }
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Unable to load post details", Toast.LENGTH_SHORT).show());
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
            etComment.setHint("Replying to " + (parentComment.getOp() != null ? parentComment.getOp().getPseudo() : "User") + "...");
            replyParentCommentId = parentComment.getId();
            inputContainer.post(() -> detailsScrollView.smoothScrollTo(0, commentsSection.getTop()));
            focusCommentInput();
        });
        adapter.setComments(comments);
        rvComments.setAdapter(adapter);
    }

    private void focusCommentInput() {
        etComment.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(etComment, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void showGiphyPicker() {
        GiphyDialogFragment giphyDialog = GiphyDialogFragment.Companion.newInstance(new GPHSettings());
        giphyDialog.setGifSelectionListener(new GiphyDialogFragment.GifSelectionListener() {
            @Override
            public void onGifSelected(@NonNull Media media, @Nullable String searchTerm, @NonNull GPHContentType selectedContentType) {
                if (media.getImages().getOriginal() != null) {
                    String gifUrl = media.getImages().getOriginal().getGifUrl();
                    postComment(gifUrl, replyParentCommentId);
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
        giphyDialog.show(getSupportFragmentManager(), "comment_giphy_dialog");
    }

    private void uploadAndPostCommentImage(Uri imageUri) {
        if (postId == null || postId.trim().isEmpty()) {
            Toast.makeText(this, "Unable to post image", Toast.LENGTH_SHORT).show();
            return;
        }

        String parentId = replyParentCommentId;
        executorService.execute(() -> {
            try {
                File file = FileUtils.getFileFromUri(this, imageUri);
                if (file == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Unable to read image", Toast.LENGTH_SHORT).show());
                    return;
                }

                String mimeType = getContentResolver().getType(imageUri);
                if (mimeType == null) {
                    mimeType = "image/jpeg";
                }

                String uploadedUrl = PostDao.uploadFile(file, file.getName(), mimeType);
                if (uploadedUrl != null) {
                    postComment(uploadedUrl, parentId);
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Unable to upload image", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Unable to upload image", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void postComment(String content, String parentId) {
        User currentUser = User.loadUser(this);
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to comment", Toast.LENGTH_SHORT).show();
            return;
        }

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
                        replyParentCommentId = null;
                        loadComments();
                    });
                }
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Unable to post comment", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private int estimateVisibleCommentCount(List<Comment> comments) {
        if (comments == null) return 0;
        int count = comments.size();
        for (Comment c : comments) {
            if (c.getReplies() != null) {
                count += c.getReplies().size();
            }
        }
        return count;
    }
}
