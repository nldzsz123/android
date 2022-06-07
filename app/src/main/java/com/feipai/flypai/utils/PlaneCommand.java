package com.feipai.flypai.utils;

import com.Messages.ApmModes;
import com.Messages.MAVLinkMessage;
import com.amap.api.maps.model.LatLng;
import com.enums.MAV_DATA_STREAM;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.socket.SocketManager;

import static com.feipai.flypai.utils.MavlinkRequestMessage.requestMavlinkPlaneParam;
import static com.feipai.flypai.utils.MavlinkRequestMessage.requestMavlinkVersion;


public class PlaneCommand {
    private static PlaneCommand mCommand;

    public static PlaneCommand getInstance() {
        if (mCommand == null) {
            synchronized (PlaneCommand.class) {
                if (mCommand == null) {
                    mCommand = new PlaneCommand();
                }
            }
        }
        return mCommand;
    }


    /**
     * 发送mav消息
     */
    public void sendMavlinkCommand(MAVLinkMessage mavMsg) {
        if (mavMsg == null)
            return;
        SocketManager.getCmdInstance().sendMavlinkCommandData(mavMsg);
    }

    /***请求飞控版本号*/
    public void requestPlaneVersion() {
        sendMavlinkCommand(requestMavlinkVersion());
    }

    /**
     * 请求云台版本号
     */
    public void requstYuntaiVersion() {
        sendMavlinkCommand(MavlinkRequestMessage.requestYuntaiVer());
    }

    public void swithQianbideng(boolean open) {
        sendMavlinkCommand(MavlinkRequestMessage.requestQianbideng(open));
    }

    public void switchHoubideng(boolean open) {
        sendMavlinkCommand(MavlinkRequestMessage.requestHoubideng(open));
    }

    public void setMavlinkParam(String mavDataStream, int paramValue) {
//        LogUtils.d("发送速度相关===>" + mavDataStream + "|||" + paramValue);
        sendMavlinkCommand(MavlinkRequestMessage.requestMavlinkParamSet(mavDataStream, paramValue));
    }

    public void requestMavlinkParam(String paramId) {
        sendMavlinkCommand(requestMavlinkPlaneParam(paramId));
    }

    public void setYTHoriparam(int value) {
        sendMavlinkCommand(MavlinkRequestMessage.yuntaiHoriCallicate(value));
    }

    /**
     * 陀螺仪校准
     */
    public void startGyroCalib() {
        sendMavlinkCommand(MavlinkRequestMessage.mavLinkGyroCalibration());
    }


    /**
     * 指南针校准
     **/
    public void startCompassCalib() {
        sendMavlinkCommand(MavlinkRequestMessage.mavLinkCompassCalibration());
    }

    /**
     * 校准结果返回成功后通知飞控保存校准结果
     */
    public void saveCalibResult() {
        sendMavlinkCommand(MavlinkRequestMessage.mavLinkSaveParameter());
    }

    /**
     * 返航点重置
     */
    public void resetHomePoint(float phoneLat, float phoneLng) {
        sendMavlinkCommand(MavlinkRequestMessage.resetTurnBackPoint(phoneLat, phoneLng));
    }

    /**
     * 安全模式
     */
    public void setSafeMode(boolean isOpen) {
        setMavlinkParam(MAV_DATA_STREAM.NEWCOMER_MODE, isOpen ? 1 : 0);
    }


    public void reBootYuntai() {
        sendMavlinkCommand(MavlinkRequestMessage.rebootYuntai());
    }

    /**
     * 飞控激活
     */
    public void activatePlaneAck() {
        sendMavlinkCommand(MavlinkRequestMessage.activationPlaneAck());
    }

    /**
     * 重置飞控激活
     */
    public void resetPlaneAck() {
        sendMavlinkCommand(MavlinkRequestMessage.resetActivation());
    }

    /**
     * 切换到全景模式=
     *
     * @param isFaceNorth 指定朝向，对于广角指定朝北
     */
    public void changToPanMode(boolean isFaceNorth) {
        sendMavlinkCommand(MavlinkRequestMessage.setMAVLinkMessagePanMode(isFaceNorth));
    }


    /**
     * 云台校准
     */
    public void yunTaiCalibration(int calib) {
        sendMavlinkCommand(MavlinkRequestMessage.yunTaiCalibration(calib));
    }

    /***6k云台校准*/
    public void yunTai6KCalib() {
        sendMavlinkCommand(MavlinkRequestMessage.yunTaiCalibration(MAV_DATA_STREAM.MAV_DATA_6K_CALIB_ACCURACY));
    }

    public void gyroOffsetCalib() {
        sendMavlinkCommand(MavlinkRequestMessage.yunTaiCalibration(MAV_DATA_STREAM.MAV_DATA_GYRO_ACCURACY));
    }

