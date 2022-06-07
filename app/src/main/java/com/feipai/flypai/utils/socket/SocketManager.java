package com.feipai.flypai.utils.socket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.MAVLinkPacket;
import com.MavLinkMsgHandler;
import com.Messages.MAVLinkMessage;
import com.common.msg_location;
import com.feipai.flypai.api.DataSocketReadCallback;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.base.BaseCameraMsg;
import com.feipai.flypai.beans.ProductModel;
import com.feipai.flypai.connect.ConnectManager;
import com.feipai.flypai.utils.CameraCommand;
import com.feipai.flypai.utils.MLog;
import com.feipai.flypai.utils.global.JsonUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.enums.MAV_CMD.TYPE_READ_WAYPOINT;
import static com.feipai.flypai.app.ConstantFields.CAMERA_CONFIG.START_SESSION;

public class SocketManager {

    private EasySocket mSocket7878;
    private EasySocket mSocket8787;
    private EasySocket mSocket8686;
    // TODO: 2019/7/4 这几个变量是不是标识是否已经连接？
    private boolean isConnecting7878;
    private boolean isConnecting8787;
    private boolean isConnecting8686;
    private boolean isDisConnected;

    private SocketBaseMsg curMsgFor7878;
    private SocketBaseMsg curMsgFor8686;

    /***针对于长指令一对一回应,这个写法可能有问题*/
    private int requestCode8686;


    private SocketManager() {

    }

    // 单例
    private static SocketManager mCmd;

    public static SocketManager getCmdInstance() {
        if (mCmd == null) {
            synchronized (SocketManager.class) {
                if (mCmd == null) {
                    mCmd = new SocketManager();
                }
            }
        }
        return mCmd;
    }

    /*******************************相机主端口*********************************/
    private String mIpFor7878;
    private TcpReadListener mlistenerFor7878;

    /**
     * 相机主端口
     */
    public void connectTo7878Socket(String ip, TcpReadListener ls) {
        LogUtils.d("connectTo7878Socket"+ ip);
        mlistenerFor7878 = ls;
        mIpFor7878 = ip;
        if (isConnecting7878) {
            MLog.log("已经在连接7878端口了");
            return;
        }
        isConnecting7878 = true;
        if (mSocket7878 == null) {
            EasySocket.Builder builder = new EasySocket.Builder();
            mSocket7878 = builder.setIp(ip)
                    .setPort(ConstantFields.FLY_PORT.CAMERA_PORT)
                    .setSendBuffSize(1024 * 10)
                    .setReceiveBufferSize(1024 * 10)
                    .setCallback(callbackFor7878)
                    .build();
        }
        mSocket7878.connect();
    }

    public boolean isCanSendDataTo7878() {
        return mSocket7878 == null ? false : mSocket7878.isConnected;
    }


    private Callback callbackFor7878 = new Callback() {
        @Override
        public void onConnected() {
            MLog.log("连接到了 ip:" + mIpFor7878 + "端口:7878 ");
            if (mlistenerFor7878 != null) {
                mlistenerFor7878.connecSuccess();
            }
        }

        @Override
        public void onDisconnected() {
            isConnecting7878 = false;
            MLog.log("onDisconnected 7878端口");
            if (mlistenerFor7878!=null)
                mlistenerFor7878.close();
        }

        @Override
        public void onReconnected() {

        }

        @Override
        public void onSend() {

        }

        @Override
        public void onReceived(byte[] msg) {
            if (curMsgFor7878 != null && curMsgFor7878.callback != null) {
                curMsgFor7878.callback.onComplete(SocketBaseMsg.Result_Ok, msg);
            }
        }

        @Override
        public void onError(String msg) {

        }

        @Override
        public void onSendHeart() {

        }
    };

    // 真正发送数据的入口
    public void sendCameraCommandData(SocketBaseMsg msg) {

        if (msg == null) {
            MLog.log("消息不能为null");
            return;
        }
        if (!isCanSendDataTo7878()) {    // 说明socket 端口未连接
            if (msg != null && msg.callback != null) {
                msg.callback.onComplete(SocketBaseMsg.Result_Unconnected, null);
            }
            return;
        }

        curMsgFor7878 = msg;
        mSocket7878.send(msg.params, msg.params.length);
    }


    public void close7878Socket() {
        LogUtils.d("close7878Socket");
        isConnecting7878 = false;
        if (mSocket7878 != null)
            mSocket7878.disconnect();
    }


    /*********************************相机数据端口*******************************/
    private String mIpFor8787;
    private DataSocketReadListener mlistenerFor8787;
    private DataSocketReadCallback mReadCallbackFor8787;

