package com.feipai.flypai.connect;

import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.DebugUtils;

import com.MAVLinkPacket;
import com.MavLinkMsgHandler;
import com.Messages.MAVLinkMessage;
import com.Parser;
import com.feipai.flypai.BuildConfig;
import com.feipai.flypai.R;
import com.feipai.flypai.api.CameraCommandCallback;
import com.feipai.flypai.api.RxLoopObserver;
import com.feipai.flypai.api.RxLoopSchedulers;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.app.FlyPieApplication;
import com.feipai.flypai.base.BaseMavlinkEntity;
import com.feipai.flypai.beans.ABCmdValue;
import com.feipai.flypai.beans.FileBean;
import com.feipai.flypai.beans.ProductModel;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.beans.mavlinkbeans.AckCommandBean;
import com.feipai.flypai.beans.mavlinkbeans.AutopilotVerisionBean;
import com.feipai.flypai.beans.mavlinkbeans.BatteryBean;
import com.feipai.flypai.beans.mavlinkbeans.CalibrationProgressBean;
import com.feipai.flypai.beans.mavlinkbeans.CalibrationSuccessBean;
import com.feipai.flypai.beans.mavlinkbeans.LocationBean;
import com.feipai.flypai.beans.mavlinkbeans.PlaneParamsBean;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.utils.CameraCommand;
import com.feipai.flypai.utils.GeneralFactory;
import com.feipai.flypai.utils.MLog;
import com.feipai.flypai.utils.cache.CacheManager;
import com.feipai.flypai.utils.global.CommandUtil;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.NetworkUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.RxBusUtils;
import com.feipai.flypai.utils.global.StringUtils;
import com.feipai.flypai.utils.global.ToastUtils;
import com.feipai.flypai.utils.gsonlib.MGson;
import com.feipai.flypai.utils.socket.DataSocketReadListener;
import com.feipai.flypai.utils.socket.MavlinkObserver;
import com.feipai.flypai.utils.socket.MavlinkSocketReadListener;
import com.feipai.flypai.utils.socket.SocketManager;
import com.feipai.flypai.utils.socket.TcpReadListener;
import com.videoplayer.NativeCode;

import java.util.ArrayList;
import java.util.List;

import static com.ardupilotmega.msg_mag_cal_report.MAVLINK_MSG_ID_MAG_CAL_REPORT;
import static com.common.msg_autopilot_version.MAVLINK_MSG_ID_AUTOPILOT_VERSION;
import static com.common.msg_battery_status.MAVLINK_MSG_ID_BATTERY_STATUS;
import static com.common.msg_cal_progress_decode.MAG_CAL_PROGRESS;
import static com.common.msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK;
import static com.common.msg_location.MAVLINK_MSG_ID_LOCATION;
import static com.common.msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE;
import static com.feipai.flypai.app.ConstantFields.PREF.PLANE_TYPE;
import static com.feipai.flypai.utils.global.NetworkUtils.NetworkType.NETWORK_WIFI;

/**
 * 备注：暴露的public 接口要在主线程中调用。
 */
public class ConnectManager {

    private static final int DEF_CONNECTION_STATUS = -1;
    private static final int DISCONNECTED_STATUS = 0;
    private static final int CONNECTED_STATUS = 1;
    private boolean mRun;
    // 连接状态;-1初始化状态 0未连接 1连接着
    private int mConnectStatus;
    private boolean mStartSessionSucess;
    public ProductModel mProductModel;
    private Handler mainHander;
    private Object mLockObject;

    private MavLinkMsgHandler mMavMsgHandler;
    private Parser mParser;
    private BatteryBean batteryBean;
    private CalibrationProgressBean calibBean;
    private PlaneParamsBean paramsBean;
    private LocationBean lBean;

    // 单例
    private static ConnectManager mInstance;

    public static ConnectManager getInstance() {
        if (mInstance == null) {
            synchronized (CameraCommand.class) {
                if (mInstance == null) {
                    mInstance = new ConnectManager();
                }
            }
        }
        return mInstance;
    }

