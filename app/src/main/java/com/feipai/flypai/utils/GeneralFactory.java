package com.feipai.flypai.utils;

import android.database.Cursor;
import android.provider.MediaStore;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.beans.FileBean;
import com.feipai.flypai.utils.global.FileUtils;
import com.feipai.flypai.utils.global.LogUtils;

import java.io.File;

public class GeneralFactory {
    public static String getRemoteIp(String ip) {
        if (ip.contains(".2.")) {
            return ConstantFields.PLANE_IP.NEW_IP;
        }
        return ConstantFields.PLANE_IP.OLD_IP;
    }

    public static String getLocalCamerBinPath() {
        return FileUtils.getSdPaths(ConstantFields.SD_DIR.FP_BIN)
                + "/" + ConstantFields.SD_FILE_NAME.FP_CAMERA_BIN;
    }

    public static String getLocalYuntaiBinPath() {
        return FileUtils.getSdPaths(ConstantFields.SD_DIR.FP_BIN)
                + "/" + ConstantFields.SD_FILE_NAME.FP_YUNTAI_BIN;
    }


    /**
     * 获取飞控版本号
     */
    public static String getPlaneSerialNumber(short[] flight_custom_version, short[] middleware_custom_version) {
        StringBuilder sb = new StringBuilder();
        for (short s : flight_custom_version) {
            String temp = Integer.toHexString(s & 0xFF);
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            sb.append(temp);
        }
        for (int i = 0; i < 4; i++) {
            String temp1 = Integer.toHexString(middleware_custom_version[i] & 0xFF);
            if (temp1.length() == 1) {
                temp1 = "0" + temp1;
            }
            sb.append(temp1);
        }
        return sb.toString();
    }


    /**
     * 获取飞机机型
     *
     * @param ssid 飞行器wifi名称
     */
    public static int getPlaneType(String ssid) {
        if (ssid.startsWith(ConstantFields.DATA_CONFIG.FLYPAI_NAME_START)) {

        }
        return ConstantFields.DATA_CONFIG.FLY_4K;
    }
}
