package com.hoyo.cz.Model;
public class Follow {
    private String fid; // Key của Follow (Firebase sẽ tự sinh ra khi thêm vào database)
    private String uid; // ID của người dùng đang đăng nhập
    private String followingId; // ID của người được theo dõi
    private boolean statusF; // Trạng thái theo dõi (true: đang theo dõi, false: không theo dõi)
    private String dateFollow; // Ngày thực hiện theo dõi

    // Constructors
    public Follow() {
        // Default constructor required for calls to DataSnapshot.getValue(Follow.class)
    }

    public Follow(String fid, String uid, String followingId, boolean statusF, String dateFollow) {
        this.fid = fid;
        this.uid = uid;
        this.followingId = followingId;
        this.statusF = statusF;
        this.dateFollow = dateFollow;
    }

    // Getters and Setters
    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFollowingId() {
        return followingId;
    }

    public void setFollowingId(String followingId) {
        this.followingId = followingId;
    }

    public boolean isStatusF() {
        return statusF;
    }

    public void setStatusF(boolean statusF) {
        this.statusF = statusF;
    }

    public String getDateFollow() {
        return dateFollow;
    }

    public void setDateFollow(String dateFollow) {
        this.dateFollow = dateFollow;
    }
}
