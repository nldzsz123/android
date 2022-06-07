package com.feipai.flypai.utils.socket;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.beans.FileBean;
import com.feipai.flypai.utils.GeneralFactory;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.NetworkUtils;
import com.feipai.flypai.utils.threadutils.ThreadUtils;

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

/**
 * Created by YangLin on 2017-12-15.
 */

public class CameraDataSocket {

    private int PORT = ConstantFields.FLY_PORT.DATA_PORT;
    private String IP;
    private static final long SLEEP_TIME = 2000;
    private Socket mClient = new Socket();
    private InputStream mIn;
    private OutputStream mOut;
    private boolean close = false;
    private DataSocketReadListener mRl;
    private static final int STATE_SUCCESS = 1;
    private static final int STATE_FAILED = 2;
    private static final int STATE_WAIT = 3;
    private static final int STATE_CLOSE = 4;
    private static final int STATE_RECONN = 5;
    private static final int STATE_DESTORY = 6;
    private static final int STATE_START_READ = 7;
    private static final int STATE_STOP_READ = 8;
    private int state_code = 0;
    private int read_state = 1;
    private static CameraDataSocket mTcp;


    private Vector<TcpReadListener> mReads = new Vector<TcpReadListener>();

    //队列，封装发送的信息
    private static ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(8);

    private static Handler mStickHandler;
    private int mStickCount = 0;
    private int requeseSize = 0;
    private ArrayList<byte[]> bytesList = new ArrayList<>();
    private ReadTask readTask;
    private SendFileWorker sendFileWorker;
    private boolean isUploading;

    public void setRequeseSize(int size) {
        this.requeseSize = size;
    }

    private CameraDataSocket() {
//        createSendTask();
    }

    /**
     * 获取实例
     */
    public static CameraDataSocket getInstance() {
        if (mTcp == null) {
            synchronized (CameraDataSocket.class) {
                if (mTcp == null) {
                    mTcp = new CameraDataSocket();
                }
            }
        }
        return mTcp;
    }

    public void startSendFile(boolean isUploading) {
        this.isUploading = isUploading;
    }

    public boolean isSendingFile() {
        return isUploading;
    }


    public void setReadListener(DataSocketReadListener r) {
        mRl = r;
    }


    public byte[] getThm() {
        byte[] allByte = new byte[0];
        try {
            mIn = mClient.getInputStream();
            int count = 0;
            while (count == 0) {
                count = mIn.available();
            }
            while (size <= requeseSize) {
                byte[] b = new byte[count];
                mIn.read(b);
                LogUtils.d("8787一直返回长度：b长度=" + b.length + "||size=" + size);
                size = size + b.length;
                bytesList.add(b);
                if (size >= requeseSize && requeseSize != 0) {
                    allByte = new byte[size];
                    int size1 = 0;
                    for (byte[] by : bytesList) {
                        for (int i = 0; i < by.length; i++) {
                            allByte[size1] = by[i];
                            size++;
                        }
                    }
                    requeseSize = 0;
                    size = 0;
                    bytesList.clear();
//                    mRl.read(size, allByte);
                    break;
                }
            }

//            if (mIn == null) {
//                connect(0);
//            }
//            socketThm = new Socket();
//            addrThm = new InetSocketAddress(ip, 8787);
//            try {
//                socketThm.connect(addrThm, timeout);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            // // read body
//                byte[] receiveBytes = new byte[size];// 接收返回数据的byte数组
////            Bitmap bitmap;
//                mIn = mClient.getInputStream();
//                int len = 0;
//                while (len < size) {
//                    System.out.println(size + "QQQQQQQQQQQQ" + len + "QQQQQQQQQQQQ"
//                            + (size - len));
//                    len += mIn.read(receiveBytes, len, size - len);
//                }
//                mIn.close();
//                mIn = null;
//            bitmap = BitmapFactory.decodeByteArray(receiveBytes, 0,
//                    receiveBytes.length);
//            bitmap = ThumbnailUtils.extractThumbnail(bitmap, 640, 480,
//                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            return allByte;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
//            closeAll(0);
        }
    }


