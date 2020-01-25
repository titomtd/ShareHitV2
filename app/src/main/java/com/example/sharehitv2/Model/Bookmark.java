package com.example.sharehitv2.Model;

public class Bookmark {

    private String keyBookmark;
    private Recommandation recommandation;

    public Bookmark(String keyBookmark, Recommandation model) {
        this.keyBookmark = keyBookmark;
        this.recommandation = model;
    }

    public String getKeyBookmark() {
        return keyBookmark;
    }

    public void setKeyBookmark(String keyBookmark) {
        this.keyBookmark = keyBookmark;
    }

    public Recommandation getRecommandation() {
        return recommandation;
    }

    public void setRecommandation(Recommandation recommandation) {
        this.recommandation = recommandation;
    }
}
