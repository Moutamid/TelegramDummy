package com.moutamid.telegramdummy.models;

public class MessageModel {
    String id, senderID, message;
    long timestamp;
    boolean isMedia;

    public MessageModel() {
    }

    public MessageModel(String id, String senderID, String message, long timestamp, boolean isMedia) {
        this.id = id;
        this.senderID = senderID;
        this.message = message;
        this.timestamp = timestamp;
        this.isMedia = isMedia;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isMedia() {
        return isMedia;
    }

    public void setMedia(boolean media) {
        isMedia = media;
    }
}