    public void connect(final int timeOut) {
        LogUtils.d("连接Datasocket时的状态--->" + state_code);
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
            mClient.setTcpNoDelay(true);
            mClient.setSendBufferSize(1024 * 1024 * 2);
            mClient.setReceiveBufferSize(1024000);
            mClient.setTcpNoDelay(true);
        } catch (SocketException e2) {
            e2.printStackTrace();
        }
        // 启动线程去建立连接
        new Thread(new Runnable() {
            @Override
            public void run() {
                String ip = NetworkUtils.getIPAddress(true);
                LogUtils.d("CameraDataSocket状态到底是？---" + state_code + ",ip=" + ip);
                for (; state_code != STATE_DESTORY; ) {
                    boolean isNetWork = NetworkUtils
                            .isWifiConnected();
                    if (!isNetWork) {
                        LogUtils.d("没有网络，正在睡眠");
                        SystemClock.sleep(SLEEP_TIME);
                        LogUtils.d("没有网络，正在重新检查");
                        continue;
                    }
                    LogUtils.d("DataSocket有网络，正在连接");
                    try {
                        mClient.connect(new InetSocketAddress(GeneralFactory.getRemoteIp(ip), PORT),
                                2000);
                        mIn = mClient.getInputStream();
                        mOut = mClient.getOutputStream();

                        if (mRl != null && mClient.isConnected()/* && mIn != null*/) {
                            mRl.connecSuccess();
                        }
                        close = false;
                        state_code = STATE_SUCCESS;
//                        startReadTask(null);
                        //启动发送消息线程
//                        ThreadUtils.run(new RequestWorker());
                        // 启动读消息线程
//                        if (readTask == null)
//                            readTask = new ReadTask();
//                        if (!readTask.isRunning())
//                            ThreadUtils.run(readTask);
//                        if (readTask == null) {
//                            readTask = new ReadTask();
//                            ThreadUtils.run(readTask);
//                        }
                        LogUtils.d("DataSocket有网络，连接成功 ip："
                                + mClient.getInetAddress().getHostAddress());

                        break;
                    } catch (Exception e) {
                        LogUtils.d("datasocket异常:" + e.getMessage());
                        e.printStackTrace();
                        if (mRl != null) {
                            mRl.connectFail();
                        }
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
            }
        }).start();


    }

    public synchronized void startReadTask(List<FileBean> fbs, int index) {
        LogUtils.d("开始读取...");
        read_state = STATE_START_READ;
        if (readTask == null)
            readTask = new ReadTask();
        if (!readTask.isRunning(fbs, index))
            ThreadUtils.run(readTask);
    }

    public synchronized void stopReadTask() {
        LogUtils.d("停止读取...");
        read_state = STATE_STOP_READ;
        requeseSize = 0;
//        state_code = STATE_CLOSE;
//        if (readTask != null) {
//            ThreadUtils.remove(readTask);
//        }
    }


    /**
     * type 1 断开重新连接 0不重新连接
     */
    public void closeAll(int type) {
        stopReadTask();
        startSendFile(false);
        state_code = type > 0 ? STATE_RECONN : STATE_DESTORY;
//        MLog.log("关闭datasocket--->" + mClient.isClosed());
        if (mClient != null && !mClient.isClosed()) {
            try {
                close = true;
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mRl != null)
            mRl.close();
        if (state_code != STATE_DESTORY) {
            state_code = STATE_RECONN;
            connect(5 * 1000);
        }
    }

    private void createSendTask() {
        ThreadUtils.run(() -> {
            Looper.prepare();
            mStickHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (state_code != STATE_SUCCESS || mOut == null)
                        return;
                    try {
                        if (msg.obj != null) {
                            byte[] data = (byte[]) msg.obj;
                            switch (msg.what) {
                                case 0:
                                    synchronized (CameraDataSocket.class) {
                                        mStickCount--;
                                    }
                                    if (data != null)
                                        mOut.write(data);
                                    break;
                            }
                            mOut.flush();

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            };
            Looper.loop();
        });
    }

    public void send(byte[] packet) {
//        MLog.log("写入数据....");
        if (packet != null) {
            Message message = mStickHandler.obtainMessage();
            message.what = 0;
            message.obj = packet;
            synchronized (CameraDataSocket.class) {
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
        if (sendFileWorker == null)
            sendFileWorker = new SendFileWorker();
        sendFileWorker.setPath(path);
        if (!sendFileWorker.isRunning())
            ThreadUtils.run(sendFileWorker);
    }


    /**
     * 添加请求
     *
     * @param content
     */
    public void putRequest(String content) {
        try {
            queue.put(content);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 队列请求线程
     */
    public class SendFileWorker implements Runnable {

        private boolean isSendRunning;
        private String path;

        public boolean isRunning() {
            return isSendRunning;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public SendFileWorker() {
            this.isSendRunning = false;
        }

        @Override
        public void run() {
            try {
                if (path != null) {
                    isSendRunning = true;
                    if (mClient != null) {
                        mClient.setSendBufferSize(1024 * 1024 * 2);
                        if (mOut == null)
                            mOut = mClient.getOutputStream();
                    }
                    FileInputStream inputStream = new FileInputStream(new File(path));

                    long count = inputStream.available();
                    while (count == 0) {
                        count = inputStream.available();
                    }
                    int packetSize = 1024 * 1024;//这里指定每包为1M
                    byte[] buf = new byte[packetSize];
                    int packet = 0;
                    int packetCount = (int) Math.ceil((double) count / ((double) packetSize));//总包数
//                        lastDataPacket = (int) (count - ((long) (packetSize * packetCount)));//余字节数，也可能会是0
                    int len;
                    //判断是否读到文件末尾
                    while (isUploading && ((len = inputStream.read(buf)) != -1)) {
                        LogUtils.d("循环--->" + isUploading);
                        Thread.sleep(2000);//每隔1s执行一次
                        packet++;
                        LogUtils.d("发送的长度--->" + packet + "||" + packetCount + "||" + len);
                        if (mRl != null)
                            mRl.uploadLisenter((int) ((float) packet / (float) packetCount * 100f));
                        if (mOut != null) {
                            mOut.write(buf, 0, len);
                            mOut.flush();
                        }
                    }
//                        closeAll(0);
                    inputStream.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isSendRunning = false;
        }
    }

    int size = 0;

    // 接收任务
    private class ReadTask implements Runnable {

        private boolean isRunning;

        private List<FileBean> fbs;
        private int index;

        public boolean isRunning(List<FileBean> fbs, int index) {
            this.fbs = fbs;
            this.index = index;
            return isRunning;
        }

        public ReadTask() {
            this.isRunning = false;
        }

        @Override
        public void run() {

            for (; state_code == STATE_SUCCESS && read_state == STATE_START_READ; ) {
                try {
                    if (mRl != null && mClient != null && !mClient.isClosed()) {
                        isRunning = true;
                        mIn = mClient.getInputStream();
                        byte[] b = new byte[requeseSize];
                        int len = 0;
                        LogUtils.d("datasocket读取数据前--->" + b.length + "||" + len + "||" + requeseSize);
                        while (state_code == STATE_SUCCESS && read_state != STATE_STOP_READ
                                && b.length != 0 && len < requeseSize) {
                            len += mIn.read(b, len, requeseSize - len);
//                            if (mRl != null && requeseSize != 0)
//                                mRl.readThumbProgress((len * 100 / requeseSize), requeseSize,index);
                            LogUtils.d("datasocket读取数据中--->" + b.length + "||" + requeseSize + "||" + len + "||"
                                    + (requeseSize - len));
                        }
                        LogUtils.d("datasocket读取数据后--->" + b.length + "||" + len);
                        if (b.length != 0 && requeseSize != 0 && len == requeseSize) {
                            mRl.read(fbs, index, b);
                        } else {
                            LogUtils.d("长度不一致,跳出循环...");
//                            mIn.close();
//                            mIn = null;
                        }
                        len = 0;
                        b = null;
                        stopReadTask();
                        break;

                    } else {
                        LogUtils.d("异常，服务器主动断开");
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

                } catch (Exception e) {
                    LogUtils.d("连接断开，主动" + e.getMessage());
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
            isRunning = false;
            requeseSize = 0;
//            try {
            if (read_state != STATE_STOP_READ) {
                // 重新连接
                if (state_code != STATE_DESTORY) {
                    LogUtils.d("CameraDataSocket在这里做了连接" + state_code);
                    state_code = STATE_RECONN;
                    if (mRl != null)
                        mRl.close();
                    connect(5 * 1000);
                }
            } else {
            }
        }

    }
}
