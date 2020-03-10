package com.example.thinkanddo;

public class ScreenItem {

    String title,decription;
    int Screenimg;

    public ScreenItem(String title, String decription, int screenimg) {
        this.title = title;
        this.decription = decription;
        Screenimg = screenimg;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDecription() {
        return decription;
    }

    public void setDecription(String decription) {
        this.decription = decription;
    }

    public int getScreenimg() {
        return Screenimg;
    }

    public void setScreenimg(int screenimg) {
        Screenimg = screenimg;
    }
}
