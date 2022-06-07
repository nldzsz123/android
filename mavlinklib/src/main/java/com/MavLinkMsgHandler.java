package com;

import android.content.Context;

import com.Messages.MAVLinkMessage;
import com.ardupilotmega.msg_camera_feedback;
import com.ardupilotmega.msg_mount_status;
import com.ardupilotmega.msg_radio;
import com.ardupilotmega.msg_rangefinder;
import com.common.msg_attitude;
import com.common.msg_global_position_int;
import com.common.msg_gps_raw_int;
import com.common.msg_heartbeat;
import com.common.msg_mission_current;
import com.common.msg_mission_request_list;
import com.common.msg_nav_controller_output;
import com.common.msg_raw_imu;
import com.common.msg_rc_channels_raw;
import com.common.msg_request_data_stream;
import com.common.msg_scaled_pressure;
import com.common.msg_servo_output_raw;
import com.common.msg_statustext;
import com.common.msg_sys_status;
import com.common.msg_vfr_hud;

/**
 * Created by YangLin on 2016-11-26.
 */

public class MavLinkMsgHandler {
    private static final String TAG = MavLinkMsgHandler.class.getSimpleName();

    private static final byte SEVERITY_HIGH = 3;
    private static final byte SEVERITY_CRITICAL = 4;
    private byte[] leftBuffer;
    private int position;
    private int limit = 1024;
    private int firstfeoffset = -1;
    private int secondoffset = -1;
    private int totalCount = 0;
    private int lossCount = 0;
    private int lastSeq;

    public MavLinkMsgHandler() {
    }

