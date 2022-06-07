package com.feipai.flypai.base;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.feipai.flypai.R;
import com.feipai.flypai.api.NotifyMessage;
import com.feipai.flypai.api.NotifyMessagers;
import com.feipai.flypai.app.FlyPieApplication;
import com.feipai.flypai.beans.NotifyMessageMode;
import com.feipai.flypai.beans.ProductModel;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.connect.ConnectManager;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.ui.activity.SplashActivity;
import com.feipai.flypai.ui.view.ActionDialog;
import com.feipai.flypai.utils.AppStatusManager;
import com.feipai.flypai.utils.CameraCommand;
import com.feipai.flypai.utils.MLog;
import com.feipai.flypai.utils.global.HandlerUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.RxBusUtils;
import com.feipai.flypai.utils.socket.CameraDataSocket;
import com.feipai.flypai.utils.socket.CameraSocket;
import com.feipai.flypai.utils.socket.DataSocketReadListener;
import com.feipai.flypai.utils.socket.MavLinkSocket;
import com.feipai.flypai.utils.socket.MavlinkObserver;
import com.feipai.flypai.utils.wifimanger.NetChangeObserver;
import com.feipai.flypai.utils.wifimanger.WifiReceiver;
import com.hwangjr.rxbus.RxBus;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.RxLifecycle;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.android.RxLifecycleAndroid;
import com.zhy.autolayout.AutoFrameLayout;
import com.zhy.autolayout.AutoLinearLayout;
import com.zhy.autolayout.AutoRadioGroup;
import com.zhy.autolayout.AutoRelativeLayout;
import com.zhy.autolayout.config.AutoLayoutConifg;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;
import me.yokeyword.fragmentation.SupportActivity;
import me.yokeyword.fragmentation.anim.DefaultHorizontalAnimator;

/**
 * 普通(MVC)模式下的Activity的抽取
 *
 * @author yanglin
 */
public abstract class BaseSimpleActivity extends SupportActivity implements LifecycleProvider<ActivityEvent>, ConnectManager.ConnectManagerCallback {

    private static final String LAYOUT_LINEARLAYOUT = "LinearLayout";
    private static final String LAYOUT_FRAMELAYOUT = "FrameLayout";
    private static final String LAYOUT_RELATIVELAYOUT = "RelativeLayout";
    private static final String RADIO_GROUP = "RadioGroup";

    Rect frame;
    int statusBarHeight;
    // 日志Tag
    protected String TAG = getClass().getSimpleName();
    // ButterKnife
    protected Unbinder mUnBinder;
    // RxLifecycle
    protected final BehaviorSubject<ActivityEvent> mLifecycleSubject = BehaviorSubject.create();
    // 权限管理
    protected RxPermissions mRxPermissions;

    protected KProgressHUD mD;

    protected NetChangeObserver mObserver;

    protected CameraCommand cmd = CameraCommand.getCmdInstance();

    protected CameraSocket cameraSocket = CameraSocket.getInstance();
    protected CameraDataSocket dataSocket = CameraDataSocket.getInstance();
    protected MavLinkSocket mavLinkSocket = MavLinkSocket.getInstance();
    private ActionDialog mActionD;

    String datas = "";


    //  ===== todo:此段代码的作用？  yanglin:适配autolayout =====//
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        View view = null;

        if (isUseAutoSize()) {
            if (name.equals(LAYOUT_FRAMELAYOUT)) {
                view = new AutoFrameLayout(context, attrs);
            }

            if (name.equals(LAYOUT_LINEARLAYOUT)) {
                view = new AutoLinearLayout(context, attrs);
            }

            if (name.equals(LAYOUT_RELATIVELAYOUT)) {
                view = new AutoRelativeLayout(context, attrs);
            }
            if (name.equals(RADIO_GROUP)) {
                view = new AutoRadioGroup(context, attrs);
            }
        }

        if (view != null) {
            return view;
        }
        return super.onCreateView(name, context, attrs);
    }
    //  ===== todo:此段代码的作用？ =====//

    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 初始化Window
        super.onCreate(savedInstanceState);
