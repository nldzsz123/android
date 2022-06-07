package com.feipai.flypai.app;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 常量自段
 *
 * @author chenshige
 */
public interface ConstantFields {

    /**
     * App配置相关
     */
    interface APP_CONFIG {
        // Bugly AppID
        String BUGLY_APP_ID = "b8b9e74569";
        String UMENG_QQ_ID = "1106338778";
        String UMENG_QQ_KEY = "vvMJplXvOS5swaiU";
        String UMENG_WX_ID = "wx5b6600336c93348f";
        String UMENG_WX_SECRET = "584e445551194565d46c86f17320b79a";//"653a98d7cf26085b217d2e924f7ba204";
        String UMENG_SINA_ID = "2259625108";
        String UMENG_SINA_KEY = "9d2e4e2b79588ab0f9be99ea00896cba";
        String UMENG_SINA_DEF_URL = "http://sns.whalecloud.com";
        String UMENG = "UMENG_APPKEY";

        // 日志打印Tag
        String DEBUG_TAG = "logger";
        // 缓存目录
        String CACHE_DIR = "cache";
        // ACache 缓存存放的目录名
        String ACACHE_DIR_NAME = "acache";
        String CACHE_BIG_THUMB_NAME_START = "big";
        // SharedPrefUtils 存储的默认名
        String SHARED_PREF_NAME = "fly_pref";

        String APP_VERSION_CODE = "android_app";
        String LOG_CACHE_DIR_PATH = "FLYMEDIA/log";


        /**
         * 等待基础时间
         */
        long WAIT_TIME = 1000L;
    }

    /**
     * 广播相关字段
     */
    interface BROADCAST {

    }

    /**
     * SharedPref相关字段
     */
    interface PREF {
        String FLY_PIE_TYPE = "FLY_PIE_TYPE";
        String CHACK_VERSION_TIME = "CHACKVERSIONTIME";//检查版本时间
        String PHOTO_MODE = "PHOTOMODE";//拍照模式
        String PHOTO_ISO = "PHOTOISO";//切换成自动ISO之前的ISO存储键
        String VIDEO_MODE = "VIDEOMODE";//录制模式
        String SHOW_GRID = "SHOWGRID";//网格显示
        String CURRENTPLAYMODE = "CURRENTPLAYMODE";//当前播放模式
        String GLIDE_KEY = "GLIDE_KEY";//glide key
        String PANOR_GLIDE_KEY = "PANOR_GLIDE_KEY";//glide key
        String DELAY_TIME = "DelayTime";//延迟拍摄时间
        String LOGGED_IN = "LoggedIn";//已登陆
        String USER_PHONE_NUMB = "UserPhoneNumb";//用户此时登陆的电话号码
        String Grid_type = "Grid_type";
        String PLANE_TYPE = "PLANE_TYPE";
    }

    /**
     * 事件类型
     */
    interface BusEventType {
        /**
         * 与飞行器断开
         */
        String WIFI_BREAK_IN_BACKGRAOUND = "0";
        /**
         * 与飞行器连接上
         */
        String WIFI_CONNTENTED = "1";
        /**
         * 断开WIFI
         */
        String WIFI_DISCONNECT = "2";
        /**
         * 刷新网络强弱
         */
        String WIFI_RSSI_CHANGE = "3";
        /**
         * 与其他WIFI连接上
         */
        String WIFI_OTHER_CONNECTED = "4";

        /**
         * 更新版本号
         */
        String UPDATE_VERSION_VALUE = "5";


        String UPDATE_VERSION_VALUE_USER_FRAGMENT = "6";

        /**
         * 刷新WIFI列表
         */
        String REFRESH_WIFI_DATA = "7";
        /**
         * session成功
         */
        String SESSION_SUCCESS = "8";
        /**
         * socket close
         */
        String CAMERA_SOCKET_CLOSE = "9";

        /**
         * 是否激活
         */
        String ACT_RESPONSE = "10";

        /**
         * 读取相机socket
         */
        String READ_CAMERA_SOCKET = "11";
        /**
         * 显示与隐藏数字键盘
         */
        String SHOW_KEYBOARD = "12";
        /**
         * 键盘输入
         */
        String KEYBOARD_EDITOR = "13";
        /**
         * 键盘输入
         */
        String KEYBOARD_DELECT = "14";
        /**
         * 切换到飞控电池页面
         */
        String SWITCH_BATTERY_TAB = "15";
        /**
         * 切换到飞控电池页面
         */
        String FIND_PLANE = "16";

