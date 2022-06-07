package com.feipai.flypai.utils.socket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import com.feipai.flypai.utils.global.LogUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EasySocket {

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private String ip;
    private int port;
    private int sendBuffSize;
    private int receiveBufferSize;
    private Callback callback;


    private Socket mSocket;
    private InputStream is = null;
    private InputStreamReader isr = null;
    private BufferedReader br = null;
    private OutputStream os = null;

    public Boolean isConnected = false;
    private Thread mThread;
    private byte[] buffer = new byte[1024];

    private String TAG = "Mlog";

    private Thread watchThread = null;

    private Boolean isAutoConnect = true;


    /**
     * 是否开启心跳监测
     * 开启心跳功能后，每隔一段间隔{@link EasySocket.heartInterval}发送心跳包{@link EasySocket.heartPackage}。
     * 同时检测等待服务器返回应答的时间间隔是否超过最大等待时间{@link EasySocket.maxSeverResponseHeartOutTime}。
     * 如果超时，将会重连。
     */
    private Boolean needHeart;

    /**
     * 心跳包发送间隔
     */
    private long heartInterval;

    /**
     * 最长等待服务器回应时间。
     * 此超时时间应该比心跳包发送间隔短{@link EasySocket.heartInterval}
     */
    private long maxSeverResponseHeartOutTime;

    /**
     * 心跳包内容
     */
    private byte[] heartPackage;

    //最后的发送时间
    private long last_send_time = 0;

    //最后的接收时间
    private long last_rec_time = 0;

    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);

    public EasySocket(Builder builder) {
        this.ip = builder.ip;
        this.port = builder.port;
        this.sendBuffSize = builder.sendBuffSize;
        this.receiveBufferSize = builder.receiveBufferSize;
        this.callback = builder.callback;
        this.needHeart = builder.needHeart;
        this.maxSeverResponseHeartOutTime = builder.maxSeverResponseHeartOutTime;
        this.heartInterval = builder.heartInterval;
        this.heartPackage = builder.heartPackage;
    }


    public void connect() {
        disconnectSocketIfNecessary();

        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {

            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    realconnect();
                }
            });

        } else {
            realconnect();
        }

        //连接了socket之后，才创建监听进程。
        openWatchThread();

    }

    private void realconnect() {
        try {

            mSocket = new Socket(ip, port);
            mSocket.setSendBufferSize(sendBuffSize);
            mSocket.setReceiveBufferSize(receiveBufferSize);
            mSocket.setTcpNoDelay(true);

            Boolean isConnect = mSocket.isConnected();

            if (isConnect) {

                is = mSocket.getInputStream();
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);

                os = mSocket.getOutputStream();

                isConnected = true;

                Log.d(TAG, "isConnected");
                mainHandler.post(() -> {
                    callback.onConnected();
                });

                //创建监听线程
                openThread();
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            mainHandler.post(() -> {
                callback.onError("断开连接异常");
            });
        }
    }


    private void disconnectSocketIfNecessary() {
        try {
            if (mSocket != null) {
                isConnected = false;
                closeThread();

                if (!mSocket.isClosed()) {
                    if (!mSocket.isInputShutdown()) {
                        mSocket.shutdownInput();
                    }
                    if (!mSocket.isOutputShutdown()) {
                        mSocket.shutdownOutput();
                    }

                    if (br != null) {
                        br.close();
                        br = null;
                    }
                    if (isr != null) {
                        isr.close();
                        isr = null;
                    }
                    if (is != null) {
                        is.close();
                        is = null;
                    }
                    if (os != null) {
                        os.close();
                        os = null;
                    }

                    mSocket.close();
                }
                mSocket = null;
                mainHandler.post(() -> {
                    callback.onDisconnected();
                });

                Log.d(TAG, "onDisconnected");
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();

        }
    }


    public void disconnect() {
        disconnectSocketIfNecessary();
        closeWatchThread();
    }

    private void closeThread() {
        if (mThread != null) {
            isConnected = false;
            mThread.interrupt();
            mThread = null;
            Log.d(TAG, "close thread");
        }
    }

    private void closeWatchThread() {
        if (watchThread != null) {
            isAutoConnect = false;
            watchThread.interrupt();
            watchThread = null;
            Log.d(TAG, "close watchThread");
        }
    }

    private void openThread() {
        closeThread();
        mThread = new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (isConnected) {
                    try {
                        int readLen = 0;

                        readLen = is.read(buffer);
                        if (readLen > 0) {
//                            if (port == 8787)
//                                LogUtils.d("读取数据===>" + readLen);
                            byte[] data = new byte[readLen];
                            System.arraycopy(buffer, 0, data, 0, readLen);


//                            mainHandler.post(() -> {
                            if (callback != null)
                                callback.onReceived(data);
//                            });

                            if (needHeart) {
                                last_rec_time = System.currentTimeMillis();
                            }
                        }

                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                        mainHandler.post(() -> {
                            callback.onError("读取数据异常");
                        });
                    }

                }
            }
        });
        mThread.start();
    }

    private void openWatchThread() {
        //closeWatchThread();
        if (watchThread != null) {
            return;
        }
        watchThread = new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (isAutoConnect) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }


                    try {

                        if (needHeart) {
                            /**
                             * 当前时间距离上次发送时间（任意发送）的间隔大于最大心跳间隔时，发送心跳。
                             */
                            if (System.currentTimeMillis() - last_send_time > heartInterval) {
                                sendHeart(heartPackage);
                            }
                            //若当前发送的时间比上次接收的时间新，而且间隔maxHearTime没有收到应答
                            /**
                             * 此处判断是否满足重连条件：
                             * 1、发送时间是否比接收时间晚
                             * 2、当前时间距离上次发送间隔超过最大等待时间
                             */
                            if (last_send_time > last_rec_time && System.currentTimeMillis() - last_send_time > maxSeverResponseHeartOutTime) {
                                isConnected = false;
                            }
                        }


                        if (isConnected) {

                        } else {
//                            //未连接的情况下，重新连接服务器
//                            mainHandler.post(() -> {
//                                callback.onReconnected();
//                            });
//
//                            disconnectSocketIfNecessary();
//                            realconnect();
                        }


                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                        Log.d(TAG, "onError");
                    }

                }
            }
        });
        watchThread.start();
    }


    /**
     * 发送命令
     *
     * @param msg 信息
     */
    public void send(final byte[] msg, int lenth) {

        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    realsend(msg, lenth);
                }
            });

        } else {
            realsend(msg, lenth);
        }

    }

    private void realsend(byte[] msg, int lenth) {

        try {
            os.write(msg, 0, lenth);
            os.flush();

            if (needHeart) {
                last_send_time = System.currentTimeMillis();
            }

            if (mSocket.isInputShutdown() || mSocket.isOutputShutdown()) {
                isConnected = false;
            }


            mainHandler.post(() -> {
                callback.onSend();
            });

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            mainHandler.post(() -> {
                callback.onError("发送失败");
            });
        }
    }


    /**
     * 发送心跳包
     *
     * @param i
     */
    private void sendHeart(byte[] i) {
        try {
            os.write(i);
            os.flush();
            Log.d(TAG, "send Heart");

            if (needHeart) {
                last_send_time = System.currentTimeMillis();
            }

            if (mSocket.isInputShutdown() || mSocket.isOutputShutdown()) {
                isConnected = false;
            }

            mainHandler.post(() -> {
                callback.onSendHeart();
            });
        } catch (Exception e) {
            // TODO: handle exception
            Log.d(TAG, "sendHeart fail");
            isConnected = false;
        }
    }


    /**
     * 配置构造器
     */
    public static class Builder {

        private String ip;
        private int port;
        private int sendBuffSize;
        private int receiveBufferSize;
        private Callback callback;

        /**
         * 是否开启心跳监测
         * 开启心跳功能后，每隔一段间隔{@link heartInterval}发送心跳包{@link heartPackage}。
         * 同时检测等待服务器返回应答的时间间隔是否超过最大等待时间{@link maxSeverResponseHeartOutTime}。
         * 如果超时，将会重连。
         */
        private Boolean needHeart = false;

        /**
         * 心跳包发送间隔
         */
        private long heartInterval = 10000;

        /**
         * 最长等待服务器回应时间。
         * 此超时时间应该比心跳包发送间隔短{@link heartInterval}
         */
        private long maxSeverResponseHeartOutTime = 5000;

        /**
         * 心跳包内容
         */
        private byte[] heartPackage = new byte[]{0xA};


        public String getIp() {
            return ip;
        }

        public Builder setIp(String ip) {
            this.ip = ip;
            return this;
        }

        public int getPort() {
            return port;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public int getSendBuffSize() {
            return sendBuffSize;
        }

        public Builder setSendBuffSize(int sendBuffSize) {
            this.sendBuffSize = sendBuffSize;
            return this;
        }

        public int getReceiveBufferSize() {
            return receiveBufferSize;
        }

        public Builder setReceiveBufferSize(int receiveBufferSize) {
            this.receiveBufferSize = receiveBufferSize;
            return this;
        }

        public Callback getCallback() {
            return callback;
        }

        public Builder setCallback(Callback callback) {
            this.callback = callback;
            return this;
        }


        public Boolean getNeedHeart() {
            return needHeart;
        }

        public Builder setNeedHeart(Boolean needHeart) {
            this.needHeart = needHeart;
            return this;
        }

        public long getHeartInterval() {
            return heartInterval;
        }

        public Builder setHeartInterval(long heartInterval) {
            this.heartInterval = heartInterval;
            return this;
        }


        public long getMaxSeverResponseHeartOutTime() {
            return maxSeverResponseHeartOutTime;
        }

        public Builder setMaxSeverResponseHeartOutTime(long maxSeverResponseHeartOutTime) {
            this.maxSeverResponseHeartOutTime = maxSeverResponseHeartOutTime;
            return this;
        }

        public byte[] getHeartPackage() {
            return heartPackage;
        }

        public Builder setHeartPackage(byte[] heartPackage) {
            this.heartPackage = heartPackage;
            return this;
        }

        public EasySocket build() {

            if (this.ip == null || this.ip.isEmpty()) {
                throw new IllegalStateException("ip is invalid");
            }

            if (this.port == 0 || this.port > 65535) {
                throw new IllegalStateException("port is invalid");
            }

            if (needHeart) {
                if (maxSeverResponseHeartOutTime >= heartInterval) {
                    throw new IllegalStateException("maxSeverResponseHeartOutTime must be less than heartInterval");
                }

                if (maxSeverResponseHeartOutTime < 2000) {
                    throw new IllegalStateException("maxSeverResponseHeartOutTime must >= 2000");
                }

                if (heartPackage == null || heartPackage.length < 1) {
                    throw new IllegalStateException("heartPackage can not be null or empty.");
                }
            }

            return new EasySocket(this);

        }


    }

}