//        LogUtils.d("重启APP=====11》" + isUseLandscape());
        if (frame == null && isUseAutoSize()) {
//            LogUtils.d("重启APP=====》" + isUseLandscape());
            frame = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            statusBarHeight = frame.top;
            AutoLayoutConifg.getInstance().init(this, frame, isUseLandscape());
        }
        setContentView(initLayout());
        switch (AppStatusManager.getInstance().getAppStatus()) {
            case AppStatusManager.STATUS_FORCE_KILLED:
                MLog.log("非正常重启APP");
                restartApp();
                break;
            case AppStatusManager.STATUS_NORMAL:
                MLog.log("正常启动APP");
                break;
        }
        if (isUseRxBus()) {
            RxBus.get().register(this);
        }
        if (isUseButterKnife()) {
            //activity中不需要解绑，其他非ac页面需要
            mUnBinder = ButterKnife.bind(this);
        }
        // 权限请求
        mRxPermissions = new RxPermissions(this);
        // Rx生命周期管理
        mLifecycleSubject.onNext(ActivityEvent.CREATE);
        // 初始化View
        initView(savedInstanceState);
        // 绑定activity
//        mUnBinder = ButterKnife.bind(this);
        // 初始化数据
        initData(savedInstanceState);
        // 初始化监听器
        initListener();
        if (isRegisterNotifyReceiver()) {
            //注册全局消息接收者
            registerNotifyReceiver();
        }
    }

    /***这一块暂时应该是不需要，后续发现问题再做更改*/
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        if (frame != null) AutoLayoutConifg.getInstance().init(this, frame, isUseLandscape());
//        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            LogUtils.d(TAG, "横屏");
//        }
//        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            LogUtils.d(TAG, "竖屏");
//        }
//        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
//        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
//        }
//    }
    protected void initNetChangeObserver(NetChangeObserver observer) {
        this.mObserver = observer;
//        //开启广播去监听 网络 改变事件
        WifiReceiver.registerObserver(mObserver);
    }

    protected void restartApp() {
        this.finish();
        startActivity(new Intent(this, SplashActivity.class));
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.putExtra(AppStatusManager.KEY_HOME_ACTION, AppStatusManager.ACTION_RESTART_APP);
//        startActivity(intent);
    }


    /**
     * 是否使用RxBus
     */
    protected boolean isUseRxBus() {
        return false;
    }

    /**
     * 是否使用注解，由于之前的页面为使用，
     * 目前初始为不使用，使用则自行返回true,
     * 建议注册在activity中
     */
    protected boolean isUseButterKnife() {
        return false;
    }

    /**
     * 是否使用AutoSize 布局
     */
    protected boolean isUseAutoSize() {
        return true;
    }

    /**
     * 是否连接mavlink 8686端口
     */
    protected boolean isUseMavlinkConnect() {
        return false;
    }

    /**
     * 是否是横屏，默认是竖屏，使用AutoLayout横屏重写
     * 避免锁屏，横竖屏切换执行多次
     */
    protected boolean isUseLandscape() {
        return false;
    }

    /**
     * 初始化布局
     */
    protected abstract int initLayout();


    /**
     * 是否注册全局消息接收者
     */
    protected boolean isRegisterNotifyReceiver() {
        return false;
    }

    protected void registerNotifyReceiver() {
        NotifyMessagers.getInstance().addNotifyMessageQueue(new NotifyMessage() {
            @Override
            public void readMessage(Object obj) {
                super.readMessage(obj);
                if (obj instanceof NotifyMessageMode) {
                    subscribeNotify((NotifyMessageMode) obj);
                }
            }
        });
    }

    protected void subscribeNotify(NotifyMessageMode msg) {

    }

    /**
     * 初始化Window
     */
    protected void initWindow() {
        setFragmentAnimator(new DefaultHorizontalAnimator());
    }

    /**
     * 初始化View
     */
    protected void initView(@Nullable Bundle savedInstanceState) {

    }

    /**
     * 初始化监听器
     */
    protected void initListener() {
        RxBusUtils.getDefault().toFlowable(RxbusBean.class)
                .observeOn(AndroidSchedulers.mainThread())
                .filter(event -> !isFinishing())
                .doOnNext(event -> {
                    initRxbusListener(event);
                }).subscribe();

    }


    /**
     * 初始化Rxbus监听器
     */
    protected void initRxbusListener(RxbusBean msg) {

    }


    /**
     * 初始化数据
     */
    protected void initData(Bundle savedInstanceState) {

    }

    protected void showKpDialog(String text) {
        if (mD == null) {
            mD = KProgressHUD.create(this)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setMaxProgress(100);
        }
        if (mD != null) {
            mD.setDetailsLabel(text);
        }
        if (!mD.isShowing() && !this.isFinishing()) {
            mD.show();
        }
    }

    protected void dismissKpDialog() {
        if (mD != null && mD.isShowing()) {
            mD.dismiss();
        }
    }

    @Override
    @CallSuper
    protected void onStart() {
        super.onStart();
        // RxLifecycle
        mLifecycleSubject.onNext(ActivityEvent.START);
//        LogUtils.d(TAG, "onStart: ");
    }

    @Override
    @CallSuper
    protected void onStop() {
        // RxLifecycle
        mLifecycleSubject.onNext(ActivityEvent.STOP);
        super.onStop();
//        LogUtils.d(TAG, "onStop: ");

    }

    @Override
    @CallSuper
    protected void onResume() {
        super.onResume();
        initWindow();
        // RxLifecycle
        mLifecycleSubject.onNext(ActivityEvent.RESUME);
//        LogUtils.d(TAG, "onResume: ");

        // 开启连接监听
        ConnectManager.getInstance().addConnectionListioner(this);
    }

    protected void startNotifyWIFI() {
        WifiReceiver.registerNetworkStateReceiver(this);
    }

    protected void stopNotifyWIFI() {
        WifiReceiver.unRegisterNetworkStateReceiver(this);
    }

    @Override
    @CallSuper
    protected void onPause() {
        // RxLifecycle
        mLifecycleSubject.onNext(ActivityEvent.PAUSE);
        // 结束连接监听
        ConnectManager.getInstance().removeConectionListioner(this);
//        FlyPieApplication.getInstance().setTcpSuccess(false);
//        FlyPieApplication.getInstance().setDataTcpSuccess(false);
//        FlyPieApplication.getInstance().setMavSocketSuccess(false);
//        if (mavLinkSocket != null) {
//            mavLinkSocket.closeAll(0);
//        }
//        if (cameraSocket != null) {
//            cameraSocket.closeAll(0);
//            cameraSocket.removeReadListener();
//        }
//        cmd.setToken(-1);
        super.onPause();
//        LogUtils.d(TAG, "onPause: ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (frame != null) {
            LogUtils.d(TAG, " --->onRestart " + frame.bottom + "||" + frame.right);
            AutoLayoutConifg.getInstance().init(this, frame, isUseLandscape());
        }
    }

    @Override
    @CallSuper
    protected void onDestroy() {
        mLifecycleSubject.onNext(ActivityEvent.DESTROY);
        super.onDestroy();
        dismissKpDialog();
        if (isUseRxBus()) {
            RxBus.get().unregister(this);
        }
        LogUtils.d(TAG, "onDestroy: ");
    }


    @Override
    @NonNull
    @CheckResult
    public final Observable<ActivityEvent> lifecycle() {
        return mLifecycleSubject.hide();
    }

    @Override
    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindUntilEvent(@NonNull ActivityEvent event) {
        return RxLifecycle.bindUntilEvent(mLifecycleSubject, event);
    }

    @Override
    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindToLifecycle() {
        return RxLifecycleAndroid.bindActivity(mLifecycleSubject);
    }


