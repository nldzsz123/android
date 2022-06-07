package com.feipai.flypai.utils.global;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.List;

public class WifiConnectUtils {
    // 定义WifiManager对象
    private WifiManager mWifiManager;
    // 定义WifiInfo对象
    private WifiInfo mWifiInfo;


    // 构造器
    public WifiConnectUtils(Context context) {
        // 取得WifiManager对象
        mWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        // 取得WifiInfo对象
        mWifiInfo = mWifiManager.getConnectionInfo();
    }


    /**
     * 方便兼容6.0以上系统
     */
    public void Wificonnect(String wifiSSID, String wifiPwd) {
        int netId = -1;
    /*先执行删除wifi操作，1.如果删除的成功说明这个wifi配置是由本APP配置出来的；
                       2.这样可以避免密码错误之后，同名字的wifi配置存在，无法连接；
                       3.wifi直接连接成功过，不删除也能用, netId = getExitsWifiConfig(SSID).networkId;*/
        if (removeWifi(wifiSSID)) {//移除成功，就新建一个
            netId = mWifiManager.addNetwork(createWifiInfo(wifiSSID, wifiPwd, 3));
        } else {
            //删除不成功，要么这个wifi配置以前就存在过，要么是还没连接过的
            if (isExsits(wifiSSID) != null) {
                //这个wifi是连接过的，如果这个wifi在连接之后改了密码，那就只能手动去删除了
                netId = isExsits(wifiSSID).networkId;
            } else {
                //没连接过的，新建一个wifi配置
                netId = mWifiManager.addNetwork(createWifiInfo(wifiSSID, wifiPwd, 3));
            }
        }

        //这个方法的第一个参数是需要连接wifi网络的networkId，第二个参数是指连接当前wifi网络是否需要断开其他网络
        //无论是否连接上，都返回true。。。。
        mWifiManager.enableNetwork(netId, true);
    }


    /**
     * 移除wifi，因为权限，无法移除的时候，需要手动去翻wifi列表删除
     * 注意：！！！只能移除自己应用创建的wifi。
     * 删除掉app，再安装的，都不算自己应用，具体看removeNetwork源码
     *
     * @param netId wifi的id
     */
    public boolean removeWifi(int netId) {
        return mWifiManager.removeNetwork(netId);
    }

    /**
     * 移除wifi
     *
     * @param SSID wifi名
     */
    public boolean removeWifi(String SSID) {
        if (isExsits(SSID) != null) {
            return removeWifi(isExsits(SSID).networkId);
        } else {
            return false;
        }
    }


    // 添加一个网络并连接
    public void addNetwork(WifiConfiguration wcg) {
        int wcgID = mWifiManager.addNetwork(wcg);
        boolean b = mWifiManager.enableNetwork(wcgID, true);
        LogUtils.d("a--" + wcgID);
        LogUtils.d("b--" + b);
    }


//然后是一个实际应用方法，只验证过没有密码的情况：

    public WifiConfiguration createWifiInfo(String SSID, String Password, int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";


        if (Type == 1) //WIFICIPHER_NOPASS
        {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 2) //WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
//            config.wepKeys[0] = "\"" + Password + "\"";
            config.wepKeys[0] = Password;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 3) //WIFICIPHER_WPA
        {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.priority = 20000;//手动配置连接优先级，不知道会不会影响到wifi连接（多测)
//            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        } else if (Type == 4) {

        }
        return config;
    }

    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        if (existingConfigs == null)
            return null;
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (SSID != null && !SSID.equals(""))
                if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                    return existingConfig;
                }
        }
        return null;
    }

    //连接一个曾经连接过得WiFi
    public void connectNetwork(WifiConfiguration wcg) {
        boolean b = mWifiManager.enableNetwork(wcg.networkId, true);
    }


    public boolean connect(String SSID, String Password, int Type) {
        // 开启wifi功能需要一段时间(我在手机上测试一般需要1-3秒左右)，所以要等到wifi
        // 状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
        while (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
            try {
                // 为了避免程序一直while循环，让它睡个100毫秒在检测……
                Thread.currentThread();
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        }

        LogUtils.d("WifiAdmin#connect==连接结束");

        WifiConfiguration wifiConfig;
        boolean bRet = false;

        WifiConfiguration tempConfig = isExsits(SSID);
        if (tempConfig == null) {
//            创建一个新的WifiConfiguration ，CreateWifiInfo()需要自己实现
            wifiConfig = createWifiInfo(SSID, Password, Type);
            addNetwork(wifiConfig);
        } else {
            //发现指定WiFi，并且这个WiFi以前连接成功过
            wifiConfig = tempConfig;
            bRet = mWifiManager.enableNetwork(wifiConfig.networkId, true);
        }


        return bRet;
    }
}
