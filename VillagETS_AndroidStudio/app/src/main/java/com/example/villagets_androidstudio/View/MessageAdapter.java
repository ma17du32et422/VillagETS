package com.example.villagets_androidstudio.View;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.villagets_androidstudio.Model.Message;
import com.example.villagets_androidstudio.R;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.tvUserName.setText(message.getSenderName());
        holder.tvMessageText.setText(message.getText());
        holder.tvTimestamp.setText(message.getTimestamp());
        holder.ivAvatar.setImageResource(R.drawable.silicate);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivAvatar;
        TextView tvUserName, tvMessageText, tvTimestamp;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.messageUserAvatar);
            tvUserName = itemView.findViewById(R.id.messageUserName);
            tvMessageText = itemView.findViewById(R.id.messageText);
            tvTimestamp = itemView.findViewById(R.id.messageTimestamp);
        }
    }
}