//    protected void connectTcp() {
//        if (!FlyPieApplication.getInstance().isTcpSuccess()) {
//            cameraSocket.bindSendStick();
//            cameraSocket.setReadListener(new CameraSocketReadListener() {
//                @Override
//                public void read(int bufferSize, Object buffer) {
////                    parsingPackage((String) buffer);
//                }
//
//                @Override
//                public void close() {
//                    LogUtils.d("BaseSimpleActivity camerasocket断开");
//                    HandlerUtils.runOnUiThread(() -> tcpCloseListener.close());
//                }
//
//                @Override
//                public void connecSuccess() {
//                    HandlerUtils.runOnUiThread(() -> tcpCloseListener.success());
//                }
//
//                @Override
//                public void connectFail() {
//
//                }
//            });
//        }
//    }


//    private TcpCloseListener tcpCloseListener = new TcpCloseListener() {
//        @Override
//        public void close() {
//            FlyPieApplication.getInstance().setTcpSuccess(false);
//            if (isFinishing())
//                return;
//            LogUtils.d("BaseSimpleActivity界面=====>重连失败");
//            socketClose();
//        }
//
//        @Override
//        public void success() {
//            FlyPieApplication.getInstance().setTcpSuccess(true);
//            if (isFinishing())
//                return;
//            LogUtils.d("7878 端口连接成功");
//            cmd.connTcp(cameraSocket);
//            cmd.startSession(new CameraCommandCallback() {
//                @Override
//                public void onComplete(String data) {
//                    ABCmdValue cb = MGson.newGson().fromJson(data, ABCmdValue.class);
//                    if (cb.getMsg_id() == ConstantFields.CAMERA_CONFIG.START_SESSION) {
//                        if (cb.getRval() == 0) {
//                            cmd.setToken((int) (double) cb.getParam());
//                            LogUtils.d("读取startSession返回" + cmd.getToken());
////                            if (FlyPieApp.getInstance().rigthWifiName != null && FlyPieApp.getInstance().rigthWifiName.startsWith(FlyingFactory.FP2K)) {
////                                cmd.setFlyType(FlyingFactory.FLY_2K);
////                            } else if (FlyPieApp.getInstance().rigthWifiName != null && FlyPieApp.getInstance().rigthWifiName.startsWith(FlyingFactory.FP6K)) {
////                                cmd.setFlyType(FlyingFactory.FLY_6K);
////                            } else {
////                                cmd.setFlyType(FlyingFactory.FLY_4K);
////                            }
//                            abSessionSuccess(true, cmd.getFlyType());
//                        } else if (cb.getRval() == -3) {
//                            /**强占*/
//                        }
//                    }
//                }
//            });
//        }
//    };

