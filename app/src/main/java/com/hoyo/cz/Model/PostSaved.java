package com.hoyo.cz.Model;
public class PostSaved {
    private String psid;
    private String uid;
    private String pid;
    private boolean statusPS;

    public PostSaved() {
        // Default constructor required for calls to DataSnapshot.getValue(PostSaved.class)
    }

    public PostSaved(String psid, String uid, String pid, boolean statusPS) {
        this.psid = psid;
        this.uid = uid;
        this.pid = pid;
        this.statusPS = statusPS;
    }

    public String getPsid() {
        return psid;
    }

    public void setPsid(String psid) {
        this.psid = psid;
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

    public boolean isStatusPS() {
        return statusPS;
    }

    public void setStatusPS(boolean statusPS) {
        this.statusPS = statusPS;
    }
}
