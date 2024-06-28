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
    public String toString() {
        return nameUser; // Trả về tên người dùng khi gọi toString()
    }
    public String userId() {
        return nameUser;
    }
    public Account(String userId, String email) {
        this.uid = userId;
        this.email = email;
        this.password= password;

    }
    public String getUserId() {
        return uid;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
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

    //phanquyen
    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}