    public boolean isOldProduct() {
        return mProductModel.productType == ConstantFields.ProductType_4k;
    }

    public String getNewTagCameraBinName() {
        String newTagCameraBingName = ConstantFields.UPGRADE_FW.NEW_4K_FW_PATH;
        if (mProductModel.productType == ConstantFields.ProductType_4kAir) {
            newTagCameraBingName = ConstantFields.UPGRADE_FW.NEW_4KA_FW_PATH;
        } else if (mProductModel.productType == ConstantFields.ProductType_6kAir) {
            newTagCameraBingName = ConstantFields.UPGRADE_FW.NEW_6KA_FW_PATH;
        }
        return newTagCameraBingName;
    }

    private ConnectManager() {
        mProductModel = new ProductModel(CacheManager.getSharedPrefUtils().getInt(PLANE_TYPE, ConstantFields.ProductType_4k),
                ConstantFields.PLANE_IP.OLD_IP,
                ConstantFields.PLANE_IP.OLD_IP,
                ConstantFields.PLANE_IP.OLD_IP,
                ConstantFields.ASSETSS_DIR.FW_CAMERA_6KA,
                ConstantFields.ASSETSS_DIR.FW_PLANE_6KA,
                ConstantFields.ASSETSS_DIR.FW_YUNTAI_6KA
        );
        mConnectStatus = DEF_CONNECTION_STATUS;
        mStartSessionSucess = false;
        mainHander = new Handler(Looper.getMainLooper());
        mLockObject = new Object();
        mListioners = new ArrayList<ConnectManagerCallback>();
        this.mMavMsgHandler = new MavLinkMsgHandler();
        this.mParser = new Parser();
    }

    // 监控连接的接口
    public interface ConnectManagerCallback {
        // 连接上了
        void onConnected(ProductModel productModel);

        // 失去连接了
        void onDisConnected();

        // 连接被占用了
        void onConnectionUsed();


    }

    private ArrayList<ConnectManagerCallback> mListioners;

    public void addConnectionListioner(ConnectManagerCallback callback) {
        if (callback == null || mListioners.contains(callback)) {
            return;
        }
        if (!mListioners.contains(callback)) {
            mListioners.add(callback);
        }

        // 首次加入 告诉下连接状态
//        MLog.log("添加连接===>" + isConneted());
        if (isConneted()) {
            callback.onConnected(mProductModel);
        } else {
            callback.onDisConnected();
        }
    }

    public void removeConectionListioner(ConnectManagerCallback callback) {
        if (callback == null) {
            return;
        }
        if (mListioners.contains(callback)) {
            mListioners.remove(callback);
        }
    }

    // 当前连接的状态
    public boolean isConneted() {
        return mConnectStatus == CONNECTED_STATUS && mStartSessionSucess;
    }

//    //获取速度信息
//    public PlaneParamsBean getPlaneParams() {
//        return paramsBean;
//    }

    // 开始监控连接状态
    public void startConnectLision() {
//        if (NetworkUtils.isWifiOpened()) {
//            LogUtils.d("wifi连接着"+NetworkUtils.getNetworkType());
//            //wifi连接着
//            if (NetworkUtils.getNetworkType() != NETWORK_WIFI) {
//                //却不是走的wifi信号,则直接使用WIFI信道
//                NetworkUtils.isOnlyWifi();
//            }
//        }
        MLog.log("startConnectLisioner ");
        if (mRun) {
            MLog.log("已经开始 监控了，返回");
            return;
        }
        mRun = true;
        if (mPingThread == null || !mPingThread.isAlive()) {
            mPingThread = new PingThread();
            mPingThread.start();
        }
        synchronized (mLockObject) {
            mLockObject.notify();
        }

    }

    // 结束监控连接状态
    public void stopConnectLision() {
        MLog.log("stopConnectLisioner ");
        if (!mRun) {
            MLog.log("已经停止监控了，返回");
            return;
        }
        mRun = false;
        mConnectStatus = DEF_CONNECTION_STATUS;
        mStartSessionSucess = false;
        for (int i = 0; i < mListioners.size(); i++) {
            ConnectManagerCallback callback = mListioners.get(i);
            if (callback != null)
                callback.onDisConnected();
        }
        mListioners.clear();
//        SocketManager.getCmdInstance().
        cloaseAll();
//        SocketManager.getCmdInstance().closeAllSocket();
        CameraCommand.getCmdInstance().clearCommands();
    }

