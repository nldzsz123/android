package com.videoplayer;

import android.view.Surface;

public class NativeCode {

    static {
        System.loadLibrary("H264Player");
    }

    static public native void setSurface(Surface surface);

    static public native boolean init(Object callback, String url, boolean enbaleHardDecode);

    static public native void play();

    static public native void stop();

    static public native void decode();

    static public native void weekup();

    static public native void releaseResources();

    static public native byte[] nativeFillBitmap(byte[] idrBytes, float atio);

    // type 为0时设置Ip type为1时获取RSSI
    static public native boolean initSocket(String ip, int port, int timeout);

    static public native int setIp(String ip);

    static public native boolean duiping(String ssid);

    static public native String getRSSI();

    /**
     * 初始化AVI转MP4工具
     */
    public native static void aviToMp4(String inputString, String outputPath);
}
