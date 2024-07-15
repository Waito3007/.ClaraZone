package com.hoyo.cz.Model;

public class Share {
    private String sid;
    private String timestamp;
    private String uid;
    private String pid;

    public Share() {
        // Constructor mặc định cho Firebase
    }

    public Share(String sid, String timestamp, String uid, String pid) {
        this.sid = sid;
        this.timestamp = timestamp;
        this.uid = uid;
        this.pid = pid;
    }

    // Getters và Setters
    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }
}