        String RESET_HOME_POINT = "17";
    }

    interface FLY_PORT {
        /**
         * Mavlink端口
         */
        int PLANE_PORT = 8686;
        /**
         * 相机端口
         */
        int CAMERA_PORT = 7878;
        /**
         * 相机数据端口
         */
        int DATA_PORT = 8787;
    }

    interface PLANE_IP {
        /**
         * 久版遥控器
         */
        String OLD_IP = "192.168.42.1";
        /**
         * 新版遥控器
         */
        String NEW_IP = "192.168.2.1";
    }

    interface ACTION_PARAM {
        int DOWNLOAD_SINGLE_FILE = 0;
        int DOWNLOADING_FILE = 1;
        int DOWNLOAD_FILES = 2;
        int DELECT_FILE = 3;
        int SYN_PANOR = 4;
        int SYN_DELAY = 5;
        int SYN_WIDE_ANGLE = 6;
        int UPDATA_UI = 7;
        int EDITOR_FILE = 8;
        int FROM_PLAYBACK_PREVIEW_WITH_ALL_FILES = 9;
        int FROM_PLAYBACK_PREVIEW_WITH_SINGLE_FILE = 10;
        int ADD_PANOR_PHOTO = 11;
        int RESET_RETURN_POINT = 12;

        /**
         * 陀螺仪校准
         */
        int START_CALIBRATION_GYRO = 13;
        int CANCEL_CALIBRATION_GYRO = 14;
        /**
         * 指南针校准
         */
        int START_CALIBRATION_COMPASS = 15;
        int CANCEL_CALIBRATION_COMPASS = 16;

        /**
         * 云台校准
         */
        int START_CALIBRATION_YUNTAI = 17;
        int CANCEL_CALIBRATION_YUNTAI = 18;
        int RETURN_BACK = 19;
        int START_UPGRADE_PLANE_FW = 20;
        int START_UPGRADE_CAMERA_FW = 21;
        int START_UPGRADE_YUNTAI_FW = 22;
        int START_UPGRADE_NET_ERROR = 23;
        int UPGRADE_IN_FLYING_ERROR = 24;
        int UPGRADE_NET_WORK_ERROR = 25;
        int UPGRADE_COMPLETE = 26;
        int UNPACK_CAMERA_BIN_ZIP = 27;
        int UPGRADE_ERROR = 28;
        int UPGRADE_COMPLETE_RESTART = 29;
        int UPGRADE_SD_ERROR_FOR_6KA = 30;

        int UPDATE_BATTERY_VALUE = 31;
        int UPDATE_SPEED_VALUE = 32;
        int COMPASS_STATE = 33;
        int GYROSCOPE_STATE = 34;
        int CALI_COMPASS_PROGRESS = 35;
        int CALI_COMPASS_SUCCESS = 36;
        int REMOVE_PANOR_PHOTO = 17;


    }

    interface DATA_CONFIG {
        int MAX_STICK_COUNT = 25;
        int FLY_2K = 0;
        int FLY_4K = 1;
        int FLY_6K = 2;
        String FLYPAI_NAME_START = "FP";
        String FLYPAI_NAME_START_4kAir = "FP4A";
        String FLYPAI_NAME_START_6kAir = "FP6A";
        String FLYPAI_NAME_START_6kPro = "FP6P";
    }

    interface PLANE_CONFIG {
        /**
         * 请求云台版本号返回
         */
        int TYPE_REQUEST_VERSION_RETURN = 1022;
        /**
         * 请求云台，飞控版本号
         */
        int TYPE_REQUEST_VERSION = 1021;
        // 后壁灯
        int TYPE_Houbideng = 1029;
        // 前臂灯
        int TYPE_Qianbideng = 1015;
        /**
         * 切换云台到转接模式
         */
        int TYPE_REBOOT_YUNTAI = 1020;

        /**
         * 激活飞控
         */
        int TYPE_PLANE_ACK = 1016;
    }

