package com.example.villagets_androidstudio.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage {
    @JsonProperty("id")
    private String id;

    private String conversationId;
    private String envoyeurId;
    private String receveurId;
    private String contenu;
    private String dateMsg;

    public ChatMessage() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getEnvoyeurId() { return envoyeurId; }
    public void setEnvoyeurId(String envoyeurId) { this.envoyeurId = envoyeurId; }
    public String getReceveurId() { return receveurId; }
    public void setReceveurId(String receveurId) { this.receveurId = receveurId; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public String getDateMsg() { return dateMsg; }
    public void setDateMsg(String dateMsg) { this.dateMsg = dateMsg; }
}