    public void receiveData(MAVLinkMessage msg) {
//        MLog.log("消息id==>" + msg.msgid);
        switch (msg.msgid) {
            case msg_attitude.MAVLINK_MSG_ID_ATTITUDE:
                msg_attitude m_att = (msg_attitude) msg;
//                drone.getOrientation().setRollPitchYaw(m_att.roll * 180.0 / Math.PI,
//                        m_att.pitch * 180.0 / Math.PI, m_att.yaw * 180.0 / Math.PI);
                break;
            case msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD:
                msg_vfr_hud m_hud = (msg_vfr_hud) msg;
//                MLog.log("消息对应状态74：" + m_hud.toString());
//                drone.setAltitudeGroundAndAirSpeeds(m_hud.alt, m_hud.groundspeed, m_hud.airspeed,
//                        m_hud.climb);
                break;
            case msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT:
                msg_mission_current m_miss = (msg_mission_current) msg;
//                MLog.log("消息对应状态42：" + m_miss.toString());
//                drone.getMissionStats().setWpno(((msg_mission_current) msg).seq);
                break;
            case msg_nav_controller_output.MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT:
                msg_nav_controller_output m_nav = (msg_nav_controller_output) msg;
//                MLog.log("消息对应状态62：" + m_nav.toString());
//                drone.setDisttowpAndSpeedAltErrors(m_nav.wp_dist, m_nav.alt_error, m_nav.aspd_error);
//                drone.getNavigation().setNavPitchRollYaw(m_nav.nav_pitch, m_nav.nav_roll,
//                        m_nav.nav_bearing);
                break;

            case msg_raw_imu.MAVLINK_MSG_ID_RAW_IMU:
                msg_raw_imu msg_imu = (msg_raw_imu) msg;
//                MLog.log("消息对应状态27：" + msg_imu.toString());
//                drone.getMagnetometer().newData(msg_imu);
                break;

            case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
                msg_heartbeat msg_heart = (msg_heartbeat) msg;
//                MLog.log("消息对应状态0：" + msg_heart.toString());
//                drone.setType(msg_heart.type);
//                drone.getState().setIsFlying(
//                        ((msg_heartbeat) msg).system_status == MAV_STATE.MAV_STATE_ACTIVE);
//                processState(msg_heart);
//                ApmModes newMode = ApmModes.getMode(msg_heart.custom_mode, drone.getType());
//                drone.getState().setMode(newMode);
//                drone.onHeartbeat(msg_heart);
                break;

            case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT:
//                MLog.log("消息对应状态33：" + ((msg_global_position_int) msg).lat / 1E7 + "||" +
//                        ((msg_global_position_int) msg).lon / 1E7);
//                drone.getGps().setPosition(
//                        new Coord2D(((msg_global_position_int) msg).lat / 1E7,
//                                ((msg_global_position_int) msg).lon / 1E7));
                break;
            case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS:
                msg_sys_status m_sys = (msg_sys_status) msg;
//                MLog.log("消息对应状态1：" + m_sys.toString());
//                drone.getBattery().setBatteryState(m_sys.voltage_battery / 1000.0,
//                        m_sys.battery_remaining, m_sys.current_battery / 100.0);
                break;
            case msg_radio.MAVLINK_MSG_ID_RADIO:
                msg_radio m_radio = (msg_radio) msg;
//                MLog.log("消息对应状态166：" + m_radio.toString());
//                drone.getRadio().setRadioState(m_radio.rxerrors, m_radio.fixed, m_radio.rssi,
//                        m_radio.remrssi, m_radio.txbuf, m_radio.noise, m_radio.remnoise);
                break;
            case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:
//                MLog.log("消息对应状态24：" + ((msg_gps_raw_int) msg).fix_type + "||" +
//                        ((msg_gps_raw_int) msg).satellites_visible + "||" + ((msg_gps_raw_int) msg).eph);
//                drone.getGps().setGpsState(((msg_gps_raw_int) msg).fix_type,
//                        ((msg_gps_raw_int) msg).satellites_visible, ((msg_gps_raw_int) msg).eph);
                break;
            case msg_rc_channels_raw.MAVLINK_MSG_ID_RC_CHANNELS_RAW:
//                MLog.log("消息对应状态35：" + ((msg_rc_channels_raw) msg).toString());
//                drone.getRC().setRcInputValues((msg_rc_channels_raw) msg);
                break;
            case msg_servo_output_raw.MAVLINK_MSG_ID_SERVO_OUTPUT_RAW:
//                MLog.log("消息对应状态36：" + (msg_servo_output_raw) msg);
//                drone.getRC().setRcOutputValues((msg_servo_output_raw) msg);
                break;
            case msg_statustext.MAVLINK_MSG_ID_STATUSTEXT:
                // These are any warnings sent from APM:Copter with
                // gcs_send_text_P()
                // This includes important thing like arm fails, prearm fails, low
                // battery, etc.
                // also less important things like "erasing logs" and
                // "calibrating barometer"
                msg_statustext msg_statustext = (msg_statustext) msg;
                String message = msg_statustext.getText();
//                MLog.log("消息对应状态253：" + msg_statustext + "||====>" + message);
                if (msg_statustext.severity == SEVERITY_HIGH || msg_statustext.severity == SEVERITY_CRITICAL) {
//                    drone.getState().setWarning(message);
                    break;
                } else if (message.equals("Low Battery!")) {
//                    drone.getState().setWarning(message);
//                    MLog.log("消息对应状态253：" + "=Low Battery!=" + message);
                    break;
                } else if (message.contains("ArduCopter")) {
//                    MLog.log("消息对应状态253：" + "=ArduCopter=" + message);
//                    drone.setFirmwareVersion(message);
                    break;
                }
                break;
            case msg_camera_feedback.MAVLINK_MSG_ID_CAMERA_FEEDBACK:
//                MLog.log("消息对应状态180：" + (msg_camera_feedback) msg);
//                drone.getCamera().newImageLocation((msg_camera_feedback) msg);
                break;
            case msg_mount_status.MAVLINK_MSG_ID_MOUNT_STATUS:
//                MLog.log("消息对应状态158：" + (msg_mount_status) msg);
//                drone.getCamera().updateMountOrientation(((msg_mount_status) msg));
                break;
            case msg_request_data_stream.MAVLINK_MSG_ID_REQUEST_DATA_STREAM:
                break;
            case msg_scaled_pressure.MAVLINK_MSG_ID_SCALED_PRESSURE:
                break;
            case msg_mission_request_list.MAVLINK_MSG_ID_MISSION_REQUEST_LIST:
                break;

            case msg_rangefinder.MAVLINK_MSG_ID_RANGEFINDER:
                msg_rangefinder msg_r = (msg_rangefinder) msg;

//                MLog.log("消息对应状态173：" + msg_r.distance + "||" + msg_r.voltage);
                break;

            default:
//                MLog.log("消息对应状态还有这个值？：" + msg.msgid);
                break;
        }
    }

