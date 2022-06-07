package com.feipai.flypai.utils;

import com.Messages.ApmModes;
import com.Messages.MAVLinkMessage;
import com.amap.api.maps.model.LatLng;
import com.common.msg_command_long;
import com.common.msg_location;
import com.common.msg_param_request_read;
import com.common.msg_param_set;
import com.common.msg_set_mode;
import com.enums.MAV_CMD;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.utils.global.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import static com.enums.MAV_CMD.TYPE_CONFIRM_AROUND_THE_ORIGIN;
import static com.enums.MAV_CMD.TYPE_DELAY_TIME_MODE;
import static com.enums.MAV_CMD.TYPE_PANOR_MODE;
import static com.enums.MAV_CMD.TYPE_READ_WAYPOINT;
import static com.enums.MAV_CMD.TYPE_RESET_ACTIVATION;
import static com.enums.MAV_CMD.TYPE_STRAT_AROUND_THE_ORIGIN;
import static com.enums.MAV_CMD.TYPE_WAYPOINT_FLY;
import static com.enums.MAV_CMD.TYPE_WAYPOINT_WRITE;
import static com.enums.MAV_CMD.TYPE_YUNTAI_CALIBRATION;
import static com.feipai.flypai.app.ConstantFields.PLANE_CONFIG.TYPE_PLANE_ACK;
import static com.feipai.flypai.app.ConstantFields.PLANE_CONFIG.TYPE_REBOOT_YUNTAI;

public class MavlinkRequestMessage {

    public final static int ACK_JSON_COMMAND = 229;

    /**********消息体*********************************************************/
    /**
     * 获取飞控版本号
     */
    public static MAVLinkMessage requestMavlinkVersion() {
        msg_command_long msg = new msg_command_long();
        msg.command = (short) 520;
        msg.target_system = 1;
        msg.target_component = 1;
        msg.confirmation = 0;
        msg.param1 = 1;
        msg.param2 = 0;
        msg.param3 = 0;
        msg.param4 = 0;
        msg.param5 = 0;
        msg.param6 = 0;
        msg.param7 = 0;
        return msg;
    }

