package com.example.sharehitv2.Model;

public class Type {

    private String CONST_NOMMAGE;
    private String name;
    private String imgUrl;
    private String spec;
    private String id;
    private String songUrl;

    public Type(String name, String imgUrl, String spec, String id){
        this.imgUrl=imgUrl;
        this.name=name;
        this.spec=spec;
        this.id=id;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

    public String getLink() {
        return id;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getConstNommage() {
        return CONST_NOMMAGE;
    }

    public void setCONST_NOMMAGE(String CONST_NOMMAGE) {
        this.CONST_NOMMAGE = CONST_NOMMAGE;
    }

    public String getSpec() {
        return spec;
    }

    public String getName() {
        return name;
    }

    public String toString(){
        return "";
    }
}
