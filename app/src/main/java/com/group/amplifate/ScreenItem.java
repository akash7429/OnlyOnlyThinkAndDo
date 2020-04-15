package com.group.amplifate;

public class ScreenItem {

    String title, Description;
    int Screenimg;

    public ScreenItem(String title, String description, int screenimg) {
        this.title = title;
        this.Description = description;
        Screenimg = screenimg;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        this.Description = description;
    }

    public int getScreenimg() {
        return Screenimg;
    }

    public void setScreenimg(int screenimg) {
        Screenimg = screenimg;
    }
}
