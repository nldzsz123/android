package com.feipai.flypai.ui.activity;

import android.content.Intent;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.Messages.ApmModes;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.enums.MAV_CMD;
import com.feipai.flypai.BuildConfig;
import com.feipai.flypai.R;
import com.feipai.flypai.api.CameraCommandCallback;
import com.feipai.flypai.api.NotifyMessagers;
import com.feipai.flypai.api.ResultCallback;
import com.feipai.flypai.api.RxLoopObserver;
import com.feipai.flypai.api.RxLoopSchedulers;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.app.FlyPieApplication;
import com.feipai.flypai.base.BaseMavlinkEntity;
import com.feipai.flypai.base.BaseMvpActivity;
import com.feipai.flypai.beans.ABCmdValue;
import com.feipai.flypai.beans.FP4KAirCameraSettings;
import com.feipai.flypai.beans.FP4KCameraSettings;
import com.feipai.flypai.beans.FP6KCameraSettings;
import com.feipai.flypai.beans.FPCameraSettingBase;
import com.feipai.flypai.beans.HintItemBean;
import com.feipai.flypai.beans.NotifyMessageMode;
import com.feipai.flypai.beans.ProductModel;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.beans.SettingBean;
import com.feipai.flypai.beans.mavlinkbeans.AckCommandBean;
import com.feipai.flypai.beans.mavlinkbeans.AutopilotVerisionBean;
import com.feipai.flypai.beans.mavlinkbeans.BatteryBean;
import com.feipai.flypai.beans.mavlinkbeans.CalibrationSuccessBean;
import com.feipai.flypai.beans.mavlinkbeans.LocationBean;
import com.feipai.flypai.beans.mavlinkbeans.PlaneParamsBean;
import com.feipai.flypai.connect.ConnectManager;
import com.feipai.flypai.mvp.contract.activitycontract.CameraActivityContract;
import com.feipai.flypai.mvp.presenters.activitypresenters.CameraActivityPresenter;
import com.feipai.flypai.ui.fragments.planesettingfragments.PlaneSettingRootFragment;
import com.feipai.flypai.ui.view.ActionDialog;
import com.feipai.flypai.ui.view.Camera.AdjustLayout;
import com.feipai.flypai.ui.view.Camera.BottomInfoView;
import com.feipai.flypai.ui.view.Camera.CameraSetAdapter;
import com.feipai.flypai.ui.view.Camera.CameraSetConstaint;
import com.feipai.flypai.ui.view.Camera.CameraSetItem;
import com.feipai.flypai.ui.view.Camera.CameraSetView;
import com.feipai.flypai.ui.view.Camera.HintLayout;
import com.feipai.flypai.ui.view.Camera.MapViewLayout;
import com.feipai.flypai.ui.view.Camera.OperateView;
import com.feipai.flypai.ui.view.Camera.ReturnBackLayout;
import com.feipai.flypai.ui.view.Camera.SmartView;
import com.feipai.flypai.ui.view.Camera.TopbarInfoView;
import com.feipai.flypai.ui.view.Camera.VideoView;
import com.feipai.flypai.ui.view.ViewSizeChangeAnimation;
import com.feipai.flypai.ui.view.VirtualKeyboardView;
import com.feipai.flypai.ui.view.mapbox.MapboxLatLng;
import com.feipai.flypai.utils.CameraCommand;
import com.feipai.flypai.utils.MLog;
import com.feipai.flypai.utils.MavlinkRequestMessage;
import com.feipai.flypai.utils.PlaneCommand;
import com.feipai.flypai.utils.cache.CacheManager;
import com.feipai.flypai.utils.global.BaseTimer;
import com.feipai.flypai.utils.global.ConvertUtils;
import com.feipai.flypai.utils.global.HandlerUtils;
import com.feipai.flypai.utils.global.IAnimationUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.NetworkUtils;
import com.feipai.flypai.utils.global.RegexUtils;
import com.feipai.flypai.utils.global.RemoteJschUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.RxBusUtils;
import com.feipai.flypai.utils.global.RxUtils;
import com.feipai.flypai.utils.global.ScreenUtils;
import com.feipai.flypai.utils.global.SensorUtil;
import com.feipai.flypai.utils.global.SizeUtils;
import com.feipai.flypai.utils.SmallerGestures;
import com.feipai.flypai.utils.global.StringUtils;
import com.feipai.flypai.utils.global.TimeUtils;
import com.feipai.flypai.utils.global.ToastUtils;
import com.feipai.flypai.utils.global.Utils;
import com.feipai.flypai.utils.global.ViewUtils;
import com.feipai.flypai.utils.languageutils.LanguageUtil;
import com.jcraft.jsch.JSchException;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.videoplayer.NativeCode;
import com.videoplayer.VideoPlayer;
import com.zhy.autolayout.utils.AutoUtils;

import java.io.IOException;
import java.lang.reflect.Array;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;

import static com.ardupilotmega.msg_mag_cal_report.MAVLINK_MSG_ID_MAG_CAL_REPORT;
import static com.common.msg_autopilot_version.MAVLINK_MSG_ID_AUTOPILOT_VERSION;
import static com.common.msg_battery_status.MAVLINK_MSG_ID_BATTERY_STATUS;
import static com.common.msg_cal_progress_decode.MAG_CAL_PROGRESS;
import static com.common.msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK;
import static com.common.msg_command_long.MAVLINK_MSG_ID_COMMAND_LONG;
import static com.common.msg_location.MAVLINK_MSG_ID_LOCATION;
import static com.common.msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE;
import static com.feipai.flypai.app.ConstantFields.ACTION_PARAM.CALI_COMPASS_PROGRESS;
import static com.feipai.flypai.app.ConstantFields.ACTION_PARAM.COMPASS_STATE;
import static com.feipai.flypai.app.ConstantFields.ACTION_PARAM.GYROSCOPE_STATE;
import static com.feipai.flypai.app.ConstantFields.ACTION_PARAM.UPDATE_BATTERY_VALUE;
import static com.feipai.flypai.app.ConstantFields.ACTION_PARAM.UPDATE_SPEED_VALUE;
import static com.feipai.flypai.app.ConstantFields.CAMERA_CONFIG.SD_FRAGMENTATION;
import static com.feipai.flypai.app.ConstantFields.CAMERA_CONFIG.SD_LOW;
import static com.feipai.flypai.app.ConstantFields.CAMERA_CONFIG.SD_SPEED_SLOW;
import static com.feipai.flypai.ui.view.Camera.CameraSetConstaint.CameraModeMovie;
import static com.feipai.flypai.ui.view.Camera.CameraSetConstaint.CameraModePhoto;
import static com.feipai.flypai.ui.view.Camera.CameraSetConstaint.CameraStatusInPano;
import static com.feipai.flypai.ui.view.Camera.CameraSetConstaint.DefaultISOValue;
import static com.feipai.flypai.ui.view.Camera.CameraSetConstaint.DefaultShutterValue;
import static com.feipai.flypai.ui.view.Camera.CameraSetConstaint.PhotoModeDelay;
import static com.feipai.flypai.ui.view.Camera.CameraSetConstaint.PhotoModeNormal;
import static com.feipai.flypai.ui.view.Camera.CameraSetConstaint.PhotoModePano;
import static com.feipai.flypai.ui.view.Camera.CameraSetConstaint.cell_photo_Atio_key;
import static com.feipai.flypai.ui.view.Camera.CameraSetConstaint.cell_photo_iso_key;
import static com.feipai.flypai.ui.view.Camera.CameraSetConstaint.cell_photo_shutter_key;
import static com.feipai.flypai.ui.view.Camera.CameraSetConstaint.cell_video_iso_key;
import static com.feipai.flypai.ui.view.Camera.CameraSetConstaint.cell_video_shutter_key;

