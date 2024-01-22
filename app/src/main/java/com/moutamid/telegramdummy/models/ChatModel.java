package com.moutamid.telegramdummy.models;

public class ChatModel {
    String id, userID, name, image, lastMessage;
    long timestamp;
    boolean isSeen;

    public ChatModel() {
    }

    public ChatModel(String id, String userID, String name, String image, String lastMessage, long timestamp, boolean isSeen) {
        this.id = id;
        this.userID = userID;
        this.name = name;
        this.image = image;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.isSeen = isSeen;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
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
