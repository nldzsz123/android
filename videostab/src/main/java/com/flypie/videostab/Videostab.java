package com.flypie.videostab;

public class Videostab {

    static {
        System.loadLibrary("VideoStab");
    }

    /**
     * 图片合成视频
     */
    public static native int[] picsToVideo(String[] filePath, int fileSize, String outPath,int delayResolutio);
}
