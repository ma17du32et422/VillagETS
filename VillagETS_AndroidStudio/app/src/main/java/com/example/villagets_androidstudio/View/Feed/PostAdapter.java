package com.example.villagets_androidstudio.View.Feed;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.villagets_androidstudio.Model.Dao.PostApi;
import com.example.villagets_androidstudio.Model.Dao.PostDao;
import com.example.villagets_androidstudio.Model.Entity.Post;
import com.example.villagets_androidstudio.Model.Entity.User;
import com.example.villagets_androidstudio.R;
import com.example.villagets_androidstudio.View.Message.MessageActivity;
import com.example.villagets_androidstudio.View.Profile.ProfileActivity;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private static final String PAYLOAD_REACTION = "payload_reaction";

    private List<Post> postList = new ArrayList<>();
    private final Map<String, Integer> mediaIndexByPostId = new HashMap<>();
    private final Set<String> reactionRequestsInFlight = new HashSet<>();
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
        holder.commentCount.setText(String.valueOf(post.getCommentCount()));
        bindReactionState(holder, post);

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

        String[] media = post.getMedia();
        String imageUrl = (media != null && media.length > 0) ? media[0] : null;
        setupMediaCarousel(post, holder);

        final String finalPosterName = posterName;
        final String finalPosterAvatarUrl = posterAvatarUrl;
        final String finalPosterId = posterId;
        User currentUser = User.loadUser(holder.itemView.getContext());

        View.OnClickListener openDetails = v -> {
            Intent intent = new Intent(v.getContext(), ItemDetailsActivity.class);
            intent.putExtra("postId", post.getId());
            intent.putExtra("title", post.getTitre());
            intent.putExtra("description", post.getContenu());
            intent.putExtra("price", post.getPrix() != null ? String.format("%.2f$", post.getPrix()) : "");
            intent.putExtra("imageUrl", imageUrl);
            intent.putExtra("media", post.getMedia());
            intent.putExtra("posterName", finalPosterName);
            intent.putExtra("posterAvatarUrl", finalPosterAvatarUrl);
            intent.putExtra("posterId", finalPosterId);
            if (v == holder.btnCommentContainer) {
                intent.putExtra("openComments", true);
            }
            v.getContext().startActivity(intent);
        };

        boolean isAuthor = currentUser != null
                && currentUser.getUserId() != null
                && currentUser.getUserId().equals(finalPosterId);
        holder.btnDetails.setVisibility(View.VISIBLE);
        holder.btnDetails.setOnClickListener(v -> showPostOptions(post, holder, isAuthor, finalPosterId, finalPosterName));

        holder.btnLike.setOnClickListener(v -> handleReaction(post, 1, holder));
        holder.btnDislike.setOnClickListener(v -> handleReaction(post, -1, holder));
        holder.btnCommentContainer.setOnClickListener(openDetails);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            Post post = postList.get(position);
            if (post != null && payloads.contains(PAYLOAD_REACTION)) {
                bindReactionState(holder, post);
                return;
            }
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    private void showPostOptions(Post post, PostViewHolder holder, boolean isAuthor, String posterId, String posterName) {
        PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(), holder.btnDetails);
        if (isAuthor) {
            popupMenu.getMenu().add(Menu.NONE, 1, 1, "Delete post");
        } else {
            popupMenu.getMenu().add(Menu.NONE, 1, 1, "Begin discussion");
        }
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Cancel");
        popupMenu.setOnMenuItemClickListener(item -> handlePostOptionClick(item, post, holder, isAuthor, posterId, posterName));
        popupMenu.show();
    }

    private boolean handlePostOptionClick(MenuItem item, Post post, PostViewHolder holder, boolean isAuthor, String posterId, String posterName) {
        if (item.getItemId() == 1) {
            if (isAuthor) {
                deletePost(post, holder);
            } else {
                beginDiscussion(holder, posterId, posterName);
            }
            return true;
        }
        return true;
    }

    private void beginDiscussion(PostViewHolder holder, String posterId, String posterName) {
        if (posterId == null || posterId.trim().isEmpty()) {
            Toast.makeText(holder.itemView.getContext(), "Unable to start discussion", Toast.LENGTH_SHORT).show();
            return;
        }
        
        User currentUser = User.loadUser(holder.itemView.getContext());
        if (currentUser != null && posterId.equals(currentUser.getUserId())) {
            Toast.makeText(holder.itemView.getContext(), "You cannot message yourself", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(holder.itemView.getContext(), MessageActivity.class);
        intent.putExtra("receiverId", posterId);
        intent.putExtra("userName", posterName);
        holder.itemView.getContext().startActivity(intent);
    }

    private void setupMediaCarousel(Post post, PostViewHolder holder) {
        String[] media = post.getMedia();
        if (media == null || media.length == 0) {
            holder.imageContainer.setVisibility(View.GONE);
            return;
        }

        holder.imageContainer.setVisibility(View.VISIBLE);
        int currentIndex = mediaIndexByPostId.getOrDefault(post.getId(), 0);
        if (currentIndex >= media.length) {
            currentIndex = 0;
            mediaIndexByPostId.put(post.getId(), currentIndex);
        }
        showMedia(post, holder, currentIndex);

        holder.btnPreviousImage.setOnClickListener(v -> {
            int nextIndex = (mediaIndexByPostId.getOrDefault(post.getId(), 0) - 1 + media.length) % media.length;
            mediaIndexByPostId.put(post.getId(), nextIndex);
            showMedia(post, holder, nextIndex);
        });

        holder.btnNextImage.setOnClickListener(v -> {
            int nextIndex = (mediaIndexByPostId.getOrDefault(post.getId(), 0) + 1) % media.length;
            mediaIndexByPostId.put(post.getId(), nextIndex);
            showMedia(post, holder, nextIndex);
        });
    }

    private void showMedia(Post post, PostViewHolder holder, int index) {
        String[] media = post.getMedia();
        if (media == null || media.length == 0 || index < 0 || index >= media.length) {
            holder.imageContainer.setVisibility(View.GONE);
            return;
        }

        String mediaUrl = media[index];
        if (mediaUrl == null || mediaUrl.trim().isEmpty()) {
            holder.image.setImageDrawable(null);
            return;
        }

        String displayUrl = mediaUrl.replace("localhost", "10.0.2.2");
        Glide.with(holder.itemView.getContext())
                .load(displayUrl)
                .into(holder.image);

        boolean hasMultiplePhotos = media.length > 1;
        holder.btnPreviousImage.setVisibility(hasMultiplePhotos ? View.VISIBLE : View.GONE);
        holder.btnNextImage.setVisibility(hasMultiplePhotos ? View.VISIBLE : View.GONE);
        holder.imageCounter.setVisibility(hasMultiplePhotos ? View.VISIBLE : View.GONE);
        holder.imageCounter.setText((index + 1) + " / " + media.length);
    }

    private void deletePost(Post post, PostViewHolder holder) {
        executorService.execute(() -> {
            try {
                boolean deleted = PostDao.deletePost(post.getId());
                holder.itemView.post(() -> {
                    if (deleted) {
                        int position = holder.getBindingAdapterPosition();
                        if (position != RecyclerView.NO_POSITION && position < postList.size()) {
                            postList.remove(position);
                            notifyItemRemoved(position);
                        } else {
                            notifyDataSetChanged();
                        }
                        Toast.makeText(holder.itemView.getContext(), "Post deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(holder.itemView.getContext(), "Only the author can delete this post", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                holder.itemView.post(() ->
                        Toast.makeText(holder.itemView.getContext(), "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
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

    private void handleReaction(Post post, int tappedReaction, PostViewHolder holder) {
        String postId = post.getId();
        if (postId == null || postId.trim().isEmpty()) {
            Toast.makeText(holder.itemView.getContext(), "Unable to update reaction", Toast.LENGTH_SHORT).show();
            return;
        }

        synchronized (reactionRequestsInFlight) {
            if (reactionRequestsInFlight.contains(postId)) {
                return;
            }
            reactionRequestsInFlight.add(postId);
        }

        int oldLikes = post.getLikes();
        int oldDislikes = post.getDislikes();
        int oldReaction = post.getUserReactionValue();

        int newReaction = computeNewReaction(oldReaction, tappedReaction);
        applyOptimisticReaction(post, oldReaction, newReaction);
        bindReactionState(holder, post);

        String requestType = tappedReaction > 0 ? "like" : "dislike";
        executorService.execute(() -> {
            try {
                PostApi.ReactionResponse response = PostDao.toggleReaction(postId, requestType);
                holder.itemView.post(() -> {
                    synchronized (reactionRequestsInFlight) {
                        reactionRequestsInFlight.remove(postId);
                    }

                    if (response != null) {
                        post.setLikes(response.likes);
                        post.setDislikes(response.dislikes);
                        post.setUserReaction(response.userReaction);
                    } else {
                        rollbackReaction(post, oldLikes, oldDislikes, oldReaction);
                        Toast.makeText(holder.itemView.getContext(), "Unable to update reaction", Toast.LENGTH_SHORT).show();
                    }

                    notifyReactionChanged(post);
                });
            } catch (IOException e) {
                holder.itemView.post(() -> {
                    synchronized (reactionRequestsInFlight) {
                        reactionRequestsInFlight.remove(postId);
                    }

                    rollbackReaction(post, oldLikes, oldDislikes, oldReaction);
                    notifyReactionChanged(post);
                    Toast.makeText(holder.itemView.getContext(), "Reaction update failed", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void bindReactionState(PostViewHolder holder, Post post) {
        holder.likeCount.setText(String.valueOf(post.getLikes()));
        holder.dislikeCount.setText(String.valueOf(post.getDislikes()));
        updateReactionUI(holder, post.getUserReactionValue());
    }

    private int computeNewReaction(int oldReaction, int tappedReaction) {
        if (oldReaction == tappedReaction) {
            return 0;
        }
        return tappedReaction;
    }

    private void applyOptimisticReaction(Post post, int oldReaction, int newReaction) {
        int likes = post.getLikes();
        int dislikes = post.getDislikes();

        if (oldReaction == 1) {
            likes = Math.max(0, likes - 1);
        } else if (oldReaction == -1) {
            dislikes = Math.max(0, dislikes - 1);
        }

        if (newReaction == 1) {
            likes += 1;
        } else if (newReaction == -1) {
            dislikes += 1;
        }

        post.setLikes(likes);
        post.setDislikes(dislikes);
        post.setUserReactionValue(newReaction);
    }

    private void rollbackReaction(Post post, int oldLikes, int oldDislikes, int oldReaction) {
        post.setLikes(oldLikes);
        post.setDislikes(oldDislikes);
        post.setUserReactionValue(oldReaction);
    }

    private void notifyPostChanged(Post post) {
        int position = postList.indexOf(post);
        if (position != RecyclerView.NO_POSITION) {
            notifyItemChanged(position);
        } else {
            notifyDataSetChanged();
        }
    }

    private void notifyReactionChanged(Post post) {
        int position = postList.indexOf(post);
        if (position != RecyclerView.NO_POSITION) {
            notifyItemChanged(position, PAYLOAD_REACTION);
        } else {
            notifyDataSetChanged();
        }
    }

    private void updateReactionUI(PostViewHolder holder, int reactionValue) {
        int activeColor = holder.itemView.getContext().getResources().getColor(R.color.red_primary);
        int inactiveColor = holder.itemView.getContext().getResources().getColor(R.color.text_primary);
        
        holder.ivLike.setColorFilter(reactionValue == 1 ? activeColor : inactiveColor);
        holder.ivDislike.setColorFilter(reactionValue == -1 ? activeColor : inactiveColor);
    }

    @Override
    public int getItemCount() {
        return postList != null ? postList.size() : 0;
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView title, content, userName, postTime, price, likeCount, dislikeCount, commentCount;
        TextView btnPreviousImage, btnNextImage, imageCounter;
        ImageView image, ivLike, ivDislike;
        ShapeableImageView userAvatar;
        ImageButton btnDetails;
        View btnLike, btnDislike, btnCommentContainer, imageContainer;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.postTitle);
            content = itemView.findViewById(R.id.postContent);
            price = itemView.findViewById(R.id.postPrice);
            imageContainer = itemView.findViewById(R.id.postImageContainer);
            image = itemView.findViewById(R.id.postImage);
            btnPreviousImage = itemView.findViewById(R.id.btnPreviousImage);
            btnNextImage = itemView.findViewById(R.id.btnNextImage);
            imageCounter = itemView.findViewById(R.id.tvImageCounter);
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
        }
    }
}
