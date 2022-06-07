package com.feipai.flypai.updatafirmware;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.MAVLinkPacket;
import com.Messages.MAVLinkMessage;
import com.Parser;
import com.common.msg_autopilot_version;
import com.common.msg_battery_status;
import com.common.msg_heartbeat;
import com.common.msg_location;
import com.common.msg_param_value;
import com.enums.MAV_DATA_STREAM;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.app.FlyPieApplication;
import com.feipai.flypai.beans.LocationMode;
import com.feipai.flypai.utils.MavlinkRequestMessage;
import com.feipai.flypai.utils.global.JsonUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.StringUtils;
import com.feipai.flypai.utils.gsonlib.MGson;
import com.feipai.flypai.yuntaiupdate.MsgBase;
import com.feipai.flypai.yuntaiupdate.MsgCon;
import com.feipai.flypai.yuntaiupdate.MsgErase;
import com.feipai.flypai.yuntaiupdate.MsgFinish;
import com.feipai.flypai.yuntaiupdate.MsgFirware;
import com.feipai.flypai.yuntaiupdate.MsgFirwareCRC;
import com.feipai.flypai.yuntaiupdate.MsgReset;
import com.feipai.flypai.yuntaiupdate.ReceivePacket;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import static com.feipai.flypai.app.ConstantFields.UPGRADE_FW.*;


/**
 * Created by feipai1 on 2017/6/22.
 */

public class UpdateFirmware {

    final public static int UpdateFirmwareErrorCannotConnectToHost = -1;
    final public static int UpdateFirmwareReadError = 2;
    final public static int UpdateFirmwareErrorInvalidFirmware = 0;
    final public static int UpdateFirmwareErrorOK = 1;

    public interface UpdateCallback {
        /**
         * 更新过程中进度回调
         * error 1 代表连接上了飞控主机并且可以开始正常更新了 -1则代表无法连接上飞控的主机 0则代表更新过程中检测到了飞控固件出现错误
         * progress 则代表更新的进度取值范围(0-100) 当无法连接飞控或者固件不正确 progress为0
         */
        void update(int error, int progress);

        void upDateYuntai(int error, int progress);

        void requestPlaneVer(boolean succed, boolean unLock, String planVer);

        void requestYuntaiVer(boolean succed, boolean unLock, String yuntaiVer);

        void requestSerial(boolean succed, String serialNo);

        void requestAct(boolean succed, int result);
    }


    private String mHost;
    private int mPort;
    private String fwContent;
    private String yuntaiLocalPath;
    private byte[] firmwareData;
    private UpdateCallback mCallback;
    private int sndNum;
    private int uProgress;
    private boolean isRebooting;
    private boolean isFoundBooter;
    private ArrayList<byte[]> sndDatas;
    private int fwMaxsize;
    private Socket mSocket;
    private ReentrantLock mLock = new ReentrantLock();
    private boolean mRun;
    private int tOutCount = 100;

    InputStream read;
    OutputStream write;

    private Thread mWorkThread;
    private Queue mEventQueue;

    private Thread mReadThread;
    private ReadQueue mReadQueue;


    private int checkTpye = CHECK_VER;


    private boolean checkSerial;
    private Parser parser;
    private boolean isChecked;
    private boolean planeVerChecked = false;
    private String planeVer = null;
    private String yuntaiVer = null;
    private String serial = null;
    private int unLock = -1;
    private int isAck = -1;


    private byte[] leftBuffer;
    private int position;
    private int limit = 1024;
    private int firstfeoffset = -1;
    private int secondoffset = -1;
    private int totalCount = 0;
    private int lossCount = 0;
    private int lastSeq;
    private int readVerTime = 1;


    //升级云台start
    private static int PROG_MULTI_MAX_NEW = 640;
    private long upStatus;
    private ArrayList<MsgBase> msgCommands;     // 消息队列
    private static long UPStatusInit = 1 << 0;
    private static long UPStatus_HandShaked = 1 << 1;
    private static long UPStatus_GetCrced = 1 << 2;
    private static long UPStatus_Eraseed = 1 << 3;
    private static long UPStatus_UpFirmwared = 1 << 4;
    private static long UPStatus_Finished = 1 << 5;
    //升级云台end

    /**
     * firmwareContent:飞控固件的内容 是一个json字符串 只需要从文件中读取了这个字符串传递过来
     * host:连接飞控的主机地址 比如192.168.1.254 port 连接飞控的端口号 比如3333
     * callback:连接飞机和更新过程中的回调
     */
    public UpdateFirmware(String host, int port, UpdateCallback callback) {
//        fwContent = firmwareContent;
        parser = new Parser();
        mHost = host;
        mPort = port;
        mCallback = callback;
    }

    public void setFirmwareContent(String firmwareContent) {
        this.fwContent = firmwareContent;
    }

    public void setYunFwPath(String path) {
        this.yuntaiLocalPath = path;
    }

    public void loadYuntaiFwData(String path) {
        firmwareData = ResourceUtils.readAssetsFileToByte(FlyPieApplication.getInstance().getApplicationContext(), path);
        if (firmwareData == null) {
            if (mCallback != null) {
                uProgress = 0;
                mCallback.upDateYuntai(UpdateFirmwareErrorOK, 0);
            }
            return;
        }
        int fwLen = firmwareData.length;
        if (fwLen > 0) {
            Log.d("Mlog", "拷贝进来的长度" + firmwareData.length);
        }

    }

    /**
     * host 连接主机的IP地址，比如192.168.1.254 port 端口号 比如3333
     *
     * @param check 是否请求序列号
     * @param type  此时应该做的操作
     */
    public void update(int type, boolean check) {
//        Log.d("Mlog", "执行的操作--->" + type + "||" + check);
        this.checkTpye = type;
        checkSerial = check;
        if (mRun) {
            Log.d("Mlog", "已经启动过更新了 不需要再重新启动了。。。。。。");
            mRun = true;
            return;
        }

        sndNum = 0;
        uProgress = 0;
        if (sndDatas != null) {
            sndDatas.clear();
        }
        isRebooting = false;
        connectsocket(1);
    }

    private int connectCount = 1;

    private void connectsocket(int where) {
        Log.d("Mlog", "connectsocket 固件升级" + where);
        reset();

        if (mEventQueue == null) {
            mEventQueue = new Queue();
        }
        if (mReadQueue == null)
            mReadQueue = new ReadQueue();

        if (mWorkThread == null) {
            mWorkThread = new Thread(mEventQueue);
        }
        if (mReadThread == null)
            mReadThread = new Thread(mReadQueue);
        mReadThread.start();
        mWorkThread.start();
        Event connect = new Event(Event.State_Connect, 0, 0, null);
        if (mEventQueue != null)
            mEventQueue.putEvent(connect);

    }

    public void reset() {
        if (mSocket != null) {
            uProgress = 0;
            sndNum = 0;
            readVerTime = 1;
            planeVerChecked = false;
            serial = null;
            planeVer = null;
            yuntaiVer = null;
            unLock = -1;
            isAck = -1;
            isChecked = false;
            if (msgCommands != null) msgCommands.clear();
            if (msgObjs != null) msgObjs.clear();
            if (msgResponses != null) msgResponses.clear();
            if (leftReceivedData != null) leftReceivedData = null;
//        firmwareData = null;
            if (mainHander != null) {
                mainHander.removeCallbacks(readRunnable);
                mainHander.removeCallbacksAndMessages(null);
            }
            if (mEventQueue != null) {
                mEventQueue.destroy();
                mEventQueue = null;
            }

            if (mWorkThread != null) {
                mWorkThread = null;
            }
            if (mReadQueue != null) {
                mReadQueue.destroy();
                mReadQueue = null;
            }
            if (mReadThread != null)
                mReadThread = null;

            if (read != null) {
                try {
                    read.close();
                } catch (IOException e) {

                }
                read = null;
            }

            if (write != null) {
                try {
                    write.close();
                } catch (IOException e) {

                }
                write = null;
            }
            if (mSocket != null && !mSocket.isClosed()) {
                try {
                    mSocket.close();
                } catch (IOException e) {

                }
//                mLock.lock();
                mSocket = null;
//                mLock.unlock();
            }
            if (sndDatas != null) {
//                LogUtils.d("测试--->数据集" + sndDatas.size());
                sndDatas.clear();
//            sndDatas = null;
            }
            if (firmwareData != null) {
                firmwareData = null;
            }
        }
    }

    private Handler mainHander = new Handler(Looper.getMainLooper());