    /**
     * data socket
     */
    public void connectTo8787Socket(String ip, DataSocketReadListener ls) {
        LogUtils.d("connectTo8787Socket");
        mlistenerFor8787 = ls;
        mIpFor8787 = ip;
        if (isConnecting8787) {
            MLog.log("已经在连接8787端口了");
            return;
        }
        isConnecting8787 = true;
        if (mSocket8787 == null) {
            EasySocket.Builder builder = new EasySocket.Builder();
            mSocket8787 = builder.setIp(mIpFor8787)
                    .setPort(ConstantFields.FLY_PORT.DATA_PORT)
                    .setSendBuffSize(1024 * 1024 * 2)
                    .setReceiveBufferSize(1024 * 1024 * 2)
                    .setCallback(callbackFor8787).build();
        }
        mSocket8787.connect();
    }

    public void setDataSocketReadListener(DataSocketReadCallback readCallbackFor8787) {
        this.mReadCallbackFor8787 = readCallbackFor8787;
    }

    public boolean isCanSendDataTo8787() {
        return mSocket8787.isConnected;
    }

    private Callback callbackFor8787 = new Callback() {
        @Override
        public void onConnected() {
            MLog.log("连接到了 ip:" + mIpFor8787 + "端口:8787 ");
            if (mlistenerFor8787 != null) {
                mlistenerFor8787.connecSuccess();
            }
        }

        @Override
        public void onDisconnected() {
            isConnecting8787 = false;
        }

        @Override
        public void onReconnected() {

        }

        @Override
        public void onSend() {

        }

        @Override
        public void onReceived(byte[] msg) {
            //读取缩略图等文件数据
            if (mReadCallbackFor8787 != null) {
                mReadCallbackFor8787.onReadData(msg);
            }
        }

        @Override
        public void onError(String msg) {
            if (mlistenerFor7878 != null) {
                mlistenerFor7878.connectFail();
            }
        }

        @Override
        public void onSendHeart() {

        }
    };

    public void sendDataCommandTo8787(byte[] bytes, int length) {
        if (bytes == null || !isCanSendDataTo8787()) {    // 说明socket 端口未连接
            return;
        }
        mSocket8787.send(bytes, length);
    }


    public void close8787Socket() {
        LogUtils.d("close8787Socket");
        isConnecting8787 = false;
        if (mSocket8787 != null)
            mSocket8787.disconnect();
    }


    /***********************************Mavlink端口*****************************/

    private String mIpFor8686;
    private MavlinkSocketReadListener mlistenerFor8686;

    /**
     * Mavlink
     */
    public void connectTo8686Socket(String ip, MavlinkSocketReadListener ls) {
        LogUtils.d("connectTo8686Socket");
        mlistenerFor8686 = ls;
        mIpFor8686 = ip;
        if (isConnecting8686) {
            MLog.log("已经在连接8686端口了");
            return;
        }
        isConnecting8686 = true;
        if (mSocket8686 == null) {
            EasySocket.Builder builder = new EasySocket.Builder();
            mSocket8686 = builder.setIp(ip)
                    .setPort(ConstantFields.FLY_PORT.PLANE_PORT)
                    .setSendBuffSize(1024 * 10)
                    .setReceiveBufferSize(1024 * 10)
                    .setCallback(callbackFor8686).build();
        }
        mSocket8686.connect();
    }

    public boolean isCanSendDataTo8686() {
        return mSocket8686 == null ? false : mSocket8686.isConnected;
    }

    public void sendMavlinkCommandData(MAVLinkMessage msg) {
        if (msg == null) {
            MLog.log("消息不能为null");
            return;
        }
        if (!isCanSendDataTo8686()) {    // 说明mavlink socket 端口未连接
            return;
        }
        MAVLinkPacket packet = msg.pack();
        byte[] buffer = packet.encodePacket();
        if (msg.msgid == 229) {
            msg_location location = (msg_location) msg;
            JsonElement object = JsonUtils.getValueByKey(location.getText(), "type");
            requestCode8686 = object.getAsInt();
        } else {
            requestCode8686 = msg.msgid;
        }
//        MLog.log("消息请求码===" + requestCode8686);
        mSocket8686.send(buffer, buffer.length);
    }

    private Callback callbackFor8686 = new Callback() {
        @Override
        public void onConnected() {
            MLog.log("连接到了 ip:" + mIpFor8686 + "端口:8686");
            if (mlistenerFor8686 != null) {
                mlistenerFor8686.connecSuccess();
            }
        }

        @Override
        public void onDisconnected() {
            isConnecting8686 = false;
        }

        @Override
        public void onReconnected() {

        }

        @Override
        public void onSend() {

        }

        @Override
        public void onReceived(byte[] msg) {
            if (mlistenerFor8686 != null && msg != null && msg.length > 0) {
                mlistenerFor8686.read(requestCode8686, msg.length, msg);
            }
        }

        @Override
        public void onError(String msg) {
            if (mlistenerFor8686 != null) {
                mlistenerFor8686.connectFail();
            }
        }

        @Override
        public void onSendHeart() {

        }
    };

    public void close8686Socket() {
        LogUtils.d("close8686Socket");
        isConnecting8686 = false;
        if (mSocket8686 != null)
            mSocket8686.disconnect();
    }


    public void closeAllSocket() {
        LogUtils.d("closeAllSocket");
        close7878Socket();
        close8787Socket();
//        close8686Socket();
    }

}
