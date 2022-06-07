package com.feipai.flypai.beans.mavlinkbeans;

import com.Messages.MAVLinkMessage;
import com.common.msg_cal_progress_decode;
import com.feipai.flypai.base.BaseEntity;
import com.feipai.flypai.base.BaseMavlinkEntity;
import com.feipai.flypai.utils.global.LogUtils;

public class CalibrationProgressBean extends BaseEntity implements BaseMavlinkEntity {

    private int msgId;
    private int progress;


    @Override
    public void setMavlinkMessage(MAVLinkMessage msg) {
        msg_cal_progress_decode msg_cal = (msg_cal_progress_decode) msg;
        LogUtils.d("指南针校准进度====>" + msg_cal.toString());
        setMsgId(msg_cal.msgid);
        setProgress(msg_cal.completion_pct);
    }

    @Override
    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
