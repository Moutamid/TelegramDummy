package com.moutamid.telegramdummy.models;

public class ChatModel {
    String id, name, image, lastMessage;
    int code;
    long timestamp;
    String status;
    int color;

    public ChatModel() {
    }

    public ChatModel(String id, int code, String name, String image, String lastMessage, long timestamp, String status, int color) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.image = image;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.status = status;
        this.color = color;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