    interface CAMERA_CONFIG {
        int APP_STATUS = 1;
        int CAMERA_SET = 2;
        int FORMAT_SD = 4;
        int ALL_SETTING = 3;
        int SDCARD_FREE = 5;
        int UPLOAD_RESPONSE = 7;
        int DETECTIOND_FW = 8;
        int PHOTO_VR = 9;
        int START_SESSION = 257;
        int RESET_VF = 259;
        int STOP_VF = 260;
        int DATA_SOCKET = 261;
        int START_RECORD = 513;
        int STOP_RECORD = 514;
        int RECORED_TIME = 515;
        int TAKE_PANOR_PHOTO_MSG = 772;
        int TAKE_TIME_LAPSE_MSG = 773;
        int TAKE_PHOTO = 769;
        int FILE_THUMB = 1025;
        int VIDEO_PARAMETERS = 1026;
        int DELETE_FILE = 1281;
        int MEDIA_DATA = 1282;
        int START_SEND_FILE = 1286;
        int DELECT_PANORAMA_FOLDER = 1290;
        int RENAME_FILE = 1291;
        int ROB_LINE = 1793;
        int CHANGE_MODE = 2049;
        int SD_INSERT = 2305;
        int SD_PULL_OUT = 2306;
        int SD_FULL = 2307;
        int SD_NOT_INIT = 2308;
        int SD_SPEED_SLOW = 2309;
        int SD_FRAGMENTATION = 2310;
        int SD_LOW = 2311;
        int SPOT_METER = 2050;
        int SAVE_PARAM = 2051;
        int CAMERA_RESTART = 2052;
        int LAST_FILE = 2054;
        int SD_INTERNAL = 2055;
    }

    interface SD_DIR {
        String DIR = "FLYDOWN";
        String LUBAN_THUMB_DIR = "/Luban/image/";
        String GLHA_NAME = "panoramic_glha";//格力海岸
        String FHL_NAME = "panoramic_fhl";//富华里
        String FP_MEDIA = "FLYMEDIA";
        String FP_IMAGE = "FLYMEDIA/image";
        String FP_SKY_IMAGE = "FLYMEDIA/sky";
        String FP_BITMAP = "FLYMEDIA/bitmap";
        String FP_TUTORIAL = "FLYMEDIA/tutorial";
        String FP_VIDEO = "FLYMEDIA/video";
        String FP_MUSIC = "FLYMEDIA/music";
        String PANOR = "panoramic";
        String PANOR_CROPPED = "pcropped";
        String TIME = "time";
        String WIDE = "wide";
        String VR = "VR";
        String FP_PANORAMIC = "FLYMEDIA/panoramic";
        String FP_BIN = "FLYMEDIA/bin";
        String FP_DB = "FLYMEDIA/db";
        String FP_YUNTAI_BIN = "FP_3AixsGimbal.bin";
        String FP_PANORAMIC_IMAGE = "FLYMEDIA/pan";

        String VR_PRO = "VR6PRO/";
        String VR_PRO_PHOTO = "VR6PRO/Photo";
        String VR_PRO_MOVIE = "VR6PRO/Movie";
        String VR_PRO_MOVIE_CACHE = "VR6PRO/Cache";
        String VR6_PANORAMA = "VR6_Panorama/";
        String VR6_PANORAMAT = "VR6_Panorama/T";
        String VR6_PANORAMAV = "VR6_Panorama/V";
        String MEDIA_START = "/tmp/SD0/";

        // 4K air 6k系列相机目录
        String VR_PRO_PHOTO_NEW = "FlyPie/Photo";
        String VR_PRO_MOVIE_NEW = "FlyPie/Movie";
        String VR_PRO_MOVIE_CACHE_NEW = "FlyPie/Cache";
        String VR6_PANORAMA_NEW = "FP_AI/";
        String VR6_PANORAMAT_NEW = "FP_AI/T";
        String VR6_PANORAMAV_NEW = "FP_AI/V";
        String MEDIA_START_Internal = "/tmp/FL0/";  // 内置卡 6k系列
        String MEDIA_END = " -D -S";
        String VR6_PRO_DIR = "DCIM/";
    }

    interface SD_FILE_NAME {
        String FP_CAMERA_BIN = "FPFW.bin";
        String FP_PLANE_BIN = "planebin.px4";
        String FP_YUNTAI_BIN = "FP_3AixsGimbal.bin";
        String FP_CAMERA_BIN_6kair = "FPFW_6kair.bin";
        String FP_PLANE_BIN_6k = "planebin_6kair.px4";
        String FP_YUNTAI_BIN_6k = "FP_3AixsGimbal_6kair.bin";
        String FP_CAMERA_BIN_4kair = "FPFW_4kair.bin";
        String FP_PLANE_BIN_4kair = "planebin_4kair.px4";
        String FP_YUNTAI_BIN_4kair = "FP_3AixsGimbal_4kair.bin";
    }

