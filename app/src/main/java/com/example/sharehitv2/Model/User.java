package com.example.sharehitv2.Model;

public class User {

    public String pseudo,pseudo_lower, userId;


    public User(){
    }

    public User(String pseudo, String pseudo_lower) {
        this.pseudo = pseudo;
        this.pseudo_lower = pseudo_lower;
    }

    public User(String pseudo, String pseudo_lower, String userId) {
        this.pseudo = pseudo;
        this.pseudo_lower = pseudo_lower;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

}

