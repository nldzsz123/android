package com;

//import com.feipai.flypai.helps.BluetoothChatService;
//import com.feipai.flypai.utils.MLog;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.Messages.MAVLinkMessage;
import com.common.msg_command_long;
import com.common.msg_param_set;
import com.common.msg_request_data_stream;
import com.enums.MAV_DATA_STREAM;

/**
 * Created by YangLin on 2016-11-28.
 */

public class MavLinkStreamRates {


//    public static void setupStreamRates(BluetoothChatService mService, byte sysid, byte compid,
//                                        int extendedStatus, int extra1, int extra2, int extra3, int position, int rcChannels,
//                                        int rawSensors, int rawControler) {
//        requestMavlinkDataStream(mService, sysid, compid, MAV_DATA_STREAM.MAV_DATA_STREAM_ALL,
//                extendedStatus);//
//    }


//    private static void requestMavlinkDataStream(BluetoothChatService mService, byte sysid, byte compid, int stream_id, int rate) {
//        msg_request_data_stream msg = new msg_request_data_stream();
//        msg.target_system = sysid;
//        msg.target_component = compid;
//        msg.req_stream_id = (byte) stream_id;
//        msg.req_message_rate = (short) rate;
//
//        if (rate > 0) {
//            msg.start_stop = 1;
//        } else {
//            msg.start_stop = 0;
//        }
//
//        MAVLinkPacket packet = msg.pack();
//        byte[] buffer = packet.encodePacket();
//        mService.write(buffer);
//    }

    public static void setupStreamRates(byte sysid, byte compid,
                                        int extendedStatus, int extra2) {
        requestMavlinkDataStream(sysid, compid, MAV_DATA_STREAM.MAV_DATA_STREAM_ALL,
                extendedStatus);//
    }


    public static MAVLinkMessage requestMavlinkDataStream(byte sysid, byte compid, int stream_id, int rate) {
        msg_request_data_stream msg = new msg_request_data_stream();
        msg.target_system = sysid;
        msg.target_component = compid;
        msg.req_stream_id = (byte) stream_id;
        msg.req_message_rate = (short) rate;

        if (rate > 0) {
            msg.start_stop = 1;
        } else {
            msg.start_stop = 0;
        }

        return msg;

    }

    /**
     * 初始化飞控数据
     */
    public static void setMavLinkParam(byte sysid, byte compid) {
//        requestMavlinkParamSet(sysid, compid, stringToByte(MAV_DATA_STREAM.SRI_EXTRA1), 8);//姿态8
//        requestMavlinkParamSet(sysid, compid, stringToByte(MAV_DATA_STREAM.SRI_EXTRA2), 6);//升降速度和航向6
//        requestMavlinkParamSet(sysid, compid, stringToByte(MAV_DATA_STREAM.SRI_EXTRA3), 6);//智能电池和指南针校准6
//        requestMavlinkParamSet(sysid, compid, stringToByte(MAV_DATA_STREAM.SR1_EXT_STAT), 2);//GPS坐标,飞行状态2
//        requestMavlinkParamSet(sysid, compid, stringToByte(MAV_DATA_STREAM.SR1_POSITION), 1);//1
//        requestMavlinkVersion();//请求版本号
    }

    public static MAVLinkMessage requestMavlinkParamSet(String bytes, float paramValue) {
        msg_param_set msg = new msg_param_set();
        msg.target_system = 1;
        msg.target_component = 1;
        msg.setParam_Id(bytes);
        msg.param_type = 4;
        msg.param_value = paramValue;
        return msg;
    }

    public static MAVLinkMessage requestMavlinkVersion() {
        msg_command_long msg = new msg_command_long();
        msg.command = (short) MAV_DATA_STREAM.MAV_DATA_VERSION;
        msg.target_system = 1;
        msg.target_component = 1;
        msg.confirmation = 0;
        msg.param1 = 1;
        msg.param2 = 0;
        msg.param3 = 1;//便于飞控识别地面站为手机端
        msg.param4 = 0;
        msg.param5 = 0;
        msg.param6 = 0;
        msg.param7 = 0;
        return msg;
    }


    /**
     * 设置返航高度
     */
    public static void setItsHeight(byte sysid, byte compid, int paramValue) {
//        requestMavlinkParamSet(sysid, compid, stringToByte(MAV_DATA_STREAM.RTL_ALT), paramValue);
    }

    public static byte[] stringToByte(String str) {
        byte[] b = null;
        try {
            b = str.getBytes("ascii");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        MLog.log("传入的参数---->" + Arrays.toString(b));
        return b;
    }


}