public class CameraActivity extends BaseMvpActivity<CameraActivityPresenter> implements CameraActivityContract.View,
        ConnectManager.ConnectManagerCallback,
        CameraSetAdapter.CameraSetListioner,
        AMapLocationListener,
        LocationSource,
        AMap.OnMapClickListener,
        AMap.OnMarkerClickListener,
        MapboxMap.OnMapClickListener,
        LocationEngineCallback<LocationEngineResult> {


    private ConstraintLayout mRootLayout;

    private HintLayout hintLayout;

    private FrameLayout mPlaneSettingLy;
    // 顶部信息栏
    private TopbarInfoView topbarInfoView;
    // 地图
    private MapViewLayout mapViewLy;
    // 右侧操作按钮
    private OperateView operateView;
    // 相机设置菜单
    private CameraSetView cameraSetView;
    // 相机设置菜单 adatper
    private CameraSetAdapter setViewAdapter;
    // 底部信息栏
    private BottomInfoView bottomInfoView;
    private Button bottomCancelFindPlane;
    // 智能菜单
    private SmartView smartView;
    // 进入回放界面的按钮
    private ImageButton playbackButton;
    // 变焦条 for 4k 6k air
    private AdjustLayout zoomSlider;

    /**
     * 一键返航对话框
     */
    private ReturnBackLayout mReturnBackLy;

    // 图传用的视图
    private VideoView videoView;
    private VideoPlayer mPlayer;

    private SmallerGestures mTapSmallGesture;

    // 相机设置
    private FPCameraSettingBase cameraSetting;
    // 用来播放提示音
    public MediaPlayer mSoundPlayer;

    // 拍照录像功能区
    private boolean isContinueShutter;  //正在连续拍照
    private boolean isVideoPlayed;  //图传流是否已经播了
    private Timer shutterTimer;   // 连续拍照的定时器
    private Runnable recordingTimer;   // 录制计时的定时器
    private int recordSec;      // 录制时间
    private boolean sdCardInserted; // sd卡是否插入
    private boolean internalsdCard; // 是否有内置卡
    private int lastScale;
    private boolean isRooming;

    private PlaneParamsBean mPlaneParamBean;
    private BatteryBean batteryBean;
    private LocationBean lBean;
    private AutopilotVerisionBean verisionBean;

    // 弹窗提醒
    private ActionDialog mActionDalog;

    private RxLoopObserver<String> mRSSIObservable;
    private BaseTimer mRSSTimer;

    @Override
    protected int initLayout() {
        return R.layout.activity_camera;
    }


    @Override
    protected void initInject() {
        mPresenter = new CameraActivityPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected boolean isUseButterKnife() {
        return true;
    }

    @Override
    protected boolean isUseMavlinkConnect() {
        return true;
    }

    @Override
    protected boolean isUseRxBus() {
        return true;
    }

    @Override
    protected void initRxbusListener(RxbusBean msg) {
        super.initRxbusListener(msg);
        switch (msg.TAG) {
            case ConstantFields.BusEventType.FIND_PLANE:
                onTapPlaneseButton();
                startFindPlane(true);
                if (mapViewLy.isSmall()) {
                    mapViewLy.toFullScreen();
                }
                break;
            case ConstantFields.BusEventType.RESET_HOME_POINT:
                LogUtils.d("重置返航点");
                mapViewLy.resetHomePosition();
                break;
        }
//        mPresenter.handlerEvent(msg);
    }

    /**
     * 寻找飞机
     *
     * @param isStart 开始  or 退出
     */
    private void startFindPlane(boolean isStart) {
        mapViewLy.changeTofindPlaneMode(isStart);
        cancelFindPlaneBtnShow(isStart);
    }

    protected boolean isUseAutoSize() {
        return false;
    }

    private Bundle mSaveState;

    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mSaveState = savedInstanceState;

        mPlaneSettingLy = findViewById(R.id.planesetting_layout);
        mPlaneSettingLy.setVisibility(View.GONE);
        initPlaneSetingFragment();
        mRootLayout = findViewById(R.id.activity_camera);
        hintLayout = findViewById(R.id.hint_layout);
        cameraSetting = new FP6KCameraSettings();
        topbarInfoView = findViewById(R.id.camera_top_info);
        topbarInfoView.setTopStatusListener(new TopbarInfoView.TopStatusCallback() {
            @Override
            public void stopSmart(boolean isStop) {
                zoomSlider.setYuntaiAdjustVisibility(!isStop);
                if (isStop) {
                    zoomSlider.stopAdjustControl(false);
                    zoomSlider.setAdjustControlVisibility(!isStop);
                    smartView.setAdjustControlBntEnable(!isStop);
                    smartView.setAdjustControlShown(false);
                } else {
                    if (!smartView.getAdjustControlBntEnabled()) {
                        smartView.setAdjustControlBntEnable(true);
                    }
                }

            }
        });
        videoView = findViewById(R.id.camera_videoview);
        videoView.setOnTouchCallback(this, new VideoView.OnTouchCallback() {
            @Override
            public void spotMetering(boolean isSpotOpen, int x, int y) {
                if (!cameraSetting.photoISOAuto) {
                    ToastUtils.showShortToast(ResourceUtils.getString(R.string.camera_set_cannot_meter));
                    return;
                }
                if (cameraSetting.photoAELock) {
                    ToastUtils.showShortToast(ResourceUtils.getString(R.string.camera_set_can_meter_a));
                    return;
                }

                if (isSpotOpen) {
                    videoView.showSpotMetering(true);
                    cmd.setSpotMeter(cameraSetting.lastUseMode == CameraModePhoto,
                            isSpotOpen, x, y);
                } else {
                    videoView.showSpotMetering(false);
                    cmd.setSpotMeter(cameraSetting.lastUseMode == CameraModePhoto,
                            isSpotOpen, x, y);
                }
            }
        });
        mapViewLy = findViewById(R.id.camera_mapview);
        mapViewLy.initMapHelper(LanguageUtil.isZh(), this);
        if (mapViewLy.isZh()) {
            mapViewLy.initGaoDeMap(savedInstanceState, this, this);
        } else {
            mapViewLy.initMapboxMap(savedInstanceState, this, this, 0);
        }
        playbackButton = findViewById(R.id.camera_plaback_btn);
        operateView = findViewById(R.id.camera_operate);
        ConstraintSet set = new ConstraintSet();
        set.clone(mRootLayout);
        set.constrainWidth(operateView.getId(), AutoUtils.getPercentHeightSize(180));
        set.constrainHeight(operateView.getId(), AutoUtils.getPercentHeightSize(522));
        set.applyTo(mRootLayout);


        cameraSetView = findViewById(R.id.camera_camerasetview);
        smartView = findViewById(R.id.camera_smart_view);
        initSmartListener();
        mReturnBackLy = findViewById(R.id.retureback_ly);
        mReturnBackLy.setCallback(new ReturnBackLayout.ConfirmCallback() {
            @Override
            public void onConfirm() {
                PlaneCommand.getInstance().changeFlyMode(ApmModes.ROTOR_RTL);
                smartView.setReturning(true);
            }
        });
        mReturnBackLy.setVisibility(View.GONE);
        mReturnBackLy.initKeyboardListener(this);
        bottomInfoView = findViewById(R.id.camera_bottomInfo);
        bottomCancelFindPlane = findViewById(R.id.contorl_cancel_find_plane_btn);
        mPlayer = new VideoPlayer(videoView.videoView, false, mVideoPlayCallback);
        mActionDalog = new ActionDialog(this);
        zoomSlider = findViewById(R.id.adjust_layout);
        zoomSlider.bindBaseView(this);

        // 处理个中单击，手势等等
        initEventListioners();
    }

    private void initSmartListener() {
        smartView.setAdjustControlBntVisibility(isNeedShowContorlLy());
        smartView.bindMapView(this, mapViewLy, new SmartView.OnActionCallback() {

            @Override
            public void onIPMDialogShown(boolean isShown) {
                smartView.setSmartViewEnabled(true);
                ConstraintSet set = new ConstraintSet();
                //获取一新的ConstraintLayout
                //mConstraintLayout 是你的当前使用的约束布局
//                set.clone(mRootLayout);
//                set.clear(hintLayout.getId());
                set.connect(hintLayout.getId(), ConstraintSet.TOP, topbarInfoView.getId(), ConstraintSet.BOTTOM);
                if (isShown) {
                    set.connect(hintLayout.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
                } else {
                    set.connect(hintLayout.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
                }
                set.constrainWidth(hintLayout.getId(), hintLayout.getWidth());
                set.constrainHeight(hintLayout.getId(), hintLayout.getHeight());
                set.applyTo(mRootLayout);
                hintLayout.bringToFront();
                hintLayout.changeToRight(isShown);
                mapViewLy.showMapChangeEnabled(!isShown);
                if (isShown) {
                    LogUtils.d("控制菜单隐藏（智能操控时候）");
                    zoomSlider.setAdjustControlVisibility(false);
                    zoomSlider.setYuntaiAdjustVisibility(false);
                    smartView.setAdjustControlBntVisibility(false);
                } else {
                    LogUtils.d("控制菜单显示（智能操控时候）");
                    zoomSlider.setYuntaiAdjustVisibility(isNeedShowContorlLy());
                    smartView.setAdjustControlBntVisibility(isNeedShowContorlLy());
                }

            }

            @Override
            public void onMapLacationResult(boolean isSuccess) {
                if (!isSuccess) {
                    HintItemBean hintItemBean = new HintItemBean();
                    hintItemBean.setType(ConstantFields.HINT_LAYOUT_TYPE.LOCATION_ERROR);
                    hintItemBean.setHintText(ResourceUtils.getString(R.string.phone_gps_low_check_permissions));
                    hintItemBean.setTextColor(R.color.color_fe9700);
                    hintLayout.addItem(hintItemBean);
                } else {
                    hintLayout.removeItem(ConstantFields.HINT_LAYOUT_TYPE.LOCATION_ERROR);
                }

            }

            @Override
            public void onReturnBack(boolean isStart) {
                if (isStart) {
                    //开始返航，取消返航
                    LogUtils.d("显示一键返航对话框");
                    mReturnBackLy.setShow(true);
                    mReturnBackLy.bringToFront();
                } else {
                    LogUtils.d("取消一键返航对话框");
                    PlaneCommand.getInstance().changeFlyMode(ApmModes.ROTOR_LOITER);
                    smartView.setReturning(false);
                }
            }

            @Override
            public void onStart(int status) {
                // TODO: 2019/7/26 开始智能功能，初始化相应的相机参数
                smartView.setSmartViewEnabled(false);
                smartView.setCameraSetting(cameraSetting);
                setCameraViewStatus(status);
                if (cameraSetView.getVisibility() == View.VISIBLE) {
                    onTapCamerasetButton();
                }
            }

            @Override
            public void onAdjustControlShown() {
                // TODO: 2019/11/19 显示方向按键页面
                if (zoomSlider != null) {
                    smartView.setAdjustControlShown(isNeedShowContorlLy() && zoomSlider.showAdjustControl());
                }
            }

            @Override
            public void onEvChange(boolean isEnabled) {
                aeLockChange(false);
                bottomInfoView.setAEButtonEnabled(isEnabled);
            }

            @Override
            public void onDelayTimer(int time) {
                operateView.delayTimerChange(time);
            }

            @Override
            public void onModeChange(int photoMode, int photoSize) {
                if (cameraSetting != null) {
                    cameraSetting.photoSubMode = photoMode;
                    cameraSetting.lastUseMode = CameraModePhoto;
                    cameraSetting.photoAtio = photoSize;
                    smartView.setCameraSetting(cameraSetting);
                    if (photoMode == PhotoModePano || photoMode == PhotoModeDelay)
                        setCameraViewStatus(CameraSetConstaint.CameraStatusDisable);
                    // 刷新一下
                    cameraSetting.photoOptionItems();
                    if (cameraSetView.getVisibility() == View.VISIBLE) {
                        loadCameraSets();
                    }
                    setISOShutterDimenssionLabelTexts();
                    setGridLine();
                }
            }

            @Override
            public void onPlayVideo(boolean isTakePhotoEnd) {
                if (isTakePhotoEnd) {
                    operateView.stopTakePhotoAnimation();
                    operateView.delayTimerChange(0);
                }
                playVideo();
            }

            @Override
            public boolean canTakeNextPhoto() {
                if (!isVideoPlayed) {
                    LogUtils.d("图传流未恢复，等待");
                }

                return isVideoPlayed;
            }

            @Override
            public void onStopVideo(boolean isWide, boolean isTakingPhoto) {
                if (isTakingPhoto) {
                    playerSound(R.raw.camera_shutter_click_01);
                    operateView.startTakePhotoAnimation();
                } else {
                    zoomSlider.setDragAble(false);
                }
                topbarInfoView.setWide(isWide);
                stopVideoWhenSendCameraCommond();
            }

            @Override
            public void onDestory() {
                operateView.delayTimerChange(0);
                bottomInfoView.setAEButtonEnabled(true);
                if (cameraSetting.lastUseMode == CameraModePhoto) {
                    setCameraViewStatus(CameraSetConstaint.CameraStatusPhotoModeNormal);
                }
            }

            @Override
            public void mapTypeChange(int type) {
                if (mapViewLy.isZh()) {
                    mapViewLy.initGaoDeMap(mSaveState, CameraActivity.this, CameraActivity.this);
                } else {
                    mapViewLy.upMapboxStyle(mSaveState, CameraActivity.this, CameraActivity.this, type);
                }
            }
        });
    }

    private boolean isNeedShowContorlLy() {
        if (verisionBean != null && verisionBean.getPlaneVersion().length() > 1) {
            String ver = verisionBean.getPlaneVersion().substring(verisionBean.getPlaneVersion().length() - 1);
            LogUtils.d("当前飞控版本号--->" + ver);
            if (TextUtils.isDigitsOnly(ver)) {
                return ConnectManager.getInstance().mProductModel.productType != ConstantFields.ProductType_4k && Integer.parseInt(ver) >= 3;
            }
        }
        return false;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        smartView.outOfTouch(ev);
        if (operateView.outOfTouchSetBnt(ev) && !ViewUtils.inRangeOfView(cameraSetView, ev)) {
            if (cameraSetView.getVisibility() == View.VISIBLE) {
                onTapCamerasetButton();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startNotifyWIFI();
        mapViewLy.onResume();
        smartView.onResume();

        // 加载
        loadPlaneModel(ConnectManager.getInstance().mProductModel);

        // for debug
//        execTimer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapViewLy.onStart();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapViewLy.onLowMemory();
    }

    private int sum = 0;
    private boolean hasFoundHome = false;

    private void execTimer() {
        HandlerUtils.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mapViewLy.isMapLoaded()) {
                    execTimer();
                    return;
                }

                LocationBean lt = new LocationBean();
                if (!hasFoundHome) {
                    hasFoundHome = true;
                    MapboxLatLng loc = new MapboxLatLng(22.633608, 113.919916);
                    lt.setMapboxLocaLtlg(loc);
                    lt.setHomeLt(loc.getLatitude());
                    lt.setHomeLg(loc.getLongitude());
                    mapViewLy.addHomePosition(lt);
                } else {
                    MapboxLatLng loc = new MapboxLatLng(22.643608 + 0.0001 * sum, 113.919916);
                    lt.setMapboxLocaLtlg(loc);
                    lt.setcRadius(10.0f + 1f * sum);
                }
                mapViewLy.movePlaneMarker(lt);
                sum++;

                execTimer();
            }
        }, 1000);
    }

    @Override
    protected void onPause() {
        zoomSlider.stopAdjustControl(false);
        zoomSlider.stopYuntaiAdjust();
        super.onPause();
        stopNotifyWIFI();
        mapViewLy.onPause();
        smartView.onPause();
        stopVideo();
        if (mRSSIObservable != null) mRSSIObservable.disposeDisposables();
//        RemoteJschUtils.getInstance().closeJsch();
        if (zoomSlider.isAutoZoom) {
            zoomAutoEnd();
            zoomSlider.hideSliderAndreleaseTimer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapViewLy.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapViewLy.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapViewLy.onSaveInstanceState(outState);
//        stopVideo();
    }

    /**
     * 更新顶部wifi信号
     */
    @Override
    protected void onWifiRssiChangedCallBack(int wifiRssi) {
        super.onWifiRssiChangedCallBack(wifiRssi);
//        LogUtils.d("刷新WIFI信号===》" + ConnectManager.getInstance().isConneted());
//        if (ConnectManager.getInstance().isConneted()) {
//            if (topbarInfoView != null)
//                topbarInfoView.updateWifiLevel(wifiRssi);
//        }
    }


    private void initPlaneSetingFragment() {
        PlaneSettingRootFragment fragment = findFragment(PlaneSettingRootFragment.class);
        if (fragment == null) {
            loadRootFragment(R.id.planesetting_layout, new PlaneSettingRootFragment());
        }
    }

    private void initProgramViews() {
        ConstraintLayout rootLayout = findViewById(R.id.activity_camera);

        cameraSetView = new CameraSetView(this);
        cameraSetView.setId(View.generateViewId());
        ConstraintSet set = new ConstraintSet();
        set.connect(cameraSetView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.connect(cameraSetView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        set.connect(cameraSetView.getId(), ConstraintSet.RIGHT, operateView.getId(), ConstraintSet.LEFT);
        set.constrainWidth(cameraSetView.getId(), SizeUtils.dp2px(322));
        set.applyTo(rootLayout);
        rootLayout.addView(cameraSetView);
        cameraSetView.setVisibility(View.GONE);

    }

    private void loadZoomSliderFor6kAir() {
        if (zoomSlider.isZoomShow()) {
            return;
        }
        zoomSlider.setZoomVisibility(true);
        zoomSlider.setYuntaiAdjustVisibility(true);
        zoomSlider.internaltimer = cameraSetting.zoomIndex == 0 ? 100 : 300;
        zoomSlider.setMinValue(0);
        int maxValue = ConnectManager.getInstance().mProductModel.productType == ConstantFields.ProductType_4kAir ? 100 : 300;
        zoomSlider.setMaxValue(maxValue);
        zoomSlider.setCurrentValue(0);
        zoomSlider.setmListioner(new AdjustLayout.Callback() {
            @Override
            public void autoZoomEndBlock() {
                LogUtils.d("停止缩放====>" + zoomSlider.isAutoZoom);
                if (zoomSlider.isAutoZoom) {
                    zoomAutoEnd();
                    zoomSlider.isAutoZoom = false;
                }
            }

            @Override
            public void valueChangeBlock(int value) {
                if (zoomSlider.isAutoZoom) {
                    zoomAutoEnd();
                    zoomSlider.isAutoZoom = false;
                }
                zoomSlider.stopAutoZoomForSlider();
                zoomSliderOnChangeValue(value);
            }

            @Override
            public void autoReduceZoomBlock(boolean begin, int value) {
                zoomSlider.isAutoZoom = true;
                zoomAutoReduce(begin);
            }

            @Override
            public void autoAddZoomBlock(boolean begin, int value) {
                zoomSlider.isAutoZoom = true;
                zoomAutoAdd(begin);
            }

            @Override
            public void adjustContorlAvailable(boolean isAvailable) {
                // TODO: 2019/12/16 智能控制按键是否可用，用于高度低于10m不可用
                if (!isAvailable) {
                    if (mActionDalog != null)
                        mActionDalog.showWithTitle(false, ResourceUtils.getString(R.string.prompt),
                                ResourceUtils.getString(R.string.disable_smart_mode), null,
                                ResourceUtils.getString(R.string.confirm), (int action) -> {
                                }, null);
                } else {

                }
            }
        });

        showZoomView(true);
        setSuofangLabelTexts();
    }

    private void removeSliderWhenNot6K() {
        zoomSlider.setZoomVisibility(false);
        zoomSlider.setYuntaiAdjustVisibility(false);
    }

    private void initEventListioners() {
        // 点击左下角窗口
        mTapSmallGesture = new SmallerGestures();
        mTapSmallGesture.bindView(this, mapViewLy, videoView, new SmallerGestures.GestureCallback() {
            @Override
            public void applyToParent(boolean isMapToSmall, ConstraintSet set) {
                set.applyTo(mRootLayout);
            }

            @Override
            public void setParentChildViewVisible(int visibility) {
                operateView.setVisibility(!mapViewLy.isSmall() ? View.GONE : visibility);
                smartView.setVisibility(visibility);
                bottomInfoViewShow(true);
                playbackButton.setVisibility(!mapViewLy.isSmall() ? View.GONE : visibility);
                if (visibility == View.VISIBLE) {//图传最大
                    topbarInfoView.bringToFront();
                    operateView.bringToFront();
                    smartView.bringToFront();
                    bottomInfoView.bringToFront();
                    playbackButton.bringToFront();
                    mPlaneSettingLy.bringToFront();
                    cameraSetView.bringToFront();
                    zoomSlider.bringToFront();
                    mReturnBackLy.bringToFront();
                    hintLayout.bringToFront();
                    bottomCancelFindPlane.bringToFront();
                }
            }
        });

        bottomInfoView.setmLisioner((int value) -> {
            if (!ConnectManager.getInstance().isConneted()) {
                return;
            }
            PlaneCommand.getInstance().setYTHoriparam(value);
        });
    }


    // 点击退出相机界面按钮
    @OnClick(R.id.ab_back_ly)
    void onTapbackButton() {
        onBackPressed();
    }


    ViewSizeChangeAnimation animation;

    // 点击飞控设置按钮
    @OnClick(R.id.ab_cotr_more_ly)
    void onTapPlaneseButton() {
//        if (mPlaneSettingLy.getVisibility() != View.VISIBLE)
//            mPlaneSettingLy.setVisibility(View.VISIBLE);
        mPlaneSettingLy.bringToFront();
        //从底部显示
        if (mPlaneSettingLy.getVisibility() == View.VISIBLE) {
            topbarInfoView.loadAnimatation(true);
            IAnimationUtils.performHideViewAnim(this, mPlaneSettingLy, R.anim.exit_bottom_anim);
        } else {
            topbarInfoView.loadAnimatation(false);
            IAnimationUtils.performShowViewAnim(this, mPlaneSettingLy, R.anim.enter_bottom_anim);
        }
//        if (mPlaneSettingLy.getMeasuredWidth() < ScreenUtils.getScreenWidth()) {
//            animataion(mPlaneSettingLy,
//                    (ScreenUtils.getScreenHeight() - topbarInfoView.getHeight()), ScreenUtils.getScreenWidth());
//            LogUtils.d("变大显示飞控设置");
//        } else {
//            animataion(mPlaneSettingLy, 0, 0);
//            LogUtils.d("变小隐藏飞控设置");
//        }

    }

    private void animataion(View view, int tagH, int tagW) {
        if (animation == null) {
            animation = new ViewSizeChangeAnimation(view, tagH, tagW);
        } else {
            animation.setTagetHeightAndWidth(tagH, tagW);
        }
        animation.startAnimation(300);
    }

    @OnClick(R.id.camera_ae_btn)
    void onAeLocked() {
        aeLockChange(true);
    }

    /**
     * @param fromUser 用户手动点击
     */
    private void aeLockChange(boolean fromUser) {
        if (cameraSetting.lastUseMode == CameraModePhoto) {
            final boolean photoAeLock = !cameraSetting.photoAELock;
            if (cameraSetting.photoISOAuto) {
                //照片自动曝光，锁定
                cmd.swithAELock(this, photoAeLock, true, new CameraCommandCallback<Boolean>() {
                    @Override
                    public void onComplete(Boolean cb) {
                        updateAELockStatus(photoAeLock, true);
                    }
                });
            }
            if (!fromUser) {
                // TODO: 2019/7/26 非手动点击，目前针对于智能功能（全景，广角，延时的状态回调）

                smartView.aeLockChangedSuccess(cameraSetting, photoAeLock);
            }

        } else if (cameraSetting.lastUseMode == CameraSetConstaint.CameraModeMovie) {
            final boolean videoAelock = !cameraSetting.videoAELock;
            cmd.swithAELock(this, videoAelock, false, new CameraCommandCallback<Boolean>() {
                @Override
                public void onComplete(Boolean cb) {
                    updateAELockStatus(videoAelock, true);
                }
            });
        }
    }


    // 点击电池图标
    @OnClick(R.id.ab_battery_ly)
    void onTapChargeButton() {
//        cmd.photoZoomClose();
        if (mPlaneSettingLy.getVisibility() != View.VISIBLE) {
            onTapPlaneseButton();
        }
//        if (mPlaneSettingLy.getWidth() == 0) {
//            onTapPlaneseButton();
//        }
        RxBusUtils.getDefault().post(new RxbusBean(ConstantFields.BusEventType.SWITCH_BATTERY_TAB,
                ConstantFields.BusEventType.SWITCH_BATTERY_TAB));
    }

    // 点击相机设置按钮
    @OnClick(R.id.camera_set_btn)
    void onTapCamerasetButton() {
        IAnimationUtils.performAnim(this, operateView.setButton, R.anim.view_up_scale_anim);

        if (cameraSetView.getVisibility() == View.VISIBLE) {
//                IAnimationUtils.performHideViewAnim(this, cameraSetView, R.anim.exit_right_anim);
            cameraSetView.setVisibility(View.GONE);
        } else {
//                IAnimationUtils.performShowViewAnim(this, cameraSetView, R.anim.enter_right_anim);
            cameraSetView.setVisibility(View.VISIBLE);
        }
        operateView.setIsSelected(cameraSetView.getVisibility() == View.VISIBLE);


        // 刷新数据
        if (cameraSetView.getVisibility() == View.VISIBLE) {
            loadCameraSets();
            bottomInfoViewShow(false);
//            IAnimationUtils.performHideViewAnim(this, bottomInfoView, R.anim.exit_bottom_anim);

            // 变焦项目放在下面
            cameraSetView.bringToFront();

            // 获取SD卡容量
            cmd.getFreeSDCardInmain(this, new CameraCommandCallback<Long>() {
                @Override
                public void onComplete(Long cb) {
                    cameraSetting.sdcardFree = cb.intValue();
                }
            });
            cmd.getTotalSDCard(this, new CameraCommandCallback<Integer>() {
                @Override
                public void onComplete(Integer cb) {
                    cameraSetting.sdcardTotal = cb;
                    loadCameraSets();
                }
            });
        } else {
            bottomInfoViewShow(true);
//            IAnimationUtils.performShowViewAnim(this, bottomInfoView, R.anim.enter_bottom_anim);
        }

        // 选择不同选项
        cameraSetView.setCameraSetViewOnClick((int index) -> {
            loadCameraSets();
        });


    }

    // 点击模式切换按钮
    @OnClick(R.id.camera_mode_exchange_btn)
    void onTapModeChangeButton() {
        IAnimationUtils.performAnim(this, operateView.modeExchangeButton, R.anim.view_up_scale_anim);

        setCameraViewStatus(CameraSetConstaint.CameraStatusDisable);
        stopVideoWhenSendCameraCommond();

        // 切换模式
        if (cameraSetting.lastUseMode == CameraModePhoto) {
            changeToMovieMode();
        } else if (cameraSetting.lastUseMode == CameraSetConstaint.CameraModeMovie) {
            changeToPhotoMode();
        } else {
            LogUtils.d("模式不对呀");
        }
    }

    //点击录像或者拍照按钮
    @OnClick(R.id.function_btn)
    void onTapPhotoOrRecordButton() {
        IAnimationUtils.performAnim(this, operateView.functionButton, R.anim.view_up_scale_anim);

        if (cameraSetting.lastUseMode == CameraModePhoto) {

            if (cameraSetting.autoTakephoto) {  // 连续拍照
                if (smartView.isStarted()) {
                    operateView.delayTimerChange(0);
                    smartView.stopDelayTimer();
                } else {
                    if (cameraSetView.getVisibility() == View.VISIBLE) {
                        onTapCamerasetButton();
                    }
                    CameraCommand.getCmdInstance().takePhotoInDelayModeComplete(this,
                            cameraSetting.lastUseMode == CameraSetConstaint.CameraModeMovie,
                            ConnectManager.getInstance().mProductModel.productType, true, new CameraCommandCallback<Boolean>() {
                                @Override
                                public void onComplete(Boolean success) {
                                    if (success) {
                                        cameraSetting.lastUseMode = CameraModePhoto;
                                        cameraSetting.photoSubMode = PhotoModeDelay;
                                        setISOShutterDimenssionLabelTexts();
                                        setCameraViewStatus(CameraSetConstaint.CameraStatusInYanshi);
                                        smartView.setCameraSetting(cameraSetting);
                                        smartView.openDelayTimer(3, true);
                                    }
                                }
                            });
                }
//                if (isContinueShutter) {
//                    stopContinuTakePhoto();
//                    return;
//                }


            } else {     // 单张拍照
                setCameraViewStatus(CameraSetConstaint.CameraStatusDisable);
                operateView.startTakePhotoAnimation();
                playerSound(R.raw.camera_shutter_click_01);
                stopVideoWhenSendCameraCommond();

//                if (cameraSetting.photoSubMode != CameraSetConstaint.PhotoModeNormal) { // 非普通拍照模式
                cmd.changeToNormalPhotoModeAndTakePhoto(this, new CameraCommandCallback<Integer>() {
                    @Override
                    public void onComplete(Integer status) {
                        if (status == CameraSetConstaint.SDCardStatusFull) {    // SD卡已满
                            setCameraViewStatus(CameraSetConstaint.CameraStatusSDCardFull);
                            ToastUtils.showLongToast(ResourceUtils.getString(R.string.sd_card_full));
                        } else if (status == CameraSetConstaint.SDCardStatusOK) {
                            cameraSetting.photoSubMode = CameraSetConstaint.PhotoModeNormal;
                            setCameraViewStatus(CameraSetConstaint.CameraStatusPhotoModeNormal);
                        } else {
                            cameraSetting.photoSubMode = CameraSetConstaint.PhotoModeNormal;
                            setCameraViewStatus(CameraSetConstaint.CameraStatusPhotoModeNormal);
                            ToastUtils.showLongToast(ResourceUtils.getString(R.string.take_photo_failure));
                        }
                        operateView.stopTakePhotoAnimation();
                        playVideo();
                    }
                });
//                } else {
//                    cmd.takePhotoComplete(this, new CameraCommandCallback<Integer>() {
//                        @Override
//                        public void onComplete(Integer status) {
//                            if (status == CameraSetConstaint.SDCardStatusFull) {    // SD卡已满
//
//                            } else if (status == CameraSetConstaint.SDCardStatusOK) {
//                                cameraSetting.photoSubMode = CameraSetConstaint.PhotoModeNormal;
//                            }
//
//                            setCameraViewStatus(CameraSetConstaint.CameraStatusPhotoModeNormal);
//                            operateView.stopTakePhotoAnimation();
//                            playVideo();
//                        }
//                    });
//                }
            }
        } else if (cameraSetting.lastUseMode == CameraSetConstaint.CameraModeMovie) {        // 点击了录制按钮

            if (cameraSetView != null && cameraSetView.getVisibility() == View.VISIBLE) {
                onTapCamerasetButton();
            }

            beginOrEndRecording(true);
        }
    }


    @OnClick(R.id.contorl_cancel_find_plane_btn)
    void onCancelFindPlane() {
        startFindPlane(false);
    }

    private void cancelFindPlaneBtnShow(boolean isShow) {
        if (isShow) {
            bottomInfoViewShow(!isShow);
            IAnimationUtils.performShowViewAnim(this, bottomCancelFindPlane, R.anim.enter_bottom_anim);
        } else {
            IAnimationUtils.performHideViewAnim(this, bottomCancelFindPlane, R.anim.exit_bottom_anim);
            bottomInfoViewShow(!isShow);
        }
    }

    private void bottomInfoViewShow(boolean isShow) {
        if (isShow) {
            if (bottomCancelFindPlane.getVisibility() != View.VISIBLE)
                IAnimationUtils.performShowViewAnim(this, bottomInfoView, R.anim.enter_bottom_anim);
        } else {
            IAnimationUtils.performHideViewAnim(this, bottomInfoView, R.anim.exit_bottom_anim);
        }
    }

    private void beginOrEndRecording(boolean isFomeUser) {
        setCameraViewStatus(CameraSetConstaint.CameraStatusDisable);
        smartView.setSmartViewEnabled(cameraSetting.isRecording);
        if (!cameraSetting.isRecording) {
            if (isFomeUser)
                playerSound(R.raw.record_begin);
            stopVideoWhenSendCameraCommond();
//            operateView.timeLabel.setText(StringUtils.toTimeFormat(recordSec));


            if (isFomeUser) {
                cmd.startRecordComplete(this, new CameraCommandCallback<Integer>() {
                    @Override
                    public void onComplete(Integer status) {
                        if (status == CameraSetConstaint.SDCardStatusOK) {
                            setCameraViewStatus(CameraSetConstaint.CameraStatusRecord);
                            cameraSetting.isRecording = true;
                            cmd.getRecordTime(CameraActivity.this, new CameraCommandCallback<Integer>() {
                                @Override
                                public void onComplete(Integer cb) {
                                    if (cb >= 0) {
                                        recordSec = cb;
                                        if (recordingTimer == null) {
                                            recordingTimer = () -> {
                                                recordSec++;
                                                operateView.timeLabel.setText(StringUtils.toTimeFormat(recordSec));

                                                // 循环调用
                                                HandlerUtils.postDelayed(recordingTimer, 1000);
                                            };
                                            HandlerUtils.postDelayed(recordingTimer, 1000);
                                        }
                                    }
                                }
                            });
                        } else if (status == CameraSetConstaint.SDCardStatusFull) {      //SD 卡已满
                            stopRecord();
                            setCameraViewStatus(CameraSetConstaint.CameraStatusSDCardFull);
                            ToastUtils.showLongToast(ResourceUtils.getString(R.string.sd_card_full));
                        } else {
                            stopRecord();
                            setCameraViewStatus(CameraSetConstaint.CameraStatusMovieModeNormal);
                            ToastUtils.showShortToast(ResourceUtils.getString(R.string.camra_set_busy));
                        }

                        playVideo();
                    }
                });
            } else {
                // TODO: 2019/8/29 这里要兼容初始化进入，是开启的录制要继续录制状态
                if (recordingTimer == null) {
                    recordingTimer = () -> {
                        recordSec++;
                        operateView.timeLabel.setText(StringUtils.toTimeFormat(recordSec));

                        // 循环调用
                        HandlerUtils.postDelayed(recordingTimer, 1000);
                    };
                    HandlerUtils.postDelayed(recordingTimer, 1000);
                }
                setCameraViewStatus(CameraSetConstaint.CameraStatusRecord);
                cameraSetting.isRecording = true;
            }
        } else {
            zoomSlider.stopRecord();
            playerSound(R.raw.record_end);
            stopVideoWhenSendCameraCommond();
            if (recordingTimer != null) {
                HandlerUtils.cancel(recordingTimer);
                recordingTimer = null;
            }
            recordSec = 0;
            operateView.timeLabel.setText("");
            if (isFomeUser) {
                if (BuildConfig.DEBUG) {
                    // 测试用 录制自动添加WiFi名字
                    cmd.stopRecordComplete(this, new CameraCommandCallback<String>() {
                        @Override
                        public void onComplete(String cb) {
                            LogUtils.d("录制完成1===>" + cb);
                            HandlerUtils.postDelayed(() -> {
                                renameRecordnameForDebug(cb);
                            }, 1000);
                        }

                        @Override
                        public void onErrorCode(int msgId, int code) {
                            super.onErrorCode(msgId, code);
                            LogUtils.d("录制完成出错1===>");
                            setCameraViewStatus(CameraSetConstaint.CameraStatusMovieModeNormal);
                            cameraSetting.isRecording = false;
                            playVideo();
                        }
                    });
                } else {
                    cmd.stopRecordComplete(this, new CameraCommandCallback<String>() {
                        @Override
                        public void onComplete(String cb) {
                            LogUtils.d("录制完成2===>" + cb);
                            setCameraViewStatus(CameraSetConstaint.CameraStatusMovieModeNormal);
                            cameraSetting.isRecording = false;
                            playVideo();
                        }

                        @Override
                        public void onErrorCode(int msgId, int code) {
                            super.onErrorCode(msgId, code);
                            LogUtils.d("录制完成出错2===>");
                            setCameraViewStatus(CameraSetConstaint.CameraStatusMovieModeNormal);
                            cameraSetting.isRecording = false;
                            playVideo();
                        }
                    });
                }
            } else {
                //监听相机主动停止录制
                setCameraViewStatus(CameraSetConstaint.CameraStatusMovieModeNormal);
                cameraSetting.isRecording = false;
                playVideo();
            }

        }
    }

    private void renameRecordnameForDebug(String path) {
        if (path != null && path.length() > 1) {
            String[] coms = path.split("\\\\");
            int len = coms.length;

            if (len > 1) {
                String fileName = coms[len - 1];
                String dir = "VR6PRO";
                String newdir = "VR6PRO";
                if (ConnectManager.getInstance().mProductModel.productType != ConstantFields.ProductType_4k) {
                    dir = "FlyPie";
                    newdir = "FlyPie";
                }
                if (ConnectManager.getInstance().mProductModel.productType >= ConstantFields.ProductType_6kAir) {
                    newdir = "Test";
                }

                String curWifi = NetworkUtils.getCurConnetWifiName();
                String oldName1 = "/tmp/SD0/" + dir + "/Movie/" + fileName;
//                String oldName2 = "/tmp/SD0/" + dir + "/Cache/" + fileName;
                String newName1 = "/tmp/SD0/" + dir + "/Movie/" + curWifi + "_" + fileName;
                if (ConnectManager.getInstance().mProductModel.productType == ConstantFields.ProductType_6kAir) {
                    newName1 = "/tmp/SD0/" + "Test" + "/Movie/" + curWifi + "_" + fileName;
                }
//                String newName2 = "/tmp/SD0/" + dir + "/Cache/" + curWifi + "_" + fileName;
//                cmd.renameFileInMain(this, oldName1, newName1, null);
                cmd.renameFileInMain(this, oldName1, newName1, new CameraCommandCallback() {
                    @Override
                    public void onComplete(Object cb) {
                        setCameraViewStatus(CameraSetConstaint.CameraStatusMovieModeNormal);
                        cameraSetting.isRecording = false;

                        playVideo();
                    }
                });
            }
        } else {
            setCameraViewStatus(CameraSetConstaint.CameraStatusMovieModeNormal);
            cameraSetting.isRecording = false;

            playVideo();
        }

    }

    // --设置自动或者手动曝光 照片和视频模式的手动iso隐藏ae锁。自动模式才开启ae锁
    private void updateAELockStatus(boolean aeLocked, boolean show) {
        bottomInfoView.setAEButtonLock(aeLocked, show);
        cameraSetting.photoAELock = aeLocked;
        cameraSetting.videoAELock = aeLocked;
    }

    private void setISOShutterDimenssionLabelTexts() {
        if (cameraSetting.lastUseMode == CameraModePhoto) {                //照片模式
            bottomInfoView.setAEButtonLock(cameraSetting.photoAELock, cameraSetting.photoISOAuto);
            // 拍照比例
            CameraSetItem.FPTwoButtonCellItem atioItem = (CameraSetItem.FPTwoButtonCellItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_photo_Atio_key);
            topbarInfoView.res_title_textView.setText(getResources().getString(R.string.the_proportion));
            topbarInfoView.res_value_textView.setText(atioItem.titles[atioItem.selectButtonIndex].replace("fps", "FPS"));

            // ISO
            topbarInfoView.iso_value_textView.setText("ISO");
            if (cameraSetting.photoISOAuto) {
                topbarInfoView.iso_value_textView.setText("AUTO");
            } else {
                CameraSetItem.FPScrollItem item = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_photo_iso_key);
                LogUtils.d("照片ISO==" + item.curIndex);
                if (item.curIndex >= item.values.length) {
                    item.curIndex = 0;
                }
                topbarInfoView.iso_value_textView.setText(item.values[item.curIndex]);
            }

            // EV
            if (cameraSetting.photoISOAuto) {          //自动
                topbarInfoView.shut_title_textView.setText("EV");
                CameraSetItem.FPScrollItem item = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_photo_baoguang_key);
                LogUtils.d("照片EV==" + item.curIndex);
                topbarInfoView.shut_value_textView.setText(item.values[item.curIndex]);
            } else {
                topbarInfoView.shut_title_textView.setText(getResources().getString(R.string.the_shutter));
                CameraSetItem.FPScrollItem item = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_photo_shutter_key);
                LogUtils.d("照片快门==" + item.curIndex);
                topbarInfoView.shut_value_textView.setText(item.values[item.curIndex]);
            }

        } else {                                                            //视频模式
            bottomInfoView.setAEButtonLock(cameraSetting.videoAELock, cameraSetting.videoISOAuto);

            // 分辨率
            CameraSetItem.FPDimenssionItem atioItem = cameraSetting.currentVideoDimenSion();

            topbarInfoView.res_title_textView.setText(atioItem.leftTitle);
            topbarInfoView.res_value_textView.setText(atioItem.currentfps.replace("fps", "FPS"));

            // ISO
            topbarInfoView.iso_value_textView.setText("ISO");
            if (cameraSetting.videoISOAuto) {
                topbarInfoView.iso_value_textView.setText("AUTO");
            } else {
                CameraSetItem.FPScrollItem item = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_video_iso_key);
                LogUtils.d("视频ISO==" + item.curIndex);
                if (item.curIndex >= item.values.length) {
                    item.curIndex = 0;
                }
                topbarInfoView.iso_value_textView.setText(item.values[item.curIndex]);
            }

            // EV
            if (cameraSetting.videoISOAuto) {          //自动
                topbarInfoView.shut_title_textView.setText("EV");
                CameraSetItem.FPScrollItem item = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_video_baoguang_key);
                LogUtils.d("视频EV==" + item.curIndex);
                topbarInfoView.shut_value_textView.setText(item.values[item.curIndex]);
            } else {
                topbarInfoView.shut_title_textView.setText(getResources().getString(R.string.the_shutter));
                CameraSetItem.FPScrollItem item = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_video_shutter_key);
                LogUtils.d("快门EV==" + item.curIndex + "||" + Arrays.toString(item.values));
                if (item.curIndex >= item.values.length) {
                    item.curIndex = 0;
                }
                topbarInfoView.shut_value_textView.setText(item.values[item.curIndex]);
            }
        }
    }

    private void setSuofangLabelTexts() {
        int zoom = lastScale + 100;
        if (zoom >= (zoomSlider.getMaxValue() - zoomSlider.getMinValue() + 100)) {
            zoom = zoomSlider.getMaxValue() - zoomSlider.getMinValue() + 100;
        }
        String zstr = String.format("%.1fx", zoom / (float) 100);
        zoomSlider.setCurrentProgressText(zstr);
        zoomSlider.updateEnableStatus();
    }

    private void setGridLine() {
        if (mPlayer != null) {
            mPlayer.setGridType(cameraSetting.gridMode);
        }
    }

    private void initCameraStatus() {
        if (cameraSetting.isRecording) {
            cameraSetting.isRecording = false;
            beginOrEndRecording(false);
        } else {
            setCameraViewStatus(cameraSetting.lastUseMode == CameraModePhoto ?
                    CameraSetConstaint.CameraStatusPhotoModeNormal : CameraSetConstaint.CameraStatusMovieModeNormal);
        }

    }

    private void initCameraHeadView() {
        if (cameraSetting.lastUseMode == CameraModeMovie) {
            cameraSetView.setHeadTextViewText(ResourceUtils.getString(R.string.camra_set_head_video));
        } else if (cameraSetting.lastUseMode == CameraModePhoto) {
            cameraSetView.setHeadTextViewText(ResourceUtils.getString(R.string.camra_set_head_photo));
        } else {
            cameraSetView.setHeadTextViewText(ResourceUtils.getString(R.string.camra_set_head_general));
        }
    }

    private void initCameraSettingsRelativeViews() {
        initCameraHeadView();
        setISOShutterDimenssionLabelTexts();
        setGridLine();
    }

    private void initISOOrShutterToValidValue() {
        if (cameraSetting.lastUseMode == CameraSetConstaint.CameraModeMovie) {    // 视频模式
            if (cameraSetting.videoISOAuto) {      // 自动模式自动切换为曝光解除锁定
                updateAELockStatus(false, true);
                cmd.swithAELock(this, false, false, null);
                return;
            }
            updateAELockStatus(true, false);
            if (cameraSetting.videoISOAutoValue || cameraSetting.videoShutterAutoValue) {
                // 要开启测光锁定
                cmd.swithAELock(this, true, false, null);
            }
            if (cameraSetting.videoISOAutoValue) {
                CameraSetItem.FPScrollItem isoItem = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(cell_video_iso_key);
                isoItem.curIndex = 3;
                isoItem.curCommand = DefaultISOValue;
                cmd.setIso(this, isoItem.curCommand, false, null);
            }
            if (cameraSetting.videoShutterAutoValue) {
                CameraSetItem.FPScrollItem shutItem = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(cell_video_shutter_key);
                shutItem.curIndex = 23;
                shutItem.curCommand = DefaultShutterValue;
                cmd.setShutter(this, shutItem.curCommand, false, null);
            }
        } else {
            if (cameraSetting.photoISOAuto) { // 自动模式
                updateAELockStatus(false, true);
                cmd.swithAELock(this, false, true, null);
                return;
            }
            updateAELockStatus(false, false);
            if (cameraSetting.photoISOAutoValue || cameraSetting.photoShutterAutoValue) {
                cmd.swithAELock(this, false, true, null);
            }
            if (cameraSetting.photoISOAutoValue) {
                CameraSetItem.FPScrollItem isoItem = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(cell_photo_iso_key);
                isoItem.curIndex = 3;
                isoItem.curCommand = DefaultISOValue;
                cmd.setIso(this, isoItem.curCommand, true, null);
            }
            if (cameraSetting.photoISOAutoValue) {
                CameraSetItem.FPScrollItem shutItem = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(cell_photo_shutter_key);
                shutItem.curIndex = 23;
                shutItem.curCommand = DefaultShutterValue;
                cmd.setShutter(this, shutItem.curCommand, true, null);
            }
        }
    }

    private void showZoomView(boolean show) {
        if (ConnectManager.getInstance().mProductModel.productType < ConstantFields.ProductType_4kAir) {
            return;
        }

        if (show) {
            // 显示变焦控件
            if (zoomSlider != null && cameraSetting.lastUseMode == CameraSetConstaint.CameraModeMovie && cameraSetView.getVisibility() == View.GONE) {
                zoomSlider.setZoomVisibility(true);
            } else {
                zoomSlider.setZoomVisibility(false);
            }
        } else {
            // 隐藏变焦控件
            if (zoomSlider.isZoomShow()) {
                zoomSlider.hideSliderAndreleaseTimer();
                zoomSlider.setZoomVisibility(false);
            }
        }
    }

    @OnClick(R.id.camera_plaback_btn)
    void onTapPlaybackBtton() {

    }

    private void setCameraViewStatus(@CameraSetConstaint.CameraStatus int status) {
        operateView.setViewStatus(status);

        if (!sdCardInserted && !internalsdCard) { //zsz:todo 没办法了 只能这样写了
            operateView.setViewStatus(CameraSetConstaint.CameraStatusSDCardNotInsert);
            playbackButton.setEnabled(false);
            playbackButton.setAlpha(0.5f);
        } else {
            if (status == CameraSetConstaint.CameraStatusPhotoModeNormal || status == CameraSetConstaint.CameraStatusMovieModeNormal
                    || status == CameraSetConstaint.CameraStatusSDCardFull) {
                playbackButton.setEnabled(true);
                playbackButton.setAlpha(1.0f);
            } else {
                // TODO: 2019/10/18  包括低速卡，应该是不考虑让低速卡进入媒体库的
                playbackButton.setEnabled(false);
                playbackButton.setAlpha(0.5f);
            }
        }
    }

    private void stopContinuTakePhoto() {
        if (shutterTimer != null) {
            shutterTimer.cancel();
            shutterTimer = null;
        }
        operateView.functionButton.setText("");
        isContinueShutter = false;
    }

    public void playerSound(int voiceRawId) {
        /**播放声音*/
        if (mSoundPlayer != null) {
            mSoundPlayer.reset();
            mSoundPlayer = null;
        }
        mSoundPlayer = MediaPlayer.create(this, voiceRawId);
        if (mSoundPlayer != null) {
            mSoundPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mSoundPlayer.start();
        } else {
            LogUtils.d("音频播放器为null");
        }
    }

    private VideoPlayer.PlayCallback mVideoPlayCallback = new VideoPlayer.PlayCallback() {
        @Override
        public void onConnectSucces(int w, int h) {
            LogUtils.d("视频 宽 " + w + "高 " + h);
            videoView.limitScope(w, h);
            isVideoPlayed = true;
            dismissKpDialog();
        }

        @Override
        public void onConnectError() {
            LogUtils.d("onConnectError");
            CameraCommand.getCmdInstance().rebootCamera();
            stopVideo();
            ConnectManager.getInstance().pauseConnectLision();

            HandlerUtils.postDelayed(new Runnable() {
                @Override
                public void run() {
                    smartView.stopDelayTimer();

                    ConnectManager.getInstance().startConnectLision();
                }
            }, 5000);
        }
    };


    private void loadPlaneModel(ProductModel productModel) {
        if (productModel.productType == ConstantFields.ProductType_4k) {
            videoView.meteringwith = 9;
            videoView.meteringHeight = 5;
            cameraSetting = new FP4KCameraSettings();
            removeSliderWhenNot6K();
        } else if (productModel.productType == ConstantFields.ProductType_4kAir) {
            videoView.meteringwith = 9;
            videoView.meteringHeight = 5;
            cameraSetting = new FP4KAirCameraSettings();
            loadZoomSliderFor6kAir();
        } else if (productModel.productType == ConstantFields.ProductType_6kAir) {
            videoView.meteringwith = 16;
            videoView.meteringHeight = 8;
            cameraSetting = new FP6KCameraSettings();
            loadZoomSliderFor6kAir();
        }
    }

    /**
     * 与飞机连接成功，仅仅只是连接上相机
     * 手动调用Mavlink端口进行连接
     */
    @Override
    protected void planeConnected(ProductModel productModel) {
        super.planeConnected(productModel);
        isLowBatteryDialogShownAndDismissFromUser = false;
        isForcedReturnDialogShownAndDismissFromUser = false;
        LogUtils.d("planeConnected " + productModel.videoUrl + "thread ==>" + Thread.currentThread());

        initCameraSetttins();
//        if (topbarInfoView != null)
//            topbarInfoView.updateWifiLevel(Math.abs(NetworkUtils.getWifiLevel()));

        mPlayer.init(productModel.videoUrl);
        loadPlaneModel(productModel);
        smartView.dismissDelayTime();
        update(productModel);
    }

    private void update(ProductModel productModel) {
        if (productModel.dataSocketIp.contains(".4.") || productModel.remoteIpAddress.contains(".2.")) {
            if (mRSSTimer == null)
                mRSSTimer = new BaseTimer();
            mRSSTimer.startTimer(1000, new BaseTimer.TimerCallBack() {

                @Override
                public void callbackThread(Handler timerHandler) {
                    String result = "";
                    if (productModel.dataSocketIp.contains(".4.")) {
                        try {
                            String cmdResult = RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP41, ConstantFields.SHELL_CMD.PING_CMD);
                            if (!StringUtils.isEmpty(cmdResult)) {
                                result = cmdResult.trim();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSchException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (productModel.remoteIpAddress.contains(".2.")) {
                            if (NativeCode.initSocket("192.168.3.2", 1234, 3)) {
//                        MLog.log("initSocket成功111");
                                String str = NativeCode.getRSSI();
//                        MLog.log("rssi " + str);
                                if (str == null)
                                    str = "";
                                result = str;
                            }
                        }
                    }
                    if (timerHandler != null) {
                        Message msg = new Message();
                        msg.what = 0;
                        msg.obj = result;
                        timerHandler.sendMessage(msg);
                    }
                }

                @Override
                public void callbackMainThread(Message msg) {
                    String str = (String) msg.obj;
                    if (ConnectManager.getInstance().isConneted()) {
                        if (!StringUtils.isEmpty(str)) {
                            if (topbarInfoView != null) {
                                if (productModel.dataSocketIp.contains(".4.")) {
//                                        LogUtils.d("返回的cmdResult=" + str);
                                    int value = Integer.valueOf(str);
                                    int types[] = {10, 12, 14, 17};
                                    topbarInfoView.updateWifiLevel(value, types);
                                } else {
                                    if (productModel.remoteIpAddress.contains(".2.")) {
                                        int types[] = {10, 20, 35, 50};
                                        topbarInfoView.updateWifiLevel(Integer.valueOf(str) + 100, types);
                                    }
                                }
                            }
                        } else {
                            int types[] = {10, 20, 35, 50};
                            if (topbarInfoView != null)
                                topbarInfoView.updateWifiLevel(100, types);
                        }
                    } else {
                        int types[] = {10, 20, 35, 50};
                        if (topbarInfoView != null) topbarInfoView.updateWifiLevel(0, types);
                        mRSSTimer.killTimer();
                    }
                }
            });
        } else {
            int types[] = {10, 20, 35, 50};
            if (topbarInfoView != null) topbarInfoView.updateWifiLevel(100, types);
        }
//        if (productModel.dataSocketIp.contains(".4.") || productModel.remoteIpAddress.contains(".2.")) {
//            if (mRSSIObservable == null) {
//                mRSSIObservable = new RxLoopObserver<String>() {
//                    @Override
//                    public void onNext(String str) {
//                        super.onNext(str);
//                        if (ConnectManager.getInstance().isConneted()) {
//                            if (!StringUtils.isEmpty(str)) {
//                                if (topbarInfoView != null) {
//                                    if (productModel.dataSocketIp.contains(".4.")) {
////                                        LogUtils.d("返回的cmdResult=" + str);
//                                        int value = Integer.valueOf(str);
//                                        int types[] = {10, 12, 14, 17};
//                                        topbarInfoView.updateWifiLevel(value, types);
//                                    } else {
//                                        if (productModel.remoteIpAddress.contains(".2.")) {
//                                            int types[] = {10, 20, 35, 50};
//                                            topbarInfoView.updateWifiLevel(Integer.valueOf(str) + 100, types);
//                                        }
//                                    }
//                                }
//                            } else {
//                                this.disposeDisposables();
//                                int types[] = {10, 20, 35, 50};
//                                if (topbarInfoView != null)
//                                    topbarInfoView.updateWifiLevel(100, types);
//                                mRSSIObservable = null;
//                                update(productModel);
//                            }
//                        } else {
//                            this.disposeDisposables();
//                            int types[] = {10, 20, 35, 50};
//                            if (topbarInfoView != null) topbarInfoView.updateWifiLevel(0, types);
//                        }
//                    }
//                };
//            }
//            RxLoopSchedulers.composeLoop(this, 0, 1000, new Function() {
//                @Override
//                public String apply(Object o) throws Exception {
//                    if (productModel.dataSocketIp.contains(".4.")) {
//                        String cmdResult = RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP41, ConstantFields.SHELL_CMD.PING_CMD);
//                        if (!StringUtils.isEmpty(cmdResult)) {
//                            return cmdResult.trim();
//                        }
////                        return "-10";
//                    } else {
//                        if (productModel.remoteIpAddress.contains(".2.")) {
//                            if (NativeCode.initSocket("192.168.3.2", 1234, 3)) {
////                        MLog.log("initSocket成功111");
//                                String str = NativeCode.getRSSI();
////                        MLog.log("rssi " + str);
//                                if (str == null)
//                                    str = "";
//                                return str;
//                            }
//                        }
//                    }
//                    return "";
//                }
//            }).subscribe(mRSSIObservable);
//        } else {
//            int types[] = {10, 20, 35, 50};
//            if (topbarInfoView != null) topbarInfoView.updateWifiLevel(100, types);
//        }
    }

    @Override
    protected void planeMavLinkConnected() {
        super.planeMavLinkConnected();
        PlaneCommand.getInstance().initMavlinkParams();
    }

    private void sendMessage(int receiverId, int action, Object obj) {
        NotifyMessageMode msg = new NotifyMessageMode();
        msg.receiver = receiverId;
        msg.actionType = action;
        msg.object = obj;
        NotifyMessagers.getInstance().sendNotifyMessage(msg);
    }

    @Override
    protected void planeMavLinkRead(BaseMavlinkEntity mavMsg) {
        super.planeMavLinkRead(mavMsg);
        if (cameraSetView.getVisibility() == View.GONE) {
            showZoomView(true);
        } else {
            showZoomView(false);
        }
        switch (mavMsg.getMsgId()) {
            case MAVLINK_MSG_ID_BATTERY_STATUS:
                /**电池数据(飞行模式，电池数据，卫星数，距离，速度，围栏，解锁)*/
                batteryBean = (BatteryBean) mavMsg;
                bottomInfoView.upDatePlaneInfo(batteryBean);
                zoomSlider.upDatePlaneInfo(batteryBean);
                topbarInfoView.updateFlyStatus(batteryBean.isRcConnented(), batteryBean.getFlyStatus(), batteryBean.getCustomMode());
                topbarInfoView.updateBatteryImg(batteryBean.getCurBattey(), lBean != null ? lBean.getRemainTime() : 6);
                topbarInfoView.updateGpsImg(batteryBean.getSatellites());
                if (batteryBean.isReturnMode()) {
                    //返航模式
                    mActionDalog.showWithTitle(true, ResourceUtils.getString(R.string.return_mode),
                            ResourceUtils.getString(R.string.long_press_return), null,
                            ResourceUtils.getString(R.string.confirm), (int action) -> {
                            }, null);
                }
                sendMessage(MAVLINK_MSG_ID_BATTERY_STATUS, UPDATE_BATTERY_VALUE, batteryBean);
                sendMessage(COMPASS_STATE, COMPASS_STATE, topbarInfoView.isCompassAnomaly());
                sendMessage(GYROSCOPE_STATE, GYROSCOPE_STATE, topbarInfoView.isGyroscopeAnomaly());
//                RxBusUtils.getDefault().post(new RxbusBean(String.valueOf(MAVLINK_MSG_ID_BATTERY_STATUS), batteryBean));
                smartView.setPlaneStatus(batteryBean);
                zoomSlider.setPlaneStatus(batteryBean);
//                batteryBean.setCapacity(100);
            /*    if (batteryBean.getCapacity() < 50 && batteryBean.getCapacity() > 30) {
//                    HintItemBean hintItemBean = new HintItemBean();
//                    hintItemBean.setType(ConstantFields.HINT_LAYOUT_TYPE.LOW_BATTERY);
//                    hintItemBean.setHintText(ResourceUtils.getString(R.string.power_lower_than_50));
//                    hintItemBean.setTextColor(R.color.color_fe9700);
//                    hintLayout.addItem(hintItemBean);

                } else */
                if (batteryBean.getCurBattey() > 10 && batteryBean.getCurBattey() <= 30) {
                    if (batteryBean.isUnlocked()) { //已解锁
                        HintItemBean hintItemBean = new HintItemBean();
                        hintItemBean.setType(ConstantFields.HINT_LAYOUT_TYPE.LOW_BATTERY);
                        hintItemBean.setHintText(ResourceUtils.getString(R.string.low_battery_must_return));
                        hintItemBean.setTextColor(R.color.color_f34235);
                        hintLayout.addItem(hintItemBean);
                        LogUtils.d("显示立即返航对话框" + batteryBean.getCurBattey());
                        showLowBatteryDialog(false);
                    }
                } else if (batteryBean.getCurBattey() <= 10) {
                    //强制返航
                    if (batteryBean.isUnlocked()) {
                        //已解锁
                        HintItemBean hintItemBean = new HintItemBean();
                        hintItemBean.setType(ConstantFields.HINT_LAYOUT_TYPE.LOW_BATTERY);
                        hintItemBean.setHintText(ResourceUtils.getString(R.string.battery_seriously_low_forcing_landing));
                        hintItemBean.setTextColor(R.color.color_f34235);
                        hintLayout.addItem(hintItemBean);
//                        LogUtils.d("显示立即返航对话框" + baty);
                        if (!isForcedReturnDialogShownAndDismissFromUser) {
//                        LogUtils.d("显示强制返航对话框");
                            isLowBatteryDialogShownAndDismissFromUser = false;
                            showLowBatteryDialog(true);
                        }
                    }
                } else {
                    if (lBean != null && (lBean.getRemainTime() > 5 || lBean.getRemainTime() <= 0))
                        hintLayout.removeItem(ConstantFields.HINT_LAYOUT_TYPE.LOW_BATTERY);
                }
                break;
            case MAG_CAL_PROGRESS:
                LogUtils.d("发送指南针校准进度");
                sendMessage(CALI_COMPASS_PROGRESS, CALI_COMPASS_PROGRESS, mavMsg);
//                RxBusUtils.getDefault().post(new RxbusBean(String.valueOf(MAG_CAL_PROGRESS), mavMsg));
                break;
            case MAVLINK_MSG_ID_MAG_CAL_REPORT:
                CalibrationSuccessBean calibSuccessBean = (CalibrationSuccessBean) mavMsg;
                sendMessage(CALI_COMPASS_PROGRESS, CALI_COMPASS_PROGRESS, calibSuccessBean.isCalibCompassSuccess());
//                RxBusUtils.getDefault().post(new RxbusBean(String.valueOf(MAVLINK_MSG_ID_MAG_CAL_REPORT), calibSuccessBean.isCalibCompassSuccess()));
                break;
            case MAVLINK_MSG_ID_COMMAND_ACK:
                AckCommandBean ackBean = (AckCommandBean) mavMsg;
//                LogUtils.d("ACK返回" + ackBean.getCommand() + "|||" + ackBean.getRequestCode());
                if (ackBean.getCommand() == MAV_CMD.MAV_CMD_PREFLIGHT_CALIBRATION) {
                    //陀螺仪校准结果回复
                    sendMessage(CALI_COMPASS_PROGRESS, CALI_COMPASS_PROGRESS, ackBean.isCalibGyroSuccess());
//                    RxBusUtils.getDefault().post(new RxbusBean(String.valueOf(MAVLINK_MSG_ID_MAG_CAL_REPORT), ackBean.isCalibGyroSuccess()));
                } else if (ackBean.getCommand() == MAV_CMD.MAV_CMD_DO_SET_SERVO) {
                    //云台旋转成功
                    if (cameraSetting != null)
                        smartView.servoSuccess();
                } else if (ackBean.getCommand() == MAVLINK_MSG_ID_LOCATION) {
                    /////229返回
                    if (ackBean.getRequestCode() == MAV_CMD.TYPE_PANOR_MODE) {
                        LogUtils.d("全景朝北===" + lBean.getType());
                        smartView.changePanorModeSuccess(true);
                    } else if (ackBean.getRequestCode() == MAV_CMD.TYPE_READ_WAYPOINT) {
                        //开始写入航点
                        smartView.startWaypoint((int) Float.parseFloat(batteryBean.getFlyAlt()));
                    } else if (ackBean.getRequestCode() == MAV_CMD.TYPE_WAYPOINT_WRITE) {
                        //循环写入航点
                        smartView.writeWaypoint((int) Float.parseFloat(batteryBean.getFlyAlt()));
                    } else if (ackBean.getCommand() == MAV_CMD.TYPE_WAYPOINT_FLY) {
                        smartView.executeWaypointFlySuccess(true);
                    }
                } else if (ackBean.getCommand() == MAV_CMD.MAV_CMD_CONDITION_YAW) {
                    if (ackBean.getRequestCode() == MAVLINK_MSG_ID_COMMAND_LONG) {
//                        smartView.yawSuccess(true);
                    }
                }
                break;
            case MAVLINK_MSG_ID_PARAM_VALUE:
                /**这个消息只在初始连接时获取一次,所以在这里更新的时候，做持久化存储，主动拿一次*/
                mPlaneParamBean = (PlaneParamsBean) mavMsg;
//                LogUtils.d("升降速度====>" + mPlaneParamBean.toString());
                mReturnBackLy.setReturnHeight(mPlaneParamBean.getReturnAlt(), mPlaneParamBean.getFenceMaxAlt());
                smartView.setWaypoitSpeed(mPlaneParamBean.getWayPointSpeed());
                zoomSlider.setPlaneParam(mPlaneParamBean);
                sendMessage(MAVLINK_MSG_ID_PARAM_VALUE, UPDATE_SPEED_VALUE, mavMsg);
//                RxBusUtils.getDefault().post(new RxbusBean(String.valueOf(MAVLINK_MSG_ID_PARAM_VALUE), mavMsg));
                break;
            case MAVLINK_MSG_ID_LOCATION:
                /**位置信息(主要数据格式为json数据格式，包含：经纬度，剩余电池使用时间，环绕半径，物理按键，升降速度，前臂灯，功能性数据)*/
                lBean = (LocationBean) mavMsg;
                if (lBean.getType() == MAV_CMD.TYPE_PLANE_SPEED) {
                    bottomInfoView.upDateLiftSpeed(ConvertUtils.floatToString(lBean.getCv()));
                } else if (lBean.getType() == MAV_CMD.TYPE_TAKE_PHOTO) {
                    LogUtils.d("物理拍照按键");
                    if (!smartView.isSmartWithPhoto()) {
                        if (operateView.functionButton.isEnabled())
                            onTapPhotoOrRecordButton();
                    }
                } else if (lBean.getType() == MAV_CMD.TYPE_TAKE_OFF) {
                    if (lBean.isCanStartFly()) {
                        //一键起飞
                        if (batteryBean != null && batteryBean.getFlyStatus() == 100 && (batteryBean.getCustomMode() == 5)) {
                            mActionDalog.showWithTitle(true, ResourceUtils.getString(R.string.key_to_take_off),
                                    ResourceUtils.getString(R.string.ensure_takeoff_environment), null,
                                    ResourceUtils.getString(R.string.confirm), (int action) -> {
                                        PlaneCommand.getInstance().startFly();
                                        ToastUtils.showLongToast(ResourceUtils.getString(R.string.confirm_take_off));
                                    }, null);
                        } else {
                            mActionDalog.showWithTitle(false, ResourceUtils.getString(R.string.key_to_take_off),
                                    ResourceUtils.getString(R.string.key_to_take_off_for_gps), null,
                                    ResourceUtils.getString(R.string.confirm), (int action) -> {
                                    }, null);
                        }
                    }
                } else if (lBean.getType() == MAV_CMD.TYPE_AUTO_TURNBACK) {
                    //自动返航（姿态）说明：{“type”:1011,“roll”:10,//单位度“pitch”:10//单位度“yaw”:360//单位度}
                    if (lBean.getLocaLt() > -90 && lBean.getLocaLt() < 90) {
                        MapboxLatLng loc = new MapboxLatLng(lBean.getLocaLt(), lBean.getLocaLg());
                        lBean.setMapboxLocaLtlg(loc);
                    }
                    smartView.readLocationData(lBean);
                    zoomSlider.setPitch(lBean.getPitch());
                } else if (lBean.getType() == MAV_CMD.TYPE_CURRENT_POSITION) {
                    //当前位置{“type”:1010,“lt”:123.456789,“lg”:12.345678,“alt”:100,//单位m}
                    if (lBean.getLocaLt() >= -90 && lBean.getLocaLt() <= 90) {
                        MapboxLatLng loc = new MapboxLatLng(lBean.getLocaLt(), lBean.getLocaLg());
                        lBean.setMapboxLocaLtlg(loc);
                    }
                    smartView.readLocationData(lBean);
                } else if (lBean.getType() == MAV_CMD.TYPE_AROUND_RADIUS) {
                    if (lBean.getLocaLt() >= -90 && lBean.getLocaLt() <= 90) {
                        MapboxLatLng loc = new MapboxLatLng(lBean.getLocaLt(), lBean.getLocaLg());
                        lBean.setMapboxLocaLtlg(loc);
                    }
                    smartView.readLocationData(lBean);
                } else if (lBean.getType() == MAV_CMD.TYPE_PANOR_MODE) {
                    smartView.changePanorModeSuccess(true);
                } else if (lBean.getType() == MAV_CMD.TYPE_LED_CTRL) {
                    cameraSetting.qianbideng = lBean.isLedCtrlOpened();
                } else if (lBean.getType() == MAV_CMD.TYPE_LED_CTRL_HOU) {
                    cameraSetting.houbideng = lBean.isLedCtrlHouOpened();
                } else if (lBean.getType() == MAV_CMD.TYPE_PLANE_HEAD) {
                    if (!bottomInfoView.isDroneHeadShow() && !lBean.isPleneHeaderPositive()) {
                        mActionDalog.showWithTitle(false, ResourceUtils.getString(R.string.prompt) + "！",
                                ResourceUtils.getString(R.string.head_change_hint_text),
                                ResourceUtils.getString(R.string.indicate_head_change_hint_text),
                                ResourceUtils.getString(R.string.confirm),
                                (int action) -> {
                                }, null);
                    } else {
                        if (lBean.isPleneHeaderPositive())//机头调整正确了，直接取消对话框
                            if (mActionDalog.isShowing() && StringUtils.equals(mActionDalog.message(), ResourceUtils.getString(R.string.head_change_hint_text))) {
                                mActionDalog.actionDialogDismiss();
                            }
                    }
                    bottomInfoView.showDroneHeadTv(!lBean.isPleneHeaderPositive());
                } else if (lBean.getType() == MAV_CMD.TYPE_HOME_POSITION) {
                    //地图中家的位置
                    mapViewLy.addHomePosition(lBean);
                } else if (lBean.getType() == MAV_CMD.TYPE_REMAINE_MIN) {
                    int remainMin = lBean.getRemainTime();
                    if (batteryBean != null && batteryBean.getCurBattey() > 10) {
                        if (remainMin > 0 && remainMin <= 5) {
                            /**低电量*/
                            if (batteryBean != null && batteryBean.isUnlocked()) {
                                HintItemBean bean = new HintItemBean();
                                bean.setType(ConstantFields.HINT_LAYOUT_TYPE.LOW_BATTERY);
                                bean.setTextColor(R.color.color_f34235);
                                bean.setHintText(getResources().getString(R.string.low_battery_must_return));
                                hintLayout.addItem(bean);
                                showLowBatteryDialog(false);
                            } else {
                                hintLayout.removeItem(ConstantFields.HINT_LAYOUT_TYPE.REMAINING_FLIGHT_TIME);
                            }
                        } else {
                            hintLayout.removeItem(ConstantFields.HINT_LAYOUT_TYPE.REMAINING_FLIGHT_TIME);
                        }
                    }
                }
                break;
            case MAVLINK_MSG_ID_AUTOPILOT_VERSION:
                verisionBean = (AutopilotVerisionBean) mavMsg;
                smartView.setAdjustControlBntVisibility(isNeedShowContorlLy());
                break;
        }
    }

    //立即返航对话框是否被用户手动取消
    boolean isForcedReturnDialogShownAndDismissFromUser = false;
    //强制返航对话框是否被用户手动取消
    boolean isLowBatteryDialogShownAndDismissFromUser = false;
    boolean mIsForced;

    /**
     * 低电量返航
     *
     * @param isForced 是否强制
     */
    private void showLowBatteryDialog(boolean isForced) {
        this.mIsForced = isForced;
        if (!isLowBatteryDialogShownAndDismissFromUser) {
            isLowBatteryDialogShownAndDismissFromUser = true;
            if (mActionDalog != null)
                mActionDalog.showWithTitle(!isForced,
                        ResourceUtils.getString(isForced ? R.string.forced_landing : R.string.low_electricity_return),
                        ResourceUtils.getString(isForced ? R.string.forced_landing_hint : R.string.low_battery_must_return),
                        null,
                        ResourceUtils.getString(isForced ? R.string.confirm : R.string.immediately_return),
                        (int action) -> {
                            // TODO: 2019/8/25 立即返航
                            isLowBatteryDialogShownAndDismissFromUser = true;
                            if (mIsForced) {
                                isForcedReturnDialogShownAndDismissFromUser = true;
                            } else {
                                //非强制返航
                                PlaneCommand.getInstance().changeFlyMode(ApmModes.ROTOR_RTL);
                                smartView.setReturning(true);
                            }
                        }, new ActionDialog.ActionDialogCancelListener() {
                            @Override
                            public void onCancelCallback() {
                                isLowBatteryDialogShownAndDismissFromUser = true;
                            }
                        });
        }
    }

    @Override
    protected void planeDisConnected() {
        super.planeDisConnected();
        LogUtils.d("断开飞机连接....");
        topbarInfoView.updateGpsImg(0);
        topbarInfoView.updateBatteryImg(0, 0);
        topbarInfoView.updateFlyStatus(false, 0, 0);
        int types[] = {10, 20, 35, 50};
        topbarInfoView.updateWifiLevel(0, types);
        if (mRSSTimer != null) mRSSTimer.killTimer();
//        if (mRSSIObservable != null) mRSSIObservable.disposeDisposables();
        setCameraViewStatus(CameraSetConstaint.CameraStatusDisable);
        stopVideo();
        isVideoPlayed = false;
//        RemoteJschUtils.getInstance().closeJsch();
    }

    private void initCameraSetttins() {
        cmd.setCareraTime();

        // 更新SD卡状态
        updateSDCardStatus();

        // 获取变焦倍数 6k才有
        int productType = ConnectManager.getInstance().mProductModel.productType;
        if (productType == ConstantFields.ProductType_6kAir || productType == ConstantFields.ProductType_4kAir) {
            cmd.getZoomSliderValueComplete(this, new CameraCommandCallback<Integer>() {
                @Override
                public void onComplete(Integer cb) {
                    if (cb >= 0) {
                        lastScale = cb;
                        setSuofangLabelTexts();
                        zoomSlider.setCurrentValue(lastScale);
                        zoomSlider.updateEnableStatus();
                        showZoomView(true);
                    }
                }
            });
        }

        cmd.getCameraStatus(this, new CameraCommandCallback<Integer>() {
            @Override
            public void onComplete(Integer status) {
                if (status == CameraSetConstaint.CameraValue_vf) {
                    cameraSetting.lastUseMode = CameraSetConstaint.CameraModeMovie;
                    fetchSetting(false);
                } else if (status == CameraSetConstaint.CameraValue_photo_vf) {
                    cameraSetting.lastUseMode = CameraModePhoto;
                    fetchSetting(false);
                } else if (status == CameraSetConstaint.CameraValue_photo_idle) {
                    cameraSetting.lastUseMode = CameraModePhoto;
                    fetchSetting(true);
                } else if (status == CameraSetConstaint.CameraValue_idle) {
                    cameraSetting.lastUseMode = CameraSetConstaint.CameraModeMovie;
                    fetchSetting(true);
                } else if (status == CameraSetConstaint.CameraValue_record) {    // 正在录制中
                    cameraSetting.lastUseMode = CameraSetConstaint.CameraModeMovie;
                    cameraSetting.isRecording = true;

                    cmd.getRecordTime(CameraActivity.this, new CameraCommandCallback<Integer>() {
                        @Override
                        public void onComplete(Integer cb) {
                            recordSec = cb;
                            fetchSetting(false);
                        }
                    });
                } else if (status == CameraSetConstaint.CameraValue_photo_mode) {    // 正在拍照中
                    cameraSetting.lastUseMode = CameraModePhoto;
                    setCameraViewStatus(CameraSetConstaint.CameraStatusDisable);
                    operateView.startTakePhotoAnimation();

                    cmd.readTakePhotoResultComplete(CameraActivity.this, new CameraCommandCallback<Integer>() {
                        @Override
                        public void onComplete(Integer cb) {
                            fetchSetting(false);
                        }
                    });
                } else {
                    // 相机初始化 出错
                }
            }
        });

        cmd.setmStatusCallback((int status) -> {
            if (status == 2305) {      // sd卡插入
                sdCardInserted = true;
                bottomInfoView.hideSDCardLabel();

                if (cameraSetting.lastUseMode == CameraModeMovie) {
                    setCameraViewStatus(CameraSetConstaint.CameraStatusMovieModeNormal);
                } else {
                    setCameraViewStatus(CameraSetConstaint.CameraStatusPhotoModeNormal);
                }
                // 对于有内置卡的 还需要重新更新一下卡容量状态
                updateSDCardStatus();
            } else if (status == 2306) {   // 卡拔出来了
                sdCardInserted = false;
                // 对于有内置卡的 还需要重新更新一下卡容量状态
                updateSDCardStatus();
            } else if (status == 2307) {
                sdCardInserted = true;
                bottomInfoView.showSDCardLabelWithDes(ResourceUtils.getString(R.string.sd_card_full));
                setCameraViewStatus(CameraSetConstaint.CameraStatusSDCardFull);

                // 停止录制
                stopRecord();
            } else if (status == 2308) {
                sdCardInserted = false;
                bottomInfoView.showSDCardLabelWithDes(ResourceUtils.getString(R.string.reinsert_sd_card));
                setCameraViewStatus(CameraSetConstaint.CameraStatusSDCardNotInsert);
            } else if (status == SD_SPEED_SLOW) {
                //卡速慢导致录像停止
                if (cameraSetting != null && cameraSetting.isRecording) {
                    beginOrEndRecording(true);
                }
            } else if (status == SD_FRAGMENTATION) {
                //卡碎片化导致录像停止
                if (cameraSetting != null && cameraSetting.isRecording) {
                    beginOrEndRecording(true);
                }
            } else if (status == SD_LOW) {
                //低速卡
//                sdCardInserted = true;
//                bottomInfoView.showSDCardLabelWithDes(ResourceUtils.getString(R.string.sd_low_speed));
//                setCameraViewStatus(CameraSetConstaint.CameraStatusSDCardLow);

                // 停止录制
//                stopRecord();
            }
        });
    }

    private void stopRecord() {
        if (recordingTimer != null) {
            HandlerUtils.cancel(recordingTimer);
            recordingTimer = null;
            cameraSetting.isRecording = false;
            recordSec = 0;
            operateView.timeLabel.setText("");
            zoomSlider.stopRecord();
            setCameraViewStatus(CameraSetConstaint.CameraStatusMovieModeNormal);
        }
    }

    private void fetchSetting(boolean needResetVF) {
        cmd.getAllSetting(this, new CameraCommandCallback<SettingBean>() {
            @Override
            public void onComplete(SettingBean bean) {
                if (bean.getRval() == 0) {
                    LogUtils.d("相机------>获取设置项" + bean.getSettingParamBean().toString());
                    cameraSetting.updateCameraSettings(bean.getSettingParamBean());
                    if (cameraSetView.getVisibility() == View.VISIBLE) {
                        loadCameraSets();
                    }
                    initCameraSettingsRelativeViews();
                    initISOOrShutterToValidValue();
                    initCameraStatus();
                    showZoomView(true);

                    if (needResetVF) {
                        cmd.resetVf((boolean success) -> {
                            playVideo();
                        });
                    } else {
                        playVideo();
                    }

                    if (smartView != null) {
                        //刚连上时，如果不在延时拍摄中，则延时模式必须是关闭的
                        if (smartView.isDelayTimeTaking()) {
                        }
                        MLog.log("全景对话框已显示" + smartView.isTakingDialogShown());
                        if (smartView.isTakingDialogShown()) {
                            //刚连上时，如果已经在拍全景或者广角
                            smartView.getLastFileName(true, false);
                        }
                    }
                }
            }
        });
    }

    public synchronized void playVideo() {
        if (mPlayer != null) {
            mPlayer.startPlay();
        }
    }

    public synchronized void stopVideo() {
        if (mPlayer != null) {
            if (mPlayer.isPlayer())
                mPlayer.stopPlay();
        }
        isVideoPlayed = false;
    }

    private void stopVideoWhenSendCameraCommond() {
        if (ConnectManager.getInstance().mProductModel.productType == ConstantFields.ProductType_6kAir) {
            return;
        }

        isVideoPlayed = false;
        stopVideo();
    }

    //  =========== 相机设置 ======== //
    private ArrayList<MultiItemEntity> items() {
        ArrayList<MultiItemEntity> list = cameraSetting.photoOptionItems();
        int selectIndex = cameraSetView.getSelectIndex();
        if (cameraSetting.lastUseMode == CameraModePhoto) {  // 照片模式

            if (selectIndex == 0) {
                list = cameraSetting.photoEffectItems();
            } else if (selectIndex == 1) {
                list = cameraSetting.photoOptionItems();
            } else if (selectIndex == 2) {
                list = cameraSetting.generaSettingItems();
            }
        } else if (cameraSetting.lastUseMode == CameraSetConstaint.CameraModeMovie) {
            if (selectIndex == 0) {
                list = cameraSetting.videoEffectItems();
            } else if (selectIndex == 1) {
                list = cameraSetting.videoOptionItems();
            } else if (selectIndex == 2) {
                list = cameraSetting.generaSettingItems();
            }
        }

        return list;
    }

    private void updateSettingsIsDisable(boolean disable) {

        if (setViewAdapter != null) {
            setViewAdapter.setEnabled(!disable);
            setViewAdapter.notifyDataSetChanged();
        }
    }

    private void loadCameraSets() {
        ArrayList<MultiItemEntity> list = items();

        if (setViewAdapter == null) {
            setViewAdapter = new CameraSetAdapter(this, list);
            setViewAdapter.setSpanSizeLookup(new BaseQuickAdapter.SpanSizeLookup() {
                @Override
                public int getSpanSize(GridLayoutManager gridLayoutManager, int position) {
                    return 1;
                }
            });
            cameraSetView.setView.setAdapter(setViewAdapter);

            // 相机设置界面事件监听
            setViewAdapter.setCameraSetLisioner(this);
        } else {
            setViewAdapter.setNewData(list);
        }
    }

    // 切换到延时模式
    private void changeToDeleyMode(ResultCallback.BoolCallback callback) {
        int cMode = cameraSetting.lastUseMode;
        if (cMode == CameraSetConstaint.CameraModeMovie) {  //  此时处于视频模式
            if (!cameraSetting.videoISOAuto) {
                updateAELockStatus(false, false);

            } else {

            }
        } else {    // 此时处于照片模式

        }
    }

    // 切换到全景模式
    private void changeToPanoMode(ResultCallback.BoolCallback callback) {

    }

    // 切换到普通拍照模式
    private void changeToPhotoMode() {
        zoomSlider.setDragAble(false);

        cmd.changeToPhotoMode(this, new CameraCommandCallback<Boolean>() {
            @Override
            public void onComplete(Boolean success) {

                if (success) {
                    cameraSetting.lastUseMode = CameraModePhoto;
                    if (!cameraSetting.photoISOAuto) {     // 手动模式下
                        updateAELockStatus(false, false);
                        cmd.swithAELock(CameraActivity.this, false, true, null);
                        CameraSetItem.FPScrollItem item1 = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_video_iso_key);
                        CameraSetItem.FPScrollItem item2 = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_video_shutter_key);
                        CameraSetItem.FPScrollItem item11 = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_photo_iso_key);
                        CameraSetItem.FPScrollItem item22 = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_photo_shutter_key);

                        if ((item1.commands != null) && ((item1.commands.length + 1) >= item1.curIndex)) {
                            String send;
                            int index = -1;
                            send = item1.commands[item1.curIndex];
                            for (int i = 0; i < item11.values.length; i++) {
                                if (send.equals(item11.values[i])) {
                                    index = i;
                                    break;
                                }
                            }
                            if (index == -1) {
                                index = item11.values.length - 1 > 0 ? item11.values.length - 1 : 0;
                                send = item11.values[index];
                            }
                            final int finalIndex = index;
                            cmd.setIso(CameraActivity.this, send, true, new CameraCommandCallback<Boolean>() {
                                @Override
                                public void onComplete(Boolean cb) {
                                    if (cb) {
                                        item11.curIndex = finalIndex;
                                        item1.curIndex = finalIndex;
                                        cameraSetting.photoISO = finalIndex;
                                        cameraSetting.videoISO = finalIndex;
                                        setISOShutterDimenssionLabelTexts();
                                    }
                                }
                            });
                        }
                        if (item2.commands != null && item2.commands.length > item2.curIndex) {
                            String send;
                            int index = -1;
                            send = item2.commands[item2.curIndex];
                            for (int i = 0; i < item22.values.length; i++) {
                                if (send.equals(item22.values[i])) {
                                    index = i;
                                    break;
                                }
                            }
                            if (index == -1) {
                                index = item22.values.length - 1 > 0 ? item22.values.length - 1 : 0;
                                send = item22.values[index];
                            }
                            final int finalIndex = index;
                            cmd.setShutter(CameraActivity.this, send, true, new CameraCommandCallback<Boolean>() {
                                @Override
                                public void onComplete(Boolean cb) {
                                    if (cb) {
                                        item22.curIndex = finalIndex;
                                        item2.curIndex = finalIndex;
                                        cameraSetting.photoShutter = finalIndex;
                                        cameraSetting.videoShutter = finalIndex;
                                        setISOShutterDimenssionLabelTexts();
                                        loadCameraSets();
                                    }
                                }
                            });
                        }

                    } else {
                        updateAELockStatus(false, true);
                        cmd.swithAELock(CameraActivity.this, false, true, null);
                        CameraSetItem.FPScrollItem item = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_photo_baoguang_key);
                        String send = item.curCommand;
                        if (item.commands != null && item.commands.length > item.curIndex) {
                            send = item.commands[item.curIndex];
                        }
                        cmd.setEV(CameraActivity.this, send, true, null);
                    }
                    cmd.setZoomValue(CameraActivity.this, 0, null);
                    if (!cameraSetting.photoISOAuto) {  // 手动ISO
                        updateAELockStatus(false, false);
                    } else {
                        updateAELockStatus(false, true);
                    }
                    setISOShutterDimenssionLabelTexts();
                    setGridLine();
                    initCameraHeadView();

                    setCameraViewStatus(CameraSetConstaint.CameraStatusPhotoModeNormal);

                    if (cameraSetView.getVisibility() == View.VISIBLE) {
                        loadCameraSets();
                    }

                    showZoomView(false);
                }
                LogUtils.d("切换成拍照模式====>");
                playVideo();
            }
        });
    }

    // 切换到视频模式
    private void changeToMovieMode() {
        cmd.changeToVideoModeComplete(this, new CameraCommandCallback<Boolean>() {
            @Override
            public void onComplete(Boolean success) {
                if (success) {
                    cameraSetting.lastUseMode = CameraSetConstaint.CameraModeMovie;
                    if (!cameraSetting.videoISOAuto) {     // 手动模式下
                        updateAELockStatus(true, false);
                        cmd.swithAELock(CameraActivity.this, true, false, null);
                        CameraSetItem.FPScrollItem item1 = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_photo_iso_key);
                        CameraSetItem.FPScrollItem item2 = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_photo_shutter_key);
                        CameraSetItem.FPScrollItem item11 = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_video_iso_key);
                        CameraSetItem.FPScrollItem item22 = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_video_shutter_key);

                        if (item1.commands != null && item1.commands.length > item1.curIndex) {
                            String send;
                            int index = -1;
                            send = item1.commands[item1.curIndex];
                            for (int i = 0; i < item11.values.length; i++) {
                                if (send.equals(item11.values[i])) {
                                    index = i;
                                    break;
                                }
                            }
                            if (index == -1) {
                                index = item11.values.length - 1 > 0 ? item11.values.length - 1 : 0;
                                send = item11.values[index];
                            }
                            final int finalIndex = index;
                            cmd.setIso(CameraActivity.this, send, false, new CameraCommandCallback<Boolean>() {
                                @Override
                                public void onComplete(Boolean cb) {
                                    if (cb) {
                                        item11.curIndex = finalIndex;
                                        item1.curIndex = finalIndex;
                                        cameraSetting.photoISO = finalIndex;
                                        cameraSetting.videoISO = finalIndex;
                                        setISOShutterDimenssionLabelTexts();
                                    }
                                }
                            });
                        }
                        if (item2.commands != null && item2.commands.length > item2.curIndex) {
                            String send;
                            int index = -1;
                            send = item2.commands[item2.curIndex];
                            for (int i = 0; i < item22.values.length; i++) {
                                if (send.equals(item22.values[i])) {
                                    index = i;
                                    break;
                                }
                            }
                            if (index == -1) {
                                index = item22.values.length - 1 > 0 ? item22.values.length - 1 : 0;
                                send = item22.values[index];
                            }
                            final int finalIndex = index;
                            cmd.setShutter(CameraActivity.this, send, false, new CameraCommandCallback<Boolean>() {
                                @Override
                                public void onComplete(Boolean cb) {
                                    if (cb) {
                                        item22.curIndex = finalIndex;
                                        item2.curIndex = finalIndex;
                                        cameraSetting.photoShutter = finalIndex;
                                        cameraSetting.videoShutter = finalIndex;
                                        setISOShutterDimenssionLabelTexts();
                                        loadCameraSets();
                                    }
                                }
                            });
                        }

                    } else {
                        updateAELockStatus(false, true);
                        cmd.swithAELock(CameraActivity.this, false, false, null);
                        CameraSetItem.FPScrollItem item = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_video_baoguang_key);
                        String send = item.curCommand;
                        if (item.commands != null && item.commands.length > item.curIndex) {
                            send = item.commands[item.curIndex];
                        }
                        cmd.setEV(CameraActivity.this, send, false, null);
                    }
                    if (!cameraSetting.videoISOAuto) {  // 手动ISO
                        updateAELockStatus(true, false);
                    } else {
                        updateAELockStatus(false, true);
                    }
                    setISOShutterDimenssionLabelTexts();
                    setGridLine();
                    initCameraHeadView();

                    setCameraViewStatus(CameraSetConstaint.CameraStatusMovieModeNormal);

                    if (cameraSetView.getVisibility() == View.VISIBLE) {
                        loadCameraSets();
                    }
                    zoomSlider.setDragAble(true);
                    showZoomView(true);
                }
                LogUtils.d("切换成视频模式====>");
                playVideo();
            }
        });
    }

    // 更新SD卡状态
    private void updateSDCardStatus() {
        cmd.getSDCardStateComplete(this, new CameraCommandCallback<Integer>() {
            @Override
            public void onComplete(Integer internalCardSate) {
                if (internalCardSate == 1) {   // 支持内置卡，正在使用内置卡
                    sdCardInserted = false;
                    internalsdCard = true;
                    bottomInfoView.showSDCardLabelWithDes(ResourceUtils.getString(R.string.camra_sd_internal_use));
                    updateSDcardCaporityFor6kSerial();
                } else if (internalCardSate == 0) {   // 支持内置卡，但是使用的是外置卡
                    sdCardInserted = true;
                    internalsdCard = true;
                    bottomInfoView.hideSDCardLabel();
                    updateSDcardCaporityFor6kSerial();
                } else {                            // 不支持内置卡
                    cmd.getFreeSDCardInmain(CameraActivity.this, new CameraCommandCallback<Long>() {
                        @Override
                        public void onComplete(Long cb) {
                            cameraSetting.sdcardFree = cb.intValue();
                        }
                    });
                    cmd.getTotalSDCard(CameraActivity.this, new CameraCommandCallback<Integer>() {
                        @Override
                        public void onComplete(Integer cb) {
                            internalsdCard = false;
                            cameraSetting.sdcardTotal = cb;
                            if (cb > 0) {
                                sdCardInserted = true;
                                bottomInfoView.hideSDCardLabel();
                            } else {
                                sdCardInserted = false;
                                bottomInfoView.showSDCardLabelWithDes(ResourceUtils.getString(R.string.sd_card_not_inserted));
                                setCameraViewStatus(CameraSetConstaint.CameraStatusSDCardNotInsert);
                            }
                            if (cameraSetView.getVisibility() == View.VISIBLE) {
                                loadCameraSets();
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateSDcardCaporityFor6kSerial() {
        cmd.getTotalSDCard(this, new CameraCommandCallback<Integer>() {
            @Override
            public void onComplete(Integer cb) {
                if (cb > 0) {
                    cameraSetting.sdcardTotal = cb;
                }
            }
        });
        cmd.getFreeSDCardInmain(this, new CameraCommandCallback<Long>() {
            @Override
            public void onComplete(Long cb) {
                if (cb > 0) {
                    cameraSetting.sdcardFree = cb.intValue();
                }
                if (cameraSetView.getVisibility() == View.VISIBLE) {
                    loadCameraSets();
                }
            }
        });

    }

    @Override
    public void configFPTwoButtonCellClick(final CameraSetItem.FPTwoButtonCellItem item,
                                           int postion, final int clickIndex) {
        if (item.selectButtonIndex == clickIndex) {
            LogUtils.d("不用返回");
            return;
        }
        // 目前选择的按钮 索引
        final int preIndex = item.selectButtonIndex;
        String curCommand = item.curCommand;
        if (item.commands != null && item.commands.length >= 2) {
            curCommand = item.commands[clickIndex];
        }

        final int status = cameraSetting.lastUseMode == CameraSetConstaint.CameraModeMovie
                ? CameraSetConstaint.CameraStatusMovieModeNormal : CameraSetConstaint.CameraStatusPhotoModeNormal;

        if (item.keyId == CameraSetConstaint.cell_photo_mode_key) { // 拍照方式
            cameraSetting.photoMethod = clickIndex;
            cameraSetting.autoTakephoto = clickIndex == 1;
            item.selectButtonIndex = clickIndex;
            setViewAdapter.notifyAtPosition(postion);
        } else if (item.keyId == CameraSetConstaint.cell_photo_Atio_key) {  // 拍照尺寸

            cameraSetting.photoAtio = clickIndex;

            stopVideoWhenSendCameraCommond();
            setCameraViewStatus(CameraSetConstaint.CameraStatusDisable);
            updateSettingsIsDisable(true);
            cmd.setPhotoAtio(this, curCommand, new CameraCommandCallback<Boolean>() {
                @Override
                public void onComplete(Boolean success) {
                    playVideo();

                    if (success) {
                        item.selectButtonIndex = clickIndex;
                        setGridLine();
                        setISOShutterDimenssionLabelTexts();
                    } else {
                        // 还原
                        cameraSetting.photoAtio = preIndex;
                    }

                    updateSettingsIsDisable(false);
                    setCameraViewStatus(status);
                }
            });
        } else if (item.keyId == CameraSetConstaint.cell_photo_ruidu_key) {  // 拍照 锐度

            cmd.setPhotoSharpness(this, curCommand, new CameraCommandCallback<Boolean>() {
                @Override
                public void onComplete(Boolean success) {
                    playVideo();
                    if (success) {
                        cameraSetting.photoRuidu = clickIndex;
                    }
                    loadCameraSets();
                    setCameraViewStatus(status);
                }
            });
        } else if (item.keyId == CameraSetConstaint.cell_video_record_hd_key) {  // 设置视频编码方式

            stopVideoWhenSendCameraCommond();
            setCameraViewStatus(CameraSetConstaint.CameraStatusDisable);
            updateSettingsIsDisable(true);

            if (clickIndex == 0) {
                cameraSetting.recordType = CameraSetConstaint.FPEncodeTypeH264;
            } else {
                cameraSetting.recordType = CameraSetConstaint.FPEncodeTypeH265;
            }
            // 刷新一下
            setViewAdapter.updateItem();
            cameraSetting.videoOptionItems();
            CameraSetItem.FPDimenssionItem dItem = cameraSetting.currentVideoDimenSion();

            cmd.setDemession(this, dItem.curCommand, new CameraCommandCallback<Boolean>() {
                @Override
                public void onComplete(Boolean success) {
                    playVideo();
                    if (success) {
                        item.selectButtonIndex = clickIndex;
                        if (clickIndex == 0) {
                            cameraSetting.recordType = CameraSetConstaint.FPEncodeTypeH264;
                        } else {
                            cameraSetting.recordType = CameraSetConstaint.FPEncodeTypeH265;
                        }
                    } else {
                        // 还原
                        if (clickIndex == 0) {
                            cameraSetting.recordType = CameraSetConstaint.FPEncodeTypeH265;
                        } else {
                            cameraSetting.recordType = CameraSetConstaint.FPEncodeTypeH264;
                        }
                    }

                    // 再刷新一下
                    cameraSetting.videoOptionItems();

                    CameraSetItem.FPAccessryBaseCellItem item1 = (CameraSetItem.FPAccessryBaseCellItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_video_Dimesion_key);
                    // 相当于重置了
                    CameraSetItem.FPDimenssionItem dItem1 = cameraSetting.currentVideoDimenSion();
                    dItem1.currentfps = dItem1.defaultfps;
                    item1.rightTitle = dItem1.leftTitle + " " + dItem1.currentfps;
                    dItem1.checked = true;
                    updateSettingsIsDisable(false);
                    setCameraViewStatus(status);
                    setISOShutterDimenssionLabelTexts();
                }
            });
        } else if (item.keyId == CameraSetConstaint.cell_zoom_switch_key) { // 变焦速度按钮
            cameraSetting.zoomIndex = clickIndex;
            item.selectButtonIndex = clickIndex;
            updateSettingsIsDisable(false);
        }
    }

    @Override
    public void configFPSwitchClick(final CameraSetItem.FPSwitchCellItem item, int position,
                                    final boolean open) {

        // 目前选择的按钮 索引
        int status = cameraSetting.lastUseMode == CameraSetConstaint.CameraModeMovie
                ? CameraSetConstaint.CameraStatusMovieModeNormal : CameraSetConstaint.CameraStatusPhotoModeNormal;

        if (item.keyId == CameraSetConstaint.cell_photo_raw_key) { // raw格式

            cmd.swithRaw(this, open, new CameraCommandCallback<Boolean>() {
                @Override
                public void onComplete(Boolean success) {
                    if (success) {
                        item.switchOn = open;
                        cameraSetting.photoRaw = open;
                    } else {
                        cameraSetting.photoRaw = !open;
                    }
                    setViewAdapter.notifyDataSetChanged();
                }
            });
        } else if (item.keyId == CameraSetConstaint.cell_video_bit_key) {
            cmd.swith10bit(this, open, new CameraCommandCallback<Boolean>() {
                @Override
                public void onComplete(Boolean success) {
                    if (success) {
                        item.switchOn = open;
                        setViewAdapter.notifyDataSetChanged();
                    }
                }
            });
        } else if (item.keyId == CameraSetConstaint.cell_qianbideng_key) {
            PlaneCommand.getInstance().swithQianbideng(open);
            // 先这样写吧
            item.switchOn = open;
            cameraSetting.qianbideng = open;
            setViewAdapter.notifyDataSetChanged();
        } else if (item.keyId == CameraSetConstaint.cell_houbideng_key) {
            PlaneCommand.getInstance().switchHoubideng(open);
            // 先这样写吧
            item.switchOn = open;
            cameraSetting.houbideng = open;
            setViewAdapter.notifyDataSetChanged();
        } else if (item.keyId == CameraSetConstaint.cell_yt_hori_set_key) {

            // 先这样写吧
            item.switchOn = open;
            cameraSetting.ytHoriSet = open;
            setViewAdapter.notifyDataSetChanged();

            bottomInfoView.ytLY.setVisibility(open ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void configFPThreeButtonClick(CameraSetItem.FPThreeButtonItem item, int position,
                                         int clickIndex) {
        if (item.selectButtonIndex == clickIndex) {
            return;
        }

        String curCommand = item.curCommand;
        if (item.commands != null && item.commands.length >= 2) {
            curCommand = item.commands[clickIndex];
        }
        int status = cameraSetting.lastUseMode == CameraSetConstaint.CameraModeMovie
                ? CameraSetConstaint.CameraStatusMovieModeNormal : CameraSetConstaint.CameraStatusPhotoModeNormal;

        if (item.keyId == CameraSetConstaint.cell_genera_grid_key) {      //设置网格线
            CacheManager.getSharedPrefUtils().putInt(ConstantFields.PREF.Grid_type, clickIndex);
            cameraSetting.gridMode = clickIndex;
            item.selectButtonIndex = clickIndex;
            setViewAdapter.notifyDataSetChanged();
            setGridLine();
        } else if (item.keyId == CameraSetConstaint.cell_genera_antiflicker_key) {
            stopVideoWhenSendCameraCommond();
            setCameraViewStatus(CameraSetConstaint.CameraStatusDisable);
            updateSettingsIsDisable(true);
            cmd.setAntifier(this, curCommand, new CameraCommandCallback<Boolean>() {
                @Override
                public void onComplete(Boolean success) {
                    playVideo();
                    if (success) {
                        item.selectButtonIndex = clickIndex;
                    }
                    updateSettingsIsDisable(false);
                    setCameraViewStatus(status);
                }
            });
        }
    }

    @Override
    public void configFPMiddleTwoButtonClick(CameraSetItem.FPMiddleTwoButtonCellItem item,
                                             int position, int clickIndex) {
        Boolean isAuto = clickIndex == 0;

        Boolean photo = item.keyId == CameraSetConstaint.cell_photo_isoShutterAuto_key;
        int isoKey = CameraSetConstaint.cell_photo_iso_key;
        int shutKey = CameraSetConstaint.cell_photo_shutter_key;
        int ohterKey = CameraSetConstaint.cell_photo_isoShutterAuto_key;
        if (!photo) {
            isoKey = CameraSetConstaint.cell_video_iso_key;
            shutKey = CameraSetConstaint.cell_video_shutter_key;
            ohterKey = CameraSetConstaint.cell_video_isoShutterAuto_key;
        }
        CameraSetItem.FPScrollItem isoitem = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(isoKey);
        CameraSetItem.FPScrollItem shutitem = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(shutKey);
        CameraSetItem.FPMiddleTwoButtonCellItem ohteritem = (CameraSetItem.FPMiddleTwoButtonCellItem) cameraSetting.itemForKeyId(ohterKey);
        stopVideoWhenSendCameraCommond();
        updateSettingsIsDisable(true);
        setCameraViewStatus(CameraSetConstaint.CameraStatusDisable);
        final int status = cameraSetting.lastUseMode == CameraSetConstaint.CameraModeMovie ?
                CameraSetConstaint.CameraStatusMovieModeNormal : CameraSetConstaint.CameraStatusPhotoModeNormal;
        cmd.swithIQAuto(this, isAuto, photo, new CameraCommandCallback<Boolean>() {
            @Override
            public void onComplete(Boolean success) {
                if (success) {
                    cameraSetting.photoISOAuto = isAuto;
                    cameraSetting.videoISOAuto = isAuto;
                    ohteritem.selectButtonIndex = clickIndex;
                    item.selectButtonIndex = clickIndex;
                    setISOShutterDimenssionLabelTexts();
                }
            }
        });

        if (!isAuto) {  // 手动的时候需要把值设置过去
            String isoComd = isoitem.curCommand;
            if (isoitem.commands != null && isoitem.commands.length > isoitem.curIndex) {
                isoComd = isoitem.commands[isoitem.curIndex];
            }
            String shutComd = shutitem.curCommand;
            if (shutitem.commands != null && shutitem.commands.length > shutitem.curIndex) {
                shutComd = shutitem.commands[shutitem.curIndex];
            }
            updateAELockStatus(photo ? false : true, false);
            cmd.swithAELock(this, photo ? false : true, photo, null);
            cmd.setIso(this, isoComd, photo, null);
            cmd.setShutter(this, shutComd, photo, new CameraCommandCallback<Boolean>() {
                @Override
                public void onComplete(Boolean cb) {
                    playVideo();
                    updateSettingsIsDisable(false);
                    setCameraViewStatus(status);
                    loadCameraSets();
                }
            });
        } else {
            updateAELockStatus(false, true);
            cmd.swithAELock(this, false, photo, new CameraCommandCallback<Boolean>() {
                @Override
                public void onComplete(Boolean cb) {
                    playVideo();
                    updateSettingsIsDisable(false);
                    setCameraViewStatus(status);
                    loadCameraSets();
                }
            });
        }
    }

    @Override
    public void configFPScrollClick(final CameraSetItem.FPScrollItem item,
                                    final int clickIndex) {

        String curComd = item.curCommand;
        if (item.commands != null && item.commands.length > clickIndex) {
            curComd = item.commands[clickIndex];
        }
        MLog.log("scroll clickIndex " + clickIndex + "value " + curComd);

        if (item.keyId == CameraSetConstaint.cell_video_baohedu_key || item.keyId == CameraSetConstaint.cell_video_duibidu_key
                || item.keyId == CameraSetConstaint.cell_photo_baohedu_key || item.keyId == CameraSetConstaint.cell_photo_duibidu_key) { // 饱和度 对比度
            String comd = "";
            if (item.keyId == CameraSetConstaint.cell_video_baohedu_key) {
                comd = "video_saturation";
                cameraSetting.videosaturation = clickIndex;
            } else if (item.keyId == CameraSetConstaint.cell_video_duibidu_key) {
                comd = "video_contrast";
                cameraSetting.videocontrast = clickIndex;
            } else if (item.keyId == CameraSetConstaint.cell_photo_baohedu_key) {
                comd = "photo_saturation";
                cameraSetting.photosaturation = clickIndex;
            } else if (item.keyId == CameraSetConstaint.cell_photo_duibidu_key) {
                comd = "photo_contrast";
                cameraSetting.photocontrast = clickIndex;
            }
            cmd.setCameraParam(this, comd, curComd, null);
        } else if (item.keyId == CameraSetConstaint.cell_photo_baoguang_key || item.keyId == CameraSetConstaint.cell_video_baoguang_key) {
            boolean photo = item.keyId == CameraSetConstaint.cell_photo_baoguang_key;
            final CameraSetItem.FPScrollItem otherItem;
            if (photo) {
                otherItem = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_video_baoguang_key);
            } else {
                otherItem = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_photo_baoguang_key);
            }

            // 要开启关闭曝光锁定
            updateAELockStatus(false, true);
            cmd.swithAELock(this, false, photo, null);
            cmd.setEV(this, curComd, photo, new CameraCommandCallback<Boolean>() {
                @Override
                public void onComplete(Boolean cb) {
                    // 保持视频和照片两边同步
                    otherItem.curIndex = clickIndex;
                    item.curIndex = clickIndex;
                    cameraSetting.photoEv = clickIndex;
                    cameraSetting.videoEv = clickIndex;
                    setISOShutterDimenssionLabelTexts();
                }
            });
        } else if (item.keyId == CameraSetConstaint.cell_photo_iso_key || item.keyId == CameraSetConstaint.cell_video_iso_key) {
            boolean photo = item.keyId == CameraSetConstaint.cell_photo_iso_key;

            // 要开启关闭曝光锁定
            updateAELockStatus(photo ? false : true, false);
            cmd.swithAELock(this, photo ? false : true, photo, null);
            cmd.setIso(this, curComd, photo, new CameraCommandCallback<Boolean>() {
                @Override
                public void onComplete(Boolean success) {
                    // 保持视频和照片两边同步
                    if (success) {
                        item.curIndex = clickIndex;
                        cameraSetting.photoISO = clickIndex;
                        cameraSetting.videoISO = clickIndex;
                        setISOShutterDimenssionLabelTexts();
                    }
                }
            });
        } else if (item.keyId == CameraSetConstaint.cell_photo_shutter_key || item.keyId == CameraSetConstaint.cell_video_shutter_key) {
            boolean photo = item.keyId == CameraSetConstaint.cell_photo_shutter_key;

            // 要开启关闭曝光锁定
            updateAELockStatus(photo ? false : true, false);
            cmd.swithAELock(this, photo ? false : true, photo, null);
            cmd.setShutter(this, curComd, photo, new CameraCommandCallback<Boolean>() {
                @Override
                public void onComplete(Boolean success) {
                    // 保持视频和照片两边同步
                    if (success) {
                        item.curIndex = clickIndex;
                        cameraSetting.photoShutter = clickIndex;
                        cameraSetting.videoShutter = clickIndex;
                        setISOShutterDimenssionLabelTexts();
                    }
                }
            });
        }
    }

    @Override
    public void configFPTitleValueClick(CameraSetItem.FPTitleValueItem item, int position) {
        if (item.keyId == CameraSetConstaint.cell_genera_format_key) {
            mActionDalog.showWithTitle(true, getResources().getString(R.string.camra_set_sd_format),
                    "", null, ResourceUtils.getString(R.string.confirm), (int action) -> {
                        if (!sdCardInserted && !internalsdCard) {
                            ToastUtils.showShortToast(getResources().getString(R.string.camra_sd_not_insert));
                            return;
                        }

                        showKpDialog(getResources().getString(R.string.formatting));
                        cmd.formatSDcard(this, new CameraCommandCallback<Boolean>() {
                            @Override
                            public void onComplete(Boolean success) {
                                if (success) {
                                    ToastUtils.showShortToast(getResources().getString(R.string.format_successful));
                                    cameraSetting.sdcardFree = cameraSetting.sdcardTotal;
                                    if (cameraSetView.getVisibility() == View.VISIBLE) {
                                        loadCameraSets();
                                    }
                                    // 这里删除本地文件缓存
                                } else {
                                    ToastUtils.showShortToast(getResources().getString(R.string.format_failure));
                                }
                                dismissKpDialog();
                            }
                        });
                    }, null);
        } else if (item.keyId == CameraSetConstaint.cell_genera_move_key) { // 转移内置卡中内容到外置卡中
            if (!sdCardInserted) {
                mActionDalog.showWithTitle(true, getResources().getString(R.string.camra_sd_not_insert), "", null, ResourceUtils.getString(R.string.confirm), null, null);
            }

            mActionDalog.showWithTitle(true, getResources().getString(R.string.prompt), getResources().getString(R.string.camra_sd_move), null, ResourceUtils.getString(R.string.confirm), (int action) -> {
                // 开始导出
                showKpDialog(getResources().getString(R.string.camra_sd_move_prepare));
                cmd.getInternalSDCardFileListComplete(CameraActivity.this, new CameraCommandCallback<ArrayList<Map<String, String>>>() {
                    private int sum = 0;

                    @Override
                    public void onComplete(ArrayList<Map<String, String>> fileList) {
                        if (fileList.size() == 0) {
                            ToastUtils.showShortToast(getResources().getString(R.string.camra_sd_move_empty));
                            return;
                        }

                        int total = fileList.size();
                        LogUtils.d("总共需要导出的张数 " + total);
                        showKpDialog(String.format("%s0/%d", getResources().getString(R.string.camra_sd_move_photo), total));
                        for (int i = 0; i < fileList.size(); i++) {
                            Map<String, String> file = fileList.get(i);
                            String src = (String) file.keySet().toArray()[0];
                            String dst = file.get(src);
                            cmd.removeFileInMain(CameraActivity.this, src, dst, new CameraCommandCallback() {
                                @Override
                                public void onComplete(Object cb) {
                                    sum += 1;
                                    LogUtils.d("已导出 " + sum);
                                    showKpDialog(String.format("%s%d/%d", getResources().getString(R.string.camra_sd_move_photo), sum, total));
                                    if (sum == total) {
                                        dismissKpDialog();
                                        ToastUtils.showShortToast(getResources().getString(R.string.camra_sd_move_ok));
                                    }
                                }
                            });
                        }
                    }
                });
            }, null);
        } else if (item.keyId == CameraSetConstaint.cell_genera_reset_key) { // 重置相机

            mActionDalog.showWithTitle(true, getResources().getString(R.string.prompt), getResources().getString(R.string.camra_set_sd_reset), null, ResourceUtils.getString(R.string.confirm), (int action) -> {
                showKpDialog(getResources().getString(R.string.camra_set_sd_reset_ing));
                // 隐藏相机设置菜单界面
                onTapCamerasetButton();
                stopVideo();
                setCameraViewStatus(CameraSetConstaint.CameraStatusDisable);
                boolean needReset = ConnectManager.getInstance().mProductModel.productType < ConstantFields.ProductType_6kAir;
                cmd.resetCameraParams(CameraActivity.this, needReset, new CameraCommandCallback<Boolean>() {
                    @Override
                    public void onComplete(Boolean cb) {
                        ConnectManager.getInstance().pauseConnectLision();
                    }
                });

                HandlerUtils.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        ConnectManager.getInstance().startConnectLision();
                    }
                }, 5000);
            }, null);
        }
    }

    @Override
    public void configFPDimensionClick(CameraSetItem.FPDimenssionItem item) {
        if (item.checked) {
            return;
        }
        // 获取命令
        String curComd = item.curCommand;

        int status = cameraSetting.lastUseMode == CameraSetConstaint.CameraModeMovie
                ? CameraSetConstaint.CameraStatusMovieModeNormal : CameraSetConstaint.CameraStatusPhotoModeNormal;

        CameraSetItem.FPDimenssionItem curCheckedDimensionItem = cameraSetting.currentVideoDimenSion();
        CameraSetItem.FPAccessryBaseCellItem curAccessorItem = (CameraSetItem.FPAccessryBaseCellItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_video_Dimesion_key);
        CameraSetItem.FPButtonsItem fpsItem = curCheckedDimensionItem.getSubItem(0);

        stopVideoWhenSendCameraCommond();
        setCameraViewStatus(CameraSetConstaint.CameraStatusDisable);
        updateSettingsIsDisable(true);
        cmd.setDemession(this, curComd, new CameraCommandCallback<Boolean>() {
            @Override
            public void onComplete(Boolean success) {
                playVideo();
                if (success) {
                    cameraSetting.updateCurDimension(curComd);
                    curCheckedDimensionItem.checked = false;
                    item.checked = true;
                    item.currentfps = item.defaultfps;
                    if (fpsItem != null) {
                        for (int i = 0; i < fpsItem.buttonTitles.length; i++) {
                            if (fpsItem.buttonTitles[i].equals(item.currentfps)) {
                                fpsItem.selectIndex = i;
                                break;
                            }
                        }
                    }
                    curAccessorItem.rightTitle = item.leftTitle + " " + item.currentfps;
                    setISOShutterDimenssionLabelTexts();
                }

                updateSettingsIsDisable(false);
                setCameraViewStatus(status);
            }
        });
    }

    @Override
    public void configFPButtonsClick(CameraSetItem.FPButtonsItem cItem, int position,
                                     int clickIndex) {
        cItem.selectIndex = 4;
        LogUtils.d("选中的选项====" + clickIndex + "||" + cItem.selectIndex);
        if (cItem.selectIndex == clickIndex) {
            LogUtils.d("已经选中了");
            return;
        }

        String curComd = cItem.curCommand;
        if (cItem.commands != null && cItem.commands.length > clickIndex) {
            curComd = cItem.commands[clickIndex];
        }

        CameraSetItem.FPAccessryBaseCellItem curAccessorItem = (CameraSetItem.FPAccessryBaseCellItem) cameraSetting.itemForKeyId(CameraSetConstaint.cell_video_Dimesion_key);
        int status = cameraSetting.lastUseMode == CameraSetConstaint.CameraModeMovie ? CameraSetConstaint.CameraStatusMovieModeNormal : CameraSetConstaint.CameraStatusPhotoModeNormal;

        stopVideoWhenSendCameraCommond();
        setCameraViewStatus(CameraSetConstaint.CameraStatusDisable);
        updateSettingsIsDisable(true);
        cmd.setDemession(this, curComd, new CameraCommandCallback<Boolean>() {
            @Override
            public void onComplete(Boolean success) {
                playVideo();

                if (success) {
                    CameraSetItem.FPDimenssionItem curCheckedDimensionItem = cameraSetting.currentVideoDimenSion();
                    if (cItem.buttonTitles.length > clickIndex) {//这里可能越界
                        curCheckedDimensionItem.currentfps = cItem.buttonTitles[clickIndex];
                    }
                    curAccessorItem.rightTitle = curCheckedDimensionItem.leftTitle + " " + curCheckedDimensionItem.currentfps;
                    LogUtils.d("分辨率设置成功====>" + curAccessorItem.rightTitle);
                    cItem.selectIndex = clickIndex;
                    setISOShutterDimenssionLabelTexts();

                    String valuesss[] = ResourceUtils.getStringArray(R.array._30VIDEO_SHUTTER);
                    if (curCheckedDimensionItem.currentfps.equals(CameraSetConstaint.Video_fps_30)) {   //30fps
                        valuesss = ResourceUtils.getStringArray(R.array._30VIDEO_SHUTTER);
                    } else if (curCheckedDimensionItem.currentfps.equals(CameraSetConstaint.Video_fps_60)) {
                        valuesss = ResourceUtils.getStringArray(R.array._60VIDEO_SHUTTER);
                    } else if (curCheckedDimensionItem.currentfps.equals(CameraSetConstaint.Video_fps_120)) {
                        valuesss = ResourceUtils.getStringArray(R.array._120VIDEO_SHUTTER);
                    } else if (curCheckedDimensionItem.currentfps.equals(CameraSetConstaint.Video_fps_24)) {
                        valuesss = ResourceUtils.getStringArray(R.array._24VIDEO_SHUTTER);
                    }
                    boolean found = false;
                    CameraSetItem.FPScrollItem item = (CameraSetItem.FPScrollItem) cameraSetting.itemForKeyId(cell_video_shutter_key);
                    for (int i = 0; i < valuesss.length; i++) {
                        if (item.values.length > item.curIndex && valuesss[i].equals(item.values[item.curIndex])) {
                            found = true;
                        }
                    }
                    if (!found) {
                        int finalIndex = valuesss.length - 1;
                        cmd.setShutter(CameraActivity.this, valuesss[finalIndex], false, new CameraCommandCallback<Boolean>() {
                            @Override
                            public void onComplete(Boolean cb) {
                                if (cb) {
                                    cameraSetting.photoShutter = finalIndex;
                                    cameraSetting.videoShutter = finalIndex;
                                    setISOShutterDimenssionLabelTexts();
                                }

                                updateSettingsIsDisable(false);
                                setCameraViewStatus(status);
                            }
                        });
                    } else {
                        updateSettingsIsDisable(false);
                        setCameraViewStatus(status);
                    }
                }

            }
        });
    }

    private void zoomAutoEnd() {
        cmd.setZoomEnd(this, new CameraCommandCallback<Boolean>() {
            @Override
            public void onComplete(Boolean cb) {
                if (cb) {
                    setSuofangLabelTexts();
                }
                isRooming = false;
            }
        });
    }

    private void zoomAutoReduce(boolean begin) {
        if (!ConnectManager.getInstance().isConneted()) {
            return;
        }

        int speed = 0;
        int step = 1;
        if (cameraSetting.zoomIndex == 1) {
            speed = 2;
            step = 0;
        }
        if (begin) {
            cmd.setZoomBegin(this, 0, speed, step, new CameraCommandCallback<Boolean>() {
                @Override
                public void onComplete(Boolean cb) {
                    setSuofangLabelTexts();
                    isRooming = false;
                }
            });
        }
        lastScale = zoomSlider.getCurrentValue();
        setSuofangLabelTexts();
    }

    private void zoomAutoAdd(boolean begin) {
        if (!ConnectManager.getInstance().isConneted()) {
            return;
        }

        int speed = 0;
        int step = 1;
        if (cameraSetting.zoomIndex == 1) {
            speed = 2;
            step = 0;
        }
        if (begin) {
            int maxValue = ConnectManager.getInstance().mProductModel.productType == ConstantFields.ProductType_4kAir ? 100 : 300;
            cmd.setZoomBegin(this, maxValue, speed, step, new CameraCommandCallback<Boolean>() {
                @Override
                public void onComplete(Boolean cb) {
                    setSuofangLabelTexts();
                    isRooming = false;
                }
            });
        }
        lastScale = zoomSlider.getCurrentValue();
        setSuofangLabelTexts();
    }

    private void zoomSliderOnChangeValue(int scale) {
        if (!ConnectManager.getInstance().isConneted()) {
            return;
        }

        int max = zoomSlider.getMaxValue() - zoomSlider.getMinValue();
        if (scale >= max) {
            LogUtils.d("已经到最大了");
        } else if (scale <= zoomSlider.getMinValue()) {
            LogUtils.d("已经到最小了");
        }

        if (scale <= 0 && lastScale == 0) {
            LogUtils.d("已经最小");
            zoomSlider.updateEnableStatus();
            return;
        }
        if (scale >= max && lastScale == max) {
            LogUtils.d("已经最大");
            zoomSlider.updateEnableStatus();
            return;
        }
        if (isRooming) {
            LogUtils.d("正在缩放");
            return;
        }
        isRooming = true;

        cmd.setZoomValue(this, scale, new CameraCommandCallback<Boolean>() {
            @Override
            public void onComplete(Boolean cb) {
                if (cb) {
                    lastScale = scale;
                    setSuofangLabelTexts();
                }
                isRooming = false;
            }
        });
    }
    //  =========== 相机设置 ======== //


    /*****地图相关start******************************************************************/
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        mapViewLy.onMapLocationChange(aMapLocation);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        //高德
        if (mPlaneParamBean != null) {
            smartView.onMapClick(latLng, mPlaneParamBean.getFenceDistance());
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        try {
            mapViewLy.mapViewActivate(onLocationChangedListener, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deactivate() {
        mapViewLy.mapViewDeactivate();
    }

    //mapbox相关信息
    @Override
    public boolean onMapClick(@NonNull com.mapbox.mapboxsdk.geometry.LatLng latLng) {
        if (mPlaneParamBean != null) {
            LogUtils.d("围栏半径====>" + mPlaneParamBean.getFenceDistance());
            smartView.onBoxMapClick(latLng, mPlaneParamBean.getFenceDistance());
        }
        return false;
    }

    //mapbox定位成功回调
    @Override
    public void onSuccess(LocationEngineResult result) {
        if (result.getLastLocation() != null) {
            mapViewLy.onMapboxLocationChange(result.getLastLocation());
        }
    }

    //mapbox定位失败回调
    @Override
    public void onFailure(@NonNull Exception exception) {
        mapViewLy.onMapboxLocationError(exception);
    }
    /*****地图相关end********************************************************************/
}
