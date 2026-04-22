package com.example.villagets_androidstudio.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Conversation {
    private String conversationId;
    private OtherUser otherUser;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OtherUser {
        @JsonProperty("id_utilisateur")
        private String id_utilisateur;
        
        @JsonProperty("id")
        private String id;

        private String nom;
        private String prenom;

        public OtherUser() {}

        public String getId_utilisateur() { 
            return id_utilisateur != null ? id_utilisateur : id; 
        }
        
        public void setId_utilisateur(String id_utilisateur) { this.id_utilisateur = id_utilisateur; }
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        public String getPrenom() { return prenom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }
    }

    public Conversation() {}

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public OtherUser getOtherUser() { return otherUser; }
    public void setOtherUser(OtherUser otherUser) { this.otherUser = otherUser; }
}