    interface UPGRADE_FW {
        /**
         * 检测版本号
         */
        int CHECK_VER = 0;
        /**
         * 检测激活状态
         */
        int CHECK_ACK = 1;
        /**
         * 上传飞控固件
         */
        int UPDATE_PLANE_FW = 2;
        /**
         * 上传云台固件
         */
        int UPDATE_YUNTAI_FW = 3;

        //开始上传飞控固件
        String START_UPGRADE_PLANE_FW = "START_UPGRADE_PLANE_FW";
        //更新飞控固件上传进度
        String UPDATE_PLANE_PROGRESS = "UPDATE_PLANE_PROGRESS";

        //上传飞控固件出错
        String UPDATE_PLANE_FW_ERROR = "UPDATE_PLANE_FW_ERROR";


        //开始上传相机固件
        String START_UPGRADE_CAMERA_FW = "START_UPGRADE_CAMERA_FW";
        //更新相机固件上传进度
        String UPDATE_CAMERA_PROGRESS = "UPDATE_CAMERA_PROGRESS";
        //相机固件上传成功
        String UPDATE_CAMERA_SUCCESS = "UPDATE_CAMERA_SUCCESS";
        //相机固件上传成功
        String UPDATE_CAMERA_SD_ERROR = "UPDATE_CAMERA_SD_ERROR";


        //开始上传云台固件
        String START_UPGRADE_YUNTAI_FW = "START_UPGRADE_YUNTAI_FW";
        //更新云台固件上传进度
        String UPDATE_YUNTAI_PROGRESS = "UPDATE_YUNTAI_PROGRESS";
        //云台固件上传成功
        String UPDATE_YUNTAI_SUCCESS = "UPDATE_YUNTAI_SUCCESS";

        //无人机已解锁
        String UAV_UNLOCK = "UAV_UNLOCK";
        String OLD_FW_PATH = "/tmp/SD0/FPFWCache.bin";
        String NEW_4K_FW_PATH = "/tmp/SD0/FPFW.bin";
        String NEW_4KA_FW_PATH = "/tmp/SD0/FP02FW.bin";
        String NEW_6KA_FW_PATH = "/tmp/SD0/VR6XFW.bin";

    }

    interface MESSAGE_WHAT {
        //更新版本号
        int UPDATE_VERSION = 100;
        //切换底部button
        int SWITCH_MAIN_TAB = 101;

        int READ_MAVLINK_MESSAGE = 102;
        /**
         * 通知刷新下载进度
         */
        int UPDATE_DOWNLOAD_PROGRESS = 103;

        int NOTIFY_ADAPTER = 104;

        int LOAD_IMGA = 105;

        int DIALOG_DISMISS = 106;

        int SHOW_TOAST = 107;

        int DOWNLOAD_SUCCESS = 108;

        int DELECT_SUCCESS = 109;
        int DELECT_SINGLE_SUCCESS = 120;

        int NOTIFY_ADAPTER_AT_POSITION = 121;

        int ADD_SKY_RUSULT = 122;

    }

    interface ACTIVATION_FRAGMENT {
        int STATEMENT = 0;
        int WARRANTY = 1;
        int NOOB = 2;
        int ACTIVATE_SUCCESS = 3;
    }

    interface DB_TABLE_NAME {
        String USER_INFO = "user_info";
        String PLANE_INFO = "plane_info";
        String YUN_URL = "yun_info";
    }

    interface INTENT_PARAM {
        String PAGE_TYPE = "page_type";
        String PAGE_CODE = "page_CODE";
        int VIDEO_TEACH = 0;
        int SPECIFICATION = 1;
        int REAL_NAME_REGISTRATION = 2;
        int REAL_NAME_REGISTRATION_TEACH = 3;
        int APP_SETTING = 4;
        int USER_AGREEMENT = 5;
        int PRIVACY_AGREEMENT = 6;
        int OUTLINE_MAP = 7;
        int ADVERTISING_4KAIR = 8;
        int ADVERTISING_6KAIR = 9;
        String PREVIEW_FILES = "preview_files";
        String INDEX = "index";
        String FILE_TYPE = "filetype";

        String CROP_PANOR = "croppanor";
        String VIDEO_URL = "video_url";

    }

    interface REQUEST_CODE {
        int REQUEST_COUNTRY_CODE = 100;
    }

    interface RESULT_PARAM {
        String COUNTRY_CODE_KEY = "COUNTRY_CODE";
        String COUNTRY_KEY = "COUNTRY";
        int PHOTO_PICKED_FROM_FILE = 1;
        int PHOTO_PICKED_FROM_CAMERA = 2;
    }

    /**
     * 文件类型
     */
    interface FILE_TYPE {
        //照片

