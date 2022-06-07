package com.zhy.autolayout.config;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.util.Log;

import com.zhy.autolayout.utils.L;
import com.zhy.autolayout.utils.ScreenUtils;

/**
 * Created by zhy on 15/11/18.
 */
public class AutoLayoutConifg {

    private static AutoLayoutConifg sIntance = new AutoLayoutConifg();


    private static final String KEY_DESIGN_WIDTH = "design_width";
    private static final String KEY_DESIGN_HEIGHT = "design_height";

    private int mScreenWidth;
    private int mScreenHeight;

    private int mDesignWidth;
    private int mDesignHeight;

    private boolean useDeviceSize;


    private AutoLayoutConifg() {
    }

    public void checkParams() {
        if (mDesignHeight <= 0 || mDesignWidth <= 0) {
            throw new RuntimeException(
                    "you must set " + KEY_DESIGN_WIDTH + " and " + KEY_DESIGN_HEIGHT + "  in your manifest file.");
        }
    }

    public AutoLayoutConifg useDeviceSize() {
        useDeviceSize = true;
        return this;
    }


    public static AutoLayoutConifg getInstance() {
        return sIntance;
    }


    public int getScreenWidth() {
        return mScreenWidth;
    }

    public int getScreenHeight() {
        return mScreenHeight;
    }

    public int getDesignWidth() {
        return mDesignWidth;
    }

    public int getDesignHeight() {
        return mDesignHeight;
    }


    public void init(Context context, Rect frame, boolean isUseLandscape) {
        int[] screenSize = ScreenUtils.getScreenSize(context, useDeviceSize);
//        boolean isPortrait = true;
        if (frame != null) {
//            isPortrait = frame.width() == screenSize[0];
        }
// else {
//        boolean isPortrait = screenSize[0] <= screenSize[1];
//                context.getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
//        }

        getMetaData(context, !isUseLandscape);

        if (!isUseLandscape) { // 竖屏
            // doSomrthing
            if (frame == null) {
                mScreenWidth = screenSize[0];
                mScreenHeight = screenSize[1];
            } else {
                mScreenWidth = frame.right - frame.left;
                mScreenHeight = frame.bottom - frame.top - ScreenUtils.getNavigationBarHeight(context) - ScreenUtils.getStatusBarHeight(context);
            }
        } else {
            // 横屏时dosomething
            if (frame == null) {
                mScreenHeight = screenSize[0];
                mScreenWidth = screenSize[1];
            } else {
                mScreenHeight = frame.height();
                mScreenWidth = frame.width();
            }

        }
//        Log.e("yanglin", "屏幕宽高===》" + mScreenHeight + "||" + mScreenWidth);
    }

    private void getMetaData(Context context, boolean isPortrait) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo;
            applicationInfo = packageManager.getApplicationInfo(context
                    .getPackageName(), PackageManager.GET_META_DATA);
            if (applicationInfo != null && applicationInfo.metaData != null) {
                if (isPortrait) {
                    mDesignWidth = (int) applicationInfo.metaData.get(KEY_DESIGN_WIDTH);
                    mDesignHeight = (int) applicationInfo.metaData.get(KEY_DESIGN_HEIGHT);
                } else {
                    mDesignWidth = (int) applicationInfo.metaData.get(KEY_DESIGN_HEIGHT);
                    mDesignHeight = (int) applicationInfo.metaData.get(KEY_DESIGN_WIDTH);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(
                    "you must set " + KEY_DESIGN_WIDTH + " and " + KEY_DESIGN_HEIGHT + "  in your manifest file.", e);
        }

        L.e(" designWidth =" + mDesignWidth + " , designHeight = " + mDesignHeight);
    }


}