    // 暂停连接状态
    public void pauseConnectLision() {
        MLog.log("pauseConnectLision ");
        if (!mRun) {
            MLog.log("已经暂停监控了，返回");
            return;
        }
        mRun = false;
        mConnectStatus = DEF_CONNECTION_STATUS;
        mStartSessionSucess = false;
        for (int i = 0; i < mListioners.size(); i++) {
            ConnectManagerCallback callback = mListioners.get(i);
            if (callback != null)
                callback.onDisConnected();
        }
        cloaseAll();
        CameraCommand.getCmdInstance().clearCommands();
    }

    private PingThread mPingThread;
    private int pingFailCount = 1;

    /**
     * 心跳检测
     */
    private class PingThread extends Thread {
        @Override
        public void run() {
            while (true) {
                if (!mRun) {
                    synchronized (mLockObject) {
                        try {
                            MLog.log("停止运行了 休眠");
                            mConnectStatus = DEF_CONNECTION_STATUS;
                            mStartSessionSucess = false;
                            cloaseAll();
                            CameraCommand.getCmdInstance().clearCommands();
                            mLockObject.wait();
                        } catch (InterruptedException ie) {

                        }
                    }
                }
                if (NetworkUtils.isWifiOpened()) {
//                    LogUtils.d("wifi连接着" + NetworkUtils.getNetworkType());
                    //wifi连接着
                    if (NetworkUtils.getNetworkType() != NETWORK_WIFI)
                        NetworkUtils.forcebindToNetwork(ConnectivityManager.TYPE_WIFI);
                }
                SystemClock.sleep(1000);
                String curWifi = NetworkUtils.getCurConnetWifiName();
                if (!StringUtils.isEmpty(curWifi) && curWifi.startsWith(ConstantFields.DATA_CONFIG.FLYPAI_NAME_START)) {
                    String myIp = NetworkUtils.getIPAddress(true);
//                    LogUtils.d("MyIP=" + myIp);
                    String pingIp = GeneralFactory.getRemoteIp(myIp);
                    String ping = CommandUtil.getPacketLoss(pingIp, 1, 5);
                    String dataSocketIp = myIp;
                    if (ping != null && ping.contains("received, 0%")) {       // ping通了
                        pingFailCount = 0;
                        if (mConnectStatus == CONNECTED_STATUS || !mRun) {
                            continue;
                        }
                        MLog.log("连接上了");
                        int result = 0;
                        if (pingIp.contains(".2.")) {  // 新遥控 避免频繁创建socket 影响性能
                            dataSocketIp = "192.168.3.2";
                            // 对于新遥控，ping 通之后新将ip发送给遥控
                            if (NativeCode.initSocket(pingIp, 1234, 3)) {
                                LogUtils.d("initSocket成功" + myIp);
                                result = NativeCode.setIp(myIp);
                                if (result == 0) {
                                    LogUtils.d("IP设置成功");

//                                stopConnectLision();
                                } else {
                                    LogUtils.d("IP设置失败" + result);
                                }
                            } else {
                                LogUtils.d("initSocket失败");
                            }
                        }
                        if (result != 32) {
                            mConnectStatus = CONNECTED_STATUS;
                        } else {
                            continue;
                        }

                        int type = ConstantFields.ProductType_4k;

                        String videoIpAddd = "rtsp://" + pingIp + "/live";
                        String cameraFimware = "cameraFimware_6kAir.bin";
                        String yuntaiFimware = "yuntaiFimware_6kAir.bin";
                        String feikongFimware = "feikongFimware_6kAir.px4";
                        ArrayList<String> test4kAirWifi = new ArrayList<String>();
                        ArrayList<String> test6kAirWifi = new ArrayList<String>();

                        for (int i = 0; i < ConstantFields.TESTWIFI.wifi_for4kAir.length; i++) {
                            test4kAirWifi.add(ConstantFields.TESTWIFI.wifi_for4kAir[i]);
                        }
                        for (int i = 0; i < ConstantFields.TESTWIFI.wifi_for6kAir.length; i++) {
                            test6kAirWifi.add(ConstantFields.TESTWIFI.wifi_for6kAir[i]);
                        }
                        LogUtils.d("当前的wifi====>" + curWifi);
                        if (curWifi != null) {//可能获取不到wifi名？
                            if (curWifi.contains(ConstantFields.DATA_CONFIG.FLYPAI_NAME_START_6kAir) || curWifi.contains(ConstantFields.DATA_CONFIG.FLYPAI_NAME_START_6kPro) || test6kAirWifi.contains(curWifi)) {
                                type = ConstantFields.ProductType_6kAir;
                                cameraFimware = ConstantFields.ASSETSS_DIR.FW_CAMERA_6KA;
                                //"cameraFimware_6kAir.bin";
                                yuntaiFimware = ConstantFields.ASSETSS_DIR.FW_YUNTAI_6KA;
                                //"yuntaiFimware_6kAir.bin";
                                feikongFimware = ConstantFields.ASSETSS_DIR.FW_PLANE_6KA;
                                //"feikongFimware_6kAir.px4";
                            } else if (curWifi.contains(ConstantFields.DATA_CONFIG.FLYPAI_NAME_START_4kAir) || test4kAirWifi.contains(curWifi)) {
                                type = ConstantFields.ProductType_4kAir;
                                cameraFimware = ConstantFields.ASSETSS_DIR.FW_CAMERA_4KA;
                                //"cameraFimware_4kAir.bin";
                                yuntaiFimware = ConstantFields.ASSETSS_DIR.FW_YUNTAI_4KA;
                                //"yuntaiFimware_4kAir.bin";
                                feikongFimware = ConstantFields.ASSETSS_DIR.FW_PLANE_4KA;
                                //"feikongFimware_4kAir.px4";
                            } else if (curWifi.contains(ConstantFields.DATA_CONFIG.FLYPAI_NAME_START)) {
                                type = ConstantFields.ProductType_4k;
                                cameraFimware = ConstantFields.ASSETSS_DIR.FW_CAMERA_4K;
                                //"cameraFimware_4k.bin";
                                yuntaiFimware = ConstantFields.ASSETSS_DIR.FW_YUNTAI_4K;
                                //"yuntaiFimware_4k.bin";
                                feikongFimware = ConstantFields.ASSETSS_DIR.FW_PLANE_4K;
//                                "feikongFimware_4k.px4";
                            }
                        }
//                        if (type == ConstantFields.ProductType_4k && !dataSocketIp.endsWith("3.2")) {
//                            dataSocketIp = myIp;
//                        }
                        mProductModel = new ProductModel(type, videoIpAddd, pingIp, dataSocketIp, cameraFimware, yuntaiFimware, feikongFimware);

                        // 成功连接上了，则直接连接各个端口
                        mainHander.post(mConnectedRunnable);
                        continue;
                    }
                }

//                    MLog.log("未ping 通");
                if (mConnectStatus == DISCONNECTED_STATUS) {
                    continue;
                }
                pingFailCount++;

                if (pingFailCount >= 3) {       // 连续ping 三次失败 则代表失去连接
                    mConnectStatus = DISCONNECTED_STATUS;
                    mStartSessionSucess = false;
                    MLog.log("失去连接了");
                    // 连接失败
                    cloaseAll();
                    mainHander.post(mDisconnectedRunnable);

                }
            }
        }
    }


