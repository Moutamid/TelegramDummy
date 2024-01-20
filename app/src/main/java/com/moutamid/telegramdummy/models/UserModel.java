package com.moutamid.telegramdummy.models;

public class UserModel {
    String ID, username, name, number, image;

    public UserModel() {
    }

    public UserModel(String ID, String username, String name, String number, String image) {
        this.ID = ID;
        this.username = username;
        this.name = name;
        this.number = number;
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
