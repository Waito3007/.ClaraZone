package com.hoyo.cz.Model;

public class Post {
    private String pid;
    private String uid;
    private String titleP;
    private String contentP;
    private String dayupP;
    private boolean statusP;
    private int likeP;
    private String mediaUrl; // Thêm thuộc tính để lưu URL của phương tiện


    // Constructor mặc định (cần thiết cho Firebase)
    public Post() {}

    // Constructor với các tham số khác
    public Post(String pid, String uid, String titleP, String contentP, String dayupP, boolean statusP, int likeP) {
        this.pid = pid;
        this.uid = uid;
        this.titleP = titleP;
        this.contentP = contentP;
        this.dayupP = dayupP;
        this.statusP = statusP;
        this.likeP = likeP;
    }

    // Getters và Setters
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

    public String getTitleP() {
        return titleP;
    }

    public void setTitleP(String titleP) {
        this.titleP = titleP;
    }

    public String getContentP() {
        return contentP;
    }

    public void setContentP(String contentP) {
        this.contentP = contentP;
    }

    public String getDayupP() {
        return dayupP;
    }

    public void setDayupP(String dayupP) {
        this.dayupP = dayupP;
    }

    public boolean isStatusP() {
        return statusP;
    }

    public void setStatusP(boolean statusP) {
        this.statusP = statusP;
    }

    public int getLikeP() {
        return likeP;
    }

    public void setLikeP(int likeP) {
        this.likeP = likeP;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }
    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }
}
