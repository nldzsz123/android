package com.feipai.flypai.beans;


import com.feipai.flypai.base.BaseEntity;

import java.io.Serializable;

/**
 * Created by YangLin on 2018-01-31.
 */

public class LocationMode extends BaseEntity {
    /**
     * type : 1001
     * value : 1
     * total : 0
     * lt : 123.456789
     * lg : 12.345678
     * alt : 100
     * roll : 10
     * pitch : 10
     * yaw : 360
     * index : 1
     * lat : 42157991
     * long : 276858901
     * rw : 0
     * lng : 12.345678
     * hv : 1000
     * cv : 1000
     * dis : 1000
     * led_ctrl：1
     * head_turn:0
     * hw_v:33554433
     * fw_v:50331649
     * remain_min:1-5
     */

    private String type;
    private String value;
    private String total;
    private double lt;
    private double lg;
    private double alt;
    private double roll;
    private double pitch;
    private int yaw;
    private double gm_pitch;
    private String index;
    private String lat;
    private int rw;
    private double lng;
    private int hv;
    private int cv;
    private int dis;
    private int led_ctrl;
    private int head_turn;
    private int rd;
    private int spd;
    private long hw_v;
    private long fw_v;
    private int remain_min;

    public int getRd() {
        return rd;
    }

    public void setRd(int rd) {
        this.rd = rd;
    }

    public int getSpd() {
        return spd;
    }

    public void setSpd(int spd) {
        this.spd = spd;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public double getLt() {
        return lt;
    }

    public void setLt(double lt) {
        this.lt = lt;
    }

    public double getLg() {
        return lg;
    }

    public void setLg(double lg) {
        this.lg = lg;
    }

    public double getAlt() {
        return alt;
    }

    public void setAlt(double alt) {
        this.alt = alt;
    }

    public double getRoll() {
        return roll;
    }

    public void setRoll(double roll) {
        this.roll = roll;
    }

    public double getPitch() {
        return gm_pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public int getYaw() {
        return yaw;
    }

    public void setYaw(int yaw) {
        this.yaw = yaw;
    }

    public double getGm_pitch() {
        return gm_pitch;
    }

    public void setGm_pitch(double gm_pitch) {
        this.gm_pitch = gm_pitch;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public int getRw() {
        return rw;
    }

    public void setRw(int rw) {
        this.rw = rw;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public int getHv() {
        return hv;
    }

    public void setHv(int hv) {
        this.hv = hv;
    }

    public int getCv() {
        return cv;
    }

    public void setCv(int cv) {
        this.cv = cv;
    }

    public int getDis() {
        return dis;
    }

    public void setDis(int dis) {
        this.dis = dis;
    }

    public int getLed_ctrl() {
        return led_ctrl;
    }

    public void setLed_ctrl(int led_ctrl) {
        this.led_ctrl = led_ctrl;
    }

    public int getHead_turn() {
        return head_turn;
    }

    public void setHead_turn(int head_turn) {
        this.head_turn = head_turn;
    }

    public long getHw_v() {
        return hw_v;
    }

    public void setHw_v(long hw_v) {
        this.hw_v = hw_v;
    }

    public long getFw_v() {
        return fw_v;
    }

    public void setFw_v(long fw_v) {
        this.fw_v = fw_v;
    }

    public int getRemain_min() {
        return remain_min;
    }

    public void setRemain_min(int remain_min) {
        this.remain_min = remain_min;
    }

    @Override
    public String toString() {
        return "LoacationMode{" +
                "type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", total='" + total + '\'' +
                ", lt=" + lt +
                ", lg=" + lg +
                ", alt=" + alt +
                ", roll=" + roll +
                ", pitch=" + pitch +
                ", yaw=" + yaw +
                ", index='" + index + '\'' +
                ", lat='" + lat + '\'' +
                ", rw=" + rw +
                ", lng=" + lng +
                ", hv=" + hv +
                ", cv=" + cv +
                ", dis=" + dis +
                ", led_ctrl=" + led_ctrl +
                ",head_turn" + head_turn +
                ",rd" + rd +
                ",spd" + spd +
                ",hw_v" + hw_v +
                ",fw_v" + fw_v +
                ",remain_min" + remain_min +
                '}';
    }
}
