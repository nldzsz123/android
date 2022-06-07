package com.feipai.flypai.beans;

import com.feipai.flypai.base.BaseEntity;

public class YunApiImgBean extends BaseEntity {
    private String imgsrc;
    private String imgname;

    public YunApiImgBean(String imgsrc, String imgname) {
        this.imgsrc = imgsrc;
        this.imgname = imgname;
    }

    public String getImgname() {
        return imgname;
    }

    public void setImgname(String imgname) {
        this.imgname = imgname;
    }

    public String getImgsrc() {
        return imgsrc;
    }

    public void setImgsrc(String imgsrc) {
        this.imgsrc = imgsrc;
    }

    @Override
    public String toString() {
        return "{" +
                "imgname='" + imgname + '\'' +
                "imgsrc='" + imgsrc + '\'' +
                '}';
    }
}
