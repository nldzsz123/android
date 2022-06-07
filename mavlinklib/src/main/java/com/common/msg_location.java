// MESSAGE ATTITUDE PACKING
package com.common;

import com.MAVLinkPacket;
import com.Messages.MAVLinkMessage;
import com.Messages.MAVLinkPayload;

/**
 * The attitude in the aeronautical frame (right-handed, Z-down, X-front, Y-right).
 * Attitude状态报告，包括滚转角、偏航角、俯仰角（及其速度）等信息。
 */
public class msg_location extends MAVLinkMessage {

    public static final int MAVLINK_MSG_ID_LOCATION = 229;
    public static final int MAVLINK_MSG_LENGTH = 100;
    private static final long serialVersionUID = MAVLINK_MSG_ID_LOCATION;


    /**
     * Severity of status. Relies on the definitions within RFC-5424. See enum MAV_SEVERITY.
     */
//    public byte severity;
    /**
     * Status text message, without null termination character
     */
    public byte text[] = new byte[100];


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
        packet.msgid = MAVLINK_MSG_ID_LOCATION;
//        packet.payload.putByte(severity);
        for (int i = 0; i < text.length; i++) {
            packet.payload.putByte(text[i]);
        }

        return packet;
    }

    /**
     * Decode a statustext message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
//        this.severity = payload.getByte();
        for (int i = 0; i < this.text.length; i++) {
            this.text[i] = payload.getByte();
        }

    }

    /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_location() {
        msgid = MAVLINK_MSG_ID_LOCATION;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     */
    public msg_location(MAVLinkPacket mavLinkPacket) {
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_LOCATION;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "STATUSTEXT");
        //Log.d("MAVLINK_MSG_ID_STATUSTEXT", toString());
    }

    /**
     * Sets the buffer of this message with a string, adds the necessary padding
     */
    public void setText(String str) {
        int len = Math.min(str.length(), 100);
        for (int i = 0; i < len; i++) {
            text[i] = (byte) str.charAt(i);
        }
        for (int i = len; i < 100; i++) {            // padding for the rest of the buffer
            text[i] = 0;
        }
    }

    /**
     * Gets the message, formated as a string
     */
    public String getText() {
        String result = "";
        for (int i = 0; i < 100; i++) {
            if (text[i] != 0)
                result = result + (char) text[i];
            else
                break;
        }
        return result;

    }

    /**
     * Returns a string with the MSG name and data
     */
    public String toString() {
        return "MAVLINK_MSG_ID_LOCATION -" + " text:" + getText() + "";
    }
}