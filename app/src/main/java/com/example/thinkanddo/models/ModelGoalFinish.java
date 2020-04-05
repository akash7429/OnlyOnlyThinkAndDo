package com.example.thinkanddo.models;

public class ModelGoalFinish {

    String gId, gTitle, gDescr, uid, uEmail, uDp, uName,gTime;

    public ModelGoalFinish() {

    }

    public ModelGoalFinish(String gId, String gTitle, String gDescr, String uid, String uEmail, String uDp, String uName, String gTime) {
        this.gId = gId;
        this.gTitle = gTitle;
        this.gDescr = gDescr;
        this.uid = uid;
        this.uEmail = uEmail;
        this.uDp = uDp;
        this.gTime = gTime;
        this.uName = uName;
    }

    public String getgTime() {
        return gTime;
    }

    public void setgTime(String gTime) {
        this.gTime = gTime;
    }

    public String getgId() {
        return gId;
    }

    public void setgId(String gId) {
        this.gId = gId;
    }

    public String getgTitle() {
        return gTitle;
    }

    public void setgTitle(String gTitle) {
        this.gTitle = gTitle;
    }

    public String getgDescr() {
        return gDescr;
    }

    public void setgDescr(String gDescr) {
        this.gDescr = gDescr;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuDp() {
        return uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

}
