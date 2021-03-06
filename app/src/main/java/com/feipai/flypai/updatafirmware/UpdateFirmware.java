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
         * ???????????????????????????
         * error 1 ??????????????????????????????????????????????????????????????? -1??????????????????????????????????????? 0????????????????????????????????????????????????????????????
         * progress ????????????????????????????????????(0-100) ?????????????????????????????????????????? progress???0
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


    //????????????start
    private static int PROG_MULTI_MAX_NEW = 640;
    private long upStatus;
    private ArrayList<MsgBase> msgCommands;     // ????????????
    private static long UPStatusInit = 1 << 0;
    private static long UPStatus_HandShaked = 1 << 1;
    private static long UPStatus_GetCrced = 1 << 2;
    private static long UPStatus_Eraseed = 1 << 3;
    private static long UPStatus_UpFirmwared = 1 << 4;
    private static long UPStatus_Finished = 1 << 5;
    //????????????end

    /**
     * firmwareContent:????????????????????? ?????????json????????? ?????????????????????????????????????????????????????????
     * host:??????????????????????????? ??????192.168.1.254 port ???????????????????????? ??????3333
     * callback:???????????????????????????????????????
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
            Log.d("Mlog", "?????????????????????" + firmwareData.length);
        }

    }

    /**
     * host ???????????????IP???????????????192.168.1.254 port ????????? ??????3333
     *
     * @param check ?????????????????????
     * @param type  ????????????????????????
     */
    public void update(int type, boolean check) {
//        Log.d("Mlog", "???????????????--->" + type + "||" + check);
        this.checkTpye = type;
        checkSerial = check;
        if (mRun) {
            Log.d("Mlog", "???????????????????????? ?????????????????????????????????????????????");
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
        Log.d("Mlog", "connectsocket ????????????" + where);
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
//                LogUtils.d("??????--->?????????" + sndDatas.size());
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
                wait();//????????????...
            } catch (InterruptedException e) {
                if (isReading) {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            Log.d("Mlog", "????????????2");
            return null;
        }

        @Override
        public void run() {
            while (isReading) {
                Event obj = getReadEvent();
                if (obj == null) {
//                    Log.d("Mlog", "??????????????????????????????");
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
                            Log.d("Mlog", "?????????????????????");
                            return;
                        }
//                        Log.d("Mlog", "?????????????????????");
                        byte[] d = new byte[readLen];
                        lenth = read.read(d);
                        if (lenth > 0) {
                            byte[] data = new byte[lenth];
                            System.arraycopy(d, 0, data, 0, lenth);
//                            Log.d("Mlog", "??????????????????==>+" + byte2hex(data) + ",count=" + lenth);
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
                                // ????????????????????????????????????
                                Log.d("Mlog", "?????????????????????==>" + e.getMessage() + "||Tag=" + tag + "||" + obj.getTag());
                                if (tag == CHECKE_VERSION_TAG) {
                                    Log.d("Mlog", "????????????????????????????????????????????????==>" + e.getMessage());
                                    if (mCallback != null) {
                                        mCallback.requestPlaneVer(false, false, "2");
                                        mCallback.requestYuntaiVer(false, false, "2");
                                    }
                                } else if (tag == CHECKE_ACK_TAG) {
                                    Log.d("Mlog", "???????????????????????????????????????????????????==>" + e.getMessage());
                                    if (mCallback != null)
                                        mCallback.requestAct(true, -1);
                                } else if (tag == YUNTAI_COMMAND_TAG) {
                                    Log.d("Mlog", "???????????????????????????????????????==>" + e.getMessage());
                                    if (mCallback != null) {
                                        mCallback.upDateYuntai(UpdateFirmwareReadError, 0);
                                    }
                                } else {
                                    Log.d("Mlog", "???????????????????????????????????????==>" + e.getMessage());
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
     * ?????????????????????
     */
    private void parsReadData(int tag, int readLen, byte[] datas) {
        int newLen = datas.length;
        if (leftReceivedData != null) {
            newLen = leftReceivedData.length + datas.length;
        }
        byte[] newData = new byte[newLen];
        if (this.leftReceivedData != null) { //????????????????????????????????????
            System.arraycopy(leftReceivedData, 0, newData, 0, leftReceivedData.length);
            System.arraycopy(datas, 0, newData, leftReceivedData.length, datas.length);
        } else {
            System.arraycopy(datas, 0, newData, 0, datas.length);
        }
        if ((newData.length >= readLen) || (tag == CHECKE_VERSION_TAG || tag == CHECKE_ACK_TAG) && newData.length >= 2) {//??????????????????????????????????????????????????????????????????
            if (tag == SNYC_TAG) {
                int c = newData[0] & 0xff;
//                            if (c == 0xfe) {
//                                rebootBymavlink();
//
//                            } else {
                boolean isOk = true;
                if (c != INSYNC) {
                    Log.d("Mlog", "????????????==>" + c);
                    isOk = false;
                }
                c = newData[1];
                if (c != OK) {
                    Log.d("Mlog", "????????????==>%d" + (char) c);
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
                    Log.d("Mlog", "??????????????????");
                    PROG_MULTI_MAX = 252;
                } else if (value == 6) {
                    PROG_MULTI_MAX = 252 * 22;
                    Log.d("Mlog", "??????????????????");
                }
//                            fwMaxsize = value;
                Log.d("Mlog", "????????????????????????==>" + value);
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
                Log.d("Mlog", "????????????????????????==>" + value);
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
                Log.d("Mlog", "??????????????????==>" + byte2hex(newData) + " value==>" + receive_crc);

                long expect_crc = crc32(firmwareData, 0);
                int padlen = fwMaxsize - firmwareData.length;
                byte[] padData = new byte[padlen];
                for (int i = 0; i < padlen; i++) {
                    padData[i] = (byte) 0xff;
                }

                expect_crc = crc32(padData, expect_crc);
                byte[] result2 = new byte[4];
                //??????????????????
                result2[0] = (byte) ((expect_crc >> 24) & 0xFF);
                result2[1] = (byte) ((expect_crc >> 16) & 0xFF);
                result2[2] = (byte) ((expect_crc >> 8) & 0xFF);
                result2[3] = (byte) (expect_crc & 0xFF);
                Log.d("Mlog", "???????????????????????????==>" + byte2hex(result2) + " value==>" + expect_crc);

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
                            Log.d("Mlog", "??????crc???????????????");
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
                        /**???????????????*/
                        Log.d("Mlog", "???????????????...");
                        crcPacket(newData.length, newData);
                    }
            }

            this.leftReceivedData = null;
        } else {
            Log.d("Mlog", "????????????????????????????????????????????????:" + newLen);
            this.leftReceivedData = new byte[newLen];
            System.arraycopy(newData, 0, leftReceivedData, 0, newLen);
            if (mSocket != null && !mSocket.isClosed()) {
                Event rd = new Event(Event.State_Read, tag, readLen, null);
                if (mReadQueue != null) {
                    Log.d("Mlog", "?????????????????????????????????----");
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
//            Log.d("Mlog", "????????????1111111");
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
//            Log.d("Mlog", "????????????2");
            return null;
        }

        public void run() {
            while (run) {
                Event obj = getEvent();
                if (obj == null) {
//                    SystemClock.sleep(10);
//                    Log.d("Mlog", "??????????????????????????????");
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
                            Log.d("Mlog", "????????????..." + e.getMessage());
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
//            Log.d("Mlog", "???????????????..." + checkTpye + "||" + readVerTime);
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
//            Log.d("Mlog", "???????????????...");
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
//                    MLog.log("?????????:" + byte2hex(newBuffer));
                }
                MAVLinkMessage receivedMsg = receivedPacket.unpack();
                if (receivedMsg != null) {
//                    Log.d("Mlog", "????????????ID" + receivedMsg.msgid);
                    if (checkTpye == CHECK_VER) {
                        if (receivedPacket.msgid == msg_autopilot_version.MAVLINK_MSG_ID_AUTOPILOT_VERSION) {
                            msg_autopilot_version msg_version = (msg_autopilot_version) receivedMsg;
                            planeVer = StringUtils.longToBinary(msg_version.flight_sw_version);
                            serial = getSerial(msg_version.flight_custom_version, msg_version.middleware_custom_version);
                            Log.d("Mlog", "????????????????????????---->" + planeVer + "||serial=" + serial);
                        } else if (receivedPacket.msgid == msg_battery_status.MAVLINK_MSG_ID_BATTERY_STATUS) {
                            /**?????????(??????1.5?????????????????????????????????),???????????????,?????????????????????????????????*/
                            msg_battery_status msg_battery_status = (msg_battery_status) receivedMsg;
                            unLock = msg_battery_status.heartbeat_base_mode >> 7 & 1;
                            Log.d("Mlog", "????????????????????????---->???????????????????????????" + unLock);
                        } else if (receivedPacket.msgid == msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT) {
                            /**?????????,??????????????????????????????,?????????????????????????????????*/
                            msg_heartbeat msg_heart = (msg_heartbeat) receivedMsg;
                            unLock = msg_heart.base_mode >> 7 & 1;
                            Log.d("Mlog", "????????????????????????---->???????????????????????????" + unLock);
                        } else if (receivedPacket.msgid == msg_location.MAVLINK_MSG_ID_LOCATION) {
                            /**???????????????????????????*/
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
                            LogUtils.d("??????????????????????????????==>" + msg_param_value.toString());
                            if (msg_param_value.getParam_Id().equals(MAV_DATA_STREAM.FP_ACTIVATION)) {
                                isAck = (int) msg_param_value.param_value;
                                mCallback.requestAct(true, isAck);
                            }
                        }
                    } else if (checkTpye == CHECK_ACK) {
                        if (receivedPacket.msgid == msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE) {
                            msg_param_value msg_param_value = (msg_param_value) receivedMsg;
                            LogUtils.d("??????????????????????????????==>" + msg_param_value.toString());
                            if (msg_param_value.getParam_Id().equals(MAV_DATA_STREAM.FP_ACTIVATION)) {
                                mainHander.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        LogUtils.d("??????1...");
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
//                    Log.d("Mlog", "???????????????fe");
                    } else {
                        if (secondoffset == -1) {
                            secondoffset = i;
//                        Log.d("Mlog", "???????????????fe");
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
                            if ((256 + receivedPacket.seq) - lastSeq > 1) {  //???????????????
                                lossCount += (256 + receivedPacket.seq - lastSeq - 1);
                            }
                            totalCount += (256 + receivedPacket.seq - lastSeq);
                        } else {
                            if (receivedPacket.seq - lastSeq > 1) {    //???????????????
                                lossCount += (receivedPacket.seq - lastSeq - 1);
                            }

                            totalCount += (receivedPacket.seq - lastSeq);
                        }
                    }
                    lastSeq = receivedPacket.seq;
//                    MLog.log("??????: " + totalCount + "??????" + " ??????: " + lossCount + " ??????" + " ?????????:" + (float) lossCount / totalCount * 100 + "%");
                } else if (parser.getParseStatus() == Parser.MAVLINK_FRAMING_BAD_CRC) {
                    if (secondoffset > 0 && i != newBufferSize - 1) {   //????????????data??????????????????????????????????????????????????????????????????

                        i = secondoffset - 1;
//                    MLog.log("????????????????????????????????????fe?????????==>" +i+1);
                    } else {
//                    MLog.log("???????????????????????????????????????fe??????");
                    }

                    firstfeoffset = -1;
                    secondoffset = -1;
                    parser.reset();
                }
                //??????mavlink????????????????????????data?????????????????????????????????????????????data??????????????????fe,????????????????????????????????????????????????data??????????????????
                if (i == newBufferSize - 1 && parser.getParseStatus() == Parser.MAVLINK_FRAMING_OK) {
                    if (firstfeoffset > 0) {
                        int leftBufferSize = newBufferSize - firstfeoffset;
                        byte[] printBuffer = new byte[leftBufferSize];
                        for (int position = 0; position < leftBufferSize; position++) {
                            leftBuffer[position] = newBuffer[firstfeoffset + position];
                            printBuffer[position] = newBuffer[firstfeoffset + position];
                        }
                        LogUtils.d("??????????????? data==>" + byte2hex(printBuffer));
                        parser.reset();
                    }
                }
                if (checkSerial) {
                    if (serial != null) {
                        mainHander.post(new Runnable() {
                            @Override
                            public void run() {
                                LogUtils.d("??????2...");
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
                            Log.d("Mlog", "???????????????????????????..." + planeVer + "||" + yuntaiVer);
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
//            LogUtils.d("????????????...");
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
                mSocket.setSoTimeout(15000); //?????????????????????????????? 15???
                mSocket.setTcpNoDelay(true);
                mLock.unlock();
            }
            if (mSocket != null)
                mSocket.connect(new InetSocketAddress(mHost, mPort), 3000);

            // ??????????????????
//            Log.d("Mlog", "??????????????????==>");
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
                        Log.d("Mlog", "????????????mavlink???????????????????????????");
                        isRebooting = false;
                        __sync();
                    } else {
                        //?????????????????????????????? ????????????
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
                    /**????????????*/
                    Log.d("Mlog", "????????????????????????");
                    timeOutCount = 0;
                    if (yuntaiLocalPath != null) {
                        loadYuntaiFwData(yuntaiLocalPath);
                    }
                    rebootYuntaiByMav();
                    break;
            }

        } catch (IOException e) {
            Log.d("Mlog", "?????????????????????==>" + e.toString());
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
                this.sndDatas.clear();   //???????????????????????????
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
            // ????????????
            resetYuntai();
        } else {
            mCallback.upDateYuntai(UpdateFirmwareReadError, 0);
        }
    }

    /**
     * ????????????
     */
    private void resetYuntai() {
        this.upStatus &= UPStatusInit;

        // ?????????????????????????????? 0.5???????????????
        MsgReset con2 = new MsgReset();
        con2.setMsgCallback(0.5f, new MsgBase.msgCallBack() {
            @Override
            public void onTimeOut(int count) {
                Log.d("Mlog", "????????????onTimeOut");
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
        // ???????????????->???????????????2.5?????????????????????????????????????????????
    }

    // ????????????
    private void handShake() {
        MsgCon con = new MsgCon();
        con.setMsgCallback(1f, new MsgBase.msgCallBack() {
            @Override
            public void onTimeOut(int count) {
                Log.d("Mlog", "????????????onTimeOut");
                if (count <= tOutCount - 2) {//??????????????????????????????
                    handShake();
                } else if (count <= tOutCount) {
                    /**????????????????????????????????????????????????????????????*/
                    resetYuntai();
                } else {
                    /**??????????????????????????????*/
                    Log.d("Mlog", "???????????????????????????...");
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
                Log.d("Mlog", "????????????onComplete");
                upStatus |= UPStatus_HandShaked;
                MsgCon con = (MsgCon) msgObj;
                // ??????????????????????????????flash
                yuntaiErase();
            }
        });
        sendYuntaiCommand(con);
    }

    int timeOutCount = 0;//????????????

    /**
     * ??????????????????
     */
    private void sendYuntaiCommand(final MsgBase baseObj) {
        if (baseObj == null) {
            return;
        }
        msgCommands.clear();
        if (baseObj.timeout > 0) {  // ??????????????????????????? ????????????????????????
            mainHander.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (msgCommands.contains(baseObj)) {
                        timeOutCount++;
//                        if (mEventQueue != null) {
//                            mEventQueue.stopRead();
//                        }
                        Log.d("Mlog", "???????????????????????????" + timeOutCount);
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
            Log.d("Mlog", "????????????????????????");
        } else {
            Log.d("Mlog", baseObj.toString());
            Event sd = new Event(Event.State_Write, YUNTAI_COMMAND_TAG, 0, baseObj.dataFromObject());
            if (mEventQueue != null)
                mEventQueue.putEvent(sd);
            if (baseObj.responseLength() != 0) {
                Event rd = new Event(Event.State_Read, YUNTAI_COMMAND_TAG, baseObj.responseLength(), null);
                if (mReadQueue != null) {
                    LogUtils.d("?????????----");
                    mReadQueue.putEvent(rd);
                }
            }
        }

    }

    /**
     * ??????????????????
     */
    private void yuntaiErase() {
        MsgErase con = new MsgErase();
        con.fileLen = firmwareData.length;
        con.setMsgCallback(3.0f, new MsgBase.msgCallBack() {
            @Override
            public void onTimeOut(int count) {
                Log.d("Mlog", "????????????onTimeOut");
                if (count <= tOutCount) {//??????????????????2?????????
                    yuntaiErase();
                } else {
                    /**????????????3??????????????????????????????*/
                    Log.d("Mlog", "????????????3???????????????");
                    reset();
//                    if (mCallback != null)
//                        mCallback.upDateYuntai(2, uProgress);
                }
            }

            @Override
            public void onComplete(MsgBase msgObj) {
                Log.d("Mlog", "????????????onComplete");
                timeOutCount = 0;
                MsgErase con = (MsgErase) msgObj;
                if (!con.crcIsRight()) {
                    // "????????????crc ???????????????");
                    Log.d("Mlog", "????????????crc ???????????????");

                    yuntaiErase();
                    return;
                }
                if (con.response != 0) {
                    Log.d("Mlog", "????????????!?????????");
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
                // ?????????????????????????????????
                programYuntaiBytes();
            }
        });
        sendYuntaiCommand(con);
    }

    /**
     * ????????????????????????
     */
    private void programYuntaiBytes() {
        if (sndNum < sndDatas.size()) {
            Log.d("Mlog", "???" + (sndNum + 1) + "???/(??????" + sndDatas.size() + ")????????????");
            byte[] sndData = sndDatas.get(sndNum);

            MsgFirware con = new MsgFirware();
            con.setMsgCallback(5.0f, new MsgBase.msgCallBack() {
                @Override
                public void onTimeOut(int count) {
                    Log.d("Mlog", "??????????????????onTimeOut");
                    if (count < 2) {
                        sndNum--;//??????????????????????????????
                        programYuntaiBytes();
                    } else {
                        /**?????????????????????????????????????????????????????????*/
                        Log.d("Mlog", "?????????????????????????????????");
                        reset();
//                        if (mCallback != null)
//                            mCallback.upDateYuntai(2, uProgress);
                    }
                }

                @Override
                public void onComplete(MsgBase msgObj) {
                    Log.d("Mlog", "??????????????????onComplete");
                    timeOutCount = 0;//??????????????????
                    MsgFirware con = (MsgFirware) msgObj;
                    if (!con.crcIsRight()) {    // crc????????? zsz:todo ?????????????????????
                        Log.d("Mlog", "???????????? crc?????????");
                        sndNum--;          // ????????????????????????
                    } else {
//                        timeOutCount = 0;//??????????????????
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
        } else if (sndNum > 0) {   // ??????????????????
            Log.d("Mlog", "?????????????????????????????????");
            upStatus |= UPStatus_UpFirmwared;
            getCRCFromRemote();
        }
    }

    // ????????????CRC
    private void getCRCFromRemote() {
        MsgFirwareCRC con = new MsgFirwareCRC();
        con.setMsgCallback(5.0f, new MsgBase.msgCallBack() {
            @Override
            public void onTimeOut(int count) {
                Log.d("Mlog", "????????????onTimeOut");
                if (count <= tOutCount) {
                    getCRCFromRemote();
                } else {
                    reset();
                    Log.d("Mlog", "????????????????????????...");
                }
            }

            @Override
            public void onComplete(MsgBase msgObj) {
                Log.d("Mlog", "????????????onComplete");
                timeOutCount = 0;//??????????????????????????????????????????????????????
                MsgFirwareCRC con = (MsgFirwareCRC) msgObj;
                if (!con.crcIsRight()) {
                    Log.d("Mlog", "???????????? crc?????????");
                    getCRCFromRemote();
                    return;
                }
                long localcrc = MsgBase.crc32(firmwareData, 0);
                byte[] llcrcs = new byte[4];
                MsgBase.setToBytesFromLong(llcrcs, 0, localcrc, 4);
                Log.d("Mlog", "?????? ??????crc" + MsgBase.byte2hex(llcrcs));
                long llcrc = con.fwcrc;
                byte[] ddddd = new byte[4];
                MsgBase.setToBytesFromLong(ddddd, 0, llcrc, 4);
                Log.d("Mlog", "?????? ??????crc" + MsgBase.byte2hex(ddddd));
                if (localcrc != llcrc) {
                    Log.d("Mlog", "??????crc????????????crc????????????????????????");
                    startUpdateYuntai();//?????????????????????,???????????????????????????????????????
                    return;
                }

                // ??????????????????
                upStatus |= UPStatus_GetCrced;
                finishUpdate();
            }
        });
        sendYuntaiCommand(con);
    }

    // ??????????????????????????????
    private void finishUpdate() {
        MsgFinish con = new MsgFinish();
        con.setMsgCallback(0.5f, new MsgBase.msgCallBack() {
            @Override
            public void onTimeOut(int count) {
                Log.d("Mlog", "??????????????????????????????(????????????!)");
                timeOutCount = 0;
                upStatus |= UPStatus_Finished;
                // "????????????";
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
                Log.d("Mlog", "??????????????????????????????(????????????!)");
                timeOutCount = 0;
                upStatus |= UPStatus_Finished;
                // "????????????";
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
     * ????????????????????????????????????????????????????????????
     *
     * @param continueRead ??????????????????????????????????????????????????????????????????
     */
    private void checkFirmwareVer(boolean continueRead) {
        Log.d("Mlog", "???????????????...");
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
     * ??????????????????
     *
     * @param continueRead ??????????????????????????????????????????????????????????????????
     */
    private void checkPlaneAck(boolean continueRead) {
        Log.d("Mlog", "????????????????????????...");
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
            Log.d("Mlog", "?????????????????????  ?????????????????????");
            loadFirmware(fwContent);
            Log.d("Mlog", "?????????????????????  ?????????????????????");
        }
        if (firmwareData == null)
            return;

        int num = firmwareData.length / PROG_MULTI_MAX;
        if (sndDatas == null) {
            sndDatas = new ArrayList<>();
        } else {
            sndDatas.clear();   //????????????????????? ????????????
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
        Log.d("Mlog", "????????????==>" + firmwareData.length + "????????????==>" + sndDatas.size());
        begin = System.currentTimeMillis();
        programBytes();


    }

    private void programBytes() {

        if (sndNum < sndDatas.size()) {
//            end = System.currentTimeMillis();
//            Log.d("Mlog","?????????????????????==>"+(end - begin)+"??????");
//            if ((end - begin) > 100) {
//                Log.d("??????","??????????????????==>"+(end-begin));
//            }
//            begin = end;

//            Log.d("Mlog","programBytes ???"+(sndNum+1)+"???"+"/"+"??????("+sndDatas.size()+"???)??????????????????");
            byte[] sndData = sndDatas.get(sndNum);
            int len = sndData.length;
            byte[] dd;

            int loc = 0;
            if (PROG_MULTI_MAX == 252 * 22) {
                dd = new byte[len + 4];
                dd[0] = PROG_MULTI;
                dd[1] = (byte) ((len) & 0xFF);
                dd[2] = (byte) ((len >> 8) & 0xFF);
                Log.d("Mlog", "?????????" + dd[1] + "||" + dd[2]);
                loc = 3;
            } else {
                dd = new byte[len + 3];
                Log.d("Mlog", "?????????");
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
            Log.d("Mlog", "?????????????????????==>" + (end - begin) / 1000 + "???");
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
            Log.d("Mlog", "base64????????????==>" + base64s.length);
            byte[] d = decompress(base64s);
            Log.d("??????", "??????????????????==>" + d.length);

            int flen = d.length;
            firmwareData = new byte[flen + flen % 4];
            System.arraycopy(d, 0, firmwareData, 0, flen);
            Log.d("??????", "????????????==>" + flen);
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
        Log.d("??????", "Original: " + data.length);
        Log.d("??????", "Compressed: " + output.length);
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
//        Log.d("??????", "????????????==>" + Arrays.toString(data));
        byte c = data[0];
        Log.d("??????", "????????????==>" + c + "||" + data[1]);
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
            Log.d("??????", "????????????==>%d" + (char) c);
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

    private byte[] leftReceivedData;    // ?????????????????????
    // ????????????????????????
    private ArrayList<MsgBase> msgResponses;    // ??????????????????
    private ArrayList<MsgBase> msgObjs;         // ?????????????????????????????????
    // ??????????????????

    private void processData(byte[] data) {
        Log.d("??????", "????????????????????????");
        int newLen = data.length;
        if (leftReceivedData != null) {
            newLen = leftReceivedData.length + data.length;
        }
        ReceivePacket parse = new ReceivePacket();
        int i_NEXT_STX_loc = 0;
        byte[] newData = new byte[newLen];

        if (this.leftReceivedData != null) { //????????????????????????????????????
            System.arraycopy(leftReceivedData, 0, newData, 0, leftReceivedData.length);
            System.arraycopy(data, 0, newData, leftReceivedData.length, data.length);
        } else {
            System.arraycopy(data, 0, newData, 0, data.length);
        }
        int leng = newData.length;
        for (int i = 0; i < leng; i++) {
            byte c = newData[i];

            ReceivePacket.Packet packet = parse.ReceiveStateMachine(c);
            if (packet != null) { // ?????????????????????????????????
                int packetLen = packet.packetLen;
                i_NEXT_STX_loc = i + 1;

                // ??????????????????
                byte[] parseData = new byte[packetLen];
                MsgBase con = new MsgBase();
                if (packet.byCmd == MsgBase.UP_CON_REQ) {   // ??????

                    if ((this.upStatus & UPStatus_HandShaked) == 1) {    // ???????????????????????????
                        continue;
                    }
                    System.arraycopy(newData, i + 1 - packetLen, parseData, 0, packetLen);
                    con = MsgCon.ObjectFromReponseData(parseData);
                } else if (packet.byCmd == MsgBase.UP_ERASE_REQ) {

                    if ((this.upStatus & UPStatus_Eraseed) == 1) {    // ??????????????????
                        continue;
                    }

                    System.arraycopy(newData, i + 1 - packetLen, parseData, 0, packetLen);
                    con = MsgErase.ObjectFromReponseData(parseData);
                } else if (packet.byCmd == MsgBase.UP_SNDFW_REQ) {

                    if ((this.upStatus & UPStatus_UpFirmwared) == 1) {    // ???????????????????????????
                        continue;
                    }

                    System.arraycopy(newData, i + 1 - packetLen, parseData, 0, packetLen);
                    con = MsgFirware.ObjectFromReponseData(parseData);
                    this.upStatus |= UPStatus_UpFirmwared;
                } else if (packet.byCmd == MsgBase.UP_GETCRC_REQ) { // ???????????????????????????????????? ?????????????????????

                    if ((upStatus & UPStatus_GetCrced) == 1) {    // ??????????????????????????? crc
                        continue;
                    }

                    System.arraycopy(newData, i + 1 - packetLen, parseData, 0, packetLen);
                    con = MsgFirwareCRC.ObjectFromReponseData(parseData);
                } else if (packet.byCmd == MsgBase.UP_FINISH_REQ) {

                    if ((upStatus & UPStatus_Finished) == 1) {    // ????????????
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
                        // ??????id???????????????????????????
                        timeOutCount = 0;
                        Log.d("??????", "??????id??????????????????");
                    }
                } else {
                    // ?????????????????????...
                    Log.d("??????", "?????????????????????????????????...");
                }
                parse.resetState();
            }
        }

        parse.resetState();

        if (i_NEXT_STX_loc < leng) {   // ??????????????????????????????????????????????????????????????????????????????
            Log.d("??????", "??????????????????????????????==>" + (leng - i_NEXT_STX_loc));
            this.leftReceivedData = new byte[leng - i_NEXT_STX_loc];
            System.arraycopy(newData, i_NEXT_STX_loc, leftReceivedData, 0, leng - i_NEXT_STX_loc);
        } else {
            // ?????????????????????
            this.leftReceivedData = null;
        }

        // ?????????????????? ??????????????????????????????
        if (this.msgResponses.size() > 0) {
            for (int i = 0; i < this.msgResponses.size(); i++) {
                MsgBase requestCon = this.msgResponses.get(0);
                MsgBase con = msgObjs.get(0);
                if (requestCon.getMsgCallBack() != null) {
                    requestCon.setTimeOut(false);
                    requestCon.getMsgCallBack().onComplete(con);
//                    if (leftReceivedData.length > 0)
//                        this.leftReceivedData = null;
//                    Log.d("??????", "?????????????????????????????????????????????????????????????????????");
                }
            }
        } else {
            if (mSocket != null && !mSocket.isClosed()) {
                Event rd = new Event(Event.State_Read, YUNTAI_COMMAND_TAG, 128, null);
                if (mReadQueue != null) {
                    Log.d("??????", "????????????????????????----");
                    mReadQueue.putEvent(rd);
                }
            }
        }

        this.msgObjs.clear();
        this.msgResponses.clear();
    }
}

