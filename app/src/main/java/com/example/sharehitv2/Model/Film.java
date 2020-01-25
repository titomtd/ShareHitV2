package com.example.sharehitv2.Model;

public class Film extends Video {

    public Film(String titre, String year, String imgUrl, String link, String id) {

        super(titre, year, imgUrl, link, id);
    }

    public String toString(){
        return "le film "+getName();
    }
}
