package com.hoyo.cz.Model;

public class Like {
    private String likeId;
    private String pid;
    private String uid;
    private String timestamp;
    private boolean status;

    public Like() {
        // Firebase cần constructor mặc định
    }

    public Like(String likeId, String pid, String uid, String timestamp, boolean status) {
        this.likeId = likeId;
        this.pid = pid;
        this.uid = uid;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters và Setters
    public String getLikeId() {
        return likeId;
    }

    public void setLikeId(String likeId) {
        this.likeId = likeId;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
