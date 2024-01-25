package com.moutamid.telegramdummy.models;

public class UserModel {
    String ID, name, number, image;
    int color;
    public UserModel() {
    }

    public UserModel(String ID, String name, String number, String image, int color) {
        this.ID = ID;
        this.name = name;
        this.number = number;
        this.image = image;
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
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
