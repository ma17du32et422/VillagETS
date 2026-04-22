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

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<Message> messageList;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).isSent()) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageText, tvTimestamp;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageText = itemView.findViewById(R.id.messageText);
            tvTimestamp = itemView.findViewById(R.id.messageTimestamp);
        }

        void bind(Message message) {
            tvMessageText.setText(message.getText());
            tvTimestamp.setText(message.getTimestamp());
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivAvatar;
        TextView tvUserName, tvMessageText, tvTimestamp;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.messageUserAvatar);
            tvUserName = itemView.findViewById(R.id.messageUserName);
            tvMessageText = itemView.findViewById(R.id.messageText);
            tvTimestamp = itemView.findViewById(R.id.messageTimestamp);
        }

        void bind(Message message) {
            tvUserName.setText(message.getSenderName());
            tvMessageText.setText(message.getText());
            tvTimestamp.setText(message.getTimestamp());
            // ivAvatar handled via image loading library if needed
        }
    }
}
