package com.feipai.flypai.base;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.feipai.flypai.R;
import com.feipai.flypai.api.NotifyMessage;
import com.feipai.flypai.api.NotifyMessagers;
import com.feipai.flypai.beans.NotifyMessageMode;
import com.feipai.flypai.beans.ProductModel;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.connect.ConnectManager;
import com.feipai.flypai.utils.CameraCommand;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.RxBusUtils;
import com.hwangjr.rxbus.RxBus;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.RxLifecycle;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.trello.rxlifecycle2.android.RxLifecycleAndroid;
import com.zhy.autolayout.utils.AutoUtils;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;
import me.yokeyword.fragmentation.SupportFragment;
import me.yokeyword.fragmentation.anim.DefaultHorizontalAnimator;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

import static com.feipai.flypai.app.ConstantFields.BusEventType.WIFI_CONNTENTED;

/**
 * 普通(MVC)模式下的Fragment的抽取
 *
 * @author yanglin
 */
public abstract class BaseSimpleFragment extends SupportFragment implements LifecycleProvider<FragmentEvent>, ConnectManager.ConnectManagerCallback {

    // 日志TAG
    protected String TAG = getClass().getSimpleName();
    // 上下文
    protected Activity mHostActivity;
    // ButterKnife
    protected Unbinder mUnBinder;
    // RxLifecycle
    protected final BehaviorSubject<FragmentEvent> mLifecycleSubject = BehaviorSubject.create();
    // 根View
    protected ViewGroup mRootView;
    // 权限管理
    protected RxPermissions mRxPermissions;

    protected KProgressHUD mD;

    protected CameraCommand cmd = CameraCommand.getCmdInstance();

    private boolean isLazyLayoutFirst;

    @Override
    @CallSuper
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mLifecycleSubject.onNext(FragmentEvent.ATTACH);
    }

    @Override
    @CallSuper
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 权限请求
        mRxPermissions = new RxPermissions(this);
        // RxLifecycle
        mLifecycleSubject.onNext(FragmentEvent.CREATE);
        if (isUseRxBus()) {
            RxBus.get().register(this);
        }
        this.mHostActivity = getActivity();
//        LogUtils.d(TAG, "onCreate: ");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.view_content_container, null);
        if (!isLazyLoadView()) {
            initView();
            // 初始化监听器
            initListener();
            // 初始化数据
            initData();
        }
        AutoUtils.auto(mRootView);