        int TYPE_PHOTO = 0;
        //视频
        int TYPE_VIDEO = 1;
        //全景照片
        int TYPE_PANOR = 2;
        //全景照片组
        int TYPE_PANOR_DIR = 3;
        //广角文件夹组
        int TYPE_WIDE_ANGLE_DIR = 4;
        //延时视频
        int TYPE_DELAY = 5;
        //延时视频文件夹
        int TYPE_DELAY_DIR = 6;

        //头部展开收起
        int TYPE_DIR = 7;

        int TYPE_EMPTY = 8;

        /**
         * 列表分组尾部填充空对象名称
         */
        String EMPTY_FILE_NAME = "empty";
        /**
         * 延时视频文件最小的
         * 默认最少48张为一个延迟视频
         * 方便测试用1
         */
        int DEALY_TIME_FILE_MIN_SIZE = 48;
        /**
         * 全景文件夹组15张
         */
        int PNAORS_SIZE15 = 15;
        /**
         * 全景文件夹组14
         */
        int PNAORS_SIZE14 = 14;
        /**
         * 广角文件组
         */
        int WIDE_SIZE = 6;
    }

    /**
     * 相机设置项参数
     */
    interface CAMERA_CMD {
        String SET_CAMERA_TIME = "camera_clock";
        String FORMAT_SD = "format_sd";
        //抗闪烁
        String SET_FREQUENCY = "frequency";
        String SET_VIDEO_MODE = "video_mode";
        String SET_VIDEO_WB = "video_wb";
        //Video色彩
        String SET_VIDEO_COLOR = "video_color";
        //Video EV
        String SET_VIDEO_EV = "video_ev";
        //Video对比度
        String SET_VIDEO_CONTRAST = "video_contrast";
        //Video锐度
        String SET_VIDEO_SHARPNESS = "video_sharpness";
        //Video饱和度
        String SET_VIDEO_SATURATION = "video_saturation";
        //Video分辨率
        String SET_VIDEO_RESOLUTION = "video_resolution";
        //视频焦距
        String SET_VIDEO_ZOOM = "video_zoom";
        //视频中央矩阵曝光
        String SET_VIDOE_METER = "video_meter";
        //视频曝光锁定
        String SET_VIDEO_AE_LOCKED = "video_3a_lock";
        //video iso
        String SET_VIDEO_ISO = "video_iso";
        //video_image_mode
        String SET_VIDEO_IMAGE_MODE = "video_image_mode";
        //video_shutter
        String SET_VIDEO_SHUTTER = "video_shutter";
        //拍照RAW格式
        String SET_PHOTO_RAW = "photo_raw";
        //全景模式
        String SET_PHOTO_VR_MODE = "photo_vr_mode";
        //延时拍摄
        String SET_TIME_LAPSE_MODE = "photo_time_lapse_mode";
        //照片大小
        String SET_PHOTO_SIZE = "photo_size";
        //photo白平衡
        String SET_PHOTO_WB = "photo_wb";
        //photo色彩
        String SET_PHOTO_COLOR = "photo_color";
        //photo EV
        String SET_PHOTO_EV = "photo_ev";
        //photo meter
        String SET_PHOTO_METER = "photo_meter";
        //photo对比度
        String SET_PHOTO_CONTRAST = "photo_contrast";
        //photo锐度
        String SET_PHOTO_SHARPNESS = "photo_sharpness";
        //photo饱和度
        String SET_PHOTO_SATURATION = "photo_saturation";
        //photo iso
        String SET_PHOTO_ISO = "photo_iso";
        //photo shutter
        String SET_PHOTO_SHUTTER = "photo_shutter";
        //photo_image_mode
        String SET_PHOTO_IMAGE_MODE = "photo_image_mode";
        //照片焦距
        String SET_PHOTO_ZOOM = "photo_zoom";
        //照片锁定曝光
        String SET_PHOTO_AE_LOCKED = "photo_3a_lock";
        //恢复默认选项
        String SET_DEFAULT_SETTING = "default_setting";
    }

    interface PANORAMIC_SYN_LEVEL {
        float VERY_SHARPNESS = 1.5f;
        float SUPER_SHARPNESS = 2.0f;
        float STANDARD_SHARPNESS = 4.0f;
    }

    interface ASSETSS_DIR {

