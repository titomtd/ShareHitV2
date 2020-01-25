package com.example.sharehitv2.Model;

public class Comment {


    public String com;
    public long timestamp;
    public String uid;

    public Comment(){

    }

    public Comment(String mCom, long mTimestamp, String mUid) {
        this.com = mCom;
        this.timestamp = mTimestamp;
        this.uid = mUid;
    }

    public String getCom() {
        return com;
    }

    public void setCom(String com) {
        this.com = com;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
