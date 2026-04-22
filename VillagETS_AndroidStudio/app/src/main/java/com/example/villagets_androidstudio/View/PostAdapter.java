package com.example.villagets_androidstudio.View;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.villagets_androidstudio.Model.Comment;
import com.example.villagets_androidstudio.Model.Dao.PostApi;
import com.example.villagets_androidstudio.Model.Dao.PostDao;
import com.example.villagets_androidstudio.Model.Post;
import com.example.villagets_androidstudio.Model.User;
import com.example.villagets_androidstudio.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public PostAdapter() {}

    @SuppressLint("NotifyDataSetChanged")
    public void setPosts(List<Post> posts) {
        this.postList = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        if (post == null) return;

        holder.title.setText(post.getTitre());
        holder.content.setText(post.getContenu());
        holder.likeCount.setText(String.valueOf(post.getLikes()));
        holder.dislikeCount.setText(String.valueOf(post.getDislikes()));
        holder.commentCount.setText(String.valueOf(post.getCommentCount()));
        
        updateReactionUI(holder, post.getUserReaction());

        if (post.getPrix() != null && post.getPrix() > 0) {
            holder.price.setVisibility(View.VISIBLE);
            holder.price.setText(String.format("$%.2f", post.getPrix()));
        } else {
            holder.price.setVisibility(View.GONE);
        }
        
        String posterName = "User Name";
        String posterAvatarUrl = null;
        String posterId = null;
        
        if (post.getOp() != null) {
            posterName = post.getOp().getPseudo();
            posterAvatarUrl = post.getOp().getPhotoProfil();
            posterId = post.getOp().getId();
            
            holder.userName.setText(posterName);
            if (posterAvatarUrl != null) {
                String avatarUrl = posterAvatarUrl.replace("localhost", "10.0.2.2");
                Glide.with(holder.itemView.getContext()).load(avatarUrl).into(holder.userAvatar);
            } else {
                holder.userAvatar.setImageDrawable(null);
            }

            final String finalPosterId = posterId;
            View.OnClickListener toProfile = v -> {
                Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                intent.putExtra("userId", finalPosterId);
                v.getContext().startActivity(intent);
            };

            holder.userName.setOnClickListener(toProfile);
            holder.userAvatar.setOnClickListener(toProfile);
        }
        
        if (post.getDatePublication() != null) {
            holder.postTime.setText(formatDate(post.getDatePublication()));
        }

        String imageUrl = (post.getMedia() != null && post.getMedia().length > 0) ? post.getMedia()[0] : null;

        if (imageUrl != null) {
            holder.image.setVisibility(View.VISIBLE);
            String displayUrl = imageUrl.replace("localhost", "10.0.2.2");
            Glide.with(holder.itemView.getContext())
                    .load(displayUrl)
                    .into(holder.image);
        } else {
            holder.image.setVisibility(View.GONE);
        }

        final String finalPosterName = posterName;
        final String finalPosterAvatarUrl = posterAvatarUrl;
        final String finalPosterId = posterId;

        holder.btnDetails.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ItemDetailsActivity.class);
            intent.putExtra("postId", post.getId());
            intent.putExtra("title", post.getTitre());
            intent.putExtra("description", post.getContenu());
            intent.putExtra("price", post.getPrix() != null ? String.format("%.2f$", post.getPrix()) : "");
            intent.putExtra("imageUrl", imageUrl);
            intent.putExtra("posterName", finalPosterName);
            intent.putExtra("posterAvatarUrl", finalPosterAvatarUrl);
            intent.putExtra("posterId", finalPosterId);
            v.getContext().startActivity(intent);
        });

        holder.btnLike.setOnClickListener(v -> handleReaction(post, "like", holder));
        holder.btnDislike.setOnClickListener(v -> handleReaction(post, "dislike", holder));

        // Comments logic
        holder.btnCommentContainer.setOnClickListener(v -> {
            if (holder.commentSection.getVisibility() == View.GONE) {
                holder.commentSection.setVisibility(View.VISIBLE);
                loadComments(post.getId(), holder);
            } else {
                holder.commentSection.setVisibility(View.GONE);
            }
        });

        User me = User.loadUser(holder.itemView.getContext());
        if (me != null && me.getPhotoProfil() != null) {
            String myAvatar = me.getPhotoProfil().replace("localhost", "10.0.2.2");
            Glide.with(holder.itemView.getContext()).load(myAvatar).placeholder(R.drawable.profile_placeholder).into(holder.ivMyAvatar);
        }

        holder.btnPostComment.setOnClickListener(v -> {
            String content = holder.etComment.getText().toString().trim();
            if (!content.isEmpty()) {
                postComment(post.getId(), content, null, holder);
            }
        });
    }

    private String formatDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = sdf.parse(dateStr);
            if (date != null) {
                long now = System.currentTimeMillis();
                return DateUtils.getRelativeTimeSpanString(date.getTime(), now, DateUtils.MINUTE_IN_MILLIS).toString();
            }
        } catch (ParseException e) {
            // Tentative avec un format plus court si les microsecondes varient
            try {
                SimpleDateFormat sdfShort = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                sdfShort.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = sdfShort.parse(dateStr);
                if (date != null) {
                    return DateUtils.getRelativeTimeSpanString(date.getTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
                }
            } catch (ParseException e2) {
                return dateStr;
            }
        }
        return dateStr;
    }

    private void loadComments(String postId, PostViewHolder holder) {
        executorService.execute(() -> {
            try {
                List<Comment> comments = PostDao.getPostComments(postId);
                holder.itemView.post(() -> {
                    setupCommentsRecyclerView(postId, holder, comments);
                    updateCommentCount(postId, holder, comments);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void setupCommentsRecyclerView(String postId, PostViewHolder holder, List<Comment> comments) {
        CommentAdapter adapter = new CommentAdapter(parentComment -> {
            // Reply click logic
            holder.etComment.requestFocus();
            holder.etComment.setHint("Replying to " + (parentComment.getOp() != null ? parentComment.getOp().getPseudo() : "User") + "...");
            holder.btnPostComment.setOnClickListener(v -> {
                String content = holder.etComment.getText().toString().trim();
                if (!content.isEmpty()) {
                    postComment(postId, content, parentComment.getId(), holder);
                }
            });
        });
        holder.rvComments.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.rvComments.setAdapter(adapter);
        adapter.setComments(comments);
    }

    private void postComment(String postId, String content, String parentId, PostViewHolder holder) {
        executorService.execute(() -> {
            try {
                Comment newComment = PostDao.createComment(postId, content, parentId);
                if (newComment != null) {
                    holder.itemView.post(() -> {
                        Post post = getPostById(postId);
                        if (post != null) {
                            post.setCommentCount(post.getCommentCount() + 1);
                            holder.commentCount.setText(String.valueOf(post.getCommentCount()));
                        }
                        holder.etComment.setText("");
                        holder.etComment.setHint("Write a comment...");
                        loadComments(postId, holder);
                        // Reset post button click listener to top-level comment
                        holder.btnPostComment.setOnClickListener(v -> {
                            String c = holder.etComment.getText().toString().trim();
                            if (!c.isEmpty()) {
                                postComment(postId, c, null, holder);
                            }
                        });
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateCommentCount(String postId, PostViewHolder holder, List<Comment> comments) {
        Post post = getPostById(postId);
        if (post != null && post.getCommentCount() > 0) {
            holder.commentCount.setText(String.valueOf(post.getCommentCount()));
            return;
        }

        holder.commentCount.setText(String.valueOf(estimateVisibleCommentCount(comments)));
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

    private Post getPostById(String postId) {
        if (postList == null) {
            return null;
        }

        for (Post post : postList) {
            if (post != null && postId.equals(post.getId())) {
                return post;
            }
        }

        return null;
    }

    private void handleReaction(Post post, String type, PostViewHolder holder) {
        executorService.execute(() -> {
            try {
                PostApi.ReactionResponse response = PostDao.toggleReaction(post.getId(), type);
                if (response != null) {
                    post.setLikes(response.likes);
                    post.setDislikes(response.dislikes);
                    post.setUserReaction(response.userReaction);
                    
                    holder.itemView.post(() -> {
                        holder.likeCount.setText(String.valueOf(post.getLikes()));
                        holder.dislikeCount.setText(String.valueOf(post.getDislikes()));
                        updateReactionUI(holder, post.getUserReaction());
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateReactionUI(PostViewHolder holder, String reaction) {
        int activeColor = holder.itemView.getContext().getResources().getColor(R.color.red_primary);
        // Utilisation de text_primary au lieu de black pour supporter le mode sombre
        int inactiveColor = holder.itemView.getContext().getResources().getColor(R.color.text_primary);
        
        holder.ivLike.setColorFilter("like".equals(reaction) ? activeColor : inactiveColor);
        holder.ivDislike.setColorFilter("dislike".equals(reaction) ? activeColor : inactiveColor);
    }

    @Override
    public int getItemCount() {
        return postList != null ? postList.size() : 0;
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView title, content, userName, postTime, price, likeCount, dislikeCount, commentCount, btnPostComment;
        ImageView image, ivLike, ivDislike, ivMyAvatar;
        ShapeableImageView userAvatar;
        ImageButton btnDetails;
        View btnLike, btnDislike, btnCommentContainer, commentSection;
        RecyclerView rvComments;
        EditText etComment;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.postTitle);
            content = itemView.findViewById(R.id.postContent);
            price = itemView.findViewById(R.id.postPrice);
            image = itemView.findViewById(R.id.postImage);
            userName = itemView.findViewById(R.id.postUserName);
            postTime = itemView.findViewById(R.id.postTime);
            userAvatar = itemView.findViewById(R.id.postUserAvatar);
            btnDetails = itemView.findViewById(R.id.btnDetails);
            
            likeCount = itemView.findViewById(R.id.tvLikeCount);
            dislikeCount = itemView.findViewById(R.id.tvDislikeCount);
            commentCount = itemView.findViewById(R.id.tvCommentCount);
            
            btnLike = itemView.findViewById(R.id.btnLikeContainer);
            btnDislike = itemView.findViewById(R.id.btnDislikeContainer);
            btnCommentContainer = itemView.findViewById(R.id.btnCommentContainer);
            
            ivLike = itemView.findViewById(R.id.ivLike);
            ivDislike = itemView.findViewById(R.id.ivDislike);
            
            commentSection = itemView.findViewById(R.id.commentSection);
            rvComments = itemView.findViewById(R.id.rvComments);
            etComment = itemView.findViewById(R.id.etComment);
            btnPostComment = itemView.findViewById(R.id.btnPostComment);
            ivMyAvatar = itemView.findViewById(R.id.ivMyAvatar);
        }
    }
}
