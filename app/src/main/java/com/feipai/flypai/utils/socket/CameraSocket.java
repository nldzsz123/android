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

    //??????????????????????????????
    private static ArrayBlockingQueue<BaseCameraMsg> queue = new ArrayBlockingQueue<BaseCameraMsg>(8);


    private List<BaseCameraMsg> msgs = new ArrayList<>();

    private static Handler mStickHandler;
    private int mStickCount = 0;

    /**
     * ?????? ?????? ?????????
     */
//    public void setStateListener(ReadTCPDataInterface sif) {
//        // mStateListener=sif;
//        mReads.add(sif);
//    }

    /**
     * ?????? ?????? ?????????
     */
//    public void removeStateListener(ReadTCPDataInterface sif) {
//        mReads.remove(sif);
//    }
    private CameraSocket() {
//        createSendTask();
    }

    /**
     * ????????????
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
            LogUtils.d("?????? 7878??????");
            if (mClient != null)
                mClient = null;
            mClient = new Socket();
        }
        close = false;

        // ????????????????????????????????????AVControlActivity
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
        // ???????????????????????????
        if (!connectRunnable.isRunning()) {
            ThreadUtils.run(connectRunnable);
        }
//        MLog.log("????????????----->");
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
            LogUtils.d("CameraSocket ????????????????????????---" + state_code);
            while (state_code != STATE_DESTORY) {
                isRunning = true;
                boolean isNetWork = NetworkUtils
                        .isWifiConnected();
                if (!isNetWork) {
                    LogUtils.d("???????????????????????????");
                    SystemClock.sleep(SLEEP_TIME);
                    LogUtils.d("?????????????????????????????????");
                    continue;
                }
                LogUtils.d("CameraSocket????????????????????????");
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
                    // ?????????????????????
                    if (readTask == null)
                        readTask = new ReadTask();
                    if (!readTask.isRunning())
                        ThreadUtils.run(readTask);
                    if (requestWorker == null)
                        requestWorker = new RequestWorker();
                    if (!requestWorker.isRunning())
                        ThreadUtils.run(requestWorker);
                    LogUtils.d("SocketH???????????????????????? ip???"
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
                    LogUtils.d("?????????????????????????????????2??????????????????");
                    // 2??????????????????
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
     * type 1 ?????????????????? 0???????????????
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
                                LogUtils.d("????????????----");
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
     * ????????????????????????
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
                int packetSize = 10240;//?????????????????????10Kbyte
                byte[] buf = new byte[packetSize];
                int packet = 0;
                int packetCount = (int) (count / ((long) packetSize));//?????????
                //??????????????????????????????
                while ((len = inputStream.read(buf)) != -1) {
                    Thread.sleep(100);//??????1s????????????
                    packet++;
                    LogUtils.d("???????????????--->" + packet + "||" + packetCount + "||" + len);
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
//                    mOut.write(buf, 0, len);//??????????????????????????????
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
     * ????????????
     *
     * @param msg
     */
    public synchronized void send(BaseCameraMsg msg) {
        LogUtils.d("????????????====>" + msg.getMsgId());
        if (msg != null) {
            msgs.clear();
            msgs.add(msg);
            mStickCount = 0;
//            if (requestWorker == null)
//                requestWorker = new RequestWorker();
//            if (!requestWorker.isRunning()) {
//                LogUtils.d("????????????????????????????????????????????????" + msg.getMsgId());
//                ThreadUtils.run(requestWorker);
//            }
        }
    }

    public void requestRulste(boolean isReturn) {
        this.isReturn = isReturn;
    }

    /**
     * ??????????????????
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
                //take??????????????????????????????????????????while(true)
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
                                        LogUtils.d("-----???" + msg.getMsgId() + "|||" + msg.getJsonMsg() + "|||" + mClient.isConnected() + "||" + msgs.size());
                                        msgs.clear();
                                        mStickCount = 0;
                                    }
                                    if (msg != null && msg.getJsonMsg() != null) {
                                        LogUtils.d("????????????---->" + msg.getJsonMsg() + "||" + msgs.size() + "|||" + mStickCount);
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
                LogUtils.d("????????????" + e.getMessage());
//                throw new RuntimeException("????????????:" + e.getMessage());
            }
//            catch (InterruptedException e) {
//            LogUtils.d("???????????????" + e.getMessage());
//                throw new RuntimeException("???????????????:" + e.getMessage());
//            }
            isRunning = false;
            LogUtils.d("????????????----->");
        }
    }

    private boolean heartbeat = false;
    private int heartTime = 1;

    /**
     * ??????stick??????
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
                    LogUtils.d("???????????????");
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
        LogUtils.d("??????bindSendStick");
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
                if (ping != null && ping.indexOf("received, 0%") != -1) {       // ping??????
//                    LogUtils.d("ping ??????");
                    if (pingIp.contains(".2.") && setIpCount <= 3) {  // ????????? ??????????????????socket ????????????
                        setIpCount++;
                        // ??????????????????ping ???????????????ip???????????????
                        if (NativeCode.initSocket(pingIp, 1234, 3)) {
                            LogUtils.d("initSocket??????");
                            if (NativeCode.setIp(pingIp)==0 && needSetIp) {
                                LogUtils.d("IP????????????");
                            } else {
                                LogUtils.d("IP????????????");
                            }
                        } else {
                            LogUtils.d("initSocket??????");
                        }
                    }
                    heartTime = 1;
//                    MLog.log("ping??????----" + state_code);
                    if (state_code != STATE_RECONN && state_code != STATE_DESTORY) {
                        if (close)
                            connect(0, pingIp, PORT);       // ping????????????????????????7878??????
                    }
                } else {
                    heartTime++;
                    setIpCount = 0;
                    if (heartTime >= 3) {
//                        LogUtils.d("ping?????????----" + heartbeat);
                        if (heartTime >= 3) {
                            bindHeartFail();
                        }
                    }
//                    SystemClock.sleep(1000);
                }
                isRunnig = false;
//                MLog.log("????????????----");
            }
        }
    }


    // ????????????
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
            LogUtils.d("sokethelp ??????????????????");
        }

        @Override
        public void run() {

            while (state_code == STATE_SUCCESS) {
//                LogUtils.d("????????????-----");
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
                                LogUtils.d("????????????---->" + buffStr);
                                parsingPackage(buffStr);
                            }
                        } else {
                            LogUtils.d("??????????????????????????????");
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
                    LogUtils.d("?????????????????????" + e.getMessage());
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
            LogUtils.d("????????????----->");
            // ????????????
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
            LogUtils.d("?????????....");
            swapStream.write(buff, 0, rc);
        }
        byte[] bytes = swapStream.toByteArray();
        return bytes;

    }


    String datas = "";

    /**
     * ???????????????
     */
    private synchronized void parsingPackage(String data) {
        datas = datas + data;
        if (!datas.contains("}{")) {
            if ((datas.startsWith("{\"rval\"") && datas.endsWith("}]}") //?????????????????????????????????
                    || datas.endsWith("[]}"))//??????????????????????????????
                    || ((!datas.contains("listing") && !datas.contains("MP4") && !datas.contains("JPG")) && datas.endsWith("}"))//??????????????????????????????
                    || (datas.startsWith("{\"rval\"") && !datas.contains("},{"))) {//?????????????????????????????????
                readSocket(datas);
                datas = "";
            } else {
                LogUtils.d("????????????" + datas);
            }
        } else {
            LogUtils.d("??????????????????");
            String b = datas.substring(0, datas.indexOf("}{") + 1);
            String c = datas.substring(datas.indexOf("}{") + 1);
            datas = "";
            LogUtils.d("??????????????????" + b + "||" + c);
            parsingPackage(b);
            if (c != null && !c.equals("") && c.length() > 0) {
                LogUtils.d("???????????????1---" + c);
                c.substring(2);
                LogUtils.d("???????????????2---" + c);
                parsingPackage(c);
            }
        }
    }

    private synchronized void readSocket(String data) {
        if (JsonUtils.isGoodJson(data)) {
            LogUtils.d("?????????????????????---->" + data);
            if (msgs.size() > 0 && state_code == STATE_SUCCESS) {
                BaseCameraMsg msg = msgs.get(0);
                msgs.clear();
//                LogUtils.d("?????????????????????---->" + data);
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
