package com.feipai.flypai.ui.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.feipai.flypai.R;
import com.feipai.flypai.api.CameraCommandCallback;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.app.FlyPieApplication;
import com.feipai.flypai.base.BaseMvpActivity;
import com.feipai.flypai.base.BaseSimpleFragment;
import com.feipai.flypai.beans.FileBean;
import com.feipai.flypai.beans.PlaneVersionBean;
import com.feipai.flypai.beans.ProductModel;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.beans.SettingBean;
import com.feipai.flypai.connect.ConnectManager;
import com.feipai.flypai.mvp.contract.activitycontract.MainContract;
import com.feipai.flypai.mvp.presenters.activitypresenters.MainPresenter;
import com.feipai.flypai.ui.fragments.MainFragment;
import com.feipai.flypai.ui.fragments.UserFragment;
import com.feipai.flypai.ui.view.BottomBar;
import com.feipai.flypai.ui.view.BottomBarTab;
import com.feipai.flypai.utils.CameraCommand;
import com.feipai.flypai.utils.GeneralFactory;
import com.feipai.flypai.utils.global.HandlerUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.NetworkUtils;
import com.feipai.flypai.utils.global.RxBusUtils;
import com.feipai.flypai.utils.global.ToastUtils;
import com.feipai.flypai.utils.global.Utils;
import com.feipai.flypai.utils.socket.DataSocketReadListener;
import com.feipai.flypai.utils.socket.TcpCloseListener;
import com.gyf.immersionbar.ImmersionBar;
import com.hwangjr.rxbus.RxBus;

import java.util.List;

import me.yokeyword.fragmentation.SupportFragment;

import static com.feipai.flypai.app.ConstantFields.BusEventType.UPDATE_VERSION_VALUE;
import static com.feipai.flypai.app.ConstantFields.BusEventType.WIFI_RSSI_CHANGE;


public class MainActivity extends BaseMvpActivity<MainPresenter> implements MainContract.View {

    BottomBar mBottomBar;
    // 再点一次退出程序时间设置
    private long TOUCH_TIME = 0;

