package com.feipai.flypai.beans.mavlinkbeans;

import com.Messages.MAVLinkMessage;
import com.ardupilotmega.msg_mag_cal_report;
import com.common.msg_cal_progress_decode;
import com.feipai.flypai.base.BaseEntity;
import com.feipai.flypai.base.BaseMavlinkEntity;
import com.feipai.flypai.utils.global.LogUtils;

public class CalibrationSuccessBean extends BaseEntity implements BaseMavlinkEntity {

    private int msgId;
    private boolean calibCompassSuccess;


    @Override
    public void setMavlinkMessage(MAVLinkMessage msg) {
        msg_mag_cal_report msg_mag_cal_report = (com.ardupilotmega.msg_mag_cal_report) msg;
        LogUtils.d("指南针校准结果====>" + msg_mag_cal_report.toString());
        setMsgId(msg_mag_cal_report.msgid);
        setCalibCompassSuccess(msg_mag_cal_report.cal_status == 4);
    }

    @Override
    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public boolean isCalibCompassSuccess() {
        return calibCompassSuccess;
    }

    public void setCalibCompassSuccess(boolean calibCompassSuccess) {
        this.calibCompassSuccess = calibCompassSuccess;
    }
}