    /**
     * 获取云台版本
     */
    public static MAVLinkMessage requestYuntaiVer() {
        try {
            msg_location msg = new msg_location();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", ConstantFields.PLANE_CONFIG.TYPE_REQUEST_VERSION);
            String result = jsonObject.toString();
            msg.setText(result);
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 云台水平调整
    public static MAVLinkMessage yuntaiHoriCallicate(int value) {
        try {
            msg_location msg = new msg_location();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", 1028);
            jsonObject.put("roll_trim", value);
            jsonObject.put("yaw_trim", 0);
            String result = jsonObject.toString();
            msg.setText(result);
            LogUtils.d("发送云台微调====" + result.toString());
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MAVLinkMessage requestQianbideng(boolean open) {
        try {
            msg_location msg = new msg_location();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", ConstantFields.PLANE_CONFIG.TYPE_Qianbideng);
            jsonObject.put("led_ctrl", open ? 1 : 0);
            String result = jsonObject.toString();
            msg.setText(result);
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MAVLinkMessage requestHoubideng(boolean open) {
        try {
            msg_location msg = new msg_location();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", ConstantFields.PLANE_CONFIG.TYPE_Houbideng);
            jsonObject.put("led_ctrl", open ? 1 : 0);
            String result = jsonObject.toString();
            msg.setText(result);
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MAVLinkMessage requestMavlinkParamSet(String bytes, int paramValue) {
        msg_param_set msg = new msg_param_set();
        msg.target_system = 1;
        msg.target_component = 1;
        msg.setParam_Id(bytes);
        msg.param_type = 4;
        msg.param_value = paramValue;
        return msg;
    }

    public static MAVLinkMessage requestMavlinkPlaneParam(String paramId) {
        msg_param_request_read msg = new msg_param_request_read();
        msg.param_index = -1;
        msg.target_system = 1;
        msg.target_component = 1;
        msg.setParam_Id(paramId);
        return msg;
    }


    public static MAVLinkMessage rebootYuntai() {
        try {
            msg_location msg = new msg_location();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", TYPE_REBOOT_YUNTAI);
            String result = jsonObject.toString();
            msg.setText(result);
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 激活飞控
     */
    public static MAVLinkMessage activationPlaneAck() {
        try {
            msg_location msg = new msg_location();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", TYPE_PLANE_ACK);
            jsonObject.put("act", 1);
            String result = jsonObject.toString();
//            result = result.replace("{", "");
//            msg.severity = 123;
            msg.setText(result);
            LogUtils.d("激活飞控--->" + msg.toString());
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static MAVLinkMessage resetActivation() {
        try {
            msg_location msg = new msg_location();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", TYPE_RESET_ACTIVATION);
            String result = jsonObject.toString();
            msg.setText(result);
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 重置返航点
     *
     * @param phoneLat,phoneLng 手机经纬度
     */
    public static MAVLinkMessage resetTurnBackPoint(float phoneLat, float phoneLng) {
        msg_command_long msg = new msg_command_long();
        msg.command = MAV_CMD.MAV_CMD_DO_SET_HOME;
        msg.target_system = 1;
        msg.target_component = 1;
        msg.param1 = 0;
        msg.param5 = phoneLat;
        msg.param6 = phoneLng;
        msg.param7 = 0;
        msg.confirmation = 0;
        return msg;
    }

    /**
     * 加速计校准,陀螺仪
     */
    public static MAVLinkMessage mavLinkGyroCalibration() {
        msg_command_long msg = new msg_command_long();
        msg.command = MAV_CMD.MAV_CMD_PREFLIGHT_CALIBRATION;
        msg.target_system = 1;
        msg.target_component = 1;
        msg.param1 = 0;
        msg.param2 = 0;
        msg.param3 = 0;
        msg.param4 = 0;
        msg.param5 = 2;
        msg.confirmation = 0;
        return msg;
    }

    /**
     * 指南针校准
     */
    public static MAVLinkMessage mavLinkCompassCalibration() {
        msg_command_long msg = new msg_command_long();
        msg.command = (short) 42424;
        msg.target_system = 1;
        msg.target_component = 1;
        msg.param1 = 0;
        msg.param2 = 1;
        msg.param3 = 0;
        msg.param4 = 0;
        msg.param5 = 0;
        msg.param6 = 0;
        msg.param7 = 0;
        msg.confirmation = 0;
        return msg;
    }


    /**
     * 云台校准
     */
    public static MAVLinkMessage yunTaiCalibration(int cali) {
        try {
            msg_location msg = new msg_location();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", TYPE_YUNTAI_CALIBRATION);
            jsonObject.put("cali", cali);
            String result = jsonObject.toString();
            MLog.log("云台校准:" + result);
            msg.setText(result);
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 保存参数：如指南针校准成功后,保存校准参数
     */
    public static MAVLinkMessage mavLinkSaveParameter() {
        msg_command_long msg = new msg_command_long();
        msg.command = (short) 42425;
        msg.target_system = 1;
        msg.target_component = 1;
        msg.param1 = 0;
        msg.param2 = 0;
        msg.param3 = 0;
        msg.param4 = 0;
        msg.param5 = 0;
        msg.param6 = 0;
        msg.param7 = 0;
        msg.confirmation = 0;
        return msg;
    }


    /**
     * 设置飞控进入全景模式
     *
     * @param faceNorth 0朝北，1禁止朝北,用于广角朝北
     */
    public static MAVLinkMessage setMAVLinkMessagePanMode(boolean faceNorth) {
        try {
            msg_location msg = new msg_location();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", TYPE_PANOR_MODE);
            jsonObject.put("dis_pt_nor", faceNorth ? 0 : 1);
            String result = jsonObject.toString();
            MLog.log("全景--->开启全景模式" + result);
            msg.setText(result);
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 设置旋转方向
     * <p>
     * 0设置中心点
     */
    public static MAVLinkMessage startAroundCenter() {
        try {
            msg_location msg = new msg_location();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", TYPE_CONFIRM_AROUND_THE_ORIGIN);
            jsonObject.put("value", 0);
            String result = jsonObject.toString();
//            result = result.replace("{", "");
            msg.setText(result);
//            msg.severity = 123;
            MLog.log("设定中心点--->" + msg.toString());
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 设置旋转方向
     *
     * @param value 0停止，-1逆时针，1顺时针
     */
    public static MAVLinkMessage startAround(float value) {
        try {
            msg_location msg = new msg_location();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", TYPE_STRAT_AROUND_THE_ORIGIN);
            jsonObject.put("value", value);
            String result = jsonObject.toString();
//            result = result.replace("{", "");
            msg.setText(result);
//            msg.severity = 123;
            MLog.log("暂停或停止环绕--->" + msg.toString());
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 写入航点个数
     *
     * @param value “value”:”0”//0读取，1写入，2擦除航点
     * @param total 航点总数
     */
    public static MAVLinkMessage writeWaypointAmount(int value, int total) {
        try {
            msg_location msg = new msg_location();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", TYPE_READ_WAYPOINT);
            jsonObject.put("value", value);
            jsonObject.put("total", total);
            String result = jsonObject.toString();
            MLog.log("发送航点总个数:" + result);
            msg.setText(result);
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 写入航点个数
     *
     * @param latLng 经纬度
     * @param index  第几个航点
     * @param alt    飞行高度
     */
    public static MAVLinkMessage writeWaypointLatLng(double lat,double lng, int index, int alt) {
        try {

            msg_location msg = new msg_location();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", TYPE_WAYPOINT_WRITE);
            jsonObject.put("index", index);
            jsonObject.put("lat", lat);
            jsonObject.put("lng", lng);
            jsonObject.put("alt", alt);
            String result = jsonObject.toString();
            MLog.log("发送航点坐标:" + result);
            msg.setText(result);
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 执行航点飞行
     *
     * @param value 0取消执行，1航点执行
     */
    public static MAVLinkMessage executeWaypointFly(int value) {
        try {
            msg_location msg = new msg_location();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", TYPE_WAYPOINT_FLY);
            jsonObject.put("value", value);
            String result = jsonObject.toString();
            MLog.log("发送航点执行飞行:" + result);
            msg.setText(result);
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MAVLinkMessage startDelayTimeMode(boolean start, int vx, int vy) {
        try {
            msg_location msg = new msg_location();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", TYPE_DELAY_TIME_MODE);
            jsonObject.put("enter", start ? 1 : 0);
            jsonObject.put("vel_x", vx);
            jsonObject.put("vel_y", vy);
            String result = jsonObject.toString();
//            MLog.log("延时拍摄:" + result);
            msg.setText(result);
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 设置云台角度取值范围1000-2000，1000代表水平状态，2000最大角度垂直向下
     * <p>
     * 临时角度
     * 垂直：1834
     * 水平：1208
     */
    public static MAVLinkMessage setMaLinkServo(int servo) {
        MLog.log("全景拍摄---->发送旋转云台指令" + servo);
        msg_command_long msg = new msg_command_long();
        msg.command = MAV_CMD.MAV_CMD_DO_SET_SERVO;
        msg.target_system = 1;
        msg.target_component = 1;
        msg.param1 = 11;
        msg.param2 = servo;
        msg.param3 = 0;
        msg.param4 = 0;
        msg.param5 = 0;
        msg.confirmation = 0;
        return msg;
    }

    /**
     * @param positiveYaw true左转
     * @param servo       云台角度
     * @param servoCount  旋转参数，与张数对应
     */
    public static MAVLinkMessage setMaLinkYaw(boolean positiveYaw, int servo, int servoCount) {
        MLog.log("全景--->飞机旋转第5个参数" + servoCount + "||" + servo + "||" + positiveYaw);
        msg_command_long msg = new msg_command_long();
        msg.command = MAV_CMD.MAV_CMD_CONDITION_YAW;
        msg.target_system = 1;
        msg.target_component = 1;
        msg.param1 = servo;//旋转角度
        msg.param2 = 5;//每秒旋转的角度
        msg.param3 = positiveYaw ? 1 : -1;//1表示顺时针旋转
        msg.param4 = 1;//1表示相对之前的位置继续旋转
        msg.param5 = servoCount;//
        msg.param6 = 0;
        msg.param7 = 0;
        msg.confirmation = 0;
        return msg;
    }

    /**
     * 开始 or 结束跟随
     */
    public static MAVLinkMessage gensuiPacket(boolean beginOrnot) {
        LogUtils.d("开始跟随===》" + beginOrnot);
        try {
            msg_location msg = new msg_location();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", 1026);
            jsonObject.put("enter", beginOrnot ? 1 : 0);
            String result = jsonObject.toString();
            msg.setText(result);
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 跟随的经纬度
     */
    public static MAVLinkMessage gensuiGPSPacket(double lat, double lng, int vx, int vy) {
        LogUtils.d("发送跟随经纬度===》" + lat + "||" + lng + "||" + vx + "||" + vy);
        try {
            msg_location msg = new msg_location();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", 1025);
            jsonObject.put("lat", lat);
            jsonObject.put("lng", lng);
            jsonObject.put("vx", vx);
            jsonObject.put("vy", vy);
            jsonObject.put("vz", 0);
            String result = jsonObject.toString();
            msg.setText(result);
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MAVLinkMessage changeFlyMode(ApmModes mode) {
        msg_set_mode msg = new msg_set_mode();
        msg.target_system = 1;
        msg.base_mode = 1; // TODO use meaningful constant
        msg.custom_mode = mode.getNumber();
        return msg;
    }

    public static MAVLinkMessage startFly() {
        try {
            msg_location msg = new msg_location();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", 1006);
            jsonObject.put("value", 2);
            String result = jsonObject.toString();
            msg.setText(result);
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 调节飞行
     * 上升下降以0.5m/s加速
     * 前后左右飞行以1m/s加速
     */
    public static MAVLinkMessage adjustFly(boolean isStart, int vx, int vy, int vz, int yaw) {
        LogUtils.d("手动调节飞行===>" + "isStart=" + isStart + "vx=" + vx + "vy=" + vy + "vz=" + vz + "yaw=" + yaw);
        try {
            msg_location msg = new msg_location();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", 1030);
            jsonObject.put("enter", isStart ? 1 : 0);//1进入，0退出
            jsonObject.put("vx", vx); //单位cm/s,向前为正
            jsonObject.put("vy", vy); //单位cm/s,向右为正
            jsonObject.put("vz", vz);//单位cm/s,向下为正
            jsonObject.put("yaw", yaw);//单位 度/s,向右为正
            String result = jsonObject.toString();
            msg.setText(result);
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MAVLinkMessage adjustYuntai(int gimbal) {
        LogUtils.d("手动调节云台===>" + "gimbal=" + gimbal);
        try {
            msg_location msg = new msg_location();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", 1031);
            jsonObject.put("gimbal", gimbal);//单位 度/s,向下为正 1,0,-1
            String result = jsonObject.toString();
            msg.setText(result);
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
