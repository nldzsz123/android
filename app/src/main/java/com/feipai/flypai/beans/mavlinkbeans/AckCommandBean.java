package com.feipai.flypai.beans.mavlinkbeans;

import com.Messages.MAVLinkMessage;
import com.amap.api.maps.model.LatLng;
import com.ardupilotmega.msg_mag_cal_report;
import com.common.msg_command_ack;
import com.enums.MAV_CMD;
import com.feipai.flypai.R;
import com.feipai.flypai.base.BaseEntity;
import com.feipai.flypai.base.BaseMavlinkEntity;
import com.feipai.flypai.utils.GPSUtils;
import com.feipai.flypai.utils.MLog;
import com.feipai.flypai.utils.MavlinkRequestMessage;
import com.feipai.flypai.utils.global.LogUtils;

import static com.enums.MAV_CMD.TYPE_DELAY_TIME_MODE;
import static com.enums.MAV_CMD.TYPE_PANOR_MODE;
import static com.enums.MAV_CMD.TYPE_READ_WAYPOINT;
import static com.enums.MAV_CMD.TYPE_RESET_ACTIVATION;
import static com.enums.MAV_CMD.TYPE_WAYPOINT_WRITE;
import static com.enums.MAV_CMD.TYPE_YUNTAI_CALIBRATION;

public class AckCommandBean extends BaseEntity implements BaseMavlinkEntity {

    private int msgId;
    private boolean isSuccess;
    private int command;
    //请求码对应的指令(航点写入，航点读取,4k云台校准，切换全景模式，切换延时模式，重置激活)
    private int requestCode;

    private boolean readWaypointSuccess;
    private boolean writeWaypointSuccess;


    @Override
    public void setMavlinkMessage(MAVLinkMessage msg) {
        msg_command_ack ack = (msg_command_ack) msg;
        setMsgId(ack.msgid);
        setSuccess(ack.result == 0);
        setCommand(ack.command);
    }

    @Override
    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
//        LogUtils.d("长指令" + command + "||" + requestCode);
        switch (command) {
            case MavlinkRequestMessage.ACK_JSON_COMMAND:
                switch (requestCode) {
                    case TYPE_READ_WAYPOINT:
                        //写入航点总数成功
                        setReadWaypointSuccess(isSuccess());
                        break;
                    case TYPE_WAYPOINT_WRITE:
                        //写入航点坐标成功
                        setWriteWaypointSuccess(isSuccess());
                        break;
                    case TYPE_YUNTAI_CALIBRATION:
                        //云台校准成功
                        break;
                    case TYPE_RESET_ACTIVATION:
                        //重置激活成功
                        break;
                    case TYPE_PANOR_MODE:
                        //初始进入全景模式的时候，就开始全景流程了咯
                        break;
                    case TYPE_DELAY_TIME_MODE:
                        //切换到延时模式
                        break;
                }
                break;
            case MAV_CMD.MAV_CMD_DO_SET_SERVO:
                //云台指令发送成功（for panor）
                break;
            case MAV_CMD.MAV_CMD_CONDITION_YAW:
                //飞机旋转指令发送成功
                break;
            case MAV_CMD.MAV_CMD_PREFLIGHT_CALIBRATION:
                /**陀螺仪校准:0：成功；1：失败*/
                LogUtils.d("陀螺仪校准结果");
                break;
        }
    }


    public void setReadWaypointSuccess(boolean success) {
        this.readWaypointSuccess = success;
    }

    public boolean isReadWaypointSuccess() {
        return readWaypointSuccess;
    }

    public void setWriteWaypointSuccess(boolean success) {
        this.writeWaypointSuccess = success;
    }

    public boolean isWriteWaypointSuccess() {
        return writeWaypointSuccess;
    }


    public void setRequestCode(int requestCode) {
        setReadWaypointSuccess(false);
        setWriteWaypointSuccess(false);
        this.requestCode = requestCode;
    }

    public int getRequestCode() {
        return requestCode;
    }

    /**
     * 是否是陀螺仪校准回复
     * 貌似无进度回复，只回复最终结果
     */
    public boolean isCalibGyroSuccess() {
        return command == MAV_CMD.MAV_CMD_PREFLIGHT_CALIBRATION && isSuccess();
    }
}