    private final static int SNYC_TAG = 10000;
    private final static int MAXSIZE_TAG = 10001;
    private final static int ERASE_TAG = 10002;
    private final static int PROG_MULTI_TAG = 10003;
    private final static int VERIFY_TAG = 10004;
    private final static int REBOOT_MAV_TAG = 10005;
    private final static int REBOOT_TAG = 10006;
    private final static int CHECKE_VERSION_TAG = 10007;
    private final static int CHECKE_ACK_TAG = 10008;
    private final static int DEVICE_TAG = 10009;
    private final static int REBOOT_YUNTAI_MAV_TAG = 10020;
    private final static int YUNTAI_COMMAND_TAG = 10021;
    private final static byte INSYNC = 0x12;
    private final static byte EOC = 0x20;
    private final static byte OK = 0x10;
    private final static byte INVALID = 0x13;
    private final static byte GET_SYNC = 0x21;
    private final static byte GET_DEVICE = 0x22;
    private final static byte CHIP_ERASE = 0x23;
    private final static byte CHIP_VERIFY = 0x24;
    private final static byte PROG_MULTI = 0x27;
    private final static byte GET_CRC = 0x29;
    private final static byte GET_CHIP = 0x2c;
    private final static byte REBOOT = 0x30;
    private final static byte INFO_FLASH_SIZE = 0x04;
    private final static byte INFO_DEVICE_SIZE = 0x01;

    private int PROG_MULTI_MAX = 252;
    private final static byte MAVLINK_REBOOT_ID1[] = {(byte) 0xfe, 0x21, 0x72, (byte) 0xff, 0x00, 0x4c, 0x00, 0x00, 0x40, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xf6, 0x00, 0x01, 0x00, 0x00, 0x53, 0x6b};
    private final static byte MAVLINK_REBOOT_ID2[] = {(byte) 0xfe, 0x21, 0x45, (byte) 0xff, 0x00, 0x4c, 0x00, 0x00, 0x40, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xf6, 0x00, 0x00, 0x00, 0x00, (byte) 0xcc, 0x37};

    private class Event {

        public final static int State_Connect = 0;
        public final static int State_Read = 1;
        public final static int State_Write = 2;
        private int state;
        private byte[] sendData;
        private int tag;
        private int len;

        public Event(int s, int t, int l, byte[] sd) {
            state = s;
            tag = t;
            sendData = sd;
            len = l;
        }

        public int getState() {
            return state;
        }

        public byte[] getSendData() {
            return sendData;
        }

        public int getTag() {
            return tag;
        }

        public int getLen() {
            return len;
        }
    }

    private class ReadQueue implements Runnable {
        private Vector readQueueData = null;
        private boolean isReading = true;

        public ReadQueue() {
            readQueueData = new Vector();
        }

        public synchronized void putEvent(Event obj) {
            readQueueData.addElement(obj);
            notify();
        }

