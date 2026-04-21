package com.example.villagets_androidstudio.View;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.villagets_androidstudio.Model.Post;
import com.example.villagets_androidstudio.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList = new ArrayList<>();

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
        
        String posterName = "User Name";
        String posterAvatarUrl = null;
        
        if (post.getOp() != null) {
            posterName = post.getOp().getPseudo();
            posterAvatarUrl = post.getOp().getPhotoProfil();
            holder.userName.setText(posterName);
            if (posterAvatarUrl != null) {
                String avatarUrl = posterAvatarUrl.replace("localhost", "10.0.2.2");
                Glide.with(holder.itemView.getContext()).load(avatarUrl).into(holder.userAvatar);
            } else {
                holder.userAvatar.setImageDrawable(null);
            }
        }
        
        if (post.getDatePublication() != null) {
            holder.postTime.setText(post.getDatePublication());
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

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ItemDetailsActivity.class);
            intent.putExtra("title", post.getTitre());
            intent.putExtra("description", post.getContenu());
            intent.putExtra("price", post.getPrix() != null ? String.format("%.2f$", post.getPrix()) : "");
            intent.putExtra("imageUrl", imageUrl);
            intent.putExtra("posterName", finalPosterName);
            intent.putExtra("posterAvatarUrl", finalPosterAvatarUrl);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return postList != null ? postList.size() : 0;
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView title, content, userName, postTime;
        ImageView image;
        ShapeableImageView userAvatar;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.postTitle);
            content = itemView.findViewById(R.id.postContent);
            image = itemView.findViewById(R.id.postImage);
            userName = itemView.findViewById(R.id.postUserName);
            postTime = itemView.findViewById(R.id.postTime);
            userAvatar = itemView.findViewById(R.id.postUserAvatar);
        }
    }
}
