package com.example.villagets_androidstudio.View.Feed;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.villagets_androidstudio.Model.Entity.Comment;
import com.example.villagets_androidstudio.Model.Dao.PostDao;
import com.example.villagets_androidstudio.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final OnReplyClickListener replyClickListener;

    public interface OnReplyClickListener {
        void onReplyClick(Comment comment);
    }

    public CommentAdapter(OnReplyClickListener replyClickListener) {
        this.replyClickListener = replyClickListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setComments(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        if (comment == null) return;

        holder.tvUserName.setText(comment.getOp() != null ? comment.getOp().getPseudo() : "User");
        holder.tvTime.setText(comment.getDateCommentaire());

        String content = comment.getContenu();
        if (isImageUrl(content)) {
            holder.tvContent.setVisibility(View.GONE);
            holder.ivCommentImage.setVisibility(View.VISIBLE);
            String displayUrl = content.replace("localhost", "10.0.2.2");
            Glide.with(holder.itemView.getContext()).load(displayUrl).into(holder.ivCommentImage);
        } else {
            holder.tvContent.setVisibility(View.VISIBLE);
            holder.ivCommentImage.setVisibility(View.GONE);
            holder.tvContent.setText(content);
        }

        if (comment.getOp() != null && comment.getOp().getPhotoProfil() != null) {
            String photoUrl = comment.getOp().getPhotoProfil().replace("localhost", "10.0.2.2");
            Glide.with(holder.itemView.getContext()).load(photoUrl).placeholder(R.drawable.profile_placeholder).into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.profile_placeholder);
        }

        updateExpandLink(holder, comment);

        holder.tvExpandReplies.setOnClickListener(v -> {
            if (comment.isExpanded()) {
                comment.setExpanded(false);
                holder.repliesContainer.setVisibility(View.GONE);
                updateExpandLink(holder, comment);
            } else {
                loadReplies(comment, holder);
            }
        });

        holder.tvReplyAction.setOnClickListener(v -> {
            if (replyClickListener != null) {
                replyClickListener.onReplyClick(comment);
            }
        });

        if (comment.isExpanded() && comment.getReplies() != null) {
            holder.repliesContainer.setVisibility(View.VISIBLE);
            setupRepliesRecyclerView(holder, comment.getReplies());
        } else {
            holder.repliesContainer.setVisibility(View.GONE);
        }
    }

    private static boolean isImageUrl(String text) {
        if (text == null) return false;
        return text.startsWith("http") && (
                text.contains("/upload/") ||
                text.endsWith(".jpg") ||
                text.endsWith(".png") ||
                text.endsWith(".jpeg") ||
                text.endsWith(".gif") ||
                text.contains("giphy.com") ||
                text.contains("10.0.2.2") ||
                text.contains("localhost")
        );
    }

    private void updateExpandLink(CommentViewHolder holder, Comment comment) {
        if (comment.getReplyCount() > 0 || (comment.getReplies() != null && !comment.getReplies().isEmpty())) {
            holder.tvExpandReplies.setVisibility(View.VISIBLE);
            if (comment.isExpanded()) {
                holder.tvExpandReplies.setText("▲ Hide replies");
                holder.tvExpandReplies.setBackgroundResource(R.drawable.blue_outline);
                holder.tvExpandReplies.setPadding(12, 4, 12, 4);
            } else {
                int count = comment.getReplyCount() > 0 ? comment.getReplyCount() : (comment.getReplies() != null ? comment.getReplies().size() : 0);
                holder.tvExpandReplies.setText("▼ " + count + " replies");
                holder.tvExpandReplies.setBackground(null);
                holder.tvExpandReplies.setPadding(0, 0, 0, 0);
            }
        } else {
            holder.tvExpandReplies.setVisibility(View.GONE);
        }
    }

    private void loadReplies(Comment comment, CommentViewHolder holder) {
        executorService.execute(() -> {
            try {
                List<Comment> replies = PostDao.getReplies(comment.getId());
                holder.itemView.post(() -> {
                    comment.setReplies(replies);
                    comment.setExpanded(true);
                    holder.repliesContainer.setVisibility(View.VISIBLE);
                    updateExpandLink(holder, comment);
                    setupRepliesRecyclerView(holder, replies);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void setupRepliesRecyclerView(CommentViewHolder holder, List<Comment> replies) {
        CommentAdapter repliesAdapter = new CommentAdapter(replyClickListener);
        holder.rvReplies.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.rvReplies.setAdapter(repliesAdapter);
        repliesAdapter.setComments(replies);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivAvatar;
        ImageView ivCommentImage;
        TextView tvUserName, tvContent, tvTime, tvReplyAction, tvExpandReplies, tvDeleteComment;
        RecyclerView rvReplies;
        View repliesContainer;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivCommentUserAvatar);
            ivCommentImage = itemView.findViewById(R.id.ivCommentImage);
            tvUserName = itemView.findViewById(R.id.tvCommentUserName);
            tvContent = itemView.findViewById(R.id.tvCommentContent);
            tvTime = itemView.findViewById(R.id.tvCommentTime);
            tvReplyAction = itemView.findViewById(R.id.tvReplyAction);
            tvExpandReplies = itemView.findViewById(R.id.tvExpandReplies);
            tvDeleteComment = itemView.findViewById(R.id.tvDeleteComment);
            rvReplies = itemView.findViewById(R.id.rvReplies);
            repliesContainer = itemView.findViewById(R.id.repliesContainer);
        }
    }
}
