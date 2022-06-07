package com;


import com.Messages.MAVLinkStats;

public class Parser {

    /**
     * States from the parsing state machine
     */
    enum MAV_states {
        MAVLINK_PARSE_STATE_UNINIT,
        MAVLINK_PARSE_STATE_IDLE,
        MAVLINK_PARSE_STATE_GOT_STX,
        MAVLINK_PARSE_STATE_GOT_LENGTH,
        MAVLINK_PARSE_STATE_GOT_SEQ,
        MAVLINK_PARSE_STATE_GOT_SYSID,
        MAVLINK_PARSE_STATE_GOT_COMPID,
        MAVLINK_PARSE_STATE_GOT_MSGID,
        MAVLINK_PARSE_STATE_GOT_CRC1,
        MAVLINK_PARSE_STATE_GOT_PAYLOAD
    }

    MAV_states state = MAV_states.MAVLINK_PARSE_STATE_UNINIT;

    private static int msg_received;
    public final static int MAVLINK_FRAMING_INCOMPLETE = 1;
    public final static int MAVLINK_FRAMING_OK = 2;
    public final static int MAVLINK_FRAMING_BAD_CRC = 3;

    public MAVLinkStats stats = new MAVLinkStats();
    private MAVLinkPacket m;

    /**
     * This is a convenience function which handles the complete MAVLink
     * parsing. the function will parse one byte at a time and return the
     * complete packet once it could be successfully decoded. Checksum and other
     * failures will be silently ignored.
     *
     * @param c The char to parse
     */
    public MAVLinkPacket mavlink_parse_char(int c, byte[] bytes) {
        msg_received = MAVLINK_FRAMING_INCOMPLETE;
//		Log.e("yangling","状态="+state);
        switch (state) {
            case MAVLINK_PARSE_STATE_UNINIT:
            case MAVLINK_PARSE_STATE_IDLE:

                if (c == MAVLinkPacket.MAVLINK_STX) {
                    state = MAV_states.MAVLINK_PARSE_STATE_GOT_STX;
                    m = new MAVLinkPacket();
                }
                break;

            case MAVLINK_PARSE_STATE_GOT_STX:
                if (msg_received == MAVLINK_FRAMING_OK) {
                    msg_received = MAVLINK_FRAMING_INCOMPLETE;
                    state = MAV_states.MAVLINK_PARSE_STATE_IDLE;
                } else {
                    m.len = c;
                    state = MAV_states.MAVLINK_PARSE_STATE_GOT_LENGTH;
                }
                break;

            case MAVLINK_PARSE_STATE_GOT_LENGTH:
                m.seq = c;
                state = MAV_states.MAVLINK_PARSE_STATE_GOT_SEQ;
                break;

            case MAVLINK_PARSE_STATE_GOT_SEQ:

                m.sysid = c;
                state = MAV_states.MAVLINK_PARSE_STATE_GOT_SYSID;
                break;

            case MAVLINK_PARSE_STATE_GOT_SYSID:

                m.compid = c;
                state = MAV_states.MAVLINK_PARSE_STATE_GOT_COMPID;
                break;

            case MAVLINK_PARSE_STATE_GOT_COMPID:
                m.msgid = c;
                if (m.len == 100) {
//                    MLog.log("消息长度为100的消息ID" + c);
                }
                if (m.len == 0) {
                    state = MAV_states.MAVLINK_PARSE_STATE_GOT_PAYLOAD;
//                    MLog.log("报文----c" + c + "MAVLINK_PARSE_STATE_GOT_PAYLOAD");
                } else {
//                    MLog.log("报文----c" + c + "MAVLINK_PARSE_STATE_GOT_MSGID");
                    state = MAV_states.MAVLINK_PARSE_STATE_GOT_MSGID;
                }
                break;

            case MAVLINK_PARSE_STATE_GOT_MSGID:
                m.payload.add((byte) c);
                if (m.payloadIsFilled()) {
//                    MLog.log("报文----c" + c + "----------");
                    state = MAV_states.MAVLINK_PARSE_STATE_GOT_PAYLOAD;
                }
                break;

            case MAVLINK_PARSE_STATE_GOT_PAYLOAD:
                m.generateCRC();
//                if (m.msgid == 229) {
//                    MLog.log("2c---->" + c);
//                    MLog.log("2c----getLSB>" + m.crc.getLSB());
//                }
                // Check first checksum byte
                if (c != m.crc.getLSB()) {
                    state = MAV_states.MAVLINK_PARSE_STATE_IDLE;
                    if (c == MAVLinkPacket.MAVLINK_STX) {
                        state = MAV_states.MAVLINK_PARSE_STATE_GOT_STX;
                        m.crc.start_checksum();
                    }
                    if (m.msgid == 229) {
//                        MLog.log("俯仰角度校验出错--->" + m.msgid);
                    }
//                    MLog.log("1数据校验出错" + m.msgid);
                    stats.crcError();
                    msg_received = MAVLINK_FRAMING_BAD_CRC;
                } else {
                    state = MAV_states.MAVLINK_PARSE_STATE_GOT_CRC1;
                }
                break;

            case MAVLINK_PARSE_STATE_GOT_CRC1:
                if (m.msgid == 229) {
//                    MLog.log("c---->" + c);
//                    MLog.log("c----getMSB>" + m.crc.getMSB());
                }
                // Check second checksum byte
                if (m.crc != null && c != m.crc.getMSB()) {
                    state = MAV_states.MAVLINK_PARSE_STATE_IDLE;
                    if (c == MAVLinkPacket.MAVLINK_STX) {
                        state = MAV_states.MAVLINK_PARSE_STATE_GOT_STX;
                        m.crc.start_checksum();
                    }
//                    MLog.log("2数据校验出错" + m.msgid);
                    stats.crcError();
                    msg_received = MAVLINK_FRAMING_BAD_CRC;
                } else { // Successfully received the message
                    stats.newPacket(m);
                    state = MAV_states.MAVLINK_PARSE_STATE_IDLE;
                    msg_received = MAVLINK_FRAMING_OK;
                    return m;
                }

                break;

        }
        if (msg_received == MAVLINK_FRAMING_OK) {
            stats.crcSuccess();
            return m;
        } else {
            return null;
        }
    }

    public int getParseStatus() {
        return msg_received;
    }

    public void reset() {
        state = MAV_states.MAVLINK_PARSE_STATE_IDLE;
    }

}