    /**
     * 连接线程
     */
    private Runnable mConnectedRunnable = new Runnable() {
        @Override
        public void run() {
            String remoteIp = mProductModel.remoteIpAddress;
            SocketManager.getCmdInstance().connectTo7878Socket(remoteIp, new TcpReadListener() {
                @Override
                public void close() {
                    mStartSessionSucess = false;
                }

                @Override
                public void connecSuccess() {
                    if (mConnectStatus != CONNECTED_STATUS) {
                        cloaseAll();
                        return;
                    }
                    FlyPieApplication.getInstance().setTcpSuccess(true);
                    // 发送启动session 命令
                    LogUtils.d("开始发送startSession");
                    CameraCommand.getCmdInstance().startSession(new CameraCommandCallback<ABCmdValue<Integer>>() {
                        @Override
                        public void onComplete(ABCmdValue<Integer> result) {
                            //建议相机端口连接上直接回调连接成功,独立相机和飞控端口
                            //可以考虑连接datasocket以表示完全连接成功
                            mStartSessionSucess = true;
                            connectTo8787ForData();
                        }

                        @Override
                        public void onErrorCode(int msgId, int code) {
                            super.onErrorCode(msgId, code);
                            FlyPieApplication.getInstance().setTcpSuccess(false);
                            if (msgId == ConstantFields.CAMERA_CONFIG.START_SESSION) {
                                if (code == -3) {
                                    activateConnectionUsedCallback();
                                } else {
                                    activateDisConnectCallback();
                                }
                            }
                        }
                    });
                }

                @Override
                public void connectFail() {
//                    activateDisConnectCallback();
                }
            });
        }
    };

//    private int time = 0;

