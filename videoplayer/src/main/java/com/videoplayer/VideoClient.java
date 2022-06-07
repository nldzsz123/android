package com.videoplayer;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

/**
 * Created by player on 16/9/2.
 */

public class VideoClient {

    private boolean mRun = false;
    private VideoPullThread thread;

    public VideoClient(String mUrl, boolean enableHardDecode) {
        NativeCode.init(this, mUrl, enableHardDecode);
    }

    interface VideoClientCallback {
        void connectStatus(final int error, final int w, final int h);

        void processYUVFrame(final byte[] ydata, final byte[] udata, final byte[] vdata, int w, int h);
    }

    private VideoClientCallback mListoner;

    public void setListoner(VideoClientCallback listoner) {
        this.mListoner = listoner;
    }

    // 开启子线程 开始获取视频流
    public void startPullVideo() {
        DDLog.log("startPullVideo");
        if (mRun) {
            DDLog.log("startPullVideo 已经在开始播放了");   //一个对象只有一个视频播放流=
            return;
        }
        mRun = true;

        if (thread == null) {
            thread = new VideoPullThread();
        }
        thread.start();
    }

    // 停止获取视频流，并且关闭线程
    public void stopPullVideo() {
        DDLog.log("stopPullVideo");
        if (mRun) {
            NativeCode.stop();
        }
        mRun = false;

//        if (thread != null) {
//            boolean retry = true;
//            while (retry) {
//                try {
//                    // TODO: 2019/9/28 这里这句造成ANR，需要修复
//                    thread.join();
//                    retry = false;
//                } catch (InterruptedException e) {
//                }
//            }
            thread = null;
//        }
    }

    public boolean isRunning() {
        return mRun;
    }

    // idrBytes 具体的视频帧数据
    // bitmap 用于ImageView 使用的bitmap
    static public Bitmap confertFromIdr(byte[] idrBytes, float scale) {
        byte[] rgbBytes = NativeCode.nativeFillBitmap(idrBytes, scale);
        if (rgbBytes == null)
            return null;
        DDLog.log("rgb数据长度==>" + rgbBytes.length + "高==>" + heigth + " 宽==>" + width);

        return rgb2Bitmap(rgbBytes, width, heigth);
    }

    static int width = 0;
    static int heigth = 0;

    static public void setWH(int w, int h) {
        width = w;
        heigth = h;
    }

    /**
     * @方法描述 将RGB字节数组转换成Bitmap，
     */
    static public Bitmap rgb2Bitmap(byte[] data, int width, int height) {
        int[] colors = convertByteToColor(data);    //取RGB值转换为int数组
        if (colors == null) {
            return null;
        }

        Bitmap bmp = Bitmap.createBitmap(colors, 0, width, width, height,
                Bitmap.Config.RGB_565);
        return bmp;
    }


    // 将一个byte数转成int
    // 实现这个函数的目的是为了将byte数当成无符号的变量去转化成int
    public static int convertByteToInt(byte data) {

        int heightBit = (int) ((data >> 4) & 0x0F);
        int lowBit = (int) (0x0F & data);
        return heightBit * 16 + lowBit;
    }


    // 将纯RGB数据数组转化成int像素数组
    public static int[] convertByteToColor(byte[] data) {
        int size = data.length;
        if (size == 0) {
            return null;
        }

        int arg = 0;
        if (size % 3 != 0) {
            arg = 1;
        }

        // 一般RGB字节数组的长度应该是3的倍数，
        // 不排除有特殊情况，多余的RGB数据用黑色0XFF000000填充
        int[] color = new int[size / 3 + arg];
        int red, green, blue;
        int colorLen = color.length;
        if (arg == 0) {
            for (int i = 0; i < colorLen; ++i) {
                red = convertByteToInt(data[i * 3]);
                green = convertByteToInt(data[i * 3 + 1]);
                blue = convertByteToInt(data[i * 3 + 2]);

                // 获取RGB分量值通过按位或生成int的像素值
                color[i] = (red << 16) | (green << 8) | blue | 0xFF000000;
            }
        } else {
            for (int i = 0; i < colorLen - 1; ++i) {
                red = convertByteToInt(data[i * 3]);
                green = convertByteToInt(data[i * 3 + 1]);
                blue = convertByteToInt(data[i * 3 + 2]);
                color[i] = (red << 16) | (green << 8) | blue | 0xFF000000;
            }

            color[colorLen - 1] = 0xFF000000;
        }

        return color;
    }

    // ============== 获取视频流的线程 ==================//
    private class VideoPullThread extends Thread {
        public void run() {

            DDLog.log("开始获取视频了。。。");
            while (mRun) {
                NativeCode.play();
            }
            DDLog.log("结束获取视频了。。。");
        }
    }
    // ============== 获取视频流的线程 ==================//

    public void update(final byte[] ydata, final byte[] udata, final byte[] vdata, int w, int h) {
        if (mListoner != null) {
            mListoner.processYUVFrame(ydata, udata, vdata, w, h);
        }
    }

    public void connectStatus(final int error, final int w, final int h) {
        DDLog.log("error=" + error + " w=" + w + " h=" + h);
        if (mListoner != null) {
            mListoner.connectStatus(error, w, h);
        }
    }
}
