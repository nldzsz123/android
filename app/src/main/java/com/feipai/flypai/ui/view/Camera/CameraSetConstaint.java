package com.feipai.flypai.ui.view.Camera;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class CameraSetConstaint {
    public final static int cell_photo_mode_key = 1;
    public final static int cell_photo_Atio_key = 2;
    public final static int cell_photo_advanced_key = 3;
    public final static int cell_photo_ruidu_key = 4;
    public final static int cell_photo_baohedu_key = 5;
    public final static int cell_photo_duibidu_key = 6;
    public final static int cell_photo_raw_key = 7;
    public final static int cell_photo_yj_key = 8;
    public final static int cell_photo_isoShutterAuto_key = 9;
    public final static int cell_photo_baoguang_key = 10;
    public final static int cell_photo_iso_key = 11;
    public final static int cell_photo_shutter_key = 12;
    public final static int cell_video_record_hd_key = 13;
    public final static int cell_video_Dimesion_key = 14;
    public final static int cell_video_Dimesion_6k_key = 15;
    public final static int cell_video_Dimesion_C4k_key = 16;
    public final static int cell_video_Dimesion_4k_key = 17;
    public final static int cell_video_Dimesion_2dot7k_key = 18;
    public final static int cell_video_Dimesion_2dot5k_key = 19;
    public final static int cell_video_Dimesion_1080P_key = 20;
    public final static int cell_video_Dimesion_1080x1920_key = 21;
    public final static int cell_video_Dimesion_fps_key = 22;
    public final static int cell_video_colorStyle_key = 23;
    public final static int cell_video_colorStyle_std_key = 23;
    public final static int cell_video_colorStyle_dlog_key = 24;
    public final static int cell_video_advanced_key = 25;
    public final static int cell_video_ruidu_key = 26;
    public final static int cell_video_baohedu_key = 27;
    public final static int cell_video_duibidu_key = 28;
    public final static int cell_video_saveFormat_key = 29;
    public final static int cell_video_isoShutterAuto_key = 30;
    public final static int cell_video_baoguang_key = 31;
    public final static int cell_video_iso_key = 32;
    public final static int cell_video_shutter_key = 33;

    public final static int cell_genera_antiflicker_key = 34;
    public final static int cell_genera_grid_key = 35;
    public final static int cell_genera_sdcapcity_key = 36;
    public final static int cell_genera_reset_key = 37;
    public final static int cell_genera_format_key = 38;
    public final static int cell_genera_move_key = 39;

    public final static int cell_video_bit_key = 40;
    public final static int cell_zoom_switch_key = 41;
    public final static int cell_yt_switch_key = 42;
    public final static int cell_qianbideng_key = 43;
    public final static int cell_houbideng_key = 44;
    public final static int cell_yt_hori_set_key = 45;

    public final static String Video_Dimenssion_6k_5472x3072 = "5472x3072";
    public final static String Video_Dimenssion_4k_3840x2160 = "3840x2160";
    public final static String Video_Dimenssion_1080P_1920x1080 = "1920x1080";

    public final static String Video_fps_24 = "24fps";
    public final static String Video_fps_30 = "30fps";
    public final static String Video_fps_60 = "60fps";
    public final static String Video_fps_120 = "120fps";

    public final static String DefaultISOValue = "400";
    public final static String DefaultShutterValue = "1/40s";


    public final static int CameraModePhoto = 0;
    public final static int CameraModeMovie = 1;

    @IntDef({CameraModePhoto, CameraModeMovie})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CameraMode {
    }

    public final static int PhotoModeNormal = 0;
    public final static int PhotoModePano = 1;
    public final static int PhotoModeDelay = 2;

    @IntDef({PhotoModeNormal, PhotoModePano, PhotoModeDelay})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PhotoMode {
    }

    public final static int FPEncodeTypeH264 = 0;
    public final static int FPEncodeTypeH265 = 1;

    @IntDef({FPEncodeTypeH264, FPEncodeTypeH265})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FPEncodeType {
    }

    public final static int CameraValue_unknown = -1;      // 状态未知
    public final static int CameraValue_photo_vf = 0;      // 照片模式 可以直接打开流
    public final static int CameraValue_photo_idle = 1;    // 照片模式 reset vf 然后打开流
    public final static int CameraValue_idle = 2;          // 视频模式 reset vf 然后打开流
    public final static int CameraValue_record = 3;        // 视频模式 正在录制
    public final static int CameraValue_photo_mode = 4;    // 拍照模式 正在拍照
    public final static int CameraValue_vf = 5;            // 视频模式 可以直接打开流

    @IntDef({CameraValue_unknown, CameraValue_photo_vf, CameraValue_photo_idle, CameraValue_idle, CameraValue_record, CameraValue_photo_mode})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CameraValue {
    }


    public final static int CameraStatusDisconnect = 0;
    public final static int CameraStatusSDCardNotInsert = 1;
    public final static int CameraStatusSDCardFull = 2;
    public final static int CameraStatusDisable = 3;
    public final static int CameraStatusPhotoContinue = 4;
    public final static int CameraStatusPhotoModeNormal = 5;
    public final static int CameraStatusMovieModeNormal = 6;
    public final static int CameraStatusRecord = 7;
    public final static int CameraStatusInPano = 8;
    public final static int CameraStatusInYanshi = 9;
    public final static int CameraStatusInHuanrao = 10;
    public final static int CameraStatusInHand = 11;
    public final static int CameraStatusInGensui = 12;
//    public final static int CameraStatusSDCardLow = 13;//低速卡

    @IntDef({CameraStatusDisconnect, CameraStatusSDCardNotInsert, CameraStatusSDCardFull, CameraStatusDisable, CameraStatusPhotoContinue,
            CameraStatusPhotoModeNormal, CameraStatusMovieModeNormal, CameraStatusRecord, CameraStatusInPano, CameraStatusInYanshi,
            CameraStatusInHuanrao, CameraStatusInHand, CameraStatusInGensui})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CameraStatus {
    }

    public final static int SDCardStatusFull = -30;   // SD卡已满
    public final static int SDCardStatusNotInsert = 1;   // SD卡未插入
    public final static int SDCardStatusOK = 0;

    @IntDef({SDCardStatusFull, SDCardStatusNotInsert, SDCardStatusOK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SDCardStatus {
    }


}