    /**
     * 连接8686，mavlinke端口，并监听注册观察者，监听返回数据
     * 需要则创建，不需要则断开,前提在 相机端口连接上
     */
    public void connectTo8686(BaseView mv, MavlinkObserver<BaseMavlinkEntity> mavlinkObserver) {
        SocketManager.getCmdInstance().connectTo8686Socket(mProductModel.remoteIpAddress, new MavlinkSocketReadListener() {

            @Override
            public void read(int requestCode, int bufferSize, byte[] buffer) {

                mMavMsgHandler.handleSocketData(mParser, bufferSize, buffer, new MavLinkMsgHandler.MavLinkMsgListener() {
                    @Override
                    public void mavLinkMsgReceive(MAVLinkMessage receivedMsg, MAVLinkPacket receivedPacket) {
                        RxLoopSchedulers.composeMain(mv, receivedPacket)
                                .subscribe(new RxLoopObserver<MAVLinkPacket>() {
                                    @Override
                                    public void onNext(MAVLinkPacket receivedPacket) {
                                        super.onNext(receivedPacket);
                                        this.disposeDisposables();
                                        if (mavlinkObserver != null) {
                                            MAVLinkMessage msg = receivedPacket.unpack();
                                            switch (msg.msgid) {
                                                case MAVLINK_MSG_ID_BATTERY_STATUS:
                                                    /**电池数据(飞行模式，电池数据，卫星数，距离，速度，围栏，解锁)*/
                                                    if (batteryBean == null)
                                                        batteryBean = new BatteryBean();
                                                    batteryBean.setMavlinkMessage(msg);
//                                                    if (batteryBean.getYawMoveStatus() == 20) {
//                                                        time++;
//                                                        ToastUtils.showLongToast("果然收到了20" + time);
//                                                    } else {
//                                                        time = 0;
//                                                    }
                                                    mavlinkObserver.onRead(batteryBean);
                                                    break;
                                                case MAG_CAL_PROGRESS:
                                                    /**指南针校准进度*/
                                                    if (calibBean == null)
                                                        calibBean = new CalibrationProgressBean();
                                                    calibBean.setMavlinkMessage(msg);
                                                    mavlinkObserver.onRead(calibBean);
                                                    break;
                                                case MAVLINK_MSG_ID_MAG_CAL_REPORT:
                                                    /**指南针校准完成回调*/
                                                    CalibrationSuccessBean bean = new CalibrationSuccessBean();
                                                    bean.setMavlinkMessage(msg);
                                                    mavlinkObserver.onRead(bean);
                                                    break;
                                                case MAVLINK_MSG_ID_COMMAND_ACK:
                                                    /**长指令回复（包括：陀螺仪校准结果）*/
                                                    AckCommandBean ackbean = new AckCommandBean();
                                                    ackbean.setRequestCode(requestCode);
                                                    ackbean.setMavlinkMessage(msg);
                                                    mavlinkObserver.onRead(ackbean);
                                                    break;
                                                case MAVLINK_MSG_ID_PARAM_VALUE:
                                                    /**速度参数*/
                                                    if (paramsBean == null)
                                                        paramsBean = new PlaneParamsBean();
                                                    paramsBean.setMavlinkMessage(msg);
                                                    mavlinkObserver.onRead(paramsBean);
                                                    break;
                                                case MAVLINK_MSG_ID_LOCATION:
                                                    if (lBean == null)
                                                        lBean = new LocationBean();
                                                    lBean.setMavlinkMessage(msg);
                                                    mavlinkObserver.onRead(lBean);
                                                    break;
                                                case MAVLINK_MSG_ID_AUTOPILOT_VERSION:
                                                    AutopilotVerisionBean verisionBean = new AutopilotVerisionBean();
                                                    verisionBean.setMavlinkMessage(msg);
                                                    mavlinkObserver.onRead(verisionBean);
                                                    break;
                                            }
                                        }

//                                        if (mavlinkObserver != null)
//                                            mavlinkObserver.onRead(msg);
                                    }
                                });
                    }
                });
            }

            @Override
            public void close() {
                FlyPieApplication.getInstance().setMavSocketSuccess(false);
            }

            @Override
            public void connecSuccess() {
                //mavlink端口刚连接上时，需要立即与飞控 通讯，所以需要成功回调
                FlyPieApplication.getInstance().setMavSocketSuccess(true);
                if (mavlinkObserver != null)
                    mavlinkObserver.connecSuccess();
            }

            @Override
            public void connectFail() {

            }
        });
    }

