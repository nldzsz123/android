package com.feipai.flypai.app;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.feipai.flypai.BuildConfig;
import com.feipai.flypai.R;
import com.feipai.flypai.connect.ConnectManager;
import com.feipai.flypai.di.component.AppComponent;
import com.feipai.flypai.di.component.DaggerAppComponent;
import com.feipai.flypai.di.module.AppModule;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ToastUtils;
import com.feipai.flypai.utils.global.Utils;
import com.feipai.flypai.utils.imageloader.GlideApp;
import com.mapbox.mapboxsdk.Mapbox;
import com.zhy.autolayout.config.AutoLayoutConifg;

public class FlyPieApplication extends Application {

    private static FlyPieApplication mContext;

    protected boolean tcpSuccess;

    protected boolean dataTcpSuccess;

    private boolean mavSocketSuccess;


    public boolean isTcpSuccess() {
        return tcpSuccess;
    }

    public void setTcpSuccess(boolean tcpSuccess) {
        this.tcpSuccess = tcpSuccess;
    }

    public boolean isDataTcpSuccess() {
        return dataTcpSuccess;
    }

    public void setDataTcpSuccess(boolean dataTcpSuccess) {
        this.dataTcpSuccess = dataTcpSuccess;
    }

    public boolean isMavSocketSuccess() {
        return mavSocketSuccess;
    }

    public void setMavSocketSuccess(boolean mavSocketSuccess) {
        this.mavSocketSuccess = mavSocketSuccess;
    }

    public static FlyPieApplication getInstance() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        // 初始化工具类
        Utils.init(this, isDebug());
        // 初始化日志
        LogUtils.init(isDebug());
        // 初始化土司工具类
        ToastUtils.init(true);
        // 初始化MapBox
        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));
        //初始化屏幕实配（Fragment）
        // 初始化自动适配库AutoLayout
        AutoLayoutConifg.getInstance().useDeviceSize();

        // 开始监控
        ConnectManager.getInstance().startConnectLision();
    }

    /**
     * 是否是Debug状态
     */
    public boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        GlideApp.get(this).clearMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        LogUtils.d("GlideApp====>onTrimMemory");
        GlideApp.get(this).clearMemory();

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    /**
     * 获取App组件
     */
    public AppComponent getAppComponent() {
        return DaggerAppComponent.builder()
                .appModule(new AppModule(mContext))
                .build();
    }
}
