package com.example.sharehitv2.Model;

public class User {

    public String pseudo, userId;


    public User(){
    }

    public User(String pseudo) {
        this.pseudo = pseudo;
    }

    public User(String pseudo, String userId) {
        this.pseudo = pseudo;
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

