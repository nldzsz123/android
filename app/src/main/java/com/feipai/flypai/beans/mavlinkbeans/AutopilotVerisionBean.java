package com.feipai.flypai.beans.mavlinkbeans;

import com.Messages.MAVLinkMessage;
import com.common.msg_autopilot_version;
import com.common.msg_command_ack;
import com.enums.MAV_CMD;
import com.feipai.flypai.base.BaseEntity;
import com.feipai.flypai.base.BaseMavlinkEntity;
import com.feipai.flypai.utils.GeneralFactory;
import com.feipai.flypai.utils.MavlinkRequestMessage;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.StringUtils;

import static com.enums.MAV_CMD.TYPE_DELAY_TIME_MODE;
import static com.enums.MAV_CMD.TYPE_PANOR_MODE;
import static com.enums.MAV_CMD.TYPE_READ_WAYPOINT;
import static com.enums.MAV_CMD.TYPE_RESET_ACTIVATION;
import static com.enums.MAV_CMD.TYPE_WAYPOINT_WRITE;
import static com.enums.MAV_CMD.TYPE_YUNTAI_CALIBRATION;

public class AutopilotVerisionBean extends BaseEntity implements BaseMavlinkEntity {

    private int msgId;
    private String serialNumb;
    private String planeVersion;


    @Override
    public void setMavlinkMessage(MAVLinkMessage msg) {
        msg_autopilot_version versionMsg = (msg_autopilot_version) msg;
        setMsgId(versionMsg.msgid);
        setSerialNumb(GeneralFactory.getPlaneSerialNumber(versionMsg.flight_custom_version, versionMsg.middleware_custom_version));
        setPlaneVersion(StringUtils.longToBinary(versionMsg.flight_sw_version));

    }

    @Override
    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public String getSerialNumb() {
        return serialNumb;
    }

    public void setSerialNumb(String serialNumb) {
        this.serialNumb = serialNumb;
    }

    public String getPlaneVersion() {
        return planeVersion;
    }

    public void setPlaneVersion(String planeVersion) {
        this.planeVersion = planeVersion;
    }
}

