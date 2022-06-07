package com.feipai.flypai.utils.wifimanger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.app.FlyPieApplication;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.NetworkUtils;

import java.util.ArrayList;

public class WifiReceiver extends BroadcastReceiver {

    private static final int TYPE_NONE = 0;
    private static final int TYPE_WIFI = 1;
    private static final int TYPE_MOBILE_MMS = 2;

    private static boolean isConnetFlypai = false;


    public final static String CUSTOM_ANDROID_NET_CHANGE_ACTION = "com.zhanyun.api.netstatus.CONNECTIVITY_CHANGE";
    private static ArrayList<NetChangeObserver> mNetChangeObservers = new ArrayList<NetChangeObserver>();
    private static WifiReceiver mBroadcastReceiver;
    private String mCurWifiSsid = null;


    private static BroadcastReceiver getReceiver() {
        if (null == mBroadcastReceiver) {
            synchronized (WifiReceiver.class) {
                if (null == mBroadcastReceiver) {
                    mBroadcastReceiver = new WifiReceiver();
                }
            }
        }
        return mBroadcastReceiver;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        notifyObserver(intent);
    }

    /**
     * 注册
     *
     * @param mContext
     */
    public static void registerNetworkStateReceiver(Context mContext) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mContext.getApplicationContext().registerReceiver(getReceiver(), filter);
    }

    /**
     * 清除
     *
     * @param mContext
     */
    public static void checkNetworkState(Context mContext) {
        Intent intent = new Intent();
        intent.setAction(CUSTOM_ANDROID_NET_CHANGE_ACTION);
        mContext.sendBroadcast(intent);
    }

    /**
     * 反注册
     *
     * @param mContext
     */
    public static void unRegisterNetworkStateReceiver(Context mContext) {
        isConnetFlypai = false;
        if (mBroadcastReceiver != null) {
            try {
                mContext.getApplicationContext().unregisterReceiver(mBroadcastReceiver);
            } catch (Exception e) {

            }
        }


    }


    private void notifyObserver(Intent intent) {
        String action = intent.getAction();
//        LogUtils.d("网络---->" + action);
        if (!mNetChangeObservers.isEmpty()) {
            int size = mNetChangeObservers.size();
            for (int i = 0; i < size; i++) {
                NetChangeObserver observer = mNetChangeObservers.get(i);
                if (observer != null) {
                    if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                        // TODO: 2019-04-02 wifi切换
                        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
//                        LogUtils.d("网络info==》" + info.getState() + "||" + isConnetFlypai);
                        if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                            observer.onWifiDisconnect();
                            if (isConnetFlypai) {
                                isConnetFlypai = false;
                                // TODO: 2019-04-02 连上之后断开
                                observer.onWifiBreakInBackground();
                            }
                        } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                            // TODO: 2019-04-02
                            String ip = NetworkUtils.getIPAddress(false);
                            int wifiLevel = NetworkUtils.getWifiLevel();
                            String bssid = NetworkUtils.getCurConnetWifiName();
                            if (bssid != null && bssid.startsWith(ConstantFields.DATA_CONFIG.FLYPAI_NAME_START)) {
//                                LogUtils.d("网络连接上飞行器--->" + mCurWifiSsid + "||" + isConnetFlypai + "||" + NetworkUtils.isWifiConnected());
                                if (mCurWifiSsid != null && mCurWifiSsid.startsWith(ConstantFields.DATA_CONFIG.FLYPAI_NAME_START) && !bssid.equals(mCurWifiSsid) && NetworkUtils.isWifiConnected()) {
                                    isConnetFlypai = true;
                                    mCurWifiSsid = bssid;
                                    observer.onWifiConnected(mCurWifiSsid);
                                }
                                if (!isConnetFlypai) {
                                    isConnetFlypai = true;
                                    mCurWifiSsid = bssid;
                                    observer.onWifiConnected(mCurWifiSsid);
                                }
                            } else {
                                // TODO: 2019-04-02 其他wifi
                                observer.onOtherWifiConneted();
                            }
                        }
                    } else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
                        // TODO: 2019-04-02 网络信号改变
                        int level = Math.abs(((WifiManager) FlyPieApplication.getInstance().getApplicationContext().getSystemService(
                                FlyPieApplication.getInstance().WIFI_SERVICE)).getConnectionInfo().getRssi());
                        observer.onWifiRssiChanged(level);
                    }
                }
            }
        }

    }

    /**
     * 添加网络监听
     *
     * @param observer
     */
    public static void registerObserver(NetChangeObserver observer) {
        if (mNetChangeObservers == null) {
            mNetChangeObservers = new ArrayList<NetChangeObserver>();
        }
        mNetChangeObservers.add(observer);
    }

    /**
     * 移除网络监听
     *
     * @param observer
     */
    public static void removeRegisterObserver(NetChangeObserver observer) {
        if (mNetChangeObservers != null) {
            if (mNetChangeObservers.contains(observer)) {
                mNetChangeObservers.remove(observer);
            }
        }
    }
}
