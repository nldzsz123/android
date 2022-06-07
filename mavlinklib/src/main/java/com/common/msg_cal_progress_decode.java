// MESSAGE ATTITUDE PACKING
package com.common;

import android.util.Log;

import com.MAVLinkPacket;
import com.Messages.MAVLinkMessage;
import com.Messages.MAVLinkPayload;

/**
 * 指南针校准
 */
public class msg_cal_progress_decode extends MAVLinkMessage {

    public static final int MAG_CAL_PROGRESS = 191;
    public static final int MAVLINK_MSG_LENGTH = 27;
    private static final long serialVersionUID = MAG_CAL_PROGRESS;


    public byte compass_id;
    public byte cal_mask;
    public byte cal_status;

    /**
     * 尝试次数
     */
    public byte attempt;
    /**
     * 校准进度百分比
     */
    public byte completion_pct;

    public byte[] completion_mask = new byte[10];
    public float direction_x;
    public float direction_y;
    public float direction_z;

    /**
     * Generates the payload for a mavlink message for a message of this type
     *
     * @return
     */
    public MAVLinkPacket pack() {
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAG_CAL_PROGRESS;
        packet.payload.putByte(compass_id);
        packet.payload.putByte(cal_mask);
        packet.payload.putByte(cal_status);
        packet.payload.putByte(attempt);
        packet.payload.putByte(completion_pct);
        packet.payload.putFloat(direction_x);
        packet.payload.putFloat(direction_y);
        packet.payload.putFloat(direction_z);
        for (int i = 0; i < completion_mask.length; i++) {
            packet.payload.putByte(completion_mask[i]);
        }

        return packet;
    }

    /**
     * Decode a attitude message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        this.direction_x = payload.getFloat();
        this.direction_y = payload.getFloat();
        this.direction_z = payload.getFloat();
        this.compass_id = payload.getByte();
        this.cal_mask = payload.getByte();
        this.cal_status = payload.getByte();
        this.attempt = payload.getByte();
        this.completion_pct = payload.getByte();
        for (int i = 0; i < this.completion_mask.length; i++) {
            this.completion_mask[i] = payload.getByte();
        }

    }

    /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_cal_progress_decode() {
        msgid = MAG_CAL_PROGRESS;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     */
    public msg_cal_progress_decode(MAVLinkPacket mavLinkPacket) {
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAG_CAL_PROGRESS;
        unpack(mavLinkPacket.payload);
        Log.d("MAVLink", "ATTITUDE");
        Log.d("MAVLINK_MSG_ID_ATTITUDE", toString());
    }


    /**
     * Returns a string with the MSG name and data
     */
    public String toString() {
        return "MAVLINK_MSG_ID_ATTITUDE -" + " compass_id:" + compass_id + " cal_mask:" + cal_mask
                + " cal_status:" + cal_status + " attempt:" + attempt + " completion_pct:" + completion_pct
                + " direction_x:" + direction_x + " direction_y:" + direction_y + " direction_z:" + direction_z
                + "completion_mask:" + completion_mask;
    }
}
        