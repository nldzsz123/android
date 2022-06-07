package com.feipai.flypai.beans;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.base.BaseEntity;
import com.feipai.flypai.connect.ConnectManager;
import com.feipai.flypai.utils.cache.CacheManager;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.StringUtils;
import com.feipai.flypai.utils.global.Utils;

public class PlaneVersionBean extends BaseEntity {

    /**
     * 服务器飞控版本号
     */
    public String serverPlaneVersion = "2.0.1.40";
    /**
     * 服务器相机版本号
     */
    public String serverCameraVersion = "2.0.8.0";
    /**
     * 服务器云台版本号
     */
    public String serverYuntaiVersion = "6.0.0.5";

    /**
     * 服务器版APP本号
     */
    public String serverAppVersionCode = "1";


    /**
     * 本地APP版本号
     */
    public String loacalAppVersion = "1";

    public int loacalAppCode = 1;


    /**
     * 本地飞控版本号
     */
    public String localPlaneVersion = "1";
    /**
     * 本地相机版本号
     */
    public String localCameraVersion = "1";
    /**
     * 本地版本号
     */
    public String localYuntaiVersion = "1";


    public boolean planeVersionIsForceUpgrade;

    /**
     * 飞控是否需要激活
     */
    public int ack = -1;


    /**
     * 是否解锁，true解锁，false未解锁
     */
    public boolean unLock = false;

    /**
     * 如果内置更新固件，只需要更改这里的版本号以及assets中对应的文件即可
     */
    public void initVer() {
        serverPlaneVersion = "2.1.1.40";
        serverCameraVersion = "2.0.7.0";
        serverYuntaiVersion = "2.0.5.0";
        if (ConnectManager.getInstance().mProductModel.productType == ConstantFields.ProductType_6kAir) {
            serverPlaneVersion = "3.0.0.5";
            serverCameraVersion = "VR6X-20191116-V5.0";
            serverYuntaiVersion = "6.0.2.2";
        } else if (ConnectManager.getInstance().mProductModel.productType == ConstantFields.ProductType_4kAir) {
            serverPlaneVersion = "3.0.0.5";
            serverCameraVersion = "2.1.1.8";
            serverYuntaiVersion = "4.0.0.1";
        }
        localPlaneVersion = "1";
        localCameraVersion = "1";
        localYuntaiVersion = "1";
        ack = -1;
    }

    public String getServerPlaneVersion() {
        return serverPlaneVersion;
    }

    public void setServerPlaneVersion(String serverPlaneVersion) {
        this.serverPlaneVersion = serverPlaneVersion;
    }

    public String getServerCameraVersion() {
        return serverCameraVersion;
    }

    public void setServerCameraVersion(String serverCameraVersion) {
        this.serverCameraVersion = serverCameraVersion;
    }

    public String getServerYuntaiVersion() {
        return serverYuntaiVersion;
    }

    public void setServerYuntaiVersion(String serverYuntaiVersion) {
        this.serverYuntaiVersion = serverYuntaiVersion;
    }

    public String getLocalPlaneVersion() {
        return localPlaneVersion;
    }

    public void setLocalPlaneVersion(String localPlaneVersion) {
        this.localPlaneVersion = localPlaneVersion;
    }

    public String getLocalCameraVersion() {
        return localCameraVersion;
    }

    public void setLocalCameraVersion(String localCameraVersion) {
        this.localCameraVersion = localCameraVersion;
    }

    public String getLocalYuntaiVersion() {
        return localYuntaiVersion;
    }

    public void setLocalYuntaiVersion(String localYuntaiVersion) {
        this.localYuntaiVersion = localYuntaiVersion;
    }

    /**
     * 4ka和6ka升级
     */
    public boolean isCamereNeedUpgrade() {
        // TODO: 2019/8/21 相机固件直接对比版本号是否相同即可
        if (StringUtils.isEmpty(localCameraVersion) || (!StringUtils.isEmpty(localCameraVersion) && localCameraVersion.length() <= 1))
            return false;
        if (ConnectManager.getInstance().mProductModel.productType != ConstantFields.ProductType_4k)
            return !StringUtils.equals(localCameraVersion, serverCameraVersion);
        return false;
//        return camparVer(false, localCameraVersion, serverCameraVersion);
    }

    /**
     * 4ka和6ka升级
     */
    public boolean isPlaneNeedUpgrade() {
        if (StringUtils.isEmpty(localPlaneVersion) || (!StringUtils.isEmpty(localPlaneVersion) && StringUtils.equals(localPlaneVersion, "1")))
            return false;
        if (ConnectManager.getInstance().mProductModel.productType != ConstantFields.ProductType_4k)
            return camparVer(true, localPlaneVersion, serverPlaneVersion);
        return false;
    }

