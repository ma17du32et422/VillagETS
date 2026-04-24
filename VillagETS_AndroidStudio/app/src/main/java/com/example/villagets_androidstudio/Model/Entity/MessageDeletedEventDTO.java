package com.example.villagets_androidstudio.Model.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageDeletedEventDTO {
    private String type = "message_deleted";
    private String id;
    private String conversationId;
    private String envoyeurId;
    private String receveurId;

    public MessageDeletedEventDTO() {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getEnvoyeurId() {
        return envoyeurId;
    }

    public void setEnvoyeurId(String envoyeurId) {
        this.envoyeurId = envoyeurId;
    }

    public String getReceveurId() {
        return receveurId;
    }

    public void setReceveurId(String receveurId) {
        this.receveurId = receveurId;
    }
}
