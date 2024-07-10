package com.dimstyl.chatroom;

public class Message {
    private final String senderUid, receiverUid, senderNickname, text, timestamp;

    public Message() {
        this.senderUid = null;
        this.receiverUid = null;
        this.senderNickname = null;
        this.text = null;
        this.timestamp = null;
    }

    public Message(String senderUid, String receiverUid, String text, String timestamp) {
        this.senderUid = senderUid;
        this.receiverUid = receiverUid;
        this.senderNickname = null;
        this.text = text;
        this.timestamp = timestamp;
    }

    public Message(String senderUid, String receiverUid, String senderNickname, String text, String timestamp) {
        this.senderUid = senderUid;
        this.receiverUid = receiverUid;
        this.senderNickname = senderNickname;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public String getReceiverUid() {
        return receiverUid;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public String getText() {
        return text;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