    public void handleSocketData(Parser parser, int bufferSize, byte[] buffer, MavLinkMsgListener listener) {
        if (bufferSize < 1) {
            return;
        }

//转换
//        MLog.log("receiv data==>"+byte2hex(buffer));
//        try {
//           MLog.log("sdCardStr---" + (new String(buffer, "ascii")));
//            CmdBean mode = new CmdBean(new String(buffer, "ascii"));
//            if (mTcpReadListener != null && mode.getCmd().equals("3020")) {
//                MLog.log("飞控检测SD卡状态=========" + mode.toString());
//                mTcpReadListener.onReadCameraStatus(mode);
//            }
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        if (leftBuffer == null) {
            leftBuffer = new byte[limit];
            position = 0;
        }
        int newBufferSize = bufferSize;
//        Log.d("Mlog","position==> "+position + "buffersize==>"+bufferSize);
        if (position > 0) {
            newBufferSize += position;
        }
        byte[] newBuffer = new byte[newBufferSize];
        if (position > 0) {
//            Log.d("Mlog","leftbuffer==> "+position);
            for (int i = 0; i < position; i++) {
                newBuffer[i] = leftBuffer[i];
            }
            for (int i = position; i < bufferSize; i++) {
                newBuffer[i] = buffer[i - position];
            }
            position = 0;
        } else {
            for (int i = 0; i < bufferSize; i++) {
                newBuffer[i] = buffer[i];
            }
        }

        firstfeoffset = -1;
        secondoffset = -1;
        for (int i = 0; i < newBufferSize; i++) {
            int code = newBuffer[i] & 0x00ff;
            MAVLinkPacket receivedPacket = parser.mavlink_parse_char(code, buffer);
            if (receivedPacket != null) {
                MAVLinkMessage receivedMsg = receivedPacket.unpack();
                if (receivedMsg != null && listener != null) {
                    listener.mavLinkMsgReceive(receivedMsg, receivedPacket);

//                    mMavMsgHandler.receiveData(receivedMsg);
//                    if (mTcpReadListener != null) {
//                        mTcpReadListener.onReadData(receivedPacket);
//                    }
                }
            }

            if (code == MAVLinkPacket.MAVLINK_STX) {
                if (firstfeoffset == -1) {
                    firstfeoffset = i;
//                    Log.d("Mlog", "发现第一个fe");
                } else {
                    if (secondoffset == -1) {
                        secondoffset = i;
//                        Log.d("Mlog", "发现第二个fe");
                    }
                }
            }
            if (parser.getParseStatus() == Parser.MAVLINK_FRAMING_OK) {
                firstfeoffset = -1;
                secondoffset = -1;

//                MLog.log("----msgId: " + receivedPacket.msgid + " seq: " + receivedPacket.seq);
                if (totalCount == 0) {
                    totalCount = 1;
                } else {
                    if (receivedPacket.seq < lastSeq) {
                        if ((256 + receivedPacket.seq) - lastSeq > 1) {  //说明丢包了
                            lossCount += (256 + receivedPacket.seq - lastSeq - 1);
                        }
                        totalCount += (256 + receivedPacket.seq - lastSeq);
                    } else {
                        if (receivedPacket.seq - lastSeq > 1) {    //说明丢包了
                            lossCount += (receivedPacket.seq - lastSeq - 1);
                        }

                        totalCount += (receivedPacket.seq - lastSeq);
                    }
                }
                lastSeq = receivedPacket.seq;
//                MLog.log("总共: " + totalCount + "个包" + " 丢掉: " + lossCount + " 个包" + " 丢包率:" + (float) lossCount / totalCount * 100 + "%");
            } else if (parser.getParseStatus() == Parser.MAVLINK_FRAMING_BAD_CRC) {
                if (secondoffset > 0 && i != newBufferSize - 1) {   //不是数据data里面的最后一个字节，否则放入下一个逻辑去处理

                    i = secondoffset - 1;
//                    MLog.log("校验失败，重新找到第一个fe的位置==>" +i+1);
                } else {
//                    MLog.log("校验失败，数据包中没有新的fe出现");
                }

                firstfeoffset = -1;
                secondoffset = -1;
                parser.reset();
            }

            //一个mavlink消息跨越两个数据data或者在一个结束的校验失败的数据data中但包含多个fe,则直接截取出来保存放到下一个数据data之前重新解析
            if (i == newBufferSize - 1 && parser.getParseStatus() == Parser.MAVLINK_FRAMING_OK) {
                if (firstfeoffset > 0) {
                    int leftBufferSize = newBufferSize - firstfeoffset;
                    byte[] printBuffer = new byte[leftBufferSize];
                    for (int position = 0; position < leftBufferSize; position++) {
                        leftBuffer[position] = newBuffer[firstfeoffset + position];
                        printBuffer[position] = newBuffer[firstfeoffset + position];
                    }
//                    MLog.log("剩余的数据 data==>" + byte2hex(printBuffer));
                    parser.reset();
                }
            }

        }
    }

    public interface MavLinkMsgListener {
        void mavLinkMsgReceive(MAVLinkMessage receivedMsg, MAVLinkPacket receivedPacket);
    }

}
