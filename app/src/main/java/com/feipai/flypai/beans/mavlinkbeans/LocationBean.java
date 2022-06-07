package com.feipai.flypai.beans.mavlinkbeans;

import android.view.View;

import com.Messages.MAVLinkMessage;
import com.amap.api.maps.model.LatLng;
import com.common.msg_location;
import com.enums.MAV_CMD;
import com.feipai.flypai.base.BaseEntity;
import com.feipai.flypai.base.BaseMavlinkEntity;
import com.feipai.flypai.beans.LocationMode;
import com.feipai.flypai.ui.view.mapbox.MapboxLatLng;
import com.feipai.flypai.utils.global.JsonUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.RegexUtils;
import com.feipai.flypai.utils.gsonlib.MGson;

public class LocationBean extends BaseEntity implements BaseMavlinkEntity {

    private int msgId;
    private String locationJason;

    private int type;
    private boolean isCanStartFly;
    /***/
    private double locaLt;
    private double locaLg;
    private LatLng locaLtlg;
    private MapboxLatLng mapBoxLocaLtlg;

    private float planeYaw;

    private double homeLt;
    private double homeLg;

    private double pitch;

    /**
     * 前臂灯
     */
    private boolean isLedCtrlOpened;

    /**
     * 后臂灯
     */
    private boolean isLedCtrlHouOpened;

    /**
     * 水平速度(用于功能调节的水平速度)
     */
    private float hv;
    /**
     * 升降速度
     */
    private float cv;

    /**
     * 环绕半径
     */
    private float cRadius = 0;

    /**
     * 机头是否正向
     */
    private boolean isPleneHeaderPositive;

    /**
     * 剩余时间
     */
    private int remainTime;

    @Override
    public void setMavlinkMessage(MAVLinkMessage msg) {
        msg_location msg_location = (msg_location) msg;
        setMsgId(msg_location.msgid);
        setLocationJason(msg_location.getText());
    }

    @Override
    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public String getLocationJason() {
        return locationJason;
    }

