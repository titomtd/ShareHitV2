package com.example.sharehitv2.Model;

public class Album extends Type {

    private String songUrl;

    public Album(String artiste, String titre, String imgUrl, String link) {
        super(titre, imgUrl, artiste, link);
        setCONST_NOMMAGE("Artiste: ");
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String urlPreview) {
        this.songUrl = urlPreview;
    }

    public String toString(){
        return "l'album "+getName()+" de "+getSpec();
    }
}