//        LogUtils.d(TAG, "onCreateView: " + isLazyLoadView());
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        LogUtils.d(TAG, "onActivityCreated: ");
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        // todo 为撒要这样写？   yanglin：这个方法有备注
        if (isLazyLoadView()) {
            // 初始化View
            initView();
            isLazyLayoutFirst = true;//此方法第一次执行时，会在onSupportVisible前面执行
            registerConnectCallback();
            // 初始化监听器
            initListener();
            // 初始化数据
            initData();
        }
    }

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

    protected void registerConnectCallback() {
        if (isLazyLoadView()) {
            if (isLazyLayoutFirst) {
//                LogUtils.d("页面注册监听...");
                ConnectManager.getInstance().addConnectionListioner(this);
            }
        } else {
            ConnectManager.getInstance().addConnectionListioner(this);
        }

    }

    @Override
    @CallSuper
    public void onDestroy() {
        mLifecycleSubject.onNext(FragmentEvent.DESTROY);
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy: ");
    }

    @Override
    @CallSuper
    public void onDestroyView() {
        mLifecycleSubject.onNext(FragmentEvent.DESTROY_VIEW);
        super.onDestroyView();
        if (isUseButterKnife() && mUnBinder != null) {
            mUnBinder.unbind();
        }
        if (isUseRxBus()) {
            RxBus.get().unregister(this);
        }
        LogUtils.d(TAG, "onDestroyView: ");
    }

    /**
     * 查找子View
     */
    protected <T extends View> T findViewById(int id) {
        if (mRootView == null) {
            mRootView = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.view_content_container, null);
        }
        return mRootView.findViewById(id);
    }

    /**
     * 是否使用RxBus
     */
    protected boolean isUseRxBus() {
        return false;
    }

    protected boolean isUseButterKnife() {
        return false;
    }


    /**
     * 是否使用懒加载，默认使用
     * 懒加载对于viewpager预加载无效
     */
    protected boolean isLazyLoadView() {
        return true;
    }

    /**
     * 初始化根布局
     */
    protected abstract int initLayout();

    /**
     * 初始化View
     */
    protected void initView() {
        // 内容容器
        FrameLayout contentContainer = findViewById(R.id.fl_content_container);
        // 内容View
        contentContainer.addView(LayoutInflater.from(mHostActivity).inflate(initLayout(), null));
        // 绑定fragment
        if (isUseButterKnife())
            mUnBinder = ButterKnife.bind(this, mRootView);
        if (isRegisterNotifyReceiver()) {
            //注册全局消息接收者
            registerNotifyReceiver();
        }
    }


    /**
     * 初始化监听器
     */
    protected void initListener() {
        if (isUseRxBus()) {
            RxBusUtils.getDefault().toFlowable(RxbusBean.class)
                    .observeOn(AndroidSchedulers.mainThread())
                    .filter(event -> isVisible())
                    .doOnNext(event -> {
                        initRxbusListener(event);
                    }).subscribe();
        }
    }

    /**
     * Rxbus监听器
     */
    protected void initRxbusListener(RxbusBean msg) {
        if (msg.TAG.equals(WIFI_CONNTENTED)) {
            LogUtils.d("在fragment中设置对应飞行器的CameraCmd");
            cmd.setFlyType((String) msg.object);
        }
    }

    /**
     * 初始化数据
     */
    protected void initData() {

    }

    @Override
    public FragmentAnimator onCreateFragmentAnimator() {
        return new DefaultHorizontalAnimator();
    }

    @Override
    @NonNull
    @CheckResult
    public final Observable<FragmentEvent> lifecycle() {
        return mLifecycleSubject.hide();
    }

    @Override
    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindUntilEvent(@NonNull FragmentEvent event) {
        return RxLifecycle.bindUntilEvent(mLifecycleSubject, event);
    }

    @Override
    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindToLifecycle() {
        return RxLifecycleAndroid.bindFragment(mLifecycleSubject);
    }

    @Override
    @CallSuper
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLifecycleSubject.onNext(FragmentEvent.CREATE_VIEW);
    }

    @Override
    @CallSuper
    public void onStart() {
        super.onStart();
        mLifecycleSubject.onNext(FragmentEvent.START);
    }

    @Override
    @CallSuper
    public void onResume() {
        super.onResume();
//        LogUtils.d(TAG,"onResume");
        mLifecycleSubject.onNext(FragmentEvent.RESUME);
        registerConnectCallback();
    }

    @Override
    @CallSuper
    public void onPause() {
        mLifecycleSubject.onNext(FragmentEvent.PAUSE);
//        LogUtils.d(TAG,"onPause");
        super.onPause();
        ConnectManager.getInstance().removeConectionListioner(this);
    }

    @Override
    @CallSuper
    public void onStop() {
        mLifecycleSubject.onNext(FragmentEvent.STOP);
        super.onStop();
    }

    @Override
    @CallSuper
    public void onDetach() {
        mLifecycleSubject.onNext(FragmentEvent.DETACH);
        dismissKpDialog();
        super.onDetach();
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        registerConnectCallback();
    }

    @Override
    public void onSupportInvisible() {
        super.onSupportInvisible();
        ConnectManager.getInstance().removeConectionListioner(this);
        isLazyLayoutFirst=true;
    }


    @Override
    public void onConnected(ProductModel productModel) {

    }

    @Override
    public void onDisConnected() {

    }

    @Override
    public void onConnectionUsed() {

    }

    protected void showKpDialog(String text) {
        if (mD == null) {
            mD = KProgressHUD.create(getActivity())
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setMaxProgress(100);
        }
        if (mD != null) {
            mD.setDetailsLabel(text);
        }
        if (!mD.isShowing() && !getActivity().isFinishing()) {
            mD.show();
        }
    }

    protected void dismissKpDialog() {
        if (mD != null && mD.isShowing())
            mD.dismiss();
    }


}