package com.hoyo.cz.Model;

import java.util.HashMap;
import java.util.Map;

public class Post {
    private String Pid;
    private String Uid;
    private String TitleP;
    private String ContentP;
    private String DayupP;
    private boolean StatusP;
    private int LikeP;
    private String MediaUrl;

    // Constructor
    public Post(String Pid, String Uid, String TitleP, String ContentP, String DayupP, boolean StatusP, int LikeP, String MediaUrl) {
        this.Pid = Pid;
        this.Uid = Uid;
        this.TitleP = TitleP;
        this.ContentP = ContentP;
        this.DayupP = DayupP;
        this.StatusP = StatusP;
        this.LikeP = LikeP;
        this.MediaUrl = MediaUrl;
    }

    // Method to convert Post object to a map for Firebase Database
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("Pid", Pid);
        result.put("Uid", Uid);
        result.put("TitleP", TitleP);
        result.put("ContentP", ContentP);
        result.put("DayupP", DayupP);
        result.put("StatusP", StatusP);
        result.put("LikeP", LikeP);
        result.put("MediaUrl", MediaUrl);

        return result;
    }

    // Getters and setters (if needed)
    public String getPid() {
        return Pid;
    }

    public void setPid(String Pid) {
        this.Pid = Pid;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String Uid) {
        this.Uid = Uid;
    }

    public String getTitleP() {
        return TitleP;
    }

    public void setTitleP(String TitleP) {
        this.TitleP = TitleP;
    }

    public String getContentP() {
        return ContentP;
    }

    public void setContentP(String ContentP) {
        this.ContentP = ContentP;
    }

    public String getDayupP() {
        return DayupP;
    }

    public void setDayupP(String DayupP) {
        this.DayupP = DayupP;
    }

    public boolean isStatusP() {
        return StatusP;
    }

    public void setStatusP(boolean StatusP) {
        this.StatusP = StatusP;
    }

    public int getLikeP() {
        return LikeP;
    }

    public void setLikeP(int LikeP) {
        this.LikeP = LikeP;
    }

    public String getMediaUrl() {
        return MediaUrl;
    }

    public void setMediaUrl(String MediaUrl) {
        this.MediaUrl = MediaUrl;
    }
}
