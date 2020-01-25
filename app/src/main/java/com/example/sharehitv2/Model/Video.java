package com.example.sharehitv2.Model;

public class Video extends Type {

    private String annee;
    private String idYoutube;

    public Video(String name, String annee, String imgUrl, String link, String idYoutube) {
        super(name, imgUrl, annee, link);
        this.idYoutube=idYoutube;
        setCONST_NOMMAGE("Année de sortie: ");
    }

    public String getIdYoutube() {
        return idYoutube;
    }

    public void setIdYoutube(String idYoutube) {
        this.idYoutube = idYoutube;
    }

    public String getAnnee() {
        return annee;
    }

    public void setAnnee(String annee) {
        this.annee = annee;
    }
}