        String STICKERS_DIR = "stickers";
        String PANOR_SKY_THUMB = "stickers/panorskythumb";
        String PANOR_SKY_THUMB_SUNNY = "stickers/panorskythumb/sunny";
        String PANOR_SKY_THUMB_CLOUDY = "stickers/panorskythumb/cloudy";
        String PANOR_SKY_THUMB_TWILIGHT = "stickers/panorskythumb/twilight";
        String PANOR_SKY_THUMB_NIGHT = "stickers/panorskythumb/night";
        String PANOR_SKY_THUMB_SPECIAL = "stickers/panorskythumb/special";
        String PANOR_SKY_UTILITY = "stickers/panorskyutility";
        String GLHA_PANOR = "panoramic/panoramic_glha.jpg";
        String FHL_PANOR = "panoramic/panoramic_fhl.jpg";

        String PANOR_MUSIC_DIR = "musics";
        String PANOR_MUSIC_BG_DIR = "musicsbg";

        String FW_CAMERA_4K = "appfws/camera_fw_4k.zip";
        String FW_PLANE_4K = "appfws/fws_4k/planebin_4k.px4";
        String FW_YUNTAI_4K = "appfws/fws_4k/yt_fw_4k.bin";

        String FW_CAMERA_4KA = "appfws/camera_fw_4ka.zip";
        String FW_PLANE_4KA = "appfws/fws_4ka/planebin_4ka.px4";
        String FW_YUNTAI_4KA = "appfws/fws_4ka/yt_fw_4ka.bin";

        String FW_CAMERA_6KA = "appfws/camera_fw_6ka.zip";
        String FW_PLANE_6KA = "appfws/fws_6ka/planebin_6ka.px4";
        String FW_YUNTAI_6KA = "appfws/fws_6ka/yt_fw_6ka.bin";
    }

    interface FUNCTION_TAB {
        int ADD_SKY = 0;
        int ADJUST = 1;

    }

    interface ADJUST_TYPE {
        int BRIGHTNESS = 0;//亮度
        int CONTRAST = 1;//对比度
        int SATURATION = 2;//饱和度
    }


    // 产品型号
    int ProductType_4k = 0;
    int ProductType_4kAir = 1;
    int ProductType_6kAir = 2;

    @IntDef({ProductType_4k, ProductType_4kAir, ProductType_6kAir})
    @Retention(RetentionPolicy.SOURCE)
    @interface ProductType {
    }

    interface TESTWIFI {
        String[] wifi_for4kAir = {/*"FP002650", "FP002312", "FP003422", "FP4A3682", "FP4A3683", "FP4A3815", "FP4A3802", */"FP000118","FP6A5220"};
        String[] wifi_for6kAir = {"FP003441", "FP003444", "FP100015", "FP4A1234"/*, "FP003444", "FP002312", "FP003421", "FP003456", "FP100015", "FP4A3681", "FP4A3803", "FP4A3693", "FP003430", "FP4A3792", "FP4A3793", "FP4A3683", "FP4A3679", "FP4A3681"*/};
    }

    interface PANORACMIC_SHARE_PARAMS {
        String TAG_IP = "202.108.22.5";//111.231.117.153
        String PANORAMIC_SHARE = "tour/";
        int TIME_OUT = 1000 * 300;
    }

    interface UMENG_SHARE_TYPE {
        int MEDIA_IMG = 0;
        int MEDAI_VIDEO = 1;
        int MEDIA_WEB = 2;
    }

    interface PLANE_SETTING_TAB {
        int FLIGHT_CONTROL_TAB = 0;
        int CALIBRATION_TAB = 1;
        int BATTERY_TAB = 2;
        int SEARCH_PLANE_TAB = 3;
    }

    interface HINT_LAYOUT_TYPE {
        int LOW_BATTERY = 0;
        int REMAINING_FLIGHT_TIME = 1;
        int LOCATION_ERROR = 2;
    }

    interface SHELL_IP {
        String IP41 = "192.168.4.1";
        String IP421 = "192.168.4.1";
        String IP424 = "192.168.42.4";
    }

    interface SHELL_CMD {
        // 预计带宽
//        String PING_CMD = "iw wlan0 station dump  | grep \"signal:\" | awk  '{print $2}'";
        String PING_CMD = "iw wlan0 station dump  | grep \"expected throughput:\" | awk  ' {print $3}' | awk -F '[.]' '{print $1}'";
        String UIC_SET_CMD = "uci set wireless.radio0.txpower=30";
        String UIC_COMMIT_CMD = "uci commit wireless";
        String UIC_GET_CMD = "uci get wireless.radio0.txpower";
        String RELOAD_CONFIG_CMD = "reload_config";

    }


}