    /**
     * 连接data socket，最终以此端口连接上表明相机连接状态
     */
    private void connectTo8787ForData() {
        SocketManager.getCmdInstance().connectTo8787Socket(mProductModel.remoteIpAddress, new DataSocketReadListener() {
            @Override
            public void read(List<FileBean> fbs, int index, byte[] buffer) {

            }

            @Override
            public void uploadLisenter(int progress) {
                //datasocket文件上传进度

            }

            @Override
            public void close() {

            }


            @Override
            public void connecSuccess() {
                FlyPieApplication.getInstance().setDataTcpSuccess(true);
                activateConnectCallback();
            }

            @Override
            public void connectFail() {
//                FlyPieApplication.getInstance().setDataTcpSuccess(false);
//                activateDisConnectCallback();
            }
        });
    }


    /**
     * 激活连接回调
     */
    public void activateConnectCallback() {
        MLog.log("activateConnectCallback() thread==>" + Thread.currentThread() + "||" + mListioners.size());
        for (int i = 0; i < mListioners.size(); i++) {
            ConnectManagerCallback callback = mListioners.get(i);
            if (callback != null)
                callback.onConnected(mProductModel);
        }
    }

    /**
     * 激活失联回调
     */
    public void activateDisConnectCallback() {
        MLog.log("activateDisConnectCallback() ");
        for (int i = 0; i < mListioners.size(); i++) {
            ConnectManagerCallback callback = mListioners.get(i);
            if (callback != null)
                callback.onDisConnected();
        }
    }

    /**
     * 激活占线回调
     */
    public void activateConnectionUsedCallback() {
        MLog.log("activateConnectionUsedCallback() ");
        for (int i = 0; i < mListioners.size(); i++) {
            ConnectManagerCallback callback = mListioners.get(i);
            if (callback != null)
                callback.onConnectionUsed();
        }
    }


    private Runnable mDisconnectedRunnable = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < mListioners.size(); i++) {
                ConnectManagerCallback callback = mListioners.get(i);
                if (callback != null)
                    callback.onDisConnected();
            }
        }
    };

    public void close8686() {
        SocketManager.getCmdInstance().close8686Socket();
    }

    public void cloaseAll() {
        SocketManager.getCmdInstance().closeAllSocket();
//        SocketManager.getCmdInstance().close7878Socket();
//        SocketManager.getCmdInstance().close8787Socket();
//        SocketManager.getCmdInstance().close8686Socket();
    }
}
