package com.example.villagets_androidstudio.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Conversation {
    private String conversationId;
    private OtherUser otherUser;

    public static class OtherUser {
        private String id_utilisateur;
        private String nom;
        private String prenom;

        public OtherUser() {}

        public String getId_utilisateur() { return id_utilisateur; }
        public void setId_utilisateur(String id_utilisateur) { this.id_utilisateur = id_utilisateur; }
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
