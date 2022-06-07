package com.videoplayer;

import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;

import java.security.PublicKey;

/**
 * Created by feipai1 on 20/04/2017.
 */

public class VideoPlayer {

    private VideoClient     videoClient;
    private GLSurfaceView   mGLSurfaceView;
    private GLHardRender    mGLHardRender;
    private GLSoftRender    mGLSoftRender;
    private boolean         mEnableHardDecode;
    public static final int GridType_None = 0;
    public static final int GridType_JGG = 1;
    public static final int GridType_JGGDJX = 2;

    DecodeThread mDecodeThread;
    private boolean mRunDecodeThreaad;
    private int curWidth;
    private int curHeight;

    /**
     *  当调用 VideoPlayer的 startPlay 的回调
     *  onConnectSucces 表示成功连接上服务器 并且正常开始播放的回调 只会调用一次
     *  onConnectError 表示与服务器连接断开(可能是连接链路断开了，也可能是读取数据超时了)，当出现这种情况后，播放器内部会自动调用VideoPLayer的
     *  stopPlay方法，所以不需要再回调里面再次去调用stop方法。如果要重新开始播放 只需要再次调用startPlay方法或者先调用initURL然后再次调用startPlay方法即可
     */
    public interface PlayCallback {
        void onConnectSucces(int width, int height);
        void onConnectError();
    }
    private VideoPlayer.PlayCallback mListioner;

    /**
     * 基于GLSurfaceView实现
     * 视频播放器的初始化方法
     * url:播放视频的地址 完整的地址
     * view:视频显示的位置
     * callback:视频播放过程中的回调
     * */
    public VideoPlayer(GLSurfaceView view, boolean enableHardDecode,VideoPlayer.PlayCallback callback) {
        mGLSurfaceView = view;
        mListioner = callback;

        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setFocusable(true);

        // 这里创建并且开启了解码线程
        if (enableHardDecode) {
            mGLHardRender = new GLHardRender();
            mGLSurfaceView.setRenderer(mGLHardRender);
        } else {
            mGLSoftRender = new GLSoftRender(view);
            mGLSurfaceView.setRenderer(mGLSoftRender);
            mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);  // 硬解码时不能设置这个值
        }

        mEnableHardDecode = enableHardDecode;
        curWidth = -1;
        curHeight = -1;
    }

    public void init(String url) {
        if (videoClient != null) {
            DDLog.log("stopPlay();");
            stopPlay();
        }
        videoClient = new VideoClient(url,mEnableHardDecode);
    }

    /**
     * 开始播放视频 在前面的初始化方法完成后调用
     * */
    public void startPlay() {
        DDLog.log("startPlay();");
        if (videoClient != null) {
            videoClient.startPullVideo();
            videoClient.setListoner(mVideoClient);
        }

        // 同时开启解码线程
        startDecodeThread();
    }

    // 设置网格线
    public void setGridType(int gridType) {
        mGLSoftRender.setGridType(gridType);
    }

    private void startDecodeThread() {
        mRunDecodeThreaad = true;
        if (mDecodeThread == null) {
            mDecodeThread = new DecodeThread();
            mDecodeThread.start();
        }

    }

    /**
     * 停止播放视频 与startPlay()对应调用
     * */
    public void stopPlay() {
        DDLog.log("stopPlay");
        if (videoClient != null) {
            videoClient.stopPullVideo();
        }
        mRunDecodeThreaad = false;
        NativeCode.weekup();
        if (mDecodeThread != null) {
            try {
                mDecodeThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mDecodeThread = null;
        }
        DDLog.log("stopPlay over");
    }

    /**
     * 现在是否正在播放 一次只能播放一个视频流
     * */
    public boolean isPlayer() {
        if (videoClient != null) {
            return videoClient.isRunning();
        }
        return false;
    }

    private Handler mainHander = new Handler(Looper.getMainLooper());
    // 拉流客户端的回调
    private VideoClient.VideoClientCallback mVideoClient = new VideoClient.VideoClientCallback() {
        // error 0代表连接正常 其它值代表连接断开 超时等错误
        @Override
        public void connectStatus(final int error, final int w, final int h) {
            curWidth = w;
            curHeight = h;

            mainHander.postAtFrontOfQueue(new Runnable() {
                @Override
                public void run() {
                    if (mListioner != null) {
                        if (error == 0) {
                            mListioner.onConnectSucces(w, h);
                        } else {
                            mListioner.onConnectError();
                        }
                    }
                }
            });
        }

        // 用于渲染 此函数执行在渲染线程中 软解码才有，硬解码不会执行此函数
        @Override
        public void processYUVFrame(byte[] ydata, byte[] udata, byte[] vdata, int w, int h) {
            mGLSoftRender.update(ydata,udata,vdata,w,h);
            if (curWidth != w || curHeight != h) {
                curWidth = w;
                curHeight = h;
                mainHander.postAtFrontOfQueue(new Runnable() {
                    @Override
                    public void run() {
                        if (mListioner != null) {
                            mListioner.onConnectSucces(curWidth, curHeight);
                        }
                    }
                });
            }
        }
    };

    // ============== 解码的线程 ==================//
    private class DecodeThread extends Thread {

        @Override
        public void run() {
            super.run();

            while (mRunDecodeThreaad) {
//                DDLog.log("开始解码");
                // 在这里进行软硬解码的切换
                NativeCode.decode();
//                DDLog.log("结束解码");
            }
            DDLog.log("解码线程结束咯");
        }
    }
    // ============== 解码的线程 ==================//



    private class PlayerStatus {
        private boolean isInited;
        private boolean isGLViewInited;
        private boolean isRunning;
        private boolean pause;
        private boolean finish;
        private boolean hasImage;

        private PlayerStatus() {
            isInited = false;
            isGLViewInited = false;
            isRunning = false;
            pause = false;
            finish = false;
            hasImage = false;
        }

        public boolean isInited() {
            return isInited;
        }

        public void setInited(boolean inited) {
            isInited = inited;
        }

        public boolean isGLViewInited() {
            return isGLViewInited;
        }

        public void setGLViewInited(boolean GLViewInited) {
            isGLViewInited = GLViewInited;
        }

        public boolean isRunning() {
            return isRunning;
        }

        public void setRunning(boolean running) {
            isRunning = running;
        }

        public boolean isPause() {
            return pause;
        }

        public void setPause(boolean pause) {
            this.pause = pause;
        }

        public boolean isFinish() {
            return finish;
        }

        public void setFinish(boolean finish) {
            this.finish = finish;
        }

        public boolean isHasImage() {
            return hasImage;
        }

        public void setHasImage(boolean hasImage) {
            this.hasImage = hasImage;
        }
    }
}

