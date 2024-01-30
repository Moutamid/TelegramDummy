package com.moutamid.telegramdummy.models;

public class MessageModel {
    String id, senderID, message, image;
    long timestamp;
    boolean isMedia, isDate;

    public MessageModel() {
    }

    public MessageModel(String id, String senderID, String message, String image, long timestamp, boolean isMedia, boolean isDate) {
        this.id = id;
        this.senderID = senderID;
        this.message = message;
        this.image = image;
        this.timestamp = timestamp;
        this.isMedia = isMedia;
        this.isDate = isDate;
    }

    public boolean isDate() {
        return isDate;
    }

    public void setDate(boolean date) {
        isDate = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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
