package com.feipai.flypai.utils.global;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Proxy;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * 网络相关工具类
 */
public class NetworkUtils {

    private NetworkUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public enum NetworkType {
        NETWORK_WIFI,
        NETWORK_MOBILE,
        NETWORK_NONE,
    }

    /**
     * 打开网络设置界面
     * <p>3.0以下打开设置界面</p>
     */
    public static void openWirelessSettings() {
        if (android.os.Build.VERSION.SDK_INT > 10) {
            Intent intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Utils.context.startActivity(intent);
        } else {
            Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Utils.context.startActivity(intent);
        }
    }

    /**
     * 获取活动网络信息
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>}</p>
     *
     * @return NetworkInfo
     */
    private static NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager cm = (ConnectivityManager) Utils.context.getSystemService(CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * 判断网络是否连接
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>}</p>
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isConnected() {
        NetworkInfo info = getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    /**
     * 判断网络是否可用
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.INTERNET"/>}</p>
     *
     * @param ip 对应IP是否可以ping通
     * @return {@code true}: 可用<br>{@code false}: 不可用
     */
    public static boolean isAvailableByPing(String ip) {
        ShellUtils.CommandResult result = ShellUtils.execCmd("ping -c 1 -w 1 " + ip, false);
        boolean ret = result.result == 0;
        if (result.errorMsg != null) {
            LogUtils.d("isAvailableByPing errorMsg", result.errorMsg);
        }
        if (result.successMsg != null) {
            LogUtils.d("isAvailableByPing successMsg", result.successMsg);
        }
        return ret;
    }

    /**
     * 判断移动数据是否打开
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean getDataEnabled() {
        try {
            TelephonyManager tm = (TelephonyManager) Utils.context.getSystemService(Context.TELEPHONY_SERVICE);
            Method getMobileDataEnabledMethod = tm.getClass().getDeclaredMethod("getDataEnabled");
            if (null != getMobileDataEnabledMethod) {
                return (boolean) getMobileDataEnabledMethod.invoke(tm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 打开或关闭移动数据
     * <p>需系统应用 需添加权限{@code <uses-permission android:name="android.permission.MODIFY_PHONE_STATE"/>}</p>
     *
     * @param enabled {@code true}: 打开<br>{@code false}: 关闭
     */
    public static void setDataEnabled(boolean enabled) {
        try {
            TelephonyManager tm = (TelephonyManager) Utils.context.getSystemService(Context.TELEPHONY_SERVICE);
            Method setMobileDataEnabledMethod = tm.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
            if (null != setMobileDataEnabledMethod) {
                setMobileDataEnabledMethod.invoke(tm, enabled);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断网络是否是4G
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>}</p>
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean is4G() {
        NetworkInfo info = getActiveNetworkInfo();
        return info != null && info.isAvailable() && info.getSubtype() == TelephonyManager.NETWORK_TYPE_LTE;
    }

    /**
     * 判断wifi是否打开
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>}</p>
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean getWifiEnabled() {
        WifiManager wifiManager = (WifiManager) Utils.context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    /**
     * 打开或关闭wifi
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>}</p>
     *
     * @param enabled {@code true}: 打开<br>{@code false}: 关闭
     */
    public static void setWifiEnabled(boolean enabled) {
        WifiManager wifiManager = (WifiManager) Utils.context.getSystemService(Context.WIFI_SERVICE);
        if (enabled) {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
        } else {
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
            }
        }
    }

    /**
     * 判断wifi是否连接状态
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>}</p>
     *
     * @return {@code true}: 连接<br>{@code false}: 未连接
     */
    public static boolean isWifiConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) Utils.context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * 判断手机网络是否连接状态
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>}</p>
     *
     * @return {@code true}: 连接<br>{@code false}: 未连接
     */
    public static boolean isMobileNetworkConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) Utils.context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return networkInfo == null ? false : networkInfo.isConnected();
    }

    /**
     * 判断wifi数据是否可用
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>}</p>
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.INTERNET"/>}</p>
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isWifiAvailable(String ip) {
        return getWifiEnabled() && isAvailableByPing(ip);
    }

    /**
     * 获取网络运营商名称
     * <p>中国移动、如中国联通、中国电信</p>
     *
     * @return 运营商名称
     */
    public static String getNetworkOperatorName() {
        TelephonyManager tm = (TelephonyManager) Utils.context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null ? tm.getNetworkOperatorName() : null;
    }

    private static final int NETWORK_TYPE_GSM = 16;
    private static final int NETWORK_TYPE_TD_SCDMA = 17;
    private static final int NETWORK_TYPE_IWLAN = 18;

    /**
     * 获取当前网络类型
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>}</p>
     *
     * @return 网络类型
     * <ul>
     * <li>{@link NetworkType#NETWORK_WIFI   } </li>
     * <li>{@link NetworkType#NETWORK_MOBILE     } </li>
     * <li>{@link NetworkType#NETWORK_NONE     } </li>
     * </ul>
     */
    public static NetworkType getNetworkType() {
        NetworkType netType = NetworkType.NETWORK_NONE;
        NetworkInfo info = getActiveNetworkInfo();
//        LogUtils.d("info类型===" + info.toString());
        if (info != null && info.isAvailable()) {
//            LogUtils.d("info类型===" + info.toString());
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                netType = NetworkType.NETWORK_WIFI;
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (info.getSubtype()) {

                    case NETWORK_TYPE_GSM:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
//                        netType = NetworkType.NETWORK_2G;
//                        break;

                    case NETWORK_TYPE_TD_SCDMA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
//                        netType = NetworkType.NETWORK_3G;
//                        break;

                    case NETWORK_TYPE_IWLAN:
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        netType = NetworkType.NETWORK_MOBILE;
//                        netType = NetworkType.NETWORK_4G;
//                        break;
                    default:

                        String subtypeName = info.getSubtypeName();
                        if (subtypeName.equalsIgnoreCase("TD-SCDMA")
                                || subtypeName.equalsIgnoreCase("WCDMA")
                                || subtypeName.equalsIgnoreCase("CDMA2000")) {
                            netType = NetworkType.NETWORK_MOBILE;
                        } else {
                            netType = NetworkType.NETWORK_NONE;
                        }
                        break;
                }
            } else {
                netType = NetworkType.NETWORK_NONE;
            }
        }
        return netType;
    }

    /**
     * 获取IP地址
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.INTERNET"/>}</p>
     *
     * @param useIPv4 是否用IPv4
     * @return IP地址
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            for (Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces(); nis.hasMoreElements(); ) {
                NetworkInterface ni = nis.nextElement();
                // 防止小米手机返回10.0.2.15
                if (!ni.isUp()) continue;
                for (Enumeration<InetAddress> addresses = ni.getInetAddresses(); addresses.hasMoreElements(); ) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String hostAddress = inetAddress.getHostAddress();
                        boolean isIPv4 = hostAddress.indexOf(':') < 0;
                        if (useIPv4) {
                            if (isIPv4) return hostAddress;
                        } else {
                            if (!isIPv4) {
                                int index = hostAddress.indexOf('%');
                                return index < 0 ? hostAddress.toUpperCase() : hostAddress.substring(0, index).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取域名ip地址
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.INTERNET"/>}</p>
     *
     * @param domain 域名
     * @return ip地址
     */
    public static String getDomainAddress(final String domain) {
        try {
            ExecutorService exec = Executors.newCachedThreadPool();
            Future<String> fs = exec.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    InetAddress inetAddress;
                    try {
                        inetAddress = InetAddress.getByName(domain);
                        return inetAddress.getHostAddress();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            });
            return fs.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 是否有Http代理服务器
     */
    public static boolean hasHttpProxy() {
        if (NetworkUtils.isWifiConnected()) {
            String host = Proxy.getDefaultHost();
            if (!TextUtils.isEmpty(host)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否有VPN服务
     */
    public static boolean isVpnConnected() {
        try {
            Enumeration<NetworkInterface> niList = NetworkInterface.getNetworkInterfaces();
            if (niList != null) {
                for (NetworkInterface intf : Collections.list(niList)) {
                    if (!intf.isUp() || intf.getInterfaceAddresses().size() == 0) {
                        continue;
                    }
                    if ("tun0".equals(intf.getName()) || "ppp0".equals(intf.getName())) {
                        return true;
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取当前连接的wifi名称
     */
    public static String getCurConnetWifiName() {
        String ssid = null;
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
            ConnectivityManager cm = (ConnectivityManager) Utils.context.getSystemService(CONNECTIVITY_SERVICE);
            assert cm != null;
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                ssid = info.getExtraInfo();
                LogUtils.d("WiFi SSID: " + ssid);
            }
            if (ssid != null) {
                if (ssid.startsWith("\"")) {
                    ssid = ssid.substring(1);
                }
                if (ssid.endsWith("\"")) {
                    ssid = ssid.substring(0, ssid.length() - 1);
                }
                return ssid;
            }
            WifiManager manager = (WifiManager) Utils.context.getSystemService(Utils.context.WIFI_SERVICE);
            WifiInfo info1 = manager.getConnectionInfo();
            ssid = info1 != null ? info1.getSSID() : null;
            LogUtils.d("当前wifi SSID1=" + ssid);
            if (TextUtils.isEmpty(ssid)) {
                return null;
            }
            if (ssid.startsWith("\"")) {
                ssid = ssid.substring(1);
            }
            if (ssid.endsWith("\"")) {
                ssid = ssid.substring(0, ssid.length() - 1);
            }
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O || Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            WifiManager manager = (WifiManager) Utils.context.getSystemService(Utils.context.WIFI_SERVICE);
            WifiInfo info = manager.getConnectionInfo();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                return info.getSSID();
            } else {
                return info.getSSID().replace("\"", "");
            }
        }
        return ssid;
    }

    public static void toggleGPS() {
        Intent GPSIntent = new Intent();
        GPSIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider");
        GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
        GPSIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(Utils.context, 0, GPSIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    public static int getWifiLevel() {
        WifiManager wifi_service = (WifiManager) Utils.context.getSystemService(Utils.context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifi_service.getConnectionInfo();
        return wifiInfo.getRssi();
    }


    /**
     * 获取Https的证书
     *
     * @param context Activity（fragment）的上下文
     * @return SSL的上下文对象
     */
    public static SSLContext getSSLContext(Context context) {

        CertificateFactory certificateFactory = null;
        SSLContext s_sSLContext = null;
        InputStream inputStream = null;
        Certificate cer = null;
        KeyStore keystore = null;
        TrustManagerFactory trustManagerFactory = null;
        try {

            certificateFactory = CertificateFactory.getInstance("X.509");
            inputStream = context.getAssets().open("httpsserver.crt");//这里导入SSL证书文件

            try {
                //读取证书
                cer = certificateFactory.generateCertificate(inputStream);
                LogUtils.d("获取到的SSL证书" + cer.getPublicKey().toString());

            } finally {
                inputStream.close();
            }

            //创建一个证书库，并将证书导入证书库
            keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(null, null); //双向验证时使用
            keystore.setCertificateEntry("trust", cer);

            // 实例化信任库
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            // 初始化信任库
            trustManagerFactory.init(keystore);

            s_sSLContext = SSLContext.getInstance("TLS");
            s_sSLContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());

            return s_sSLContext;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    //获取wifi列表
    public static List<ScanResult> getWifiList() {
        WifiManager wifiManager = (WifiManager) Utils.context.getSystemService(Context.WIFI_SERVICE);
        //6.0系统需要调用此方法后，才可进行扫描
        wifiManager.startScan();
        List<ScanResult> list = new ArrayList<>();
        list = wifiManager.getScanResults();
        return list;
    }

    /**
     * 检测wifi是否开启
     */
    public static boolean isWifiOpened() {
        WifiManager wifiManager = (WifiManager) Utils.context
                .getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }


    /**
     * 检测GPS是否打开
     *
     * @return
     */
    public static boolean isGpsOpen() {
        boolean isOpen;
        LocationManager locationManager = (LocationManager) Utils.context
                .getSystemService(Context.LOCATION_SERVICE);
        isOpen = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
        return isOpen;
    }

    /**
     * wifi开启 or 关闭
     */
    public static void toggleWiFi(boolean enabled) {
        WifiManager wifiManager = (WifiManager) Utils.context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(enabled);
    }

    public static void isOnlyWifi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP/*5.0*/) {
            ConnectivityManager connectivityManager = (ConnectivityManager) Utils.context.
                    getSystemService(CONNECTIVITY_SERVICE);
            // 请注意这里会有一个版本适配bug，所以请在这里添加非空判断
            if (connectivityManager != null) {
                NetworkRequest.Builder builder = new NetworkRequest.Builder();

                NetworkRequest request = builder
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
//                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//                        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                        .build();
                connectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {

                    /**
                     * 网络可用的回调连接成功
                     * */
                    @Override
                    public void onAvailable(Network network) {
                        super.onAvailable(network);
                        LogUtils.d("onAvailable ==>" + network.toString());
                        if (Build.VERSION.SDK_INT >= 23) {
                            connectivityManager.bindProcessToNetwork(null);
                            LogUtils.d("bindProcessToNetwork : " + connectivityManager.bindProcessToNetwork(network));
                            connectivityManager.bindProcessToNetwork(network);
                        } else {
                            // 23后这个方法舍弃了
                            connectivityManager.setProcessDefaultNetwork(null);
                            connectivityManager.setProcessDefaultNetwork(network);
                        }
                        connectivityManager.unregisterNetworkCallback(this);
                    }

                    /**
                     * 实践中在网络连接正常的情况下，丢失数据会有回调
                     * */
                    @Override
                    public void onLosing(Network network, int maxMsToLive) {
                        super.onLosing(network, maxMsToLive);
                        LogUtils.d("onLosing ==>" + network.toString() + " max==>" + maxMsToLive);
                    }

                    /**
                     * 网络不可用时调用和onAvailable成对出现
                     */
                    @Override
                    public void onLost(Network network) {
                        super.onLost(network);
                        LogUtils.d("onLost ==>" + network.toString());
                    }

                    @Override
                    public void onUnavailable() {
                        super.onUnavailable();
                        LogUtils.d("onUnavailable ==>");
                    }

                    /**
                     * 字面直接能理解
                     * @param network 新连接网络
                     * @param networkCapabilities 新连接网络的一些能力参数
                     */
                    @Override
                    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                        super.onCapabilitiesChanged(network, networkCapabilities);
                        LogUtils.d("onCapabilitiesChanged ==>" + networkCapabilities.toString());
                        //WIFI -> CELLULAR
                        //[ Transports: CELLULAR Capabilities: INTERNET&NOT_RESTRICTED&TRUSTED&NOT_VPN&VALIDATED LinkUpBandwidth>=51200Kbps LinkDnBandwidth>=102400Kbps Specifier: <3>]
                        //CELLULAR -> WIFI
                        //==>[ Transports: WIFI Capabilities: INTERNET&NOT_RESTRICTED&TRUSTED&NOT_VPN&VALIDATED LinkUpBandwidth>=1048576Kbps LinkDnBandwidth>=1048576Kbps SignalStrength: -42]
                    }

                    /**
                     * 和上面类似，但是没有试出效果
                     */
                    @Override
                    public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                        super.onLinkPropertiesChanged(network, linkProperties);
                        LogUtils.d("onLinkPropertiesChanged ==>" + linkProperties.toString());
                    }
                });
            }
        }

    }

    public static boolean forcebindToNetwork(int NetType) {
        final ConnectivityManager cMgr = (ConnectivityManager) Utils.context.getSystemService(CONNECTIVITY_SERVICE);
        if (cMgr == null) {
            Network[] networks = cMgr.getAllNetworks();
            for (int i = 0; i < networks.length; i++) {
                NetworkInfo netInfo = cMgr.getNetworkInfo(networks[i]);
//                LogUtils.d("所有网络=====》" + netInfo);//bugyly:#76839
                if (netInfo != null && netInfo.getType() == NetType) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        cMgr.bindProcessToNetwork(networks[i]);
                    } else {
                        // 23后这个方法舍弃了
                        cMgr.setProcessDefaultNetwork(networks[i]);
                    }
                    String mNetworkTypeName = netInfo.getTypeName();
                    LogUtils.d(mNetworkTypeName + "bounded!");
                }
            }
            if (cMgr == null || cMgr.getActiveNetworkInfo().getState() != NetworkInfo.State.CONNECTED) {
                return false;
            }
            return true;
        }
        return false;
    }

}