        private synchronized Event getReadEvent() {
            try {
                if (readQueueData != null)
                    return (Event) readQueueData.remove(0);
            } catch (ArrayIndexOutOfBoundsException aEx) {
            }
            try {
                wait();//造成阻塞...
            } catch (InterruptedException e) {
                if (isReading) {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            Log.d("Mlog", "时间消耗2");
            return null;
        }

        @Override
        public void run() {
            while (isReading) {
                Event obj = getReadEvent();
                if (obj == null) {
//                    Log.d("Mlog", "读取队列中还没有事件");
                    continue;
                }
                int state = obj.getState();
                if (state == Event.State_Read) {
                    int tag = obj.getTag();
                    int readLen = obj.getLen();
                    int lenth = 0;
                    try {
                        if (mSocket != null && !mSocket.isClosed()) {
                            if (read == null)
                                read = mSocket.getInputStream();
                        }
                        if (read == null) {
                            Log.d("Mlog", "读取流已经关闭");
                            return;
                        }
//                        Log.d("Mlog", "消息读取循环前");
                        byte[] d = new byte[readLen];
                        lenth = read.read(d);
                        if (lenth > 0) {
                            byte[] data = new byte[lenth];
                            System.arraycopy(d, 0, data, 0, lenth);
//                            Log.d("Mlog", "读取数据长度==>+" + byte2hex(data) + ",count=" + lenth);
                            if (tag == YUNTAI_COMMAND_TAG) {
                                processData(data);
                            } else {
                                parsReadData(tag, readLen, data);
                            }
                        }
                    } catch (IOException e) {
                        mainHander.post(new Runnable() {
                            @Override
                            public void run() {
                                reset();
                                // 出现读取数据超时的错误时
                                Log.d("Mlog", "读取数据超时了==>" + e.getMessage() + "||Tag=" + tag + "||" + obj.getTag());
                                if (tag == CHECKE_VERSION_TAG) {
                                    Log.d("Mlog", "读取数据超时了（检测版本号超时）==>" + e.getMessage());
                                    if (mCallback != null) {
                                        mCallback.requestPlaneVer(false, false, "2");
                                        mCallback.requestYuntaiVer(false, false, "2");
                                    }
                                } else if (tag == CHECKE_ACK_TAG) {
                                    Log.d("Mlog", "读取数据超时了（检测激活状态超时）==>" + e.getMessage());
                                    if (mCallback != null)
                                        mCallback.requestAct(true, -1);
                                } else if (tag == YUNTAI_COMMAND_TAG) {
                                    Log.d("Mlog", "读取数据超时了（云台升级）==>" + e.getMessage());
                                    if (mCallback != null) {
                                        mCallback.upDateYuntai(UpdateFirmwareReadError, 0);
                                    }
                                } else {
                                    Log.d("Mlog", "读取数据超时了（上传超时）==>" + e.getMessage());
                                    if (mCallback != null) {
                                        mCallback.update(UpdateFirmwareReadError, 0);
                                    }
                                }
                            }
                        });
                    }

                }
            }
        }

        public synchronized void destroy() {
            isReading = false;
            readQueueData = null;
//            notify();
        }
    }

    /**
     * 解析读取的数据
     */
    private void parsReadData(int tag, int readLen, byte[] datas) {
        int newLen = datas.length;
        if (leftReceivedData != null) {
            newLen = leftReceivedData.length + datas.length;
        }
        byte[] newData = new byte[newLen];
        if (this.leftReceivedData != null) { //说明还有剩余的没有处理完
            System.arraycopy(leftReceivedData, 0, newData, 0, leftReceivedData.length);
            System.arraycopy(datas, 0, newData, leftReceivedData.length, datas.length);
        } else {
            System.arraycopy(datas, 0, newData, 0, datas.length);
        }
        if ((newData.length >= readLen) || (tag == CHECKE_VERSION_TAG || tag == CHECKE_ACK_TAG) && newData.length >= 2) {//已经比指定要用的数据长度大了，就可以去解析了
            if (tag == SNYC_TAG) {
                int c = newData[0] & 0xff;
//                            if (c == 0xfe) {
//                                rebootBymavlink();
//
//                            } else {
                boolean isOk = true;
                if (c != INSYNC) {
                    Log.d("Mlog", "输出的值==>" + c);
                    isOk = false;
                }
                c = newData[1];
                if (c != OK) {
                    Log.d("Mlog", "输出的值==>%d" + (char) c);
                    isOk = false;
                }
                if (isOk) {
                    isFoundBooter = true;
                    getFWDevice();
                } else {
                    isFoundBooter = false;
                    __sync();
                }
//                            }
            } else if (tag == DEVICE_TAG) {
                int b0 = newData[0] & 0xFF;
                int b1 = newData[1] & 0xFF;
                int b2 = newData[2] & 0xFF;
                int b3 = newData[3] & 0xFF;
                int value = (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
                if (value == 5) {
                    Log.d("Mlog", "老式飞控底层");
                    PROG_MULTI_MAX = 252;
                } else if (value == 6) {
                    PROG_MULTI_MAX = 252 * 22;
                    Log.d("Mlog", "新式飞控底层");
                }
//                            fwMaxsize = value;
                Log.d("Mlog", "读取到的固件长度==>" + value);
//                            if (firmwareData == null) {
//                                loadFirmware(fwContent);
//                            }

                byte[] snyc = new byte[2];
                snyc[0] = newData[4];
                snyc[1] = newData[5];
                if (isRightSync(snyc)) {
                    getFWMaxSize();
                    mainHander.post(new Runnable() {
                        @Override
                        public void run() {
                            uProgress = 5;
                            if (mCallback != null) {
                                mCallback.update(UpdateFirmwareErrorOK, uProgress);
                            }
                        }
                    });
                }
            } else if (tag == MAXSIZE_TAG) {
                int b0 = newData[0] & 0xFF;
                int b1 = newData[1] & 0xFF;
                int b2 = newData[2] & 0xFF;
                int b3 = newData[3] & 0xFF;
                int value = (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
                fwMaxsize = value;
                Log.d("Mlog", "读取到的固件长度==>" + value);
                if (firmwareData == null && fwContent != null) {
                    loadFirmware(fwContent);
                }

                byte[] snyc = new byte[2];
                snyc[0] = newData[4];
                snyc[1] = newData[5];
                if (isRightSync(snyc)) {
                    erase();
                    mainHander.post(new Runnable() {
                        @Override
                        public void run() {
                            uProgress = 5;
                            if (mCallback != null) {
                                mCallback.update(UpdateFirmwareErrorOK, uProgress);
                            }
                        }
                    });
                }
            } else if (tag == ERASE_TAG) {
                if (isRightSync(newData)) {
                    program();
                    mainHander.post(new Runnable() {
                        @Override
                        public void run() {
                            uProgress = 6;
                            if (mCallback != null) {
                                mCallback.update(UpdateFirmwareErrorOK, uProgress);
                            }
                        }
                    });
                }
            } else if (tag == PROG_MULTI_TAG) {
                if (isRightSync(newData)) {
                    programBytes();
                    int step = sndDatas.size() / 90;
                    if (sndNum % step == 0) {
                        uProgress += 1;
                        if (uProgress <= 96) {
                            mainHander.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mCallback != null) {
                                        mCallback.update(UpdateFirmwareErrorOK, uProgress);
                                    }
                                }
                            });
                        }
                    }
                }

            } else if (tag == VERIFY_TAG) {
                long b0 = newData[0] & 0xFF;
                long b1 = newData[1] & 0xFF;
                long b2 = newData[2] & 0xFF;
                long b3 = newData[3] & 0xFF;
                long receive_crc = (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
                Log.d("Mlog", "收到的校验码==>" + byte2hex(newData) + " value==>" + receive_crc);

                long expect_crc = crc32(firmwareData, 0);
                int padlen = fwMaxsize - firmwareData.length;
                byte[] padData = new byte[padlen];
                for (int i = 0; i < padlen; i++) {
                    padData[i] = (byte) 0xff;
                }

                expect_crc = crc32(padData, expect_crc);
                byte[] result2 = new byte[4];
                //由高位到低位
                result2[0] = (byte) ((expect_crc >> 24) & 0xFF);
                result2[1] = (byte) ((expect_crc >> 16) & 0xFF);
                result2[2] = (byte) ((expect_crc >> 8) & 0xFF);
                result2[3] = (byte) (expect_crc & 0xFF);
                Log.d("Mlog", "本地计算出的校验码==>" + byte2hex(result2) + " value==>" + expect_crc);

                if (expect_crc == receive_crc) {
                    reboot();
                    mainHander.post(new Runnable() {
                        @Override
                        public void run() {
                            uProgress = 99;
                            if (mCallback != null) {
                                mCallback.update(UpdateFirmwareErrorOK, uProgress);
                            }
                        }
                    });
                } else {
                    mainHander.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("Mlog", "固件crc校验码不对");
                            reset();
                            if (mCallback != null) {
                                mCallback.update(UpdateFirmwareErrorInvalidFirmware, 0);
                            }
                        }
                    });
                }
            } else if (tag == REBOOT_TAG) {
                if (isFoundBooter) {
                    if (isRightSync(newData)) {
                        mainHander.post(new Runnable() {
                            @Override
                            public void run() {
                                uProgress = 100;
                                if (mCallback != null) {
                                    mCallback.update(UpdateFirmwareErrorOK, uProgress);
                                }
                                reset();
                            }
                        });
                    }
                } else {
                    mainHander.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            __sync();
                        }
                    }, 1000);
                }
            } else if (tag == CHECKE_VERSION_TAG || tag == CHECKE_ACK_TAG) {
                Log.d("Mlog", newData[0] + "||" + newData[1]);
                if (newData.length >= 2)
                    if (newData[0] == 18) {
                        if (mCallback != null)
                            switch (tag) {
                                case CHECKE_VERSION_TAG:
                                    mCallback.requestPlaneVer(false, false, "");
                                    mCallback.requestYuntaiVer(false, false, "");
                                    break;
                                case CHECKE_ACK_TAG:
                                    mCallback.requestAct(true, -1);
                                    break;
                            }
                    } else {
                        /**校验版本号*/
                        Log.d("Mlog", "版本号校验...");
                        crcPacket(newData.length, newData);
                    }
            }

            this.leftReceivedData = null;
        } else {
            Log.d("Mlog", "固件升级及版本号读取时，剩余字节:" + newLen);
            this.leftReceivedData = new byte[newLen];
            System.arraycopy(newData, 0, leftReceivedData, 0, newLen);
            if (mSocket != null && !mSocket.isClosed()) {
                Event rd = new Event(Event.State_Read, tag, readLen, null);
                if (mReadQueue != null) {
                    Log.d("Mlog", "读取的长度不够，继续读----");
                    mReadQueue.putEvent(rd);
                }
            }
        }
    }

    private class Queue implements Runnable {

        private Vector queueData = null;
        private boolean run = true;

        public Queue() {
            queueData = new Vector();
        }

        public synchronized void putEvent(Event obj) {
//            Log.d("Mlog", "时间消耗1111111");
            queueData.addElement(obj);
            notify();
        }

        private synchronized Event getEvent() {
            try {
                if (queueData != null)
                    return (Event) queueData.remove(0);
            } catch (ArrayIndexOutOfBoundsException aEx) {
            }
            try {
                wait();
            } catch (InterruptedException e) {
                if (run) {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            Log.d("Mlog", "时间消耗2");
            return null;
        }

        public void run() {
            while (run) {
                Event obj = getEvent();
                if (obj == null) {
//                    SystemClock.sleep(10);
//                    Log.d("Mlog", "写入队列中还没有事件");
                    continue;
                }

                int state = obj.getState();

                if (state == Event.State_Connect) {
                    connect();
                } else if (state == Event.State_Write) {
                    int tag = obj.getTag();
                    byte[] data = obj.getSendData();
                    if (data != null && data.length > 0) {
//                        Log.d("Mlog", "write data==>  tag" + tag + "len==>" + data.length);
                        try {
                            if (mSocket != null && !mSocket.isClosed()) {
                                if (write == null)
                                    write = mSocket.getOutputStream();
                            }
                            if (write != null)
                                write.write(data);
                            if (write != null)
                                write.flush();
                        } catch (IOException e) {
                            Log.d("Mlog", "异常断开..." + e.getMessage());
                        }
                    }
                }
            }
        }

        public synchronized void destroy() {
            run = false;
            queueData = null;
            notify();
        }
    }

    private Runnable readRunnable = new Runnable() {
        @Override
        public void run() {
//            Log.d("Mlog", "重新去读取..." + checkTpye + "||" + readVerTime);
            readVerTime++;
            switch (checkTpye) {
                case CHECK_VER:
                    checkFirmwareVer(readVerTime < 3);
                    break;
                case CHECK_ACK:
                    checkPlaneAck(readVerTime < 3);
                    break;
            }
            if (readVerTime >= 3) {
                readVerTime = 1;
            }

        }
    };

//    private Runnable readRunnable = new Runnable() {
//        @Override
//        public void run() {
//            Log.d("Mlog", "重新去读取...");
//            readVerTime++;
//            if (readVerTime >= 5) {
//                byte[] msg_id = PlaneCommand.requestMavlinkVersion().pack().encodePacket();
//                Event sd1 = new Event(Event.State_Write, CHECKE_VERSION_TAG, 0, msg_id);
//                if (mEventQueue != null) {
//                    mEventQueue.putEvent(sd1);
//                    readVerTime = 1;
//                }
//            }
//            Event rd = new Event(Event.State_Read, CHECKE_VERSION_TAG, 1024, null);
//            if (mEventQueue != null)
//                mEventQueue.putEvent(rd);
//        }
//    };

    private void crcPacket(int bufferSize, byte[] buffer) {
        if (leftBuffer == null) {
            leftBuffer = new byte[limit];
            position = 0;
        }
        int newBufferSize = bufferSize;
        Log.d("Mlog", "position==> " + position + "buffersize==>" + bufferSize);
        if (position > 0) {
            newBufferSize += position;
        }

        byte[] newBuffer = new byte[newBufferSize];
        if (position > 0) {
            Log.d("Mlog", "leftbuffer==> " + position);
            for (int i = 0; i < position; i++) {
                newBuffer[i] = leftBuffer[i];
            }
            for (int i = position; i < bufferSize; i++) {
                newBuffer[i] = buffer[i - position];
            }
            position = 0;
        } else {
            for (int i = 0; i < bufferSize; i++) {
                newBuffer[i] = buffer[i];
            }
        }

        firstfeoffset = -1;
        secondoffset = -1;
        for (int i = 0; i < newBufferSize; i++) {
            int code = newBuffer[i] & 0x00ff;
            MAVLinkPacket receivedPacket = parser.mavlink_parse_char(code, buffer);
            if (receivedPacket != null) {
                if (receivedPacket.msgid != 148) {
//                    MLog.log("版本号:" + byte2hex(newBuffer));
                }
                MAVLinkMessage receivedMsg = receivedPacket.unpack();
                if (receivedMsg != null) {
//                    Log.d("Mlog", "此消息的ID" + receivedMsg.msgid);
                    if (checkTpye == CHECK_VER) {
                        if (receivedPacket.msgid == msg_autopilot_version.MAVLINK_MSG_ID_AUTOPILOT_VERSION) {
                            msg_autopilot_version msg_version = (msg_autopilot_version) receivedMsg;
                            planeVer = StringUtils.longToBinary(msg_version.flight_sw_version);
                            serial = getSerial(msg_version.flight_custom_version, msg_version.middleware_custom_version);
                            Log.d("Mlog", "请求飞控的版本号---->" + planeVer + "||serial=" + serial);
                        } else if (receivedPacket.msgid == msg_battery_status.MAVLINK_MSG_ID_BATTERY_STATUS) {
                            /**心跳包(最新1.5以上只有一个电池包数据),是否已解锁,已解锁是不可升级固件的*/
                            msg_battery_status msg_battery_status = (msg_battery_status) receivedMsg;
                            unLock = msg_battery_status.heartbeat_base_mode >> 7 & 1;
                            Log.d("Mlog", "请求飞控的版本号---->（新固件）是否解锁" + unLock);
                        } else if (receivedPacket.msgid == msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT) {
                            /**心跳包,是否已解锁（久固件）,已解锁是不可升级固件的*/
                            msg_heartbeat msg_heart = (msg_heartbeat) receivedMsg;
                            unLock = msg_heart.base_mode >> 7 & 1;
                            Log.d("Mlog", "请求飞控的版本号---->（老固件）是否解锁" + unLock);
                        } else if (receivedPacket.msgid == msg_location.MAVLINK_MSG_ID_LOCATION) {
                            /**此处接收云台版本号*/
                            msg_location msg_location = (msg_location) receivedMsg;
                            String locationData = msg_location.getText();
                            if (JsonUtils.isGoodJson(locationData)) {
                                LocationMode loacationMode = MGson.newGson().fromJson(locationData, LocationMode.class);
                                if (Integer.parseInt(loacationMode.getType()) == ConstantFields.PLANE_CONFIG.TYPE_REQUEST_VERSION_RETURN) {
                                    Long hw_v = loacationMode.getHw_v();
                                    if (hw_v == 0) {
                                        yuntaiVer = "0";
                                    } else {
                                        yuntaiVer = StringUtils.longToBinary(hw_v);
                                    }
                                }
                            }

                        } else if (receivedPacket.msgid == msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE) {
                            msg_param_value msg_param_value = (msg_param_value) receivedMsg;
                            LogUtils.d("长指令发送成功后返回==>" + msg_param_value.toString());
                            if (msg_param_value.getParam_Id().equals(MAV_DATA_STREAM.FP_ACTIVATION)) {
                                isAck = (int) msg_param_value.param_value;
                                mCallback.requestAct(true, isAck);
                            }
                        }
                    } else if (checkTpye == CHECK_ACK) {
                        if (receivedPacket.msgid == msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE) {
                            msg_param_value msg_param_value = (msg_param_value) receivedMsg;
                            LogUtils.d("长指令发送成功后返回==>" + msg_param_value.toString());
                            if (msg_param_value.getParam_Id().equals(MAV_DATA_STREAM.FP_ACTIVATION)) {
                                mainHander.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        LogUtils.d("移除1...");
                                        isAck = (int) msg_param_value.param_value;
                                        mCallback.requestAct(true, isAck);
                                        reset();
                                        mainHander.removeCallbacks(readRunnable);

                                    }
                                });
                                break;
                            }
                        }
                    }
                }
                if (code == MAVLinkPacket.MAVLINK_STX) {
                    if (firstfeoffset == -1) {
                        firstfeoffset = i;
//                    Log.d("Mlog", "发现第一个fe");
                    } else {
                        if (secondoffset == -1) {
                            secondoffset = i;
//                        Log.d("Mlog", "发现第二个fe");
                        }
                    }
                }

                if (parser.getParseStatus() == Parser.MAVLINK_FRAMING_OK) {
                    firstfeoffset = -1;
                    secondoffset = -1;

//                    MLog.log("----msgId: " + receivedPacket.msgid + " seq: " + receivedPacket.seq);
                    if (totalCount == 0) {
                        totalCount = 1;
                    } else {
                        if (receivedPacket.seq < lastSeq) {
                            if ((256 + receivedPacket.seq) - lastSeq > 1) {  //说明丢包了
                                lossCount += (256 + receivedPacket.seq - lastSeq - 1);
                            }
                            totalCount += (256 + receivedPacket.seq - lastSeq);
                        } else {
                            if (receivedPacket.seq - lastSeq > 1) {    //说明丢包了
                                lossCount += (receivedPacket.seq - lastSeq - 1);
                            }

                            totalCount += (receivedPacket.seq - lastSeq);
                        }
                    }
                    lastSeq = receivedPacket.seq;
//                    MLog.log("总共: " + totalCount + "个包" + " 丢掉: " + lossCount + " 个包" + " 丢包率:" + (float) lossCount / totalCount * 100 + "%");
                } else if (parser.getParseStatus() == Parser.MAVLINK_FRAMING_BAD_CRC) {
                    if (secondoffset > 0 && i != newBufferSize - 1) {   //不是数据data里面的最后一个字节，否则放入下一个逻辑去处理

                        i = secondoffset - 1;
//                    MLog.log("校验失败，重新找到第一个fe的位置==>" +i+1);
                    } else {
//                    MLog.log("校验失败，数据包中没有新的fe出现");
                    }

                    firstfeoffset = -1;
                    secondoffset = -1;
                    parser.reset();
                }
                //一个mavlink消息跨越两个数据data或者在一个结束的校验失败的数据data中但包含多个fe,则直接截取出来保存放到下一个数据data之前重新解析
                if (i == newBufferSize - 1 && parser.getParseStatus() == Parser.MAVLINK_FRAMING_OK) {
                    if (firstfeoffset > 0) {
                        int leftBufferSize = newBufferSize - firstfeoffset;
                        byte[] printBuffer = new byte[leftBufferSize];
                        for (int position = 0; position < leftBufferSize; position++) {
                            leftBuffer[position] = newBuffer[firstfeoffset + position];
                            printBuffer[position] = newBuffer[firstfeoffset + position];
                        }
                        LogUtils.d("剩余的数据 data==>" + byte2hex(printBuffer));
                        parser.reset();
                    }
                }
                if (checkSerial) {
                    if (serial != null) {
                        mainHander.post(new Runnable() {
                            @Override
                            public void run() {
                                LogUtils.d("移除2...");
                                mainHander.removeCallbacks(readRunnable);
                                mCallback.requestSerial(true, serial);
                                reset();
                            }
                        });
                        isChecked = true;
                        break;
                    }
                } else {
                    if (unLock != -1) {
                        if (planeVer != null) {
                            if (!planeVerChecked) {
                                planeVerChecked = true;
                                mCallback.requestPlaneVer(true, unLock == 1, planeVer);
                            }
                        }
                        if (yuntaiVer != null) {
                            mCallback.requestYuntaiVer(true, unLock == 1, yuntaiVer);
                        }

                        if (planeVer != null && yuntaiVer != null && isAck != -1) {
                            Log.d("Mlog", "飞控版本号获取成功..." + planeVer + "||" + yuntaiVer);
                            mainHander.removeCallbacks(readRunnable);
//                            mCallback.requestVersion(true, unLock == 1 ? true : false, version, yuntaiVer);
                            isChecked = true;
                            mainHander.post(new Runnable() {
                                @Override
                                public void run() {
                                    reset();
                                }
                            });
                            break;
                        }
                    }
                }
            }
        }
        if (!isChecked) {
//            LogUtils.d("继续读吧...");
            mainHander.postDelayed(readRunnable, 500);
        }

    }

    private void connect() {
        try {

            if (mSocket == null || mSocket.isClosed()) {
                if (mSocket != null)
                    mSocket = null;
                mLock.lock();
                mSocket = new Socket();
                mSocket.setKeepAlive(true);
                mSocket.setSoTimeout(15000); //设置读取数据超时时间 15秒
                mSocket.setTcpNoDelay(true);
                mLock.unlock();
            }
            if (mSocket != null)
                mSocket.connect(new InetSocketAddress(mHost, mPort), 3000);

            // 连接上主机了
//            Log.d("Mlog", "连接上主机了==>");
            switch (checkTpye) {
                case CHECK_VER:
                    isChecked = false;
                    checkFirmwareVer(false);
                    break;
                case CHECK_ACK:
                    checkPlaneAck(false);
                    break;
                case UPDATE_PLANE_FW:
                    if (isRebooting) {
                        Log.d("Mlog", "通过发送mavlink格式的重启命令完成");
                        isRebooting = false;
                        __sync();
                    } else {
                        //刚刚连接上飞控主机时 开始更新
                        rebootBymavlink();
//                        mainHander.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (mSocket != null && !mSocket.isClosed()) {
//                                    try {
//                                        mSocket.close();
//                                    } catch (IOException e) {
//
//                                    }
//                                    mSocket = null;
//                                }
//                                connectsocket(2);
//                            }
//                        }, 2000);
//                    __sync();
                        if (mCallback != null) {
                            mainHander.post(new Runnable() {
                                @Override
                                public void run() {
                                    mCallback.update(UpdateFirmwareErrorOK, 0);
                                }
                            });
                        }
                    }
                    break;
                case UPDATE_YUNTAI_FW:
                    /**升级云台*/
                    Log.d("Mlog", "进入升级云台流程");
                    timeOutCount = 0;
                    if (yuntaiLocalPath != null) {
                        loadYuntaiFwData(yuntaiLocalPath);
                    }
                    rebootYuntaiByMav();
                    break;
            }

        } catch (IOException e) {
            Log.d("Mlog", "没有连接上主机==>" + e.toString());
            if (connectCount <= 3) {
                mainHander.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        connectsocket(3);
                        connectCount++;
                    }
                }, 1000);
            } else {
                if (mCallback != null) {
                    mainHander.post(new Runnable() {
                        @Override
                        public void run() {
                            reset();
                            connectCount = 1;
                            mCallback.update(UpdateFirmwareErrorCannotConnectToHost, 0);
                            mCallback.requestYuntaiVer(false, false, "");
                        }
                    });
                }

            }

        }
    }

    private void __sync() {
        Log.d("Mlog", "__sync");
        byte TSYNC[] = {GET_SYNC, EOC};
        Event sd = new Event(Event.State_Write, SNYC_TAG, 0, TSYNC);
        if (mEventQueue != null)
            mEventQueue.putEvent(sd);
        Event rd = new Event(Event.State_Read, SNYC_TAG, 2, null);
        if (mReadQueue != null)
            mReadQueue.putEvent(rd);
    }

    private void getFWMaxSize() {
        Log.d("Mlog", "getFWMaxSize");
        byte FWMaxSize[] = {GET_DEVICE, INFO_FLASH_SIZE, EOC};
        Event sd = new Event(Event.State_Write, MAXSIZE_TAG, 0, FWMaxSize);
        if (mEventQueue != null)
            mEventQueue.putEvent(sd);
        Event rd = new Event(Event.State_Read, MAXSIZE_TAG, 6, null);
        if (mReadQueue != null)
            mReadQueue.putEvent(rd);
    }

    private void getFWDevice() {
        Log.d("Mlog", "getFWDevice");
        byte FWDeviceSize[] = {GET_DEVICE, INFO_DEVICE_SIZE, EOC};
        Event sd = new Event(Event.State_Write, DEVICE_TAG, 0, FWDeviceSize);
        if (mEventQueue != null)
            mEventQueue.putEvent(sd);
        Event rd = new Event(Event.State_Read, DEVICE_TAG, 6, null);
        if (mReadQueue != null)
            mReadQueue.putEvent(rd);
    }


    private void rebootBymavlink() {
        Log.d("Mlog", "rebootBymavlink");
        Event sd1 = new Event(Event.State_Write, REBOOT_MAV_TAG, 0, MAVLINK_REBOOT_ID1);
        if (mEventQueue != null)
            mEventQueue.putEvent(sd1);
        Event sd2 = new Event(Event.State_Write, REBOOT_MAV_TAG, 0, MAVLINK_REBOOT_ID2);
        if (mReadQueue != null)
            mReadQueue.putEvent(sd2);

        isRebooting = true;
        mainHander.postDelayed(new Runnable() {
            @Override
            public void run() {
                connectsocket(4);
            }
        }, 2000);

    }

    private void rebootYuntaiByMav() {
        Log.d("Mlog", "rebootYuntaiByMav");
        Event sd1 = new Event(Event.State_Write, REBOOT_YUNTAI_MAV_TAG, 0, MavlinkRequestMessage.rebootYuntai().pack().encodePacket());
        if (mEventQueue != null)
            mEventQueue.putEvent(sd1);
//        Event rd1 = new Event(Event.State_Read, REBOOT_YUNTAI_MAV_TAG, 0, PlaneCommand.rebootYuntai().pack().encodePacket());
//        if (mEventQueue != null)
//            mEventQueue.putEvent(rd1);

        mainHander.post(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    uProgress = 1;
                    mCallback.upDateYuntai(UpdateFirmwareErrorOK, 1);
                }
            }
        });
        mainHander.postDelayed(new Runnable() {
            @Override
            public void run() {
                startUpdateYuntai();
            }
        }, 2000);
    }

    private void startUpdateYuntai() {
        Log.d("Mlog", "startUpdateYuntai");
        this.sndNum = 0;
        this.uProgress = 0;
        if (firmwareData != null && firmwareData.length > 0) {
            int read_value = PROG_MULTI_MAX_NEW;
            int num = this.firmwareData.length / PROG_MULTI_MAX_NEW;
            if (this.sndDatas == null) {
                this.sndDatas = new ArrayList<>();
            } else {
                this.sndDatas.clear();   //重新开始的先清除掉
            }
            if (this.msgCommands == null) {
                msgCommands = new ArrayList<>();
            } else {
                this.msgCommands.clear();
            }
            if (this.msgResponses == null) {
                msgResponses = new ArrayList<>();
            } else {
                this.msgResponses.clear();
            }
            if (this.msgObjs == null) {
                msgObjs = new ArrayList<>();
            } else {
                this.msgObjs.clear();
            }
            int i = 0;
            for (; i < num; i++) {
                byte[] d = new byte[read_value];
                System.arraycopy(firmwareData, read_value * i, d, 0, read_value);
                this.sndDatas.add(d);
            }

            int llen = this.firmwareData.length % read_value;
            if (llen > 0) {
                byte[] d = new byte[llen];
                System.arraycopy(firmwareData, read_value * i, d, 0, llen);
                this.sndDatas.add(d);
            }
            mainHander.post(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        uProgress = 2;
                        mCallback.upDateYuntai(UpdateFirmwareErrorOK, 2);
                    }
                }
            });
            // 复位云台
            resetYuntai();
        } else {
            mCallback.upDateYuntai(UpdateFirmwareReadError, 0);
        }
    }

    /**
     * 重置云台
     */
    private void resetYuntai() {
        this.upStatus &= UPStatusInit;

        // 握手之前先让云台复位 0.5秒之后发送
        MsgReset con2 = new MsgReset();
        con2.setMsgCallback(0.5f, new MsgBase.msgCallBack() {
            @Override
            public void onTimeOut(int count) {
                Log.d("Mlog", "重置云台onTimeOut");
                mainHander.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            uProgress = 3;
                            mCallback.upDateYuntai(UpdateFirmwareErrorOK, 3);
                        }
                    }
                });
                handShake();
            }

            @Override
            public void onComplete(MsgBase msgObj) {

            }
        });
        sendYuntaiCommand(con2);
        // 重置云台后->开始握手，2.5秒内还没有握手成功，则重新开始
    }

    // 云台握手
    private void handShake() {
        MsgCon con = new MsgCon();
        con.setMsgCallback(1f, new MsgBase.msgCallBack() {
            @Override
            public void onTimeOut(int count) {
                Log.d("Mlog", "云台握手onTimeOut");
                if (count <= tOutCount - 2) {//表示每个回合握手两次
                    handShake();
                } else if (count <= tOutCount) {
                    /**握手两个回合都失败的话，就再重置云台一次*/
                    resetYuntai();
                } else {
                    /**此时就判定为彻底挂了*/
                    Log.d("Mlog", "握手失败，彻底挂掉...");
                    reset();
//                    if (mCallback != null)
//                        mCallback.upDateYuntai(2, uProgress);
                }
            }

            @Override
            public void onComplete(MsgBase msgObj) {
                timeOutCount = 0;
                mainHander.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            uProgress = 4;
                            mCallback.upDateYuntai(UpdateFirmwareErrorOK, 4);
                        }
                    }
                });
                Log.d("Mlog", "云台握手onComplete");
                upStatus |= UPStatus_HandShaked;
                MsgCon con = (MsgCon) msgObj;
                // 握手正确后就开始擦除flash
                yuntaiErase();
            }
        });
        sendYuntaiCommand(con);
    }

    int timeOutCount = 0;//超时次数

    /**
     * 发送云台固件
     */
    private void sendYuntaiCommand(final MsgBase baseObj) {
        if (baseObj == null) {
            return;
        }
        msgCommands.clear();
        if (baseObj.timeout > 0) {  // 如果设置了超时时间 则指定延迟后执行
            mainHander.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (msgCommands.contains(baseObj)) {
                        timeOutCount++;
//                        if (mEventQueue != null) {
//                            mEventQueue.stopRead();
//                        }
                        Log.d("Mlog", "消息超时了。。。。" + timeOutCount);
//                        msgCommands.remove(baseObj);
                        if (baseObj.getMsgCallBack() != null) {
                            baseObj.setTimeOut(true);
                            baseObj.getMsgCallBack().onTimeOut(timeOutCount);
                        }
                    }
                }
            }, (long) (baseObj.timeout * 1000));
        }

        this.msgCommands.add(baseObj);
        if (this.msgCommands.size() > 1) {
            Log.d("Mlog", "消息数目超过一条");
        } else {
            Log.d("Mlog", baseObj.toString());
            Event sd = new Event(Event.State_Write, YUNTAI_COMMAND_TAG, 0, baseObj.dataFromObject());
            if (mEventQueue != null)
                mEventQueue.putEvent(sd);
            if (baseObj.responseLength() != 0) {
                Event rd = new Event(Event.State_Read, YUNTAI_COMMAND_TAG, baseObj.responseLength(), null);
                if (mReadQueue != null) {
                    LogUtils.d("继续读----");
                    mReadQueue.putEvent(rd);
                }
            }
        }

    }

    /**
     * 擦除云台程序
     */
    private void yuntaiErase() {
        MsgErase con = new MsgErase();
        con.fileLen = firmwareData.length;
        con.setMsgCallback(3.0f, new MsgBase.msgCallBack() {
            @Override
            public void onTimeOut(int count) {
                Log.d("Mlog", "云台擦除onTimeOut");
                if (count <= tOutCount) {//最多循环擦除2次云台
                    yuntaiErase();
                } else {
                    /**擦除云台3次都挂掉，那是没救了*/
                    Log.d("Mlog", "擦除云台3次都挂掉了");
                    reset();
//                    if (mCallback != null)
//                        mCallback.upDateYuntai(2, uProgress);
                }
            }

            @Override
            public void onComplete(MsgBase msgObj) {
                Log.d("Mlog", "云台擦除onComplete");
                timeOutCount = 0;
                MsgErase con = (MsgErase) msgObj;
                if (!con.crcIsRight()) {
                    // "擦除回应crc 校验不一致");
                    Log.d("Mlog", "擦除回应crc 校验不一致");

                    yuntaiErase();
                    return;
                }
                if (con.response != 0) {
                    Log.d("Mlog", "擦除失败!，重试");
                    yuntaiErase();
                    return;
                }
                mainHander.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            uProgress = 5;
                            mCallback.upDateYuntai(UpdateFirmwareErrorOK, 5);
                        }
                    }
                });
                upStatus |= UPStatus_Eraseed;
                // 擦除完毕则开始上传固件
                programYuntaiBytes();
            }
        });
        sendYuntaiCommand(con);
    }

    /**
     * 开始上传云台固件
     */
    private void programYuntaiBytes() {
        if (sndNum < sndDatas.size()) {
            Log.d("Mlog", "第" + (sndNum + 1) + "次/(总共" + sndDatas.size() + ")写入固件");
            byte[] sndData = sndDatas.get(sndNum);

            MsgFirware con = new MsgFirware();
            con.setMsgCallback(5.0f, new MsgBase.msgCallBack() {
                @Override
                public void onTimeOut(int count) {
                    Log.d("Mlog", "上传云台固件onTimeOut");
                    if (count < 2) {
                        sndNum--;//重新发送上一个数据包
                        programYuntaiBytes();
                    } else {
                        /**三次发过去都是无济于事的话，那是没救了*/
                        Log.d("Mlog", "上传云台固件彻底挂掉了");
                        reset();
//                        if (mCallback != null)
//                            mCallback.upDateYuntai(2, uProgress);
                    }
                }

                @Override
                public void onComplete(MsgBase msgObj) {
                    Log.d("Mlog", "上传云台固件onComplete");
                    timeOutCount = 0;//重置超时次数
                    MsgFirware con = (MsgFirware) msgObj;
                    if (!con.crcIsRight()) {    // crc不一致 zsz:todo 继续上传固件？
                        Log.d("Mlog", "上传固件 crc不一致");
                        sndNum--;          // 重发上一个数据包
                    } else {
//                        timeOutCount = 0;//重置超时次数
                    }
                    programYuntaiBytes();

//                    if (sndDatas.size() < 96) {
                    uProgress = ((sndNum + 5) * 100 / (sndDatas.size() + 5));
//                    } else {
//                        int step = sndDatas.size() / 10;
//                        if ((sndNum) % (step) == 0) {
//                            uProgress += 1;
//                        }
//                    }

                    mainHander.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mCallback != null) {
                                mCallback.upDateYuntai(UpdateFirmwareErrorOK, uProgress);
                            }
                        }
                    });
                }
            });
            con.fwSeq = sndNum;
            con.fwData = sndData;
            sendYuntaiCommand(con);
            sndNum++;
        } else if (sndNum > 0) {   // 固件上传完成
            Log.d("Mlog", "上传云台固件完成做校验");
            upStatus |= UPStatus_UpFirmwared;
            getCRCFromRemote();
        }
    }

    // 获取固件CRC
    private void getCRCFromRemote() {
        MsgFirwareCRC con = new MsgFirwareCRC();
        con.setMsgCallback(5.0f, new MsgBase.msgCallBack() {
            @Override
            public void onTimeOut(int count) {
                Log.d("Mlog", "云台校验onTimeOut");
                if (count <= tOutCount) {
                    getCRCFromRemote();
                } else {
                    reset();
                    Log.d("Mlog", "云台校验彻底挂了...");
                }
            }

            @Override
            public void onComplete(MsgBase msgObj) {
                Log.d("Mlog", "云台校验onComplete");
                timeOutCount = 0;//只要有校验码，说明还有网络，就不断开
                MsgFirwareCRC con = (MsgFirwareCRC) msgObj;
                if (!con.crcIsRight()) {
                    Log.d("Mlog", "获取固件 crc不一致");
                    getCRCFromRemote();
                    return;
                }
                long localcrc = MsgBase.crc32(firmwareData, 0);
                byte[] llcrcs = new byte[4];
                MsgBase.setToBytesFromLong(llcrcs, 0, localcrc, 4);
                Log.d("Mlog", "本地 固件crc" + MsgBase.byte2hex(llcrcs));
                long llcrc = con.fwcrc;
                byte[] ddddd = new byte[4];
                MsgBase.setToBytesFromLong(ddddd, 0, llcrc, 4);
                Log.d("Mlog", "机器 固件crc" + MsgBase.byte2hex(ddddd));
                if (localcrc != llcrc) {
                    Log.d("Mlog", "本地crc与机器中crc不一致，重新升级");
                    startUpdateYuntai();//只能重新上传了,这里应该给个手动回调才对？
                    return;
                }

                // 通知升级完成
                upStatus |= UPStatus_GetCrced;
                finishUpdate();
            }
        });
        sendYuntaiCommand(con);
    }

    // 发送固件更新完成通知
    private void finishUpdate() {
        MsgFinish con = new MsgFinish();
        con.setMsgCallback(0.5f, new MsgBase.msgCallBack() {
            @Override
            public void onTimeOut(int count) {
                Log.d("Mlog", "发送固件更新完成通知(超时回调!)");
                timeOutCount = 0;
                upStatus |= UPStatus_Finished;
                // "升级成功";
                mainHander.post(new Runnable() {
                    @Override
                    public void run() {
                        reset();
                        if (mCallback != null) {
                            mCallback.upDateYuntai(UpdateFirmwareErrorOK, 100);
                        }
                    }
                });
            }

            @Override
            public void onComplete(MsgBase msgObj) {
                Log.d("Mlog", "发送固件更新完成通知(完成回调!)");
                timeOutCount = 0;
                upStatus |= UPStatus_Finished;
                // "升级成功";
                mainHander.post(new Runnable() {
                    @Override
                    public void run() {
                        reset();
                        if (mCallback != null) {
                            mCallback.upDateYuntai(UpdateFirmwareErrorOK, 100);
                        }
                    }
                });
            }
        });
        sendYuntaiCommand(con);
    }

    /**
     * 获取版本号：飞控，云台，同时判断解锁状态
     *
     * @param continueRead 是否只是为了继续读，只为继续读，就不用写入啦
     */
    private void checkFirmwareVer(boolean continueRead) {
        Log.d("Mlog", "检测版本号...");
        if (!continueRead) {
            byte[] msg_idver = MavlinkRequestMessage.requestMavlinkVersion().pack().encodePacket();
            Event sd1 = new Event(Event.State_Write, CHECKE_VERSION_TAG, 0, msg_idver);

            byte[] msg_id_yuntai_ver = MavlinkRequestMessage.requestYuntaiVer().pack().encodePacket();
            Event sd2 = new Event(Event.State_Write, CHECKE_VERSION_TAG, 0, msg_id_yuntai_ver);

            byte[] msg_idlocked = MavlinkRequestMessage.requestMavlinkParamSet(MAV_DATA_STREAM.SR1_EXTRA3, unLock == -1 ? 6 : 0).pack().encodePacket();
            Event sd3 = new Event(Event.State_Write, CHECKE_VERSION_TAG, 0, msg_idlocked);

            byte[] msg_ack = MavlinkRequestMessage.requestMavlinkPlaneParam(MAV_DATA_STREAM.FP_ACTIVATION).pack().encodePacket();
            Event sd4 = new Event(Event.State_Write, CHECKE_ACK_TAG, 0, msg_ack);
            if (mEventQueue != null) {
                if (sd1 != null && planeVer == null) {
                    mEventQueue.putEvent(sd1);
//                    mEventQueue.putEvent(sd1);
//                    mEventQueue.putEvent(sd1);
//                    mEventQueue.putEvent(sd1);
//                    mEventQueue.putEvent(sd1);
                }
                if (sd2 != null && yuntaiVer == null){
                    mEventQueue.putEvent(sd2);
                    mEventQueue.putEvent(sd2);
                    mEventQueue.putEvent(sd2);
                    mEventQueue.putEvent(sd2);
                    mEventQueue.putEvent(sd2);
                }
                if (sd3 != null){
                    mEventQueue.putEvent(sd3);
                    mEventQueue.putEvent(sd3);
                    mEventQueue.putEvent(sd3);
                    mEventQueue.putEvent(sd3);
                    mEventQueue.putEvent(sd3);
                }
                if (sd4 != null && isAck == -1){
                    mEventQueue.putEvent(sd4);
                    mEventQueue.putEvent(sd4);
                    mEventQueue.putEvent(sd4);
                    mEventQueue.putEvent(sd4);
                    mEventQueue.putEvent(sd4);
                }
            }
        }
        Event rd = new Event(Event.State_Read, CHECKE_VERSION_TAG, 1024, null);
        if (mReadQueue != null)
            mReadQueue.putEvent(rd);
    }

    /**
     * 检测激活状态
     *
     * @param continueRead 是否只是为了继续读，只为继续读，就不用写入啦
     */
    private void checkPlaneAck(boolean continueRead) {
        Log.d("Mlog", "检测飞控激活状态...");
        if (!continueRead) {
            byte[] msg_idver = MavlinkRequestMessage.requestMavlinkPlaneParam(MAV_DATA_STREAM.FP_ACTIVATION).pack().encodePacket();
            Event sd1 = new Event(Event.State_Write, CHECKE_ACK_TAG, 0, msg_idver);
            if (mEventQueue != null) {
                mEventQueue.putEvent(sd1);
                mEventQueue.putEvent(sd1);
                mEventQueue.putEvent(sd1);
                mEventQueue.putEvent(sd1);
                mEventQueue.putEvent(sd1);
            }
        }
        Event rd = new Event(Event.State_Read, CHECKE_ACK_TAG, 1024, null);
        if (mReadQueue != null)
            mReadQueue.putEvent(rd);
    }

    private void erase() {
        Log.d("Mlog", "erase");
        byte ERASE[] = {CHIP_ERASE, EOC};
        Event sd = new Event(Event.State_Write, ERASE_TAG, 0, ERASE);
        if (mEventQueue != null)
            mEventQueue.putEvent(sd);
        Event rd = new Event(Event.State_Read, ERASE_TAG, 2, null);
        if (mReadQueue != null)
            mReadQueue.putEvent(rd);
    }

    static long begin, end;

    private void program() {
        Log.d("Mlog", "program");
        if (firmwareData == null && fwContent != null) {
            Log.d("Mlog", "加载固件到内存  开始。。。。。");
            loadFirmware(fwContent);
            Log.d("Mlog", "加载固件到内存  完成。。。。。");
        }
        if (firmwareData == null)
            return;

        int num = firmwareData.length / PROG_MULTI_MAX;
        if (sndDatas == null) {
            sndDatas = new ArrayList<>();
        } else {
            sndDatas.clear();   //重新开始的时候 先清除掉
        }

        int i = 0;
        for (; i < num; i++) {
            byte[] d = new byte[PROG_MULTI_MAX];
            System.arraycopy(firmwareData, PROG_MULTI_MAX * i, d, 0, PROG_MULTI_MAX);
            sndDatas.add(d);
        }

        int llen = firmwareData.length % PROG_MULTI_MAX;
        if (llen > 0) {
            byte[] d = new byte[llen];
            System.arraycopy(firmwareData, PROG_MULTI_MAX * i, d, 0, llen);
            sndDatas.add(d);
        }
        Log.d("Mlog", "固件长度==>" + firmwareData.length + "发送次数==>" + sndDatas.size());
        begin = System.currentTimeMillis();
        programBytes();


    }

    private void programBytes() {

        if (sndNum < sndDatas.size()) {
//            end = System.currentTimeMillis();
//            Log.d("Mlog","写入固件的时间==>"+(end - begin)+"毫秒");
//            if ((end - begin) > 100) {
//                Log.d("异常","异常写入时间==>"+(end-begin));
//            }
//            begin = end;

//            Log.d("Mlog","programBytes 第"+(sndNum+1)+"次"+"/"+"总共("+sndDatas.size()+"次)写入固件数据");
            byte[] sndData = sndDatas.get(sndNum);
            int len = sndData.length;
            byte[] dd;

            int loc = 0;
            if (PROG_MULTI_MAX == 252 * 22) {
                dd = new byte[len + 4];
                dd[0] = PROG_MULTI;
                dd[1] = (byte) ((len) & 0xFF);
                dd[2] = (byte) ((len >> 8) & 0xFF);
                Log.d("Mlog", "新固件" + dd[1] + "||" + dd[2]);
                loc = 3;
            } else {
                dd = new byte[len + 3];
                Log.d("Mlog", "老固件");
                dd[0] = PROG_MULTI;
                dd[1] = (byte) len;
                loc = 2;
            }
            int i = 0;
            for (; i < len; i++) {
                dd[loc + i] = sndData[i];
            }
            dd[loc + i] = EOC;
            Event sd = new Event(Event.State_Write, PROG_MULTI_TAG, 0, dd);
            if (mEventQueue != null)
                mEventQueue.putEvent(sd);
            Event rd = new Event(Event.State_Read, PROG_MULTI_TAG, 2, null);
            if (mReadQueue != null)
                mReadQueue.putEvent(rd);
            sndNum++;
        } else {
            end = System.currentTimeMillis();
            Log.d("Mlog", "写入固件的时间==>" + (end - begin) / 1000 + "秒");
            verify();
        }
    }

    private void verify() {
        Log.d("Mlog", "verify");
        byte VERIFY[] = {GET_CRC, EOC};
        Event sd = new Event(Event.State_Write, VERIFY_TAG, 0, VERIFY);
        if (mEventQueue != null)
            mEventQueue.putEvent(sd);
        Event rd = new Event(Event.State_Read, VERIFY_TAG, 6, null);
        if (mReadQueue != null)
            mReadQueue.putEvent(rd);
    }

    private void reboot() {
        Log.d("Mlog", "reboot");
        byte LREBOOT[] = {REBOOT, EOC};

        Event sd = new Event(Event.State_Write, REBOOT_TAG, 0, LREBOOT);
        if (mEventQueue != null)
            mEventQueue.putEvent(sd);
        Event rd = new Event(Event.State_Read, REBOOT_TAG, 2, null);
        if (mReadQueue != null)
            mReadQueue.putEvent(rd);
    }


    private void loadFirmware(String content) {
        try {
            JSONObject json = new JSONObject(content);
            String imageString = (String) json.get("image");
            BASE64Decoder base64 = new BASE64Decoder();
            byte[] base64s = base64.decodeBuffer(imageString);
            Log.d("Mlog", "base64解码长度==>" + base64s.length);
            byte[] d = decompress(base64s);
            Log.d("测试", "最终固件长度==>" + d.length);

            int flen = d.length;
            firmwareData = new byte[flen + flen % 4];
            System.arraycopy(d, 0, firmwareData, 0, flen);
            Log.d("测试", "固件长度==>" + flen);
            while (flen % 4 != 0) {
                byte ff = (byte) 0xff;
                firmwareData[flen] = ff;
                flen++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static byte[] decompress(byte[] data) throws IOException, DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();
        Log.d("测试", "Original: " + data.length);
        Log.d("测试", "Compressed: " + output.length);
        return output;
    }

    private long crc32(final byte[] src, long state) {
        long[] crctab = new long[256];

        /* check whether we have generated the CRC table yet */
        /* this is much smaller than a static table */
        if (crctab[1] == 0) {
            for (int i = 0; i < 256; i++) {
                long c = i;

                for (int j = 0; j < 8; j++) {
                    if ((c & 1) > 0) {
                        c = 0xedb88320L ^ (c >> 1);

                    } else {
                        c = c >> 1;
                    }
                }
                crctab[i] = c;
            }
        }


        for (int i = 0; i < src.length; i++) {
            int c = (int) ((state ^ (src[i] & 0xffL)) & 0xffL);
            state = crctab[c] ^ (state >> 8);
        }

        return state;
    }

    private String byte2hex(byte[] buffer) {
        String h = "";

        for (int i = 0; i < buffer.length; i++) {
            String temp = Integer.toHexString(buffer[i] & 0xFF);
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            h = h + " " + temp;
        }

        return h;

    }

    public String getSerial(short[] flight_custom_version, short[] middleware_custom_version) {
        StringBuilder sb = new StringBuilder();
        for (short s : flight_custom_version) {
            String temp = Integer.toHexString(s & 0xFF);
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            sb.append(temp);
        }
        for (int i = 0; i < 4; i++) {
            String temp1 = Integer.toHexString(middleware_custom_version[i] & 0xFF);
            if (temp1.length() == 1) {
                temp1 = "0" + temp1;
            }
            sb.append(temp1);
        }
        return sb.toString();
    }

    private boolean isRightSync(byte[] data) {
        if (data == null || data.length < 2) {
            return false;
        }
//        Log.d("测试", "直接输出==>" + Arrays.toString(data));
        byte c = data[0];
        Log.d("测试", "输出的值==>" + c + "||" + data[1]);
        if (c != INSYNC) {
            if (mCallback != null) {
                mainHander.postAtFrontOfQueue(new Runnable() {
                    @Override
                    public void run() {
                        reset();
                        mCallback.update(UpdateFirmwareErrorInvalidFirmware, 0);
                    }
                });

            }
            return false;
        }

        c = data[1];
        if (c != OK) {
            Log.d("测试", "输出的值==>%d" + (char) c);
            if (mCallback != null) {
                mainHander.postAtFrontOfQueue(new Runnable() {
                    @Override
                    public void run() {
                        reset();
                        mCallback.update(UpdateFirmwareErrorInvalidFirmware, 0);
                    }
                });

            }
            return false;
        }

        return true;
    }

    private byte[] leftReceivedData;    // 剩余接收的消息
    // 生产者消费者模型
    private ArrayList<MsgBase> msgResponses;    // 消息回应队列
    private ArrayList<MsgBase> msgObjs;         // 消息回应队列对应的消息
    // 固件升级方法

    private void processData(byte[] data) {
        Log.d("测试", "云台升级一直读取");
        int newLen = data.length;
        if (leftReceivedData != null) {
            newLen = leftReceivedData.length + data.length;
        }
        ReceivePacket parse = new ReceivePacket();
        int i_NEXT_STX_loc = 0;
        byte[] newData = new byte[newLen];

        if (this.leftReceivedData != null) { //说明还有剩余的没有处理完
            System.arraycopy(leftReceivedData, 0, newData, 0, leftReceivedData.length);
            System.arraycopy(data, 0, newData, leftReceivedData.length, data.length);
        } else {
            System.arraycopy(data, 0, newData, 0, data.length);
        }
        int leng = newData.length;
        for (int i = 0; i < leng; i++) {
            byte c = newData[i];

            ReceivePacket.Packet packet = parse.ReceiveStateMachine(c);
            if (packet != null) { // 说明找到了一个完整的包
                int packetLen = packet.packetLen;
                i_NEXT_STX_loc = i + 1;

                // 完整包的回应
                byte[] parseData = new byte[packetLen];
                MsgBase con = new MsgBase();
                if (packet.byCmd == MsgBase.UP_CON_REQ) {   // 握手

                    if ((this.upStatus & UPStatus_HandShaked) == 1) {    // 已经过了握手阶段了
                        continue;
                    }
                    System.arraycopy(newData, i + 1 - packetLen, parseData, 0, packetLen);
                    con = MsgCon.ObjectFromReponseData(parseData);
                } else if (packet.byCmd == MsgBase.UP_ERASE_REQ) {

                    if ((this.upStatus & UPStatus_Eraseed) == 1) {    // 已经擦除过了
                        continue;
                    }

                    System.arraycopy(newData, i + 1 - packetLen, parseData, 0, packetLen);
                    con = MsgErase.ObjectFromReponseData(parseData);
                } else if (packet.byCmd == MsgBase.UP_SNDFW_REQ) {

                    if ((this.upStatus & UPStatus_UpFirmwared) == 1) {    // 已经发送完毕固件了
                        continue;
                    }

                    System.arraycopy(newData, i + 1 - packetLen, parseData, 0, packetLen);
                    con = MsgFirware.ObjectFromReponseData(parseData);
                    this.upStatus |= UPStatus_UpFirmwared;
                } else if (packet.byCmd == MsgBase.UP_GETCRC_REQ) { // 如果是读取最大的固件长度 读取前四个字节

                    if ((upStatus & UPStatus_GetCrced) == 1) {    // 已经过了获取了机器 crc
                        continue;
                    }

                    System.arraycopy(newData, i + 1 - packetLen, parseData, 0, packetLen);
                    con = MsgFirwareCRC.ObjectFromReponseData(parseData);
                } else if (packet.byCmd == MsgBase.UP_FINISH_REQ) {

                    if ((upStatus & UPStatus_Finished) == 1) {    // 已经结束
                        continue;
                    }

                    System.arraycopy(newData, i + 1 - packetLen, parseData, 0, packetLen);
                    con = MsgFinish.ObjectFromReponseData(parseData);
                }

                if (msgCommands.size() > 0) {
                    MsgBase requestCon = this.msgCommands.get(0);
                    if (requestCon.msgId == con.msgId) {
                        this.msgCommands.remove(requestCon);
                        this.msgResponses.add(requestCon);
                        this.msgObjs.add(con);
                    } else {
                        // 消息id不一致，丢弃此回应
                        timeOutCount = 0;
                        Log.d("测试", "消息id不一致，丢弃");
                    }
                } else {
                    // 没有回应消息了...
                    Log.d("测试", "用于回应的消息处理完毕...");
                }
                parse.resetState();
            }
        }

        parse.resetState();

        if (i_NEXT_STX_loc < leng) {   // 收到的数据中包含了一个完整的包，但是又剩余了部分数据
            Log.d("测试", "还剩余没有处理的字节==>" + (leng - i_NEXT_STX_loc));
            this.leftReceivedData = new byte[leng - i_NEXT_STX_loc];
            System.arraycopy(newData, i_NEXT_STX_loc, leftReceivedData, 0, leng - i_NEXT_STX_loc);
        } else {
            // 释放已经存在的
            this.leftReceivedData = null;
        }

        // 进行数据回调 如果找到回应的消息了
        if (this.msgResponses.size() > 0) {
            for (int i = 0; i < this.msgResponses.size(); i++) {
                MsgBase requestCon = this.msgResponses.get(0);
                MsgBase con = msgObjs.get(0);
                if (requestCon.getMsgCallBack() != null) {
                    requestCon.setTimeOut(false);
                    requestCon.getMsgCallBack().onComplete(con);
//                    if (leftReceivedData.length > 0)
//                        this.leftReceivedData = null;
//                    Log.d("测试", "已经有了这个消息回应了，反正多余的，我就丢掉吧");
                }
            }
        } else {
            if (mSocket != null && !mSocket.isClosed()) {
                Event rd = new Event(Event.State_Read, YUNTAI_COMMAND_TAG, 128, null);
                if (mReadQueue != null) {
                    Log.d("测试", "还没读完，继续读----");
                    mReadQueue.putEvent(rd);
                }
            }
        }

        this.msgObjs.clear();
        this.msgResponses.clear();
    }
}

