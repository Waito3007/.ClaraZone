package com.hoyo.cz.Model;
import java.util.List;

public class Comment {
    private String cid;
    private String uid;
    private String pid;
    private String daycm;
    private String contentCm;
    private List<String> imageCmt;

    public Comment() {
    }

    public Comment(String cid, String uid, String pid, String daycm, String contentCm, List<String> imageCmt) {
        this.cid = cid;
        this.uid = uid;
        this.pid = pid;
        this.daycm = daycm;
        this.contentCm = contentCm;
        this.imageCmt = imageCmt;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
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

    public String getDaycm() {
        return daycm;
    }

    public void setDaycm(String daycm) {
        this.daycm = daycm;
    }

    public String getContentCm() {
        return contentCm;
    }
    public void setContentCm(String contentCm) {
        this.contentCm = contentCm;
    }
    public List<String> getImageCmt() {
        return imageCmt;
    }
    public void setImageCmt(List<String> imageCmt) {
        this.imageCmt = imageCmt;
    }
}
