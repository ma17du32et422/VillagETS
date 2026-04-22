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

import java.util.ArrayList;
import java.util.List;

public class MarketPlaceAdapter extends RecyclerView.Adapter<MarketPlaceAdapter.ViewHolder> {

    private List<Post> postList = new ArrayList<>();

    public MarketPlaceAdapter() {}

    @SuppressLint("NotifyDataSetChanged")
    public void setPosts(List<Post> posts) {
        this.postList = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_marketplace, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);
        if (post != null) {
            holder.name.setText(post.getTitre());
            String priceText = String.format("%.2f$", post.getPrix() != null ? post.getPrix() : 0.0);
            holder.price.setText(priceText);
            String posterName = post.getOp() != null && post.getOp().getPseudo() != null
                    ? post.getOp().getPseudo()
                    : "User Name";
            String posterAvatarUrl = post.getOp() != null ? post.getOp().getPhotoProfil() : null;
            String posterId = post.getOp() != null ? post.getOp().getId() : null;
            
            String imageUrl = (post.getMedia() != null && post.getMedia().length > 0) ? post.getMedia()[0] : null;
            
            if (imageUrl != null) {
                // Remplacement de localhost par 10.0.2.2 pour l'émulateur
                String displayUrl = imageUrl.replace("localhost", "10.0.2.2");
                
                Glide.with(holder.itemView.getContext())
                        .load(displayUrl)
                        .into(holder.image);
            } else {
                holder.image.setImageDrawable(null);
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), ItemDetailsActivity.class);
                intent.putExtra("title", post.getTitre());
                intent.putExtra("description", post.getContenu());
                intent.putExtra("price", priceText);
                intent.putExtra("imageUrl", imageUrl);
                intent.putExtra("posterName", posterName);
                intent.putExtra("posterAvatarUrl", posterAvatarUrl);
                intent.putExtra("posterId", posterId);
                v.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView price;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.itemName);
            price = itemView.findViewById(R.id.itemPrice);
            image = itemView.findViewById(R.id.itemImage);
        }
    }
}