    public void setLocationJason(String locationJason) {
        this.locationJason = locationJason;
        if (JsonUtils.isGoodJson(locationJason)) {
            LocationMode locationMode = MGson.newGson().fromJson(locationJason, LocationMode.class);
            if (RegexUtils.canParseInt(locationMode.getType())) {
                setType(Integer.parseInt(locationMode.getType()));
                switch (type) {
                    case MAV_CMD.TYPE_TAKE_PHOTO:
                        //遥控拍照操作{“type”:”1001”,“value”:”1”}此时value没有意义
                        break;
                    case MAV_CMD.TYPE_TAKE_OFF:
                        //一键起飞{“type”:”1006”,“value”:”1”}value:1 飞机送过来准备起飞 2 app告诉飞机可以起飞
                        setCanStartFly(locationMode.getValue().equals("1"));
                        break;
                    case MAV_CMD.TYPE_CURRENT_POSITION:
                        //当前位置{“type”:1010,“lt”:123.456789,“lg”:12.345678,“alt”:100,//单位m}
                        setLocaLt(locationMode.getLt());
                        setLocaLg(locationMode.getLg());
                        if (locaLt >= -90 && locaLt <= 90) {
                            setLocaLtlg(new LatLng(locaLt, locaLg));
                            LogUtils.d("坐标值=====>" + locaLt + "||" + locaLg);
                            setMapboxLocaLtlg(new MapboxLatLng(locaLt, locaLg));
                        }
                        break;
                    case MAV_CMD.TYPE_AUTO_TURNBACK:
                        //自动返航（姿态）说明：{“type”:1011,“roll”:10,//单位度“pitch”:10//单位度“yaw”:360//单位度}
                        setPlaneYaw((locationMode.getYaw() / 10f));
//                        LogUtils.d("云台角度===>" + locationMode.getGm_pitch());
                        setPitch(locationMode.getGm_pitch());
                        break;
                    case MAV_CMD.TYPE_HOME_POSITION:
                        //家的位置{“type”:1012,“rw”:0,//0读取家位置，1设置家位置“lat”:123.456789,,“lng”:12.345678,}
                        /**坐标系需要转换*/
                        setHomeLt(Double.parseDouble(locationMode.getLat()));
                        setHomeLg(locationMode.getLng());
                        break;
                    case MAV_CMD.TYPE_LED_CTRL:
//                    {“type”:1015,“led_ctrl”:0//0关闭，1开启}
                        setLedCtrlOpened(locationMode.getLed_ctrl() == 1);
                        break;
                    case MAV_CMD.TYPE_LED_CTRL_HOU:
//                    {“type”:1015,“led_ctrl”:0//0关闭，1开启}
                        setLedCtrlHouOpened(locationMode.getLed_ctrl() == 1);
                        break;
                    case MAV_CMD.TYPE_PLANE_SPEED:
//                        MLog.log("水平距离和速度=" + msg_location.toString());
                        //水平速度
                        setHv(locationMode.getHv() / 100f);
                        //升降速度
                        setCv(locationMode.getCv() / 100f);
                        break;
                    case MAV_CMD.TYPE_PLANE_HEAD:
                        //{“type”:1014,“head_turn”:0//0代表机头正常，1代表切换机头}
//                        MLog.log("飞机机头方向--->" + loacationMode.getHead_turn());
                        setPleneHeaderPositive(locationMode.getHead_turn() == 0);
                        break;
                    case MAV_CMD.TYPE_AROUND_RADIUS:
                        //{“type”:1017,“lt”:123.456789,“lg”:12.345678,“rd”:500,/* 单位cm，500cm为5m */“spd”:500,/* 单位cm，500cm为5m */}
                        LogUtils.d("环绕半径和速度--->" + locationMode.getRd() + "||" + locationMode.getSpd());
                        setcRadius(locationMode.getRd());
//                        if (aroundMarker != null && planeMarker != null) {
//                            drawCircle(aroundMarker.getPosition(), planeMarker.getPosition(), loacationMode.getRd() / 100);
//                            aroundMarker = moveMarker(0, aroundMarker, new LatLng(loacationMode.getLt(), loacationMode.getLg())
//                                    /*GPSUtils.gcj2WGSExactly(planeMarker.getPosition().latitude, planeMarker.getPosition().longitude)*/);
//                        }
                        break;
                    case MAV_CMD.TYPE_REMAINE_MIN:
                        setRemainTime(locationMode.getRemain_min());
//                            MLog.log("剩余飞行时长---->" + remainMin);
                        break;
                }
            }
        }
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isCanStartFly() {
        return isCanStartFly;
    }

    public void setCanStartFly(boolean canStartFly) {
        isCanStartFly = canStartFly;
    }

    public double getLocaLt() {
        return locaLt;
    }

    public void setLocaLt(double locaLt) {
        this.locaLt = locaLt;
    }

    public double getLocaLg() {
        return locaLg;
    }

    public void setLocaLg(double locaLg) {
        this.locaLg = locaLg;
    }

    public LatLng getLocaLtlg() {
        return locaLtlg;
    }

    public void setLocaLtlg(LatLng locaLtlg) {
        this.locaLtlg = locaLtlg;
    }

    public MapboxLatLng getMapboxLocaLtlg() {
        return mapBoxLocaLtlg;
    }

    public void setMapboxLocaLtlg(MapboxLatLng locaLtlg) {
        this.mapBoxLocaLtlg = locaLtlg;
    }

    public float getPlaneYaw() {
        return planeYaw;
    }

    public void setPlaneYaw(float planeYaw) {
        this.planeYaw = planeYaw;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public double getPitch() {
        return pitch;
    }

    public double getHomeLt() {
        return homeLt;
    }

    public void setHomeLt(double homeLt) {
        this.homeLt = homeLt;
    }

    public double getHomeLg() {
        return homeLg;
    }

    public void setHomeLg(double homeLg) {
        this.homeLg = homeLg;
    }

    public boolean isLedCtrlOpened() {
        return isLedCtrlOpened;
    }

    public boolean isLedCtrlHouOpened() {
        return isLedCtrlHouOpened;
    }

    public void setLedCtrlOpened(boolean ledCtrlOpened) {
        isLedCtrlOpened = ledCtrlOpened;
    }

    public void setLedCtrlHouOpened(boolean ledCtrlHouOpened) {
        isLedCtrlHouOpened = ledCtrlHouOpened;
    }

    public float getHv() {
        return hv;
    }

    public void setHv(float hv) {
        this.hv = hv;
    }

    public float getCv() {
        return cv;
    }

    public void setCv(float cv) {
        this.cv = cv;
    }

    public float getcRadius() {
        return cRadius;
    }

    public void setcRadius(float cRadius) {
        LogUtils.d("value11 " + cRadius);
        this.cRadius = cRadius;
    }

    public boolean isPleneHeaderPositive() {
        return isPleneHeaderPositive;
    }

    public void setPleneHeaderPositive(boolean pleneHeaderPositive) {
        isPleneHeaderPositive = pleneHeaderPositive;
    }

    public int getRemainTime() {
        return remainTime;
    }

    public void setRemainTime(int remainTime) {
        this.remainTime = remainTime;
    }


    @Override
    public String toString() {
        return "LocationBean{" +
                "msgId=" + msgId +
                ", locationJason='" + locationJason + '\'' +
                ", type=" + type +
                ", isCanStartFly=" + isCanStartFly +
                ", locaLt=" + locaLt +
                ", locaLg=" + locaLg +
                ", locaLtlg=" + locaLtlg +
                ", planeYaw=" + planeYaw +
                ", homeLt=" + homeLt +
                ", homeLg=" + homeLg +
                ", isLedCtrlOpened=" + isLedCtrlOpened +
                ", hv=" + hv +
                ", cv=" + cv +
                ", cRadius=" + cRadius +
                ", isPleneHeaderPositive=" + isPleneHeaderPositive +
                ", remainTime=" + remainTime +
                '}';
    }
}
