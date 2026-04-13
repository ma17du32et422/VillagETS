package com.example.villagets_androidstudio.View;

import android.annotation.SuppressLint;
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

        if (post.getMedia() != null && post.getMedia().length > 0) {
            holder.image.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(post.getMedia()[0])
                    .into(holder.image);
        } else {
            holder.image.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return postList != null ? postList.size() : 0;
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView title, content;
        ImageView image;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.postTitle);
            content = itemView.findViewById(R.id.postContent);
            image = itemView.findViewById(R.id.postImage);
        }
    }
}
