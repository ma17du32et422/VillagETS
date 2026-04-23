package com.example.villagets_androidstudio.View.Message;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.villagets_androidstudio.R;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private String[] userNames;
    private String[] lastMessages;
    private String[] times;
    private String[] photoUrls;
    private String[] receiverIds;

    public ConversationAdapter(String[] userNames, String[] lastMessages, String[] times, String[] photoUrls, String[] receiverIds) {
        this.userNames = userNames;
        this.lastMessages = lastMessages;
        this.times = times;
        this.photoUrls = photoUrls;
        this.receiverIds = receiverIds;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        String name = userNames[position];
        String receiverId = receiverIds[position];
        String photoUrl = photoUrls[position];

        holder.userName.setText(name);
        holder.lastMessage.setText(lastMessages[position]);
        holder.messageTime.setText(times[position]);

        String displayUrl = photoUrl;
        if (displayUrl != null) {
            displayUrl = displayUrl.replace("localhost", "10.0.2.2");
        }

        Glide.with(holder.itemView.getContext())
                .load(displayUrl)
                .placeholder(R.drawable.profile_placeholder)
                .error(R.drawable.profile_placeholder)
                .circleCrop()
                .into(holder.userAvatar);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MessageActivity.class);
            intent.putExtra("userName", name);
            intent.putExtra("receiverId", receiverId);
            intent.putExtra("photoUrl", photoUrl);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userNames.length;
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        ImageView userAvatar;
        TextView userName, lastMessage, messageTime;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            userName = itemView.findViewById(R.id.userName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            messageTime = itemView.findViewById(R.id.messageTime);
        }
    }
}
