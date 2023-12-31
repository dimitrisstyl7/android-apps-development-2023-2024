package com.dimstyl.chatroom;

public class Message {
    private final String senderUid;
    private final String receiverUid;
    private final String text;
    private final String timestamp;

    public Message(String senderUid, String receiverUid, String text, String timestamp) {
        this.senderUid = senderUid;
        this.receiverUid = receiverUid;
        this.text = text;
        this.timestamp = timestamp;
    }

    public Message(String senderUid, String receiverUid, String text) {
        this.senderUid = senderUid;
        this.receiverUid = receiverUid;
        this.text = text;
        this.timestamp = null;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public String getReceiverUid() {
        return receiverUid;
    }

    public String getText() {
        return text;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
