package com.feipai.flypai.beans;

import com.feipai.flypai.base.BaseEntity;

public class RequestVersionBean extends BaseEntity {
    private String drone = "";
    private String camera = "";
    private String yuntai = "";
    private String android_app = "";

    public String getDrone() {
        return drone;
    }

    public void setDrone(String drone) {
        this.drone = drone;
    }

    public String getCamera() {
        return camera;
    }

    public void setCamera(String camera) {
        this.camera = camera;
    }

    public String getYuntai() {
        return yuntai;
    }

    public void setYuntai(String yuntai) {
        this.yuntai = yuntai;
    }

    public String getApp() {
        return android_app;
    }

    public void setApp(String android_app) {
        this.android_app = android_app;
    }


    @Override
    public String toString() {
        return "RequestVersionBean{" +
                "drone='" + drone + '\'' +
                ", camera='" + camera + '\'' +
                ", yuntai='" + yuntai + '\'' +
                ", android_app='" + android_app + '\'' +
                '}';
    }
}
