package com.example.villagets_androidstudio.Model.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Post {
    private String id;
    
    @JsonProperty("nom")
    @JsonAlias({"titre", "title"})
    private String titre;
    
    private String contenu;
    private String categorie;
    private String[] media;
    private String datePublication;
    private Double prix;
    private boolean articleAVendre;
    private Author op;
    
    private int likes;
    private int dislikes;
    @JsonAlias({"commentaires", "commentCount"})
    private int commentCount;
    private String userReaction; // "like", "dislike" or null

    public static class Author {
        private String id;
        private String pseudo;
        private String photoProfil;

        public Author() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getPseudo() { return pseudo; }
        public void setPseudo(String pseudo) { this.pseudo = pseudo; }
        public String getPhotoProfil() { return photoProfil; }
        public void setPhotoProfil(String photoProfil) { this.photoProfil = photoProfil; }
    }

    public Post() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String[] getMedia() { return media; }
    public void setMedia(String[] media) { this.media = media; }

    public String getDatePublication() { return datePublication; }
    public void setDatePublication(String datePublication) { this.datePublication = datePublication; }

    public Double getPrix() { return prix; }
    public void setPrix(Double prix) { this.prix = prix; }

    public boolean isArticleAVendre() { return articleAVendre; }
    public void setArticleAVendre(boolean articleAVendre) { this.articleAVendre = articleAVendre; }

    public Author getOp() { return op; }
    public void setOp(Author op) { this.op = op; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getDislikes() { return dislikes; }
    public void setDislikes(int dislikes) { this.dislikes = dislikes; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public String getUserReaction() { return userReaction; }
    public void setUserReaction(String userReaction) { this.userReaction = userReaction; }

    public int getUserReactionValue() {
        if ("like".equals(userReaction)) {
            return 1;
        }
        if ("dislike".equals(userReaction)) {
            return -1;
        }
        return 0;
    }

    public void setUserReactionValue(int reactionValue) {
        if (reactionValue > 0) {
            userReaction = "like";
        } else if (reactionValue < 0) {
            userReaction = "dislike";
        } else {
            userReaction = null;
        }
    }
}
