package com.feipai.flypai.utils.global;


import android.os.Handler;
import android.os.Message;

public class BaseTimer {
    private TimerCallBack timerCallBack = null;
    private boolean timerRun = false;
    private boolean bInterval = false;
    private long curMsecond = 0;

    /**
     * 回调接口定义
     */
    public interface TimerCallBack {
        public void callbackThread(Handler timerHandler);

        public void callbackMainThread(Message msg);
    }

    private Handler timerHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (timerCallBack != null) {
                timerCallBack.callbackMainThread(msg);
            }
            timerRun = false;
            if (bInterval) {
                startTimer(curMsecond, timerCallBack);
                bInterval = true;
            }
            super.handleMessage(msg);
        }

    };

    private Runnable keyRunnable = new Runnable() {
        @Override
        public void run() {
            timerCallBack.callbackThread(timerHandler);
        }
    };

    /**
     * 关闭定时器
     */
    public void killTimer() {
        bInterval = false;
        timerRun = false;
        try {
            timerHandler.removeCallbacks(keyRunnable);
        } catch (Exception e) {

        }

    }

    /**
     * 启动延时器
     *
     * @param msecond 毫秒
     * @param cb      回调函数
     */
    public void startTimer(int msecond, TimerCallBack cb) {
        killTimer();
        curMsecond = msecond;
        timerRun = true;
        timerCallBack = cb;
        timerHandler.postDelayed(keyRunnable, curMsecond);
    }

    /**
     * 启动延时器
     *
     * @param msecond 毫秒
     * @param cb      回调函数
     */
    public void startTimer(long msecond, TimerCallBack cb) {
        killTimer();
        timerRun = true;
        timerCallBack = cb;
        timerHandler.postDelayed(keyRunnable, msecond);
    }

    /**
     * 启动定时器
     *
     * @param msecond 毫秒
     * @param cb      回调函数
     */
    public void startInterval(int msecond, TimerCallBack cb) {
        startTimer(msecond, cb);
        bInterval = true;
    }

    /**
     * 获取定时器是否运行
     */
    public boolean isRunning() {
        return timerRun;
    }
}