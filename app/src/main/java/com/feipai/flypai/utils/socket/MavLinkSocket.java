package com.feipai.flypai.utils.socket;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import com.feipai.flypai.api.MavLinkReadInterface;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.utils.GeneralFactory;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.NetworkUtils;
import com.feipai.flypai.utils.threadutils.ThreadUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by YangLin on 2017-12-15.
 */

public class MavLinkSocket {

    private int PORT = ConstantFields.FLY_PORT.PLANE_PORT;
    private static final long SLEEP_TIME = 2000;
    private Socket mClient = new Socket();
    private InputStream mIn;
    private OutputStream mOut;
    private boolean close = false;
    private MavlinkSocketReadListener mRl;
    private static final int STATE_SUCCESS = 1;
    private static final int STATE_FAILED = 2;
    private static final int STATE_WAIT = 3;
    private static final int STATE_CLOSE = 4;
    private static final int STATE_RECONN = 5;
    private static final int STATE_DESTORY = 6;
    private int state_code = 0;
    private static MavLinkSocket mTcp;

    private ConnectWorker connectWorker;


    private Vector<MavLinkReadInterface> mReads = new Vector<MavLinkReadInterface>();

    private static Handler mStickHandler;
    private int mStickCount = 0;
    private RequestWorker requestWorker;
    //队列，封装发送的信息
    private static ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(15);

    /**
     * 添加 关闭 观察者
     */
    public void setStateListener(MavLinkReadInterface sif) {
        // mStateListener=sif;
        mReads.add(sif);
    }

    /**
     * 移除 关闭 观察者
     */
    public void removeStateListener(MavLinkReadInterface sif) {
        mReads.remove(sif);
    }

    private MavLinkSocket() {
        createSendTask();
    }

    /**
     * 获取实例
     */
    public static MavLinkSocket getInstance() {
        if (mTcp == null) {
            synchronized (MavLinkSocket.class) {
                if (mTcp == null) {
                    mTcp = new MavLinkSocket();
                }
            }
        }
        return mTcp;
    }


    public void setReadListener(MavlinkSocketReadListener r) {
        mRl = r;

    }

    public void connect(final int timeOut) {
        if (state_code == STATE_SUCCESS)
            return;
        state_code = 0;
        if (mClient != null && mClient.isConnected()) {
            return;
        }
        if (mClient == null || mClient.isClosed()) {
            if (mClient != null)
                mClient = null;
            mClient = new Socket();
        }
        close = false;

        // 无论数据包多大，立刻发送
        try {
            if (mClient != null)
                mClient.setTcpNoDelay(true);
            if (mClient != null)
                mClient.setTcpNoDelay(true);
            if (mClient != null)
                mClient.setSendBufferSize(1);
        } catch (SocketException e2) {
            e2.printStackTrace();
        }
        // 启动线程去建立连接
        if (connectWorker == null)
            connectWorker = new ConnectWorker();
        if (!connectWorker.isRunning()) {
            ThreadUtils.run(connectWorker);
        }


    }


