package com.example.villagets_androidstudio;

public class Categorie {
    private int id;
    private String nom;
    private boolean cat_base;
    public Categorie() {
    }
    public Categorie(int id, String nom) {
        this.id = id;
        this.nom = nom;}
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }




}
