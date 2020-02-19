package com.example.thinkanddo.notifications;

public class Data {
    private String user,body, titile, sent;
    private Integer icon;

    public Data(){

    }

    public Data(String user, String body, String titile, String sent, Integer icon) {
        this.user = user;
        this.body = body;
        this.titile = titile;
        this.sent = sent;
        this.icon = icon;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitile() {
        return titile;
    }

    public void setTitile(String titile) {
        this.titile = titile;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public Integer getIcon() {
        return icon;
    }

    public void setIcon(Integer icon) {
        this.icon = icon;
    }
}
