package com.feipai.flypai.beans.mavlinkbeans;

import com.Messages.MAVLinkMessage;
import com.common.msg_cal_progress_decode;
import com.common.msg_param_value;
import com.enums.MAV_DATA_STREAM;
import com.feipai.flypai.R;
import com.feipai.flypai.base.BaseEntity;
import com.feipai.flypai.base.BaseMavlinkEntity;
import com.feipai.flypai.utils.MLog;
import com.feipai.flypai.utils.global.LogUtils;

public class PlaneParamsBean extends BaseEntity implements BaseMavlinkEntity {

    private int msgId;
    private String paramId;
    private float paramValue;

    private int returnAlt;
    /**
     * 上升速度
     */
    private int polotSpeedUp;

    /**
     * 下降速度
     */
    private int polotSpeedDn;

    /**
     * 水平速度
     */
    private int wpnavLoitSpeed;


    /**
     * 距离最大围栏
     */
    private int fenceDistance;

    /**
     * 高度最大围栏
     */
    private int fenceMaxAlt;
    /**
     * 航点飞行速度
     */
    private int wayPointSpeed;
    /**
     * 安全模式已打开
     */
    private boolean isSafeModeOpen;


    @Override
    public void setMavlinkMessage(MAVLinkMessage msg) {
        msg_param_value msg_param_value = (msg_param_value) msg;
//        LogUtils.d("飞控速度数据===>" + msg_param_value.toString());
        setMsgId(msg_param_value.msgid);
        setParamValue(msg_param_value.param_value);
        setParamId(msg_param_value.getParam_Id());
    }

    @Override
    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public String getParamId() {
        return paramId;
    }

    public void setParamId(String paramId) {
        this.paramId = paramId;
        switch (paramId) {
            case MAV_DATA_STREAM.RTL_ALT:
                setReturnAlt((int) getParamValue() / 100);
                break;
            case MAV_DATA_STREAM.PILOT_SPEED_UP:
                setPolotSpeedUp((int) getParamValue() / 100);
                break;
            case MAV_DATA_STREAM.PILOT_SPEED_DN:
                setPolotSpeedDn((int) getParamValue() / 100);
                break;
            case MAV_DATA_STREAM.WPNAV_LOIT_SPEED:
                setWpnavLoitSpeed((int) getParamValue() / 100);
                break;
            case MAV_DATA_STREAM.FENCE_ALT_MAX:
                setFenceMaxAlt((int) getParamValue());
                break;
            case MAV_DATA_STREAM.FENCE_RADIUS:
                setFenceDistance((int) getParamValue());
                break;
            case MAV_DATA_STREAM.NEWCOMER_MODE:
                setSafeModeOpen(getParamValue() != 0);
                break;
            case MAV_DATA_STREAM.WPNAV_SPEED:
                /**航点飞行的速度*/
                setWayPointSpeed((int) getParamValue());
                break;

        }
    }


    public float getParamValue() {
        return paramValue;
    }

    public void setParamValue(float paramValue) {
        this.paramValue = paramValue;
    }

    public int getReturnAlt() {
        return returnAlt;
    }

    public void setReturnAlt(int returnAlt) {
        this.returnAlt = returnAlt;
    }

    public int getPolotSpeedUp() {
        return polotSpeedUp;
    }

    public void setPolotSpeedUp(int polotSpeedUp) {
        this.polotSpeedUp = polotSpeedUp;
    }

    public int getPolotSpeedDn() {
        return polotSpeedDn;
    }

    public void setPolotSpeedDn(int polotSpeedDn) {
        this.polotSpeedDn = polotSpeedDn;
    }

    public int getWpnavLoitSpeed() {
        return wpnavLoitSpeed;
    }

    public void setWpnavLoitSpeed(int wpnavLoitSpeed) {
        this.wpnavLoitSpeed = wpnavLoitSpeed;
    }

    public int getFenceDistance() {
        return fenceDistance;
    }

    public void setFenceDistance(int fenceDistance) {
        this.fenceDistance = fenceDistance;
    }

    public int getFenceMaxAlt() {
        return fenceMaxAlt;
    }

    public void setFenceMaxAlt(int fenceMaxAlt) {
        this.fenceMaxAlt = fenceMaxAlt;
    }

    public int getWayPointSpeed() {
        return wayPointSpeed;
    }

    public void setWayPointSpeed(int wayPointSpeed) {
        this.wayPointSpeed = wayPointSpeed;
    }

    public boolean isSafeModeOpen() {
        return isSafeModeOpen;
    }

    public void setSafeModeOpen(boolean safeModeOpen) {
        isSafeModeOpen = safeModeOpen;
    }

    @Override
    public String toString() {
        return "PlaneParamsBean{" +
                "msgId=" + msgId +
                ", paramId='" + paramId + '\'' +
                ", paramValue=" + paramValue +
                ", returnAlt=" + returnAlt +
                ", polotSpeedUp=" + polotSpeedUp +
                ", polotSpeedDn=" + polotSpeedDn +
                ", wpnavLoitSpeed=" + wpnavLoitSpeed +
                ", fenceDistance=" + fenceDistance +
                ", fenceMaxAlt=" + fenceMaxAlt +
                ", wayPointSpeed=" + wayPointSpeed +
                ", isSafeModeOpen=" + isSafeModeOpen +
                '}';
    }
}
