package com.hoyo.cz.Model;

public class Like {
    private String postId;
    private String userId;
    private String likedAt;
    private boolean status; // Thêm thuộc tính để xác định trạng thái like

    // Constructor mặc định (cần thiết cho Firebase)
    public Like() {}

    // Constructor với các tham số
    public Like(String postId, String userId, String likedAt, boolean status) {
        this.postId = postId;
        this.userId = userId;
        this.likedAt = likedAt;
        this.status = status;
    }

    // Getters và Setters
    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLikedAt() {
        return likedAt;
    }

    public void setLikedAt(String likedAt) {
        this.likedAt = likedAt;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
