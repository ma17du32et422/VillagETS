package com.example.villagets_androidstudio;

public class Post {
    private int id;
    private int usrId;
    private String titre;
    private String contenu;
    private String lienImage;
    private String date;
    private Categorie[] categories;


    public Post() {
    }

    public Post(int id, int usrId, String titre, String contenu, String lienImage, String date,Categorie[] categories) {
        this.id = id;
        this.usrId = usrId;
        this.titre = titre;
        this.contenu = contenu;
        this.lienImage = lienImage;
        this.date = date;
        this.categories = categories;
    }

    public Categorie[] getCategories() {
        return categories;
    }

    public void setCategories(Categorie[] categories) {
        this.categories = categories;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsrId() {
        return usrId;
    }

    public void setUsrId(int usrId) {
        this.usrId = usrId;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getLienImage() {
        return lienImage;
    }

    public void setLienImage(String lienImage) {
        this.lienImage = lienImage;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
