package com.example.andrew.doctorschatsystem;

public class ImageModel {

    private String imgDate ;
    private String imgLink ;
    private String form ;

    public ImageModel() { }

    public ImageModel(String imgDate, String imgLink, String form) {
        this.imgDate = imgDate;
        this.imgLink = imgLink;
        this.form = form;
    }

    public String getImgDate() {
        return imgDate;
    }

    public void setImgDate(String imgDate) {
        this.imgDate = imgDate;
    }

    public String getImgLink() {
        return imgLink;
    }

    public void setImgLink(String imgLink) {
        this.imgLink = imgLink;
    }

    public String getForm() { return form; }

    public void setForm(String form) { this.form = form; }
}
