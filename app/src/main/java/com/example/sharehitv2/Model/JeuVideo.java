package com.example.sharehitv2.Model;

public class JeuVideo extends Video {

    public JeuVideo(String titre, String year, String imgUrl, String link, String id) {
        super(titre, year, imgUrl, link, id);
        setCONST_NOMMAGE("Année de sortie: ");
    }

    public String toString(){
        return "le jeu vidéo "+getName();
    }
}
