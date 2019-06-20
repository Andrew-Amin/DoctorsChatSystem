package com.example.andrew.doctorschatsystem;

public class Contacts {
    private String uName , uStatus , image ;

    public Contacts(){}

    public Contacts(String uName, String uStatus, String image) {
        this.uName = uName;
        this.uStatus = uStatus;
        this.image = image;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getuStatus() {
        return uStatus;
    }

    public void setuStatus(String uStatus) {
        this.uStatus = uStatus;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