    /**
     * type 1 断开重新连接 0不重新连接
     */
    public void closeAll(int type) {

        state_code = type > 0 ? STATE_RECONN : STATE_DESTORY;
        LogUtils.d("当前关闭状态===>" + state_code);
        queue.clear();
        if (mClient != null && !mClient.isClosed()) {
            try {
                close = true;
                mClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mClient = null;
        }
    }

    private void createSendTask() {
        ThreadUtils.run(() -> {
            if (state_code != STATE_DESTORY) {
                Looper.prepare();
                mStickHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (state_code != STATE_SUCCESS || mOut == null)
                            return;
                        if (msg.obj != null) {
                            byte[] data = (byte[]) msg.obj;
                            synchronized (MavLinkSocket.class) {
                                mStickCount--;
                            }
                            try {
//                            MLog.log("发送次数----");
                                if (data != null)
                                    mOut.write(data);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                };
            }
            Looper.loop();
        });
    }

    public void send(byte[] packet) {
//        MLog.log("写入数据....");
        if (packet != null) {
            Message message = mStickHandler.obtainMessage();
            message.what = 0;
            message.obj = packet;
            synchronized (MavLinkSocket.class) {
                mStickCount++;
                if (mStickCount > ConstantFields.DATA_CONFIG.MAX_STICK_COUNT) {
                    mStickCount = 0;
                    mStickHandler.removeMessages(0);
                }
            }
            message.sendToTarget();
        }
    }

    public class ConnectWorker implements Runnable {
        private boolean isRunning;

        public ConnectWorker() {
            isRunning = false;
        }

        public boolean isRunning() {
            return isRunning;
        }

        @Override
        public void run() {
            LogUtils.d("Mavlinksocket状态到底是？---" + state_code);
            for (; state_code != STATE_DESTORY; ) {
                String ip = NetworkUtils.getIPAddress(true);
                boolean isNetWork = NetworkUtils
                        .isWifiConnected();
                if (!isNetWork) {
                    LogUtils.d("Mavlinksocket没有网络，正在睡眠");
                    SystemClock.sleep(SLEEP_TIME);
                    LogUtils.d("没有网络，正在重新检查");
                    continue;
                }
                LogUtils.d("MavlinkSocket有网络，正在连接");
                try {
                    if (mClient.isConnected())
                        break;
                    mClient.connect(new InetSocketAddress(GeneralFactory.getRemoteIp(ip), PORT),
                            2000);

                    mIn = mClient.getInputStream();
                    mOut = mClient.getOutputStream();
//                    if (requestWorker == null) {
//                        requestWorker = new RequestWorker();
//                    }
                    if (mRl != null && mClient.isConnected() && mIn != null) {
                        mRl.connecSuccess();
                    }
                    close = false;
                    state_code = STATE_SUCCESS;
                    // 启动读消息线程
                    ThreadUtils.run(new MavLinkSocket.ReadTask());
                    LogUtils.d("MavLink有网络，连接成功 ip："
                            + mClient.getInetAddress().getHostAddress());

                    break;
                } catch (Exception e) {
                    LogUtils.d("MavlinkSocket断开");
                    e.printStackTrace();
                    if (mRl != null)
                        mRl.connectFail();
                    close = true;
                    try {
                        if (mClient != null)
                            mClient.close();
                        mClient = null;
                        mClient = new Socket();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    if (state_code != STATE_DESTORY) {
                        state_code = STATE_FAILED;
                        LogUtils.d("有网络，连接失败，等待2秒后重新连接");
                        // 2秒后重新连接
                        SystemClock.sleep(SLEEP_TIME);

                    }
                }
            }
            isRunning = false;
        }
    }

    /**
     * 队列请求线程
     */
    public class RequestWorker implements Runnable {
        private boolean isRunning;

        private boolean isRunning() {
            return isRunning;
        }

        public RequestWorker() {
            this.isRunning = false;
        }

        @Override
        public void run() {
            try {
                if (mClient != null) {
                    //take是个阻塞式方法，所以必须是用while(true)
                    while (state_code == STATE_SUCCESS && mOut != null) {
                        isRunning = true;
                        LogUtils.d("Mavlink发送消息---->");
                        String content = queue.take();
                        LogUtils.d("Mavlink开始发送消息--->" + content);
                        mOut.write(content.getBytes());
                        mOut.flush();
//                        STATE = false;
                        break;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Mavlink发送失败:" + e.getMessage());
            } catch (InterruptedException e) {
                throw new RuntimeException("Mavlink信息被中断:" + e.getMessage());
            }
            isRunning = false;
            LogUtils.d("Mavlink跳出请求----->");
        }
    }

    // 接收任务
    private class ReadTask implements Runnable {
        private byte[] buff = new byte[1024];
        private int size = 0;

        @Override
        public void run() {

            for (; state_code != STATE_CLOSE && state_code == STATE_SUCCESS; ) {
                try {
//                    MLog.log("读取数据---长度---"+buff.length);
//                    mIn = mClient.getInputStream();
                    if (mIn != null) {
                        size = mIn.read(buff);
//                        String buffStr = new String(buff, 0, size);
                        if (size > 0 && mRl != null) {
                            byte[] cBuffer = new byte[size];
                            System.arraycopy(buff, 0, cBuffer, 0, size);
                            mRl.read(0, size, cBuffer);
                        } else {
                            LogUtils.d("MavlinkSocket异常，服务器主动断开" + "||" + state_code);
                            if (mRl != null) {
                                mRl.close();
                                // mRl.onConnectionError();
                            }
                            if (state_code != STATE_DESTORY)
                                state_code = STATE_CLOSE;
                            if (mClient != null) {
                                mClient.close();
                                mClient = null;
                            }
                            if (mIn != null) {
                                mIn.close();
                                mIn = null;
                            }
                            if (mOut != null) {
                                mOut.close();
                                mOut = null;
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    LogUtils.d("MavlinkSocket连接断开，主动" + e.getMessage() + "||" + state_code);
                    if (state_code != STATE_DESTORY)
                        state_code = STATE_CLOSE;
                    e.printStackTrace();
                    if (mRl != null)
                        mRl.close();
                    if (mClient != null) {
                        try {
                            mClient.close();
                            mClient = null;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    // return;
                }
            }

            // 重新连接
            if (state_code != STATE_DESTORY)
                state_code = STATE_RECONN;
            if (state_code != STATE_DESTORY)
                connect(0);
        }

    }

}
