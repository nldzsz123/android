package com.feipai.flypai.utils.socket;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.base.BaseCameraMsg;
import com.feipai.flypai.beans.ABCmdValue;
import com.feipai.flypai.utils.GeneralFactory;
import com.feipai.flypai.utils.global.CloseUtils;
import com.feipai.flypai.utils.global.CommandUtil;
import com.feipai.flypai.utils.global.JsonUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.NetworkUtils;
import com.feipai.flypai.utils.gsonlib.MGson;
import com.feipai.flypai.utils.threadutils.ThreadUtils;
import com.videoplayer.NativeCode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by YangLin on 2016-11-09.
 */

public class CameraSocket {

    private int PORT = ConstantFields.FLY_PORT.CAMERA_PORT;
    private String IP;
    private static final long SLEEP_TIME = 2000;
    private Socket mClient = new Socket();
    private InputStream mIn;
    private OutputStream mOut;
    private boolean close = true;
    private CameraSocketReadListener mRl;
    private static final int STATE_SUCCESS = 1;
    private static final int STATE_FAILED = 2;
    private static final int STATE_WAIT = 3;
    private static final int STATE_CLOSE = 4;
    private static final int STATE_RECONN = 5;
    private static final int STATE_DESTORY = 6;
    private int state_code = 0;
    private Lock mLock = new ReentrantLock();
    private Condition mNotif = mLock.newCondition();
    private long mHz = 1000 / 15;
    private static CameraSocket mTcp;

    private boolean isReturn = false;

    private RequestWorker requestWorker;
    private ReadTask readTask;
    private ConnectRunnable connectRunnable;


    private Vector<TcpReadListener> mReads = new Vector<TcpReadListener>();

    //队列，封装发送的信息
    private static ArrayBlockingQueue<BaseCameraMsg> queue = new ArrayBlockingQueue<BaseCameraMsg>(8);


    private List<BaseCameraMsg> msgs = new ArrayList<>();

    private static Handler mStickHandler;
    private int mStickCount = 0;

    /**
     * 添加 关闭 观察者
     */
//    public void setStateListener(ReadTCPDataInterface sif) {
//        // mStateListener=sif;
//        mReads.add(sif);
//    }

    /**
     * 移除 关闭 观察者
     */
//    public void removeStateListener(ReadTCPDataInterface sif) {
//        mReads.remove(sif);
//    }
    private CameraSocket() {
//        createSendTask();
    }

    /**
     * 获取实例
     */
    public static CameraSocket getInstance() {
        if (mTcp == null) {
            synchronized (CameraSocket.class) {
                if (mTcp == null) {
                    mTcp = new CameraSocket();
                }
            }
        }
        return mTcp;
    }


    public void setReadListener(CameraSocketReadListener r) {
        mRl = r;
    }

    public void removeReadListener() {
        mRl = null;
    }

    public void connect(final int timeOut, String ip, int port) {
        this.IP = ip;
        this.PORT = port;
        if (state_code == STATE_SUCCESS)
            return;
        state_code = 0;
        if (mClient != null && mClient.isConnected()) {
            return;
        }
        if (mClient == null || mClient.isClosed() || !mClient.isConnected()) {
            LogUtils.d("连接 7878端口");
            if (mClient != null)
                mClient = null;
            mClient = new Socket();
        }
        close = false;

        // 无论数据包多大，立刻发送AVControlActivity
        try {
            mClient.setTcpNoDelay(true);
            mClient.setSendBufferSize(1024);
            mClient.setReceiveBufferSize(102400);
            mClient.setTcpNoDelay(true);
        } catch (SocketException e2) {
            e2.printStackTrace();
        }
        if (connectRunnable == null)
            connectRunnable = new ConnectRunnable();
        // 启动线程去建立连接
        if (!connectRunnable.isRunning()) {
            ThreadUtils.run(connectRunnable);
        }
//        MLog.log("跳出重连----->");
    }

    public class ConnectRunnable implements Runnable {
        private boolean isRunning;

        public ConnectRunnable() {
            this.isRunning = false;
        }

        public boolean isRunning() {
            return isRunning;
        }

