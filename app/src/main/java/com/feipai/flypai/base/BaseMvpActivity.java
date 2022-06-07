package com.feipai.flypai.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;


import com.feipai.flypai.api.RxLoopObserver;
import com.feipai.flypai.api.RxLoopSchedulers;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.app.FlyPieApplication;
import com.feipai.flypai.beans.PlaneInfo;
import com.feipai.flypai.beans.ProductModel;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.beans.UserBean;
import com.feipai.flypai.connect.ConnectManager;
import com.feipai.flypai.di.module.ActivityModule;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.utils.CameraCommand;
import com.feipai.flypai.utils.GeneralFactory;
import com.feipai.flypai.utils.daoutils.DBClient;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.NetworkUtils;
import com.feipai.flypai.utils.global.RxBusUtils;
import com.feipai.flypai.utils.wifimanger.NetChangeObserver;
import com.feipai.flypai.utils.wifimanger.WifiReceiver;
import com.j256.ormlite.dao.EagerForeignCollection;

import javax.inject.Inject;

/**
 * MVP模式下的Activity的抽取
 *
 * @author yanglin
 */
public abstract
class BaseMvpActivity<P extends BasePresenter> extends BaseSimpleActivity implements BaseView {

    @Inject
    public P mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 初始化注入
        initInject();
        if (mPresenter != null) {
            mPresenter.attachView(this);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initListener() {
        super.initListener();
    }

    @Override
    protected void initRxbusListener(RxbusBean msg) {
        super.initRxbusListener(msg);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initNetChangeObserver(new NetChangeObserver() {
            @Override
            public void onWifiConnected(String ssid) {
//                LogUtils.d("网络状态--->与飞行器连接");
                onWifiConnectedCallBack(ssid);
            }

            @Override
            public void onWifiBreakInBackground() {
//                mPresenter.getVersionFromServier();
//                LogUtils.d("网络状态--->与飞行器断开连接");
                onWifiBreakInBackgroundCallBack();

            }

            @Override
            public void onWifiDisconnect() {
//                LogUtils.d("网络状态--->断开WIFI连接");
                onWifiDisconnectCallBack();
            }

            @Override
            public void onWifiRssiChanged(int wifiRssi) {
//                LogUtils.d("网络状态--->刷新WIFI强弱");
                onWifiRssiChangedCallBack(wifiRssi);
            }

            @Override
            public void onOtherWifiConneted() {
//                LogUtils.d("网络状态--->与其他WIFI连接");
                onOtherWifiConnetedCallBack();
//
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mObserver != null) {
            WifiReceiver.removeRegisterObserver(mObserver);
        }
    }

    /**
     * 连接上飞行器的回调
     * 根据ssid区分
     */
    protected void onWifiConnectedCallBack(String ssid) {
//        LogUtils.d("网络mObserver---》onWifiConnected");
        cmd.setFlyType(ssid);
//        connectTcp();
    }

    /**
     * 与飞行器断开的回调
     */
    public void onWifiBreakInBackgroundCallBack() {

    }

    /**
     * 与飞行器断开的回调
     */
    protected void onWifiDisconnectCallBack() {
        if (NetworkUtils.is4G()) {
            onOtherNetConnetedCallBack();

        }
    }

    /**
     * 刷新wifi信号强弱的回调
     */
    protected void onWifiRssiChangedCallBack(int wifiRssi) {

    }

    /**
     * 与其他wifi连上的回调
     */
    protected void onOtherWifiConnetedCallBack() {
        onOtherNetConnetedCallBack();
    }

    /**
     * 与其他网络连接
     */
    protected void onOtherNetConnetedCallBack() {
        LogUtils.d("与其他网络连接上");
    }

    @Override
    protected void onDestroy() {
        if (mPresenter != null) {
            mPresenter.detachView();
        }
        if (isUseMavlinkConnect()) {
            ConnectManager.getInstance().close8686();
        }
        super.onDestroy();
    }

    @Override
    public Activity getPageActivity() {
        return this;
    }

    /**
     * 创建ActivityModule
     */
    protected ActivityModule getActivityModule() {
        return new ActivityModule(this);
    }

    /**
     * 初始化注入
     */
    protected abstract void initInject();


    /**
     * 与无人机连接成功
     */
    @Override
    protected void planeConnected(ProductModel productModel) {
        super.planeConnected(productModel);
        LogUtils.d("飞机类型 " + productModel.productType);
        if (isUseMavlinkConnect()) {
            connectMavlinkTcp(this, productModel);
        }
        UserBean bean = DBClient.findObjByColumn(UserBean.class, UserBean.COLUMNNAME_ID, 0);
        if (bean != null) {
            PlaneInfo info = new PlaneInfo();
            info.setWifiName(NetworkUtils.getCurConnetWifiName());
            info.setUser_id(bean);
            if (DBClient.findObjById(PlaneInfo.class, info.getWifiName()) == null) {
                DBClient.addObject(info);
//                bean.setPlane(info);
            } else {
                DBClient.updateObject(info);
            }
        }
//        planeConnected(productModel);
    }

//    protected void planeConnected(ProductModel productModel) {
//    }


//    @Override
//    protected void onPlaneDisConnected() {
//        super.onPlaneDisConnected();
//        if (isUseMavlinkConnect()) {
//            ConnectManager.getInstance().close8686();
//        }
//        //保证主线程
//        RxLoopSchedulers.composeMain(this, 0)
//                .subscribe(new RxLoopObserver<Integer>() {
//                    @Override
//                    public void onNext(Integer integer) {
//                        super.onNext(integer);
//                        this.disposeDisposables();
//                        planeDisConnected();
//                    }
//                });
//    }

    protected void planeDisConnected() {
        super.planeDisConnected();
//        if (isUseMavlinkConnect()) {
//            ConnectManager.getInstance().close8686();
//        }
//        if (NetworkUtils.isAvailableByPing(ConstantFields.PANORACMIC_SHARE_PARAMS.TAG_IP)){
//            LogUtils.d("网络可用 ");
//        }
    }
}