    private BaseSimpleFragment[] fragments = new BaseSimpleFragment[3];
    public static final int USER = 0;
    public static final int MAIN = 1;
    private PlaneVersionBean versionBean = new PlaneVersionBean();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ConstantFields.MESSAGE_WHAT.UPDATE_VERSION:
                    updateVersion();
                    break;
                case ConstantFields.MESSAGE_WHAT.SWITCH_MAIN_TAB:
                    //强制升级
//                    switchBottomBar((int) msg.obj);
                    break;
            }
        }
    };

    @Override
    public void sendHandlerMessage(int what, Object obj) {
        if (mHandler != null) {
            Message msg = new Message();
            msg.what = what;
            msg.obj = obj;
            mHandler.sendMessage(msg);
        }
    }

    @Override
    protected void initWindow() {
        super.initWindow();
        ImmersionBar.with(this)
                .navigationBarColor(R.color.color_ffffff) //导航栏颜色，不写默认黑色
                .navigationBarDarkIcon(true) //导航栏图标是深色，不写默认为亮色
                .statusBarDarkFont(true)//状态栏字体是深色，不写默认为亮色
                .titleBarMarginTop(R.id.main_top_view)
                .init();

    }


    @Override
    protected void restartApp() {
    }


    @Override
    protected int initLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initInject() {
        mPresenter = new MainPresenter(FlyPieApplication.getInstance().getAppComponent().retrofitHelper());
        mPresenter.attachView(this);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mBottomBar = findViewById(R.id.bottombar);
        BaseSimpleFragment mainFragment = findFragment(MainFragment.class);
        if (mainFragment == null) {
            fragments[USER] = new UserFragment();
            fragments[MAIN] = new MainFragment();

            loadMultipleRootFragment(R.id.fly_container, MAIN,
                    fragments[USER],
                    fragments[MAIN]);
        } else {
            // 这里库已经做了Fragment恢复,所有不需要额外的处理了, 不会出现重叠问题
            // 这里我们需要拿到mFragments的引用
            fragments[USER] = findFragment(UserFragment.class);
            fragments[MAIN] = mainFragment;
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
    }

    @Override
    protected void initListener() {
        super.initListener();

        mBottomBar.addItem(new BottomBarTab(this, R.drawable.user_tab_selcetor, R.mipmap.user_tab_img_sele, getResources().getString(R.string.flypai)))
                .addItem(new BottomBarTab(this, R.drawable.main_tab_selcetor, R.mipmap.main_tab_img_sele, getResources().getString(R.string.uav)));

        mBottomBar.setOnTabSelectedListener(new BottomBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position, int prePosition) {
                /**按钮切换*/
                LogUtils.d(TAG, "从" + prePosition + "切换到" + position);
                final SupportFragment currentFragment = fragments[position];
                showHideFragment(fragments[position], fragments[prePosition]);
                if ((currentFragment instanceof UserFragment) && versionBean != null) {
                    ((UserFragment) currentFragment).initVersionBean(versionBean);
                }
            }

            @Override
            public void onTabUnselected(int position) {
                LogUtils.d(TAG, "onTabUnselected" + position);

            }

            @Override
            public void onTabReselected(int position) {
                LogUtils.d(TAG, "onTabReselected" + position);

            }
        });
        switchBottomBar(1);
    }

    @Override
    protected void initRxbusListener(RxbusBean msg) {
        super.initRxbusListener(msg);
        mPresenter.handlerEvent(msg);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startNotifyWIFI();
        if (Utils.isDebug)
            mHandler.post(runnable);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            //每隔一秒刷新一次wifi列表
            RxBusUtils.getDefault().post(new RxbusBean(ConstantFields.BusEventType.REFRESH_WIFI_DATA, 1));
            if (mHandler != null)
                mHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if (mHandler != null) {
            mHandler.removeCallbacks(runnable);
        }
//        versionBean.initVer();
        FlyPieApplication.getInstance().setDataTcpSuccess(false);
        mPresenter.resetUpdateFirm();
        stopNotifyWIFI();
    }

    @Override
    protected boolean isUseRxBus() {
        return true;
    }


    //    @OnClick({R.id.text})
    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.text:
                RxBus.get().post("onClick");
                break;
        }
    }


    @Override
    public void switchBottomBar(int tab) {
        mBottomBar.setCurrentItem(tab);
    }

    public int getBottomBarIndex() {
        return mBottomBar.getCurrentItemPosition();
    }

    @Override
    protected void onWifiConnectedCallBack(String ssid) {
        super.onWifiConnectedCallBack(ssid);
//        RxBusUtils.getDefault().post(new RxbusBean(WIFI_CONNTENTED, ssid));
    }

    @Override
    public void onWifiBreakInBackgroundCallBack() {
        super.onWifiBreakInBackgroundCallBack();

//        if (versionBean != null) {
//            versionBean.initVer();
//            updateVersion();
//        }
//        RxBusUtils.getDefault().post(new RxbusBean(WIFI_BREAK_IN_BACKGRAOUND, 1));
    }

    @Override
    protected void onWifiDisconnectCallBack() {
        super.onWifiDisconnectCallBack();
//        RxBusUtils.getDefault().post(new RxbusBean(WIFI_DISCONNECT, 1));
    }

    @Override
    protected void onWifiRssiChangedCallBack(int wifiRssi) {
        super.onWifiRssiChangedCallBack(wifiRssi);
        RxBusUtils.getDefault().post(new RxbusBean(WIFI_RSSI_CHANGE, wifiRssi));
    }

    @Override
    protected void onOtherWifiConnetedCallBack() {
        super.onOtherWifiConnetedCallBack();
        RxBusUtils.getDefault().post(new RxbusBean(WIFI_RSSI_CHANGE, 1));
    }

    @Override
    protected void onOtherNetConnetedCallBack() {
        super.onOtherNetConnetedCallBack();
        LogUtils.d("开始获取服务器版本号");
        mPresenter.requestAllVersionFromServer();
    }

    @Override
    protected void planeConnected(ProductModel productModel) {
        super.planeConnected(productModel);
        //初始化版本号，再去获取服务器版本号
        if (versionBean != null) versionBean.initVer();
        LogUtils.d("与飞行器连接上");
        mPresenter.checkPlaneFwVersion(ConstantFields.UPGRADE_FW.CHECK_VER);
        CameraCommand.getCmdInstance().getAllSetting(this, new CameraCommandCallback<SettingBean>() {
            @Override
            public void onComplete(SettingBean bean) {
                if (bean.getRval() == 0) {
                    LogUtils.d("相机------>获取设置项" + bean.getSettingParamBean().toString());
                    SettingBean.ParamBean settingParamBean = bean.getSettingParamBean();
                    getCmd().putSettingStatus(settingParamBean);
                    getVersionBean().setLocalCameraVersion(settingParamBean.getVersion());
                    sendHandlerMessage(ConstantFields.MESSAGE_WHAT.UPDATE_VERSION, null);
                    LogUtils.d("相机需要升级===>" + getVersionBean().isCamereNeedUpgrade());
                }
            }
        });
    }

    @Override
    protected void planeDisConnected() {
        super.planeDisConnected();
        mPresenter.resetUpdateFirm();
        if (versionBean != null) {
            versionBean.initVer();
            updateVersion();
        }
        if (NetworkUtils.is4G()) {
            mPresenter.requestAckForServer();
        }
//        if (dataSocket != null)
//            dataSocket.startSendFile(false);
    }

    public CameraCommand getCmd() {
        return cmd;
    }

    public PlaneVersionBean getVersionBean() {
        return versionBean;
    }

    @Override
    public void updateVersion() {
        if (versionBean.isAppNeedUpgrade() || (ConnectManager.getInstance().isConneted() && (versionBean.isCamereNeedUpgrade() || versionBean.isPlaneNeedUpgrade() || versionBean.isYuntaiNeedUpgrade()))) {
            mBottomBar.getItem(0).setRedDotViewVis(View.VISIBLE);
        } else {
            mBottomBar.getItem(0).setRedDotViewVis(View.GONE);
        }
        LogUtils.d("更新版本号" + versionBean.toString());
        RxBusUtils.getDefault().post(new RxbusBean(UPDATE_VERSION_VALUE, versionBean));
    }

    @Override
    public void connectCameraDataTcp() {
        connectDataSocket(new DataSocketReadListener() {
            @Override
            public void read(List<FileBean> fbs, int index, byte[] buffer) {
                mPresenter.readCameraDataSocket(buffer);
            }


            @Override
            public void close() {
                LogUtils.d("主页面 cameraDateSocket服务器断开");
                if (cameraDataCloseListener != null) {
                    HandlerUtils.runOnUiThread(() -> cameraDataCloseListener.close());
                }
            }

            @Override
            public void connecSuccess() {
                if (cameraDataCloseListener != null) {
                    HandlerUtils.runOnUiThread(() -> cameraDataCloseListener.success());
                }
            }

            @Override
            public void connectFail() {

            }

            @Override
            public void uploadLisenter(int progress) {
                // TODO: 2019/4/18 上传文件回调
                if (FlyPieApplication.getInstance().isDataTcpSuccess()) {
                    LogUtils.d("上传camera bin进度---》" + progress);
                    RxBusUtils.getDefault().post(new RxbusBean(ConstantFields.UPGRADE_FW.UPDATE_CAMERA_PROGRESS, progress));
                }

            }
        });
    }

    @Override
    public void sendFilePrepare() {
        mPresenter.sendFilePrepare();
    }

    @Override
    public void sendFileStart() {
        dataSocket.startSendFile(true);
        dataSocket.sendFile(GeneralFactory.getLocalCamerBinPath());
    }

    @Override
    public void sendFileComplete() {
        dataSocket.startSendFile(false);
        dataSocket.closeAll(0);
        LogUtils.d("相机固件上传完成，重置cameraDataSocket");
        RxBusUtils.getDefault().post(new RxbusBean(ConstantFields.UPGRADE_FW.UPDATE_CAMERA_SUCCESS, 100));
//        cmd.cameraRestart();
    }

    @Override
    public void uploadPlaneFw(int porgress) {
        // TODO: 2019/4/19 更新飞控上传进度
        RxBusUtils.getDefault().post(new RxbusBean(ConstantFields.UPGRADE_FW.UPDATE_PLANE_PROGRESS, porgress));
    }

    @Override
    public void uploadYuntaiFw(int porgress) {
        RxBusUtils.getDefault().post(new RxbusBean(ConstantFields.UPGRADE_FW.UPDATE_YUNTAI_PROGRESS, porgress));
    }

    private TcpCloseListener cameraDataCloseListener = new TcpCloseListener() {
        @Override
        public void close() {
            FlyPieApplication.getInstance().setDataTcpSuccess(false);
            if (isFinishing())
                return;
            LogUtils.d("主页面 cameraDateSocket=====>重连失败");
        }

        @Override
        public void success() {
            FlyPieApplication.getInstance().setDataTcpSuccess(true);
            if (isFinishing())
                return;
            LogUtils.d("主页面 cameraDateSocket 8787 端口连接成功");
            cmd.connTcp(cameraSocket);
        }
    };


    @Override
    public void onBackPressedSupport() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            pop();
        } else {
            if (System.currentTimeMillis() - TOUCH_TIME < ConstantFields.APP_CONFIG.WAIT_TIME * 2) {
                finish();
                System.exit(0);
            } else {
                TOUCH_TIME = System.currentTimeMillis();
                ToastUtils.showShortToast(R.string.press_again_exit);
            }
        }
    }
}