    /**
     * 云台升级 zsz:todo 先不开启云台升级
     */
    public boolean isYuntaiNeedUpgrade() {
//        if (StringUtils.isEmpty(localYuntaiVersion)||(!StringUtils.isEmpty(localYuntaiVersion)&&localYuntaiVersion.length()<=1))
//            return false;
//        if (ConnectManager.getInstance().mProductModel.productType == ConstantFields.ProductType_6kAir)
//            return camparVer(false, localYuntaiVersion, serverYuntaiVersion);
        return false;
    }


    public void loaclPlaneFwError() {
        this.setLocalPlaneVersion("2");
        setPlaneVersionIsForceUpgrade(true);
    }


    public boolean isUnLock() {
        return unLock;
    }

    public void setUnLock(boolean unLock) {
        this.unLock = unLock;
    }

    public boolean isPlaneVersionIsForceUpgrade() {
        LogUtils.d("是否强制升级--》" + isPlaneNeedUpgrade() + "||" + serverPlaneVersion.replace(".", "").substring(1, 2).equals("1"));
        planeVersionIsForceUpgrade = (isPlaneNeedUpgrade() && serverPlaneVersion != null &&
                serverPlaneVersion.length() > 2 && serverPlaneVersion.replace(".", "").substring(1, 2).equals("1"))
                || localPlaneVersion.equals("2");
        return false;//暂时去掉所有强制升级
    }

    public void setPlaneVersionIsForceUpgrade(boolean planeVersionIsForceUpgrade) {
        this.planeVersionIsForceUpgrade = planeVersionIsForceUpgrade;
    }

    public void setLoacalAppVersion(String loacalAppVersion) {
        this.loacalAppVersion = loacalAppVersion;
    }

    public String getLoacalAppVersion() {
        return loacalAppVersion;
    }

    public void setServerAppVersionCode(String serverAppVersionCode) {
        this.serverAppVersionCode = serverAppVersionCode;
        CacheManager.getSharedPrefUtils().putString(ConstantFields.APP_CONFIG.APP_VERSION_CODE, serverAppVersionCode);
    }

    public int getAck() {
        return ack;
    }

    public void setPlaneNeedAck(int ack) {
        this.ack = ack;
    }

    public boolean isAppNeedUpgrade() {
        try {
            //获取APP版本versionName
            PackageInfo packageInfo = Utils.context.getPackageManager().getPackageInfo(Utils.context.getPackageName(), 0);
            String versionName = packageInfo.versionName;
            loacalAppCode =
                    packageInfo.versionCode;
            StringBuilder builder = new StringBuilder("V" + versionName);
            setLoacalAppVersion(builder.toString().trim());
            serverAppVersionCode = CacheManager.getSharedPrefUtils().getString(ConstantFields.APP_CONFIG.APP_VERSION_CODE);
            if (!StringUtils.isEmpty(serverAppVersionCode) && TextUtils.isDigitsOnly(serverAppVersionCode)) {
//               LogUtils.d("APP是否需要升级====>"+serverAppVersionCode+"||"+loacalAppCode);
                return Integer.parseInt(serverAppVersionCode) > loacalAppCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 版本号对比
     *
     * @param loc 本地版本号,本地可能会出现加了横线的测试版本
     * @param ser 服务器版本号
     * @return 是否需要更新
     */
    public boolean camparVer(boolean isPlaneVer, String loc, String ser) {
        loc = loc.replace(".", "");
        ser = ser.replace(".", "");
        LogUtils.d("本地=" + loc + "，远程=" + ser);
        if (loc.length() > 2 && ser.length() > 2) {
            if (!loc.contains("-")) {//有可能出现测试使用的版本，或者较早的版本
                int firstLoc = loc.charAt(0);
                int firstSer = ser.charAt(0);
                if (firstLoc == firstSer) {
                    loc = loc.substring(isPlaneVer ? 3 : 2, loc.length());
                    ser = ser.substring(isPlaneVer ? 3 : 2, ser.length());
//                    LogUtils.d("------>" + loc + "||" + ser);
                    if (TextUtils.isDigitsOnly(loc) && TextUtils.isDigitsOnly(ser)) {
                        return Integer.parseInt(ser) > Integer.parseInt(loc);
                    }
                } else {
                    return firstSer > firstLoc;
                }
            }
            return true;
        } else {
            if (loc.equals("2") || loc.equals("0")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "PlaneVersionBean{" +
                "serverPlaneVersion='" + serverPlaneVersion + '\'' +
                ", serverCameraVersion='" + serverCameraVersion + '\'' +
                ", serverYuntaiVersion='" + serverYuntaiVersion + '\'' +
                ", serverAppVersionCode='" + serverAppVersionCode + '\'' +
                ", localPlaneVersion='" + localPlaneVersion + '\'' +
                ", localCameraVersion='" + localCameraVersion + '\'' +
                ", localYuntaiVersion='" + localYuntaiVersion + '\'' +
                ", loacalAppVersion='" + loacalAppVersion + '\'' +
                ", loacalAppCode='" + loacalAppCode + '\'' +
                '}';
    }
}