    /**
     * 初始化进入环绕模式
     */
    public void initAroundMode() {
        sendMavlinkCommand(MavlinkRequestMessage.startAroundCenter());
    }

    /**
     * 设置环绕方向和速度
     */
    public void starAround(float speed) {
        sendMavlinkCommand(MavlinkRequestMessage.startAround(speed));
    }

    /**
     * 航点
     */
    public void waypointCount(int type, int count) {
        sendMavlinkCommand(MavlinkRequestMessage.writeWaypointAmount(type, count));
    }

    /**
     * 写入航点
     */
    public void writeWaypoinit(int count) {
        waypointCount(1, count);
    }

    /**
     * 擦除航点
     */
    public void clearWayponit(int count) {
        waypointCount(2, count);
    }

    public void writhWaypointLg(double lat, double lng, int index, int alt) {
        sendMavlinkCommand(MavlinkRequestMessage.writeWaypointLatLng(lat, lng, index, alt));
    }

    public void executeWaypointFly(int value) {
        sendMavlinkCommand(MavlinkRequestMessage.executeWaypointFly(value));
    }

    public void executeDelayTime(boolean start, int vx, int vy) {
        sendMavlinkCommand(MavlinkRequestMessage.startDelayTimeMode(start, vx, vy));
    }

    public void sendServo(int servo) {
        sendMavlinkCommand(MavlinkRequestMessage.setMaLinkServo(servo));
    }

    public void sendYaw(boolean positiveYaw, int servo, int servoCount) {
        sendMavlinkCommand(MavlinkRequestMessage.setMaLinkYaw(positiveYaw, servo, servoCount));
    }

    public void startFollow(boolean isStart) {
        sendMavlinkCommand(MavlinkRequestMessage.gensuiPacket(isStart));
    }

    public void followGPSPosition(double lat, double lng, int vx, int vy) {
        sendMavlinkCommand(MavlinkRequestMessage.gensuiGPSPacket(lat, lng, vx, vy));
    }

    public void changeFlyMode(ApmModes mode) {
        sendMavlinkCommand(MavlinkRequestMessage.changeFlyMode(mode));

    }

    public void startFly() {
        sendMavlinkCommand(MavlinkRequestMessage.startFly());
    }

    public void adjustFly(boolean isStart, int vx, int vy, int vz, int yaw) {
        sendMavlinkCommand(MavlinkRequestMessage.adjustFly(isStart, vx, vy, vz, yaw));
    }

    public void adjustYuntai(int gimbal) {
        sendMavlinkCommand(MavlinkRequestMessage.adjustYuntai(gimbal));
    }


    /**
     * 初始化飞控数据
     */
    public void initMavlinkParams() {
        setMavlinkParam(MAV_DATA_STREAM.SR1_ADSB, 0);
        setMavlinkParam(MAV_DATA_STREAM.SR1_EXT_STAT, 0);
        setMavlinkParam(MAV_DATA_STREAM.SR1_EXTRA1, 0);
        setMavlinkParam(MAV_DATA_STREAM.SR1_EXTRA2, 0);
        setMavlinkParam(MAV_DATA_STREAM.SR1_EXTRA3, 6);
        setMavlinkParam(MAV_DATA_STREAM.SR1_PARAMS, 0);
        setMavlinkParam(MAV_DATA_STREAM.SR1_POSITION, 0);
        setMavlinkParam(MAV_DATA_STREAM.SR1_RAW_CTRL, 0);
        setMavlinkParam(MAV_DATA_STREAM.SR1_RAW_SENS, 0);
        setMavlinkParam(MAV_DATA_STREAM.SR1_RC_CHAN, 0);
        requestPlaneVersion();
        requestPlaneVersion();
        requestPlaneVersion();
        requstYuntaiVersion();
        initMavlinkSpeedParams();
    }

    /**
     * 这个是否建议在每次打开飞控页显示的时候获取一次
     */
    public void initMavlinkSpeedParams() {
        requestMavlinkParam(MAV_DATA_STREAM.NEWCOMER_MODE);
        requestMavlinkParam(MAV_DATA_STREAM.PILOT_SPEED_UP);
        requestMavlinkParam(MAV_DATA_STREAM.PILOT_SPEED_DN);
        requestMavlinkParam(MAV_DATA_STREAM.WPNAV_LOIT_SPEED);
        requestMavlinkParam(MAV_DATA_STREAM.RTL_ALT);
        requestMavlinkParam(MAV_DATA_STREAM.FENCE_ALT_MAX);
        requestMavlinkParam(MAV_DATA_STREAM.FENCE_RADIUS);
        requestMavlinkParam(MAV_DATA_STREAM.WPNAV_SPEED);
    }


}
