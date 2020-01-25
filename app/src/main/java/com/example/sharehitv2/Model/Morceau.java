package com.example.sharehitv2.Model;

public class Morceau extends Type {

    private String songUrl;
    private String album;

    public Morceau(String name, String album, String imgUrl, String songUrl, String id, String artiste){
        super(name, imgUrl, artiste, id);
        this.songUrl = songUrl;
        this.album = album;
        setCONST_NOMMAGE("Artiste: ");
    }

    public String getSongUrl() {
        return songUrl;
    }

    public String getAlbum() {
        return album;
    }

    public String toString(){
        return "le morceau "+getName()+" de "+getSpec();
    }
}
