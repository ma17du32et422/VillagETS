package com.example.villagets_androidstudio.View;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.villagets_androidstudio.R;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private String[] userNames;
    private String[] lastMessages;
    private String[] times;
    private Integer[] avatarResIds;

    public MessageAdapter(String[] userNames, String[] lastMessages, String[] times, Integer[] avatarResIds) {
        this.userNames = userNames;
        this.lastMessages = lastMessages;
        this.times = times;
        this.avatarResIds = avatarResIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvUserName.setText(userNames[position]);
        holder.tvLastMessage.setText(lastMessages[position]);
        holder.tvTime.setText(times[position]);

        if (avatarResIds != null && position < avatarResIds.length) {
            Glide.with(holder.itemView.getContext())
                    .load(avatarResIds[position])
                    .circleCrop()
                    .placeholder(android.R.color.darker_gray)
                    .into(holder.ivAvatar);
        }
    }

    @Override
    public int getItemCount() {
        return userNames.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvLastMessage, tvTime;
        ImageView ivAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.userName);
            tvLastMessage = itemView.findViewById(R.id.lastMessage);
            tvTime = itemView.findViewById(R.id.messageTime);
            ivAvatar = itemView.findViewById(R.id.userAvatar);
        }
    }
}
