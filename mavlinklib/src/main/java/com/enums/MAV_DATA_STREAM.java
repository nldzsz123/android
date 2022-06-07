/**
 * Data stream IDs. A data stream is not a fixed set of messages, but rather a
 * recommendation to the autopilot software. Individual autopilots may or may not obey
 * the recommended messages.
 */
package com.enums;

public class MAV_DATA_STREAM {
    public static final int MAV_DATA_STREAM_ALL = 0; /* Enable all data streams | */
    public static final int MAV_DATA_VERSION = 520; /* Enable all data streams | */
    //            	public static final int MAV_DATA_STREAM_RAW_SENSORS = 1; /*原始GPS、GPS状态数据包 Enable IMU_RAW, GPS_RAW, GPS_STATUS packets. | */
    public static final int MAV_DATA_STREAM_EXTENDED_STATUS = 2; /*GPS状态,控制状态,辅助地位 Enable GPS_STATUS, CONTROL_STATUS, AUX_STATUS | */
    //    public static final int MAV_DATA_STREAM_RC_CHANNELS = 3; /* Enable RC_CHANNELS_SCALED, RC_CHANNELS_RAW, SERVO_OUTPUT_RAW | */
    //            	public static final int MAV_DATA_STREAM_RAW_CONTROLLER = 4; /*使态度控制器输出,位置控制器输出,导航控制器输出 Enable ATTITUDE_CONTROLLER_OUTPUT, POSITION_CONTROLLER_OUTPUT, NAV_CONTROLLER_OUTPUT. | */
    public static final int MAV_DATA_STREAM_POSITION = 6; /* 使当地的位置,全球地位/全球地位INT消息 Enable LOCAL_POSITION, GLOBAL_POSITION/GLOBAL_POSITION_INT messages. | */
    //            	public static final int MAV_DATA_STREAM_EXTRA1 = 10; /* Dependent on the autopilot | */
    public static final int MAV_DATA_STREAM_EXTRA2 = 11; /* Dependent on the autopilot | */
    //            	public static final int MAV_DATA_STREAM_EXTRA3 = 12; /* Dependent on the autopilot | */
    public static final int MAV_DATA_STREAM_ENUM_END = 13; /*  | */

    /**
     * 校准
     */
    public static final int MAV_DATA_6K_CALIB_ACCURACY = 9;//6k云台精确校准
    public static final int MAV_DATA_GYRO_ACCURACY = 12;//陀螺仪偏移校准
    public static final int MAV_DATA_4K_CALIB_YUNTAI_ROLL = 1;//4K云台校准，roll电机对齐
    public static final int MAV_DATA_4K_CALIB_YUNTAI_PITCH = 2;//4K云台校准，pitch电机对齐
    public static final int MAV_DATA_4K_CALIB_YUNTAI_YAW = 3;//4K云台校准，yaw电机对齐
    public static final int MAV_DATA_4K_CALIB_YUNTAI_IMU = 4;//4K云台校准，imu电位器静态校准
    public static final int MAV_DATA_4K_CALIB_YUNTAI_START = 5;//4K云台校准，开始
    public static final int MAV_DATA_4K_CALIB_YUNTAI_END = 6;//4K云台校准，结束


    public static final String SR1_ADSB = "SR1_ADSB";//0
    public static final String SR1_EXT_STAT = "SR1_EXT_STAT";//0
    public static final String SR1_EXTRA1 = "SR1_EXTRA1";//0
    public static final String SR1_EXTRA2 = "SR1_EXTRA2";//0
    public static final String SR1_EXTRA3 = "SR1_EXTRA3";//6
    public static final String SR1_PARAMS = "SR1_PARAMS";//0
    public static final String SR1_POSITION = "SR1_POSITION";//0
    public static final String SR1_RAW_CTRL = "SR1_RAW_CTRL";//0
    public static final String SR1_RAW_SENS = "SR1_RAW_SENS";//0
    public static final String SR1_RC_CHAN = "SR1_RC_CHAN";//0

    public static final String RTL_ALT = "RTL_ALT";//限制返航高度
    public static final String FENCE_ALT_MAX = "FENCE_ALT_MAX";//限制围栏高度
    public static final String FENCE_RADIUS = "FENCE_RADIUS";//限制围栏半径
    public static final String PILOT_SPEED_UP = "PILOT_SPEED_UP";//限制上升速度
    public static final String PILOT_SPEED_DN = "PILOT_SPEED_DN";//限制下降速度
    public static final String WPNAV_LOIT_SPEED = "WPNAV_LOIT_SPEED";//限制水平速度
    public static final String NEWCOMER_MODE = "NEWCOMER_MODE";//安全保护开关(新手模式)0关闭 1打开
    public static final String FP_ACTIVATION = "FP_ACTIVATION";//激活指令0未激活，1激活
    public static final String WPNAV_SPEED = "WPNAV_SPEED";//航点速度设定500cm-1000cm，单次最少设置50cm


}
            