package com.example.thinkanddo.models;

public class ModelDefineStep {

    String gId, gTitle, gDescr, uid, uEmail, uDp, uName,gTime,dsTitle,dsId,dsTime;

    public ModelDefineStep() {

    }

    public ModelDefineStep(String gId, String gTitle, String gDescr, String uid, String uEmail, String uDp, String uName, String gTime, String dsTitle, String dsId, String dsTime) {
        this.gId = gId;
        this.gTitle = gTitle;
        this.gDescr = gDescr;
        this.uid = uid;
        this.uEmail = uEmail;
        this.uDp = uDp;
        this.uName = uName;
        this.gTime = gTime;
        this.dsTitle = dsTitle;
        this.dsId = dsId;
        this.dsTime = dsTime;
    }

    public String getDsTitle() {
        return dsTitle;
    }

    public void setDsTitle(String dsTitle) {
        this.dsTitle = dsTitle;
    }

    public String getDsId() {
        return dsId;
    }

    public void setDsId(String dsId) {
        this.dsId = dsId;
    }

    public String getDsTime() {
        return dsTime;
    }

    public void setDsTime(String dsTime) {
        this.dsTime = dsTime;
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
