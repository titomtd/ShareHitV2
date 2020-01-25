package com.example.sharehitv2.Model;

public class Serie extends Video {

    public Serie(String titre, String year, String imgUrl, String link, String id) {
        super(titre, year, imgUrl, link, id);
    }

    public String toString(){
        return "la série "+getName();
    }
}
