package com.example.sharehitv2.Model;

public class Artist extends Type {

    private String songUrl;

    public Artist(String name,  String nbFans,  String imgUrl, String link) {
        super(name, imgUrl, nbFans, link);
        setCONST_NOMMAGE("Nombre de fans: ");
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String urlPreview) {
        this.songUrl = urlPreview;
    }

    public String toString(){
        return "l'artiste "+getName();
    }

}