//    protected void socketClose() {
//        LogUtils.d("MainSocket关闭");
//        cmd.setToken(-1);
////        if (dataSocket != null)
////            dataSocket.closeAll(0);
//
//    }

    protected void connectDataSocket(DataSocketReadListener listener) {
        if (!FlyPieApplication.getInstance().isDataTcpSuccess()) {
            LogUtils.d("cameraDateSocket连接DATASOCKET");
//            dataSocket.connect(5 * 1000);
//            dataSocket.setReadListener(listener);
        }
    }


    // 该回调函数说明 相机7878端口启动成功 start session成功。8686端口成功
    @Override
    public void onConnected(ProductModel productModel) {
        CameraCommand.getCmdInstance().clearCommands();
        HandlerUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                planeConnected(productModel);
            }
        });

    }

    @Override
    public void onDisConnected() {
        HandlerUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                planeDisConnected();
            }
        });
    }

    @Override
    public void onConnectionUsed() {
        // 被占用提示
        if (mActionD == null) {
            mActionD = new ActionDialog(this);
        }
        mActionD.showWithTitle(false, ResourceUtils.getString(R.string.wifi_occupied),
                ResourceUtils.getString(R.string.wifi_occupied_disconnect_other_connections), null,
                ResourceUtils.getString(R.string.confirm), (int action) -> {
                }, null);

    }

    public final void connectMavlinkTcp(BaseView mv, ProductModel productModel) {
        ConnectManager.getInstance().connectTo8686(mv, new MavlinkObserver<BaseMavlinkEntity>() {
            @Override
            public void onRead(BaseMavlinkEntity mavMsg) {
                planeMavLinkRead(mavMsg);
            }

            @Override
            public void connecSuccess() {
                super.connecSuccess();
                planeMavLinkConnected();
            }
        });
    }


    // ======= 子类集成如下两个方法监控连接状态 ======
    // 与飞行器相机端口7878,8787连接上;
    protected void planeConnected(ProductModel productModel) {
    }

    // 与飞机主端口 7878断开
    protected void planeDisConnected() {
        if (isUseMavlinkConnect())
            ConnectManager.getInstance().close8686();
    }

    /**
     * 与8686端口连接上,
     * isUseMavlinkConnect==true时，子类重写
     */
    protected void planeMavLinkConnected() {
    }

    /**
     * 读取8686端口数据，
     * isUseMavlinkConnect==true时，子类重写
     */
    protected void planeMavLinkRead(BaseMavlinkEntity mavMsg) {
    }

}