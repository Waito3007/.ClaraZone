package com.hoyo.cz.Model;

public class Account {
    private String uid;
    private String email;
    private String password;
    private String avatarUser;
    private String nameUser;
    private boolean isAdmin; // phan quyen

    public Account() {
        // Cần phải có constructor không tham số để Firebase có thể chuyển đổi dữ liệu từ database
    }

    public Account(String uid, String email) {
        this.uid = uid;
        this.email = email;
    }

    public Account(String uid, String email, String password, String avatarUser, String nameUser, boolean isAdmin) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.avatarUser = avatarUser;
        this.nameUser = nameUser;
        this.isAdmin = isAdmin;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatarUser() {
        return avatarUser;
    }

    public void setAvatarUser(String avatarUser) {
        this.avatarUser = avatarUser;
    }

    public String getNameUser() {
        return nameUser;
    }

    public void setNameUser(String nameUser) {
        this.nameUser = nameUser;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
