package com.example.conectamobile.models;

public class Message {
    public String id;
    public String senderId;
    public String receiverId;
    public String text;
    public long timestamp;

    public Message() {}

    public Message(String id, String senderId, String receiverId, String text, long timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.timestamp = timestamp;
    }
}
