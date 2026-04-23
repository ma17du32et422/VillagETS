package com.example.villagets_androidstudio.Model.Entity;

public class Message {
    private String senderName;
    private String text;
    private String timestamp;
    private String avatarUrl;
    private boolean isSent;

    public Message(String senderName, String text, String timestamp, String avatarUrl, boolean isSent) {
        this.senderName = senderName;
        this.text = text;
        this.timestamp = timestamp;
        this.avatarUrl = avatarUrl;
        this.isSent = isSent;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getText() {
        return text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public boolean isSent() {
        return isSent;
    }
}
