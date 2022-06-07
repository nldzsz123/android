package com.feipai.flypai.utils.global;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.feipai.flypai.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionUtils {
    private String[] ps = new String[]{Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_SETTINGS};

    private static PermissionUtils permissionHelper = null;
    private static List<String> mListPermissions;
    private static final String PERMISSIONS_INTERNET = Manifest.permission.INTERNET;
    private static final String PERMISSIONS_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String PERMISSIONS_ACCOUNTS = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String PERMISSIONS_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String PERMISSIONS_LOCATION_EXTRA_COMMANDS = Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS;
    private static final String PERMISSIONS_READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    //系统级权限
    private static final String PERMISSIONS_WRITE_SETTING = Manifest.permission.WRITE_SETTINGS;


    /**
     * 添加权限
     * author LH
     * data 2016/7/27 11:27
     */
    private void addAllPermissions(List<String> mListPermissions) {
        mListPermissions.addAll(Arrays.asList(ps));
//        mListPermissions.add(PERMISSIONS_INTERNET);
//        mListPermissions.add(PERMISSIONS_STORAGE);
//        mListPermissions.add(PERMISSIONS_ACCOUNTS);
//        mListPermissions.add(PERMISSIONS_LOCATION);
//        mListPermissions.add(PERMISSIONS_LOCATION_EXTRA_COMMANDS);
//        mListPermissions.add(PERMISSIONS_READ_PHONE_STATE);
//        mListPermissions.add(PERMISSIONS_WRITE_SETTING);
    }

    /**
     * 单例模式初始化
     * author LH
     * data 2016/7/27 11:27
     */
    public static PermissionUtils getInstance() {
        if (permissionHelper == null) {
            permissionHelper = new PermissionUtils();
        }
        return permissionHelper;
    }

    /**
     * 判断当前为M以上版本
     * author LH
     * data 2016/7/27 11:29
     */
    public boolean isOverMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * 获得没有授权的权限
     * author LH
     * data 2016/7/27 11:46
     */
    @TargetApi(value = Build.VERSION_CODES.M)
    public List<String> findDeniedPermissions(Activity activity, List<String> permissions) {
        List<String> denyPermissions = new ArrayList<>();
        for (String value : permissions) {
            if (activity.checkSelfPermission(value) != PackageManager.PERMISSION_GRANTED) {
                denyPermissions.add(value);
            }
        }
        return denyPermissions;
    }

    /**
     * 获取所有权限
     * author LH
     * data 2016/7/27 13:37
     */
    @TargetApi(value = Build.VERSION_CODES.M)
    public void requestPermissions(Activity activity, int requestCode, PermissionCallBack permissionCallBack) {
        if (mListPermissions == null) {
            mListPermissions = new ArrayList<String>();
            addAllPermissions(mListPermissions);
        }
        if (!isOverMarshmallow()) {
            return;
        }
        mListPermissions = findDeniedPermissions(activity, mListPermissions);
        if (mListPermissions != null && mListPermissions.size() > 0) {
            activity.requestPermissions(mListPermissions.toArray(new String[mListPermissions.size()]),
                    requestCode);
            for (String str : mListPermissions) {
//                LogUtils.d("其中存在拒绝的权限" + str);
            }
        } else {
            permissionCallBack.onPermissionSuccess();
//            LogUtils.d("权限全都有");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestResult(Activity activity, String[] permissions, int[] grantResults, PermissionCallBack permissionCallBack) {
        mListPermissions = new ArrayList<String>();
        ArrayList<String> noPermissions = new ArrayList<String>();
        ArrayList<String> rejectPermissons = new ArrayList<String>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                if (isOverMarshmallow()) {
                    boolean isShould = activity.shouldShowRequestPermissionRationale(permissions[i]);
                    mListPermissions.add(permissions[i]);
                    if (isShould) {
                        /**用户曾请求过此权限被拒绝了*/
                        noPermissions.add(permissions[i]);
                        LogUtils.d("用户曾请求过此权限被拒绝了" + permissions[i]);
                    } else {
                        /**用户曾请求过此权限并拒绝了此权限，并且勾选了别再提示*/
                        LogUtils.d("用户曾请求过此权限并拒绝了此权限，并且勾选了别再提示" + permissions[i]);
                        rejectPermissons.add(permissions[i]);
                    }
                }
            }
        }

        if (noPermissions.size() > 0) {
            permissionCallBack.onPermissionFail();
        } else if (rejectPermissons.size() > 0) {
            ArrayList<String> permissonsList = new ArrayList<String>();
            for (int i = 0; i < rejectPermissons.size(); i++) {
                String strPermissons = rejectPermissons.get(i);
                if (PERMISSIONS_STORAGE.equals(strPermissons)) {
                    permissonsList.add("android.permission.WRITE_EXTERNAL_STORAGE");
                } else if (PERMISSIONS_INTERNET.equals(strPermissons)) {
                    permissonsList.add("android.permission.INTERNET");
                } else if (PERMISSIONS_ACCOUNTS.equals(strPermissons)) {
                    permissonsList.add("android.permission.ACCESS_COARSE_LOCATION");
                } else if (PERMISSIONS_LOCATION.equals(strPermissons)) {
                    permissonsList.add("android.permission.ACCESS_FINE_LOCATION");
                } else if (PERMISSIONS_LOCATION_EXTRA_COMMANDS.equals(strPermissons)) {
                    permissonsList.add("android.permission.ACCESS_LOCATION_EXTRA_COMMANDS");
                } else if (PERMISSIONS_READ_PHONE_STATE.equals(strPermissons)) {
                    permissonsList.add("android.permission.READ_PHONE_STATE");
                } else if (PERMISSIONS_WRITE_SETTING.equals(strPermissons)) {
                    if (!Settings.System.canWrite(activity))
                        permissonsList.add("android.permission.WRITE_SETTINGS");
                }
            }
            LogUtils.d("被禁用的权限有几个" + permissonsList.size() + "是否可定位" + isLocationEnabled(activity));
            if (permissonsList.size() == 0) {
                permissionCallBack.onPermissionSuccess();
                return;
            }
            showPermissonsDialog(activity, permissionCallBack, permissonsList);
        } else {
            permissionCallBack.onPermissionSuccess();
        }
    }

    private void showPermissonsDialog(Activity activity, PermissionCallBack permissionCallBack, ArrayList<String> permissonsList) {
        String strPermissons = permissonsList.toString();
        strPermissons = strPermissons.replace("[", "");
        strPermissons = strPermissons.replace("]", "");
        strPermissons = strPermissons.replace(",", "、");
        String strAppName = activity.getString(R.string.app_name);
        String strMessage = "100";
        strMessage = String.format(strMessage, strAppName, strPermissons, "\"");
        LogUtils.d("这个特殊的权限是：" + strPermissons);
        if (strPermissons.equals("android.permission.WRITE_SETTINGS"))
            permissionCallBack.onPermissionReject(true, strMessage);
        else
            permissionCallBack.onPermissionReject(false, strMessage);
    }

    public boolean isLocationEnabled(Activity activity) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(activity.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }


    public interface PermissionCallBack {
        void onPermissionSuccess();

        void onPermissionReject(boolean isSystemLevel, String strMessage);

        void onPermissionFail();
    }
}
