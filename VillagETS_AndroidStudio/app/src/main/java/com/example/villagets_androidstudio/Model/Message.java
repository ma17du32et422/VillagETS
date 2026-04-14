package com.example.villagets_androidstudio.Model;

public class Message {
    private String senderName;
    private String text;
    private String timestamp;
    private String avatarUrl; // or resource ID string

    public Message(String senderName, String text, String timestamp, String avatarUrl) {
        this.senderName = senderName;
        this.text = text;
        this.timestamp = timestamp;
        this.avatarUrl = avatarUrl;
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
}
