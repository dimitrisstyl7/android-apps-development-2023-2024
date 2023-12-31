package com.dimstyl.chatroom;

public class User {
    private final String uid, email, nickname;

    public User(String uid, String email, String nickname) {
        this.uid = uid;
        this.email = email;
        this.nickname = nickname;
    }

    String getUid() {
        return uid;
    }

    String getEmail() {
        return email;
    }

    String getNickname() {
        return nickname;
    }
}
