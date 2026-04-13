package com.example.villagets_androidstudio.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Post {
    private String id;
    private String titre;
    private String contenu;
    private String[] media;
    private String datePublication;
    private Double prix;
    private boolean articleAVendre;
    private Author op;

    // Classe interne pour l'auteur (op)
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

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

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
}
