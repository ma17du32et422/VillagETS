package com.example.villagets_androidstudio.View.Search;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.villagets_androidstudio.Model.Entity.User;
import com.example.villagets_androidstudio.R;
import com.example.villagets_androidstudio.View.Profile.ProfileActivity;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {

    private List<User> userList = new ArrayList<>();

    public void setUsers(List<User> users) {
        this.userList = users != null ? users : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_search, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvPseudo.setText(user.getPseudo());
        holder.tvFullName.setText(user.getPrenom() + " " + user.getNom());

        if (user.getPhotoProfil() != null && !user.getPhotoProfil().isEmpty()) {
            String photoUrl = user.getPhotoProfil().replace("localhost", "10.0.2.2");
            Glide.with(holder.itemView.getContext())
                    .load(photoUrl)
                    .placeholder(R.drawable.profile_placeholder)
                    .into(holder.ivPhoto);
        } else {
            holder.ivPhoto.setImageResource(R.drawable.profile_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            String selectedUserId = user.getUserId();
            if (selectedUserId == null || selectedUserId.trim().isEmpty()) {
                Toast.makeText(v.getContext(), "Unable to open this profile", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(v.getContext(), ProfileActivity.class);
            intent.putExtra("userId", selectedUserId);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivPhoto;
        TextView tvPseudo, tvFullName;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivUserPhoto);
            tvPseudo = itemView.findViewById(R.id.tvUserPseudo);
            tvFullName = itemView.findViewById(R.id.tvUserFullName);
        }
    }
}