        @Override
        public void run() {
            LogUtils.d("CameraSocket 连接状态到底是？---" + state_code);
            while (state_code != STATE_DESTORY) {
                isRunning = true;
                boolean isNetWork = NetworkUtils
                        .isWifiConnected();
                if (!isNetWork) {
                    LogUtils.d("没有网络，正在睡眠");
                    SystemClock.sleep(SLEEP_TIME);
                    LogUtils.d("没有网络，正在重新检查");
                    continue;
                }
                LogUtils.d("CameraSocket有网络，正在连接");
                try {
                    queue.clear();
                    if (mClient != null && !mClient.isConnected())
                        mClient.connect(new InetSocketAddress(IP, PORT),
                                2000);
                    mIn = mClient.getInputStream();
                    mOut = mClient.getOutputStream();

                    if (mRl != null && mClient.isConnected() && mIn != null) {
                        mRl.connecSuccess();
                    }
                    close = false;
                    state_code = STATE_SUCCESS;
                    // 启动读消息线程
                    if (readTask == null)
                        readTask = new ReadTask();
                    if (!readTask.isRunning())
                        ThreadUtils.run(readTask);
                    if (requestWorker == null)
                        requestWorker = new RequestWorker();
                    if (!requestWorker.isRunning())
                        ThreadUtils.run(requestWorker);
                    LogUtils.d("SocketH有网络，连接成功 ip："
                            + mClient.getInetAddress().getHostAddress());
                    break;
                } catch (Exception e) {
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
                    state_code = STATE_FAILED;
                    LogUtils.d("有网络，连接失败，等待2秒后重新连接");
                    // 2秒后重新连接
                    SystemClock.sleep(SLEEP_TIME);
                }
            }
            isRunning = false;
        }
    }


    public void clearMsg() {
        if (queue != null)
            queue.clear();
    }


