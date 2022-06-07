package com.feipai.flypai.utils.wifimanger;


/**
 * 网络改变观察者，观察网络改变后回调的方法
 */
public interface NetChangeObserver {


    /**
     * 与飞行器wifi连接上
     */
    void onWifiConnected(String ssid);

    /**
     * 与飞行器wifi断开
     */
    void onWifiBreakInBackground();

    /**
     * 未连接wifi
     */
    void onWifiDisconnect();

    /**
     * wifi信号强弱
     */
    void onWifiRssiChanged(int wifiRssi);

    /**
     * 连接上其他wifi
     */
    void onOtherWifiConneted();


}