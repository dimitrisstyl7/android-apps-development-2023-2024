package com.dimstyl.chatroom;

public class User {
    private String uid, email, nickname;

    public User() {
    }

    public User(String uid, String email, String nickname) {
        this.uid = uid;
        this.email = email;
        this.nickname = nickname;
    }

    String getUid() {
        return uid;
    }

    void setUid(String uid) {
        this.uid = uid;
    }

    String getEmail() {
        return email;
    }

    void setEmail(String email) {
        this.email = email;
    }

    String getNickname() {
        return nickname;
    }

    void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