    /**
     * type 1 断开重新连接 0不重新连接
     */
    public void closeAll(int type) {
        mStickCount = 0;
        state_code = type > 0 ? STATE_RECONN : STATE_DESTORY;
        if (mClient != null && !mClient.isClosed()) {
            try {
                close = true;
                mClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mClient = null;
        }
        if (readTask != null)
            ThreadUtils.remove(readTask);
        if (requestWorker != null)
            ThreadUtils.remove(requestWorker);
    }

    private void createSendTask() {
        ThreadUtils.run(() -> {
            while (STATE) {
                STATE = false;
                Looper.prepare();
                mStickHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (state_code != STATE_SUCCESS || mOut == null)
                            return;
                        if (msg.obj != null) {
                            byte[] data = ((String) msg.obj).getBytes();
                            synchronized (CameraSocket.class) {
                                mStickCount--;
                            }
                            try {
                                LogUtils.d("发送次数----");
                                if (data != null) {
                                    mOut.write(data, msg.arg1, msg.arg2);
                                    mOut.flush();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                };
                Looper.loop();
            }
        });
    }

    public void setStateSuccess(boolean state) {
        STATE = state;
    }


    boolean STATE = true;

    public void sendData(String msg) {
        if (state_code != STATE_SUCCESS)
            return;
        if (msg != null) {
            Message message = mStickHandler.obtainMessage();
            message.what = 0;
            message.obj = msg;
            synchronized (CameraSocket.class) {
                mStickCount++;
                if (mStickCount > ConstantFields.DATA_CONFIG.MAX_STICK_COUNT) {
                    mStickCount = 0;
                    mStickHandler.removeMessages(0);
                }
            }
            message.sendToTarget();
        }
    }

    /**
     * 传输文件到服务器
     */
    public void sendFile(String path) {
        try {
            if (path != null) {
                FileInputStream inputStream = new FileInputStream(new File(path));
                int len;
                long count = inputStream.available();
                while (count == 0) {
                    count = inputStream.available();
                }
                int packetSize = 10240;//这里指定每包为10Kbyte
                byte[] buf = new byte[packetSize];
                int packet = 0;
                int packetCount = (int) (count / ((long) packetSize));//总包数
                //判断是否读到文件末尾
                while ((len = inputStream.read(buf)) != -1) {
                    Thread.sleep(100);//每隔1s执行一次
                    packet++;
                    LogUtils.d("发送的长度--->" + packet + "||" + packetCount + "||" + len);
                    Message message = mStickHandler.obtainMessage();
                    message.what = 1;
                    message.obj = buf;
                    message.arg1 = 0;
                    message.arg2 = len;
                    synchronized (CameraSocket.class) {
                        mStickCount++;
                        if (mStickCount > ConstantFields.DATA_CONFIG.MAX_STICK_COUNT) {
                            mStickCount = 0;
                            mStickHandler.removeMessages(0);
                        }
                    }
                    message.sendToTarget();
//                    mOut.write(buf, 0, len);//将文件循环写入输出流
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 添加请求
     *
     * @param msg
     */
    public synchronized void send(BaseCameraMsg msg) {
        LogUtils.d("添加消息====>" + msg.getMsgId());
        if (msg != null) {
            msgs.clear();
            msgs.add(msg);
            mStickCount = 0;
//            if (requestWorker == null)
//                requestWorker = new RequestWorker();
//            if (!requestWorker.isRunning()) {
//                LogUtils.d("每次发送的时候还添加一次发送线程" + msg.getMsgId());
//                ThreadUtils.run(requestWorker);
//            }
        }
    }

    public void requestRulste(boolean isReturn) {
        this.isReturn = isReturn;
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
                //take是个阻塞式方法，所以必须是用while(true)
                while (state_code == STATE_SUCCESS) {
                    isRunning = true;
                    if (msgs.size() < 1 || mStickCount > 0)
                        continue;
                    if (mClient != null && mClient.isConnected()) {
                        mOut = mClient.getOutputStream();
                        if (mOut != null) {
                            synchronized (CameraSocket.class) {
                                if (msgs.size() > 0) {
                                    BaseCameraMsg msg = msgs.get(0);
                                    if (msg.getJsonMsg() == null) {
                                        LogUtils.d("-----》" + msg.getMsgId() + "|||" + msg.getJsonMsg() + "|||" + mClient.isConnected() + "||" + msgs.size());
                                        msgs.clear();
                                        mStickCount = 0;
                                    }
                                    if (msg != null && msg.getJsonMsg() != null) {
                                        LogUtils.d("发送消息---->" + msg.getJsonMsg() + "||" + msgs.size() + "|||" + mStickCount);
                                        mStickCount = 0;
                                        mStickCount++;
                                        mOut.write(msg.getJsonMsg().getBytes());
                                        mOut.flush();
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                msgs.clear();
                mStickCount = 0;
                LogUtils.d("发送失败" + e.getMessage());
//                throw new RuntimeException("发送失败:" + e.getMessage());
            }
//            catch (InterruptedException e) {
//            LogUtils.d("信息被中断" + e.getMessage());
//                throw new RuntimeException("信息被中断:" + e.getMessage());
//            }
            isRunning = false;
            LogUtils.d("跳出请求----->");
        }
    }

    private boolean heartbeat = false;
    private int heartTime = 1;

    /**
     * 取消stick发送
     */
    public void unBindSendHeartbeat(boolean heartbeat) {
        this.heartbeat = heartbeat;
        bindHeartFail();
    }

    public void bindHeartFail() {
        try {
            if (state_code != STATE_CLOSE) {
                if (mClient != null && !mClient.isClosed()) {
                    mClient.close();
                    mClient = null;
                }
                if (mIn != null) {
                    mIn.close();
                    mIn = null;
                }
                if (mOut != null) {
                    LogUtils.d("关闭输出流");
                    mOut.close();
                    mOut = null;
                }
//                if (heartbeat) {
//                    state_code = STATE_RECONN;
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private sendHeartbeatTask task;
    private int setIpCount;
    public boolean needSetIp = false;
    String stick;

    public void bindSendStick() {
        LogUtils.d("开始bindSendStick");
        stick = "abcdefg";
        heartTime = 1;
        state_code = 0;
        heartbeat = true;
        if (task == null)
            task = new sendHeartbeatTask();
        if (!task.isRunnig())
            ThreadUtils.run(task);
    }

    private class sendHeartbeatTask implements Runnable {


        private boolean isRunnig = false;

        public sendHeartbeatTask() {
            this.isRunnig = false;
        }

        public boolean isRunnig() {
            return isRunnig;
        }

        public void setRunnig(boolean run) {

        }

        @Override
        public void run() {
            while (heartbeat) {
                isRunnig = true;
                String pingIp = GeneralFactory.getRemoteIp(NetworkUtils.getIPAddress(true));
                String ping = CommandUtil.getPacketLoss(pingIp, 2, 1000);
//                SystemClock.sleep(1000);
                if (ping != null && ping.indexOf("received, 0%") != -1) {       // ping通了
//                    LogUtils.d("ping 通了");
                    if (pingIp.contains(".2.") && setIpCount <= 3) {  // 新遥控 避免频繁创建socket 影响性能
                        setIpCount++;
                        // 对于新遥控，ping 通之后新将ip发送给遥控
                        if (NativeCode.initSocket(pingIp, 1234, 3)) {
                            LogUtils.d("initSocket成功");
                            if (NativeCode.setIp(pingIp)==0 && needSetIp) {
                                LogUtils.d("IP设置成功");
                            } else {
                                LogUtils.d("IP设置失败");
                            }
                        } else {
                            LogUtils.d("initSocket失败");
                        }
                    }
                    heartTime = 1;
//                    MLog.log("ping通咯----" + state_code);
                    if (state_code != STATE_RECONN && state_code != STATE_DESTORY) {
                        if (close)
                            connect(0, pingIp, PORT);       // ping通了之后则去连接7878端口
                    }
                } else {
                    heartTime++;
                    setIpCount = 0;
                    if (heartTime >= 3) {
//                        LogUtils.d("ping不通咯----" + heartbeat);
                        if (heartTime >= 3) {
                            bindHeartFail();
                        }
                    }
//                    SystemClock.sleep(1000);
                }
                isRunnig = false;
//                MLog.log("跳出心跳----");
            }
        }
    }


    // 接收任务
    private class ReadTask implements Runnable {
        private byte[] buff = new byte[1024 * 3];
        String bufStr = null;
        private int size = 0;

        private boolean isRunning;

        private boolean isRunning() {
            return isRunning;
        }

        private ReadTask() {
            this.isRunning = false;
            LogUtils.d("sokethelp 创建读取线程");
        }

        @Override
        public void run() {

            while (state_code == STATE_SUCCESS) {
//                LogUtils.d("读取消息-----");
                isRunning = true;
                try {
                    mIn = mClient.getInputStream();
                    if (mIn != null) {
                        size = mIn.read(buff);
                        if (size > 0) {
                            if (msgs.size() < 1)
                                continue;
                            String buffStr = new String(buff, 0, size);
                            synchronized (CameraSocket.class) {
                                LogUtils.d("原始消息---->" + buffStr);
                                parsingPackage(buffStr);
                            }
                        } else {
                            LogUtils.d("异常，服务器主动断开");
                            mStickCount = 0;
                            datas = "";
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
                            close = true;
                            break;
                        }
                    }
                } catch (Exception e) {
                    mStickCount = 0;
                    msgs.clear();
                    LogUtils.d("连接断开，主动" + e.getMessage());
                    close = true;
                    if (state_code != STATE_DESTORY)
                        state_code = STATE_CLOSE;
                    e.printStackTrace();
                    datas = "";
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
                    CloseUtils.closeIO(mIn, mOut);

                    // return;
                }
            }
            isRunning = false;
            LogUtils.d("跳出读取----->");
            // 重新连接
//            if (state_code != STATE_DESTORY)
//                state_code = STATE_RECONN;
//            if (mRl != null)
//                mRl.close();
//            if (state_code != STATE_DESTORY)
//                connect(0, IP, PORT);
        }

    }

    public byte[] toByteArray(InputStream input) throws IOException {

        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int rc = 0;
        while ((rc = input.read(buff, 0, 1024)) > 0) {
            LogUtils.d("读取中....");
            swapStream.write(buff, 0, rc);
        }
        byte[] bytes = swapStream.toByteArray();
        return bytes;

    }


    String datas = "";

    /**
     * 解析初始包
     */
    private synchronized void parsingPackage(String data) {
        datas = datas + data;
        if (!datas.contains("}{")) {
            if ((datas.startsWith("{\"rval\"") && datas.endsWith("}]}") //判定是完整的媒体库返回
                    || datas.endsWith("[]}"))//判定是空的媒体库返回
                    || ((!datas.contains("listing") && !datas.contains("MP4") && !datas.contains("JPG")) && datas.endsWith("}"))//判定是普通的设置请求
                    || (datas.startsWith("{\"rval\"") && !datas.contains("},{"))) {//判断是媒体库单文件请求
                readSocket(datas);
                datas = "";
            } else {
                LogUtils.d("断包一次" + datas);
            }
        } else {
            LogUtils.d("出现粘包问题");
            String b = datas.substring(0, datas.indexOf("}{") + 1);
            String c = datas.substring(datas.indexOf("}{") + 1);
            datas = "";
            LogUtils.d("粘包重新打印" + b + "||" + c);
            parsingPackage(b);
            if (c != null && !c.equals("") && c.length() > 0) {
                LogUtils.d("粘包再打印1---" + c);
                c.substring(2);
                LogUtils.d("粘包再打印2---" + c);
                parsingPackage(c);
            }
        }
    }

    private synchronized void readSocket(String data) {
        if (JsonUtils.isGoodJson(data)) {
            LogUtils.d("读取一整条消息---->" + data);
            if (msgs.size() > 0 && state_code == STATE_SUCCESS) {
                BaseCameraMsg msg = msgs.get(0);
                msgs.clear();
//                LogUtils.d("解析出一条消息---->" + data);
                if (msg != null && msg.getCallback() != null) {
                    ABCmdValue cb = MGson.newGson().fromJson(data, ABCmdValue.class);
                    if (msg.getMsgId() == cb.getMsg_id()) {
                        msg.getCallback().onComplete(data);
                        mStickCount--;
                    } else {
                        mRl.read(data.length(), data);
                    }
                } else {
                    mRl.read(data.length(), data);
                }
            }
        }
    }
}
