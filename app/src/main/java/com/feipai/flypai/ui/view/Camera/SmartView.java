package com.feipai.flypai.ui.view.Camera;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.enums.MAV_DATA_STREAM;
import com.feipai.flypai.R;
import com.feipai.flypai.api.CameraCommandCallback;
import com.feipai.flypai.api.RxLoopObserver;
import com.feipai.flypai.api.RxLoopSchedulers;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.beans.ABCmdValue;
import com.feipai.flypai.beans.FPCameraSettingBase;
import com.feipai.flypai.beans.WaypointModel;
import com.feipai.flypai.beans.mavlinkbeans.BatteryBean;
import com.feipai.flypai.beans.mavlinkbeans.LocationBean;
import com.feipai.flypai.connect.ConnectManager;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.ui.view.ipm.AroundDialog;
import com.feipai.flypai.ui.view.ipm.DelayTimeDialog;
import com.feipai.flypai.ui.view.ipm.FollowSnapDialog;
import com.feipai.flypai.ui.view.ipm.IPMHelper;
import com.feipai.flypai.ui.view.ipm.TakingPanorDialog;
import com.feipai.flypai.ui.view.ipm.WayPointDialog;
import com.feipai.flypai.ui.view.mapbox.MapboxLatLng;
import com.feipai.flypai.ui.view.mapbox.MapboxWayPointModel;
import com.feipai.flypai.utils.CameraCommand;
import com.feipai.flypai.utils.GPSUtils;
import com.feipai.flypai.utils.PlaneCommand;
import com.feipai.flypai.utils.global.ConvertUtils;
import com.feipai.flypai.utils.global.IAnimationUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.StringUtils;
import com.feipai.flypai.utils.global.TimeUtils;
import com.feipai.flypai.utils.global.ToastUtils;
import com.feipai.flypai.utils.global.Utils;
import com.feipai.flypai.utils.global.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Function;

import static com.feipai.flypai.ui.view.Camera.CameraSetConstaint.CameraModePhoto;
import static com.feipai.flypai.ui.view.Camera.CameraSetConstaint.CameraStatusInPano;
import static com.feipai.flypai.ui.view.Camera.CameraSetConstaint.CameraStatusInYanshi;
import static com.feipai.flypai.ui.view.Camera.CameraSetConstaint.PhotoModeDelay;
import static com.feipai.flypai.ui.view.Camera.CameraSetConstaint.PhotoModePano;

public class SmartView extends RelativeLayout {


    private MapViewLayout mapV;
    private BaseView mbv;


    @BindView(R.id.adjust_control_show_btn)
    ImageView adjustControlShowBtn;
    @BindView(R.id.camera_smart_button_ly)
    LinearLayout cameraSmartButtonLy;
    @BindView(R.id.camera_smart_button)
    ImageButton cameraSmartBtn;
    @BindView(R.id.smart_view_cancel_tv)
    TextView smartViewCancelTv;
    @BindView(R.id.camera_turn_back_btn_ly)
    LinearLayout cameraTurnBackBtnLy;
    @BindView(R.id.camera_turn_back_btn)
    ImageButton cameraTurnBackBtn;
    @BindView(R.id.camera_turn_back_tv)
    TextView cameraTurnBackBtnTv;
    @BindView(R.id.smart_vr_btn)
    Button smartVrBtn;
    @BindView(R.id.smart_delay_btn)
    Button smartDelayBtn;
    @BindView(R.id.smart_gensui_btn)
    Button smartGensuiBtn;
    @BindView(R.id.smart_huanrao_btn)
    Button smartHuanraoBtn;
    @BindView(R.id.smart_hand_btn)
    Button smartHandBtn;
    @BindView(R.id.smart_guangjiao_btn)
    Button smartGuangjiaoBtn;
    @BindView(R.id.ab_control_function_ly)
    LinearLayout abControlFunctionLy;

    //????????????
    private IPMHelper mIPMHelper;

    private WaypointModel waypointModel;
    private MapboxWayPointModel mapboxWayPointModel;
    private int wpIndex = -1;

    private BatteryBean mStatusBean;
    private FPCameraSettingBase mCameraSetting;
    private boolean isAutoPhoto;
    private boolean mIsWide;
    private boolean isStart;
    public boolean isPaused;
    private int mCurPanorCount = 1;
    private int maxCount = 15;
    private OnActionCallback mCallback;
    private boolean isPanorSuccess;
    private int mServoAngle;//??????????????????
    private int mYawAngle;//??????????????????
    private int mYawCount = 1;//?????????????????????????????????????????????????????????
    private int mYawMoveStatus = -1;
    //    private boolean mIsPositiveYaw;
    private final static int CENTER_SERVO = 1242;

    //?????????????????????
    private final static int FIRST_SERVO_PANOR = 1260;
    //?????????????????????
    private final static int SECOND_SERVO_PANOR = 1510;
    //?????????????????????
    private final static int THIRD_SERVO_PANOR = 1754;
    //?????????????????????
    private final static int FIRST_YAW_PANOR = 360 / 7;
    //?????????????????????
    private final static int SECOND_YAW_PANOR = 360 / 6;
    //?????????????????????
    private final static int THIRD_YAW_PANOR = 360 / 2;

    /*** ??????????????????
     * 5  1  4
     * 6  2  3
     */
    //???????????????
    private final static int FIRST_SERVO_WIDE = 1347;
    //???????????????
    private final static int SECOND_SERVO_WIDE = 1138;
    //?????????????????????
    private final static int FIRST_YAW_WIDE = 380 / 8;
    private final static int SECOND_YAW_WIDE = 380 / 8 * 2;

    /**
     * ????????????????????????????????????
     */
    private long mCurrentVrTime = 0;
    private long mLastVrTime = 0;

    public SmartView(Context context) {
        super(context);
        initView(context);
    }

    public SmartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.camera_smart_view, this, true);
        ButterKnife.bind(this, view);
        mIPMHelper = new IPMHelper(context);
        dialogShowingAndStart(false, false);
    }

    /**
     * ???????????????
     */
    public void bindMapView(BaseView baseView, MapViewLayout mapViewLy, OnActionCallback callback) {
        this.mCallback = callback;
        this.mbv = baseView;
        this.mapV = mapViewLy;
        this.mapV.setMapChangeListener(new MapViewLayout.OnMapChangedCallback() {
            @Override
            public void onWaypointRemoved(Marker marker) {
                LogUtils.d("??????????????????");
                if (mIPMHelper != null && mIPMHelper.isWaypointSetPoints()) {
                    WaypointModel model = mapViewLy.removeWaypointFromUser(marker);

                    if (model != null && model.getWaypointMarkers() != null) {
                        mIPMHelper.updateWayponitD(model.getWaypointMarkers().size(), ConvertUtils.floatToString(model.getTotalMileages()));
                    }
                }
            }

            @Override
            public void onAroundChanged(double aroundRadius) {
                mIPMHelper.updateAroundR(aroundRadius);
            }

//            @Override
//            public void onDistanceChangeBetweenMobileAndPlane(float distance) {
//                updateFollowDistance(distance);
//            }

            @Override
            public void onGPGChange(float mobileGPS, LatLng mobileLatlng, float distance) {
                if (isFollowDShown()) {
                    // TODO: 2019/7/27 ??????????????????????????????????????????,????????????
                }
//                LogUtils.d("??????GPS??????" + mobileGPS + "||" + mobileLatlng.latitude + "||" + mobileLatlng.longitude);
                updateFollowGps(mobileGPS, mobileLatlng, distance);
            }

            @Override
            public void onLocationResult(boolean isSuccess) {
                if (callback != null) callback.onMapLacationResult(isSuccess);
            }

            @Override
            public void onChangeMapType(int type) {
                if (callback != null) {
                    callback.mapTypeChange(type);
                }
            }
        });
    }

    /**
     * ??????????????????
     */
    private void dialogShowingAndStart(boolean isStart, boolean isDgShow) {
        if (mCallback != null) mCallback.onIPMDialogShown(isDgShow);
        if (!isStart) {
            if (mCallback != null) mCallback.onDestory();
        }
        this.isStart = isStart;
        smartViewCancelTv.setVisibility(isStart ? VISIBLE : GONE);
        cameraSmartBtn.setImageResource(isStart ? R.mipmap.smart_cancel_img : R.mipmap.camera_smart_button);
    }

    /**
     * ???????????????????????????
     */
    public void setSmartViewEnabled(boolean isEnable) {
        cameraSmartButtonLy.setEnabled(isEnable);
        cameraSmartBtn.setEnabled(isEnable);
        cameraSmartBtn.setAlpha(isEnable ? 1f : 0.5f);
    }

    public void animationToPoint(int x1, int x2, int y1, int y2, int duration) {
        TranslateAnimation translateAnimation = new TranslateAnimation(x1, x2, y1, y2);
        translateAnimation.setDuration(duration);
        setAnimation(translateAnimation);
        startAnimation(translateAnimation);
    }

    @OnClick({R.id.camera_turn_back_btn,
            R.id.camera_smart_button_ly,
            R.id.camera_smart_button,
            R.id.smart_vr_btn,
            R.id.smart_delay_btn,
            R.id.smart_gensui_btn,
            R.id.smart_huanrao_btn,
            R.id.smart_hand_btn,
            R.id.smart_guangjiao_btn,
            R.id.adjust_control_show_btn})
    public void onViewClicked(View view) {
        bringToFront();
        switch (view.getId()) {
            case R.id.camera_turn_back_btn:
//                IAnimationUtils.performAnim(Utils.context, cameraTurnBackBtn, R.anim.view_up_scale_anim);
                if (mStatusBean == null) {
                    ToastUtils.showShortToast(ResourceUtils.getString(R.string.plane_not_connected));
                } else {
                    if (!mStatusBean.isUnlocked()) {
                        //?????????
                        ToastUtils.showShortToast(ResourceUtils.getString(R.string.plane_unlocked));
                    } else {
//                        if (/*mStatusBean.getFlyStatus() == 100 &&*/ mStatusBean.getCustomMode() == 5) {
                        if (mCallback != null)
                            mCallback.onReturnBack(cameraTurnBackBtnTv.getVisibility() != VISIBLE);
//                        } else {
//                            ToastUtils.showShortToast(ResourceUtils.getString(R.string.plane_not_positioned));
//                        }
                    }
                }

                break;
            case R.id.camera_smart_button_ly:
            case R.id.camera_smart_button:
                if (smartViewCancelTv.getVisibility() == VISIBLE) {
                    //?????????????????????
                    dismissDialogAndCancelFunction(1);
                } else {
                    showAndHintFunctionLy(abControlFunctionLy.getVisibility() != VISIBLE, 1);
                }
                IAnimationUtils.performAnim(Utils.context, cameraSmartButtonLy, R.anim.view_up_scale_anim);
                break;
            case R.id.smart_vr_btn:
            case R.id.smart_delay_btn:
            case R.id.smart_gensui_btn:
            case R.id.smart_huanrao_btn:
            case R.id.smart_hand_btn:
            case R.id.smart_guangjiao_btn:
                startSmart(view.getId());
                break;
            case R.id.adjust_control_show_btn:
                if (mCallback != null)
                    mCallback.onAdjustControlShown();
                break;
        }
    }

    private void startSmart(int type) {
        if (mStatusBean == null) {
            ToastUtils.showShortToast(ResourceUtils.getString(R.string.plane_not_connected));
        } else {
            if (!mStatusBean.isUnlocked()) {
                //?????????
                ToastUtils.showShortToast(ResourceUtils.getString(R.string.plane_unlocked));
            } else {
                if (mStatusBean.getFlyStatus() == 100 && mStatusBean.getCustomMode() == 5) {
                    switch (type) {
                        case R.id.smart_vr_btn:
                            if (mCallback != null) mCallback.onStart(CameraStatusInPano);
                            mIsWide = false;
                            //?????????????????????????????????????????????
                            delayTime = TimeUtils.isEvening() ?
//                                    ConnectManager.getInstance().mProductModel.productType == ConstantFields.ProductType_6kAir ?
                                    7 : 4;
                            getLastFileName(true, true);
                            break;
                        case R.id.smart_delay_btn:
                            if (mCallback != null) mCallback.onStart(CameraStatusInYanshi);
                            startDelayTime();
                            break;
                        case R.id.smart_gensui_btn:
                            if (mapV.isMapPositioningSuccess()) {
                                LogUtils.d("???????????????===>" + mapV.distanceBetweenMobileWithPlane);
                                if (mapV.distanceBetweenMobileWithPlane > 80) {
                                    /**????????????80???*/
                                    ToastUtils.showLongToast(ResourceUtils.getString(R.string.too_far_away_for_follow));
                                } else {
                                    showAndHintFunctionLy(false, 2);
                                    showFollowDialog();
                                }
                            } else {
                                ToastUtils.showLongToast(ResourceUtils.getString(R.string.no_phone_gps_for_follow));
                            }
                            break;
                        case R.id.smart_huanrao_btn:
                            showAndHintFunctionLy(false, 3);
                            showAroundDialog();
                            break;
                        case R.id.smart_hand_btn:
                            showAndHintFunctionLy(false, 4);
                            showWayPointDialog();
                            break;
                        case R.id.smart_guangjiao_btn:
                            if (mCallback != null) mCallback.onStart(CameraStatusInPano);
                            mIsWide = true;
                            getLastFileName(true, true);
                            break;
                    }
                } else {
                    ToastUtils.showShortToast(ResourceUtils.getString(R.string.plane_not_positioned_auto));
                }
            }
        }
    }

    /**
     * ???????????????????????????????????????
     */
    private void dismissDialogAndCancelFunction(int where) {
        dialogShowingAndStart(false, false);
        if (isTakingDialogShown()) {
            //????????????
            ToastUtils.showLongToast(ResourceUtils.getString(R.string.cancel_panor));
            mIPMHelper.dismissTakingPanorD(1);
        } else if (mIPMHelper.isAroundDShowing()) {
            mIPMHelper.dismissAroundD();
        } else if (mIPMHelper.isDelayTimeDShowing()) {
            ToastUtils.showLongToast(ResourceUtils.getString(R.string.cancel_delay));
            dismissDelayTime();
        } else if (mIPMHelper.isWaypointDShowing()) {
            mIPMHelper.dismissWaypointD();
        } else if (mIPMHelper.isFollowDShowing()) {
            mIPMHelper.dismissFollowD();
        }
        if (delayTime > 0 && mIsDelayTaking) {
            stopDelayTimer();
        }
    }

    private void showAndHintFunctionLy(boolean isShow, int where) {
//        LogUtils.d("??????===>" + isShow + where);
        if (isShow) {
            IAnimationUtils.performShowViewAnim(getContext(), abControlFunctionLy, R.anim.enter_left_anim);
        } else {
            IAnimationUtils.performHideViewAnim(getContext(), abControlFunctionLy, R.anim.exit_left_anim);
        }
    }


    /*****????????????start******************************************************************/

    /*??????start*********************************************************/
    private void startTakingPanor(boolean isWide) {
        if (mCallback != null) mCallback.onStopVideo(isWide, false);
        CameraCommand.getCmdInstance().changeToPanorMode(mbv,
                ConnectManager.getInstance().mProductModel.productType, mCameraSetting.lastUseMode == CameraSetConstaint.CameraModeMovie,
                true, new CameraCommandCallback<Boolean>() {
                    @Override
                    public void onComplete(Boolean isSuccess) {
                        if (isSuccess) {
                            //????????????????????????????????????
                            if (mCallback != null) {
                                mCallback.onPlayVideo(false);
                                mCallback.onModeChange(PhotoModePano, 0);
                            }
                            isStart = true;
                            startChangePlanePanorMode(true, isWide);
                        }
                    }
                });

    }

    /**
     * ?????????????????????????????????????????????
     */
    private void startChangePlanePanorMode(boolean panorMode, boolean isWide) {
        isPanorSuccess = panorMode;
        if (isPanorSuccess) {
            RxLoopSchedulers.composeLoop(mbv, 0, 1000, new Function() {
                @Override
                public Object apply(Object o) throws Exception {
                    if (isStart && !isPaused)
                        PlaneCommand.getInstance().changToPanMode(!isWide);
                    return isPanorSuccess;
                }
            }).subscribe(new RxLoopObserver<Object>() {
                @Override
                public void onNext(Object obj) {
                    super.onNext(obj);
                    if (!isPanorSuccess) {
                        //????????????
                        this.disposeDisposables();
                        if (isStart && !isPaused) {
                            showTakingPanorDialog(isWide);
                        }
                    }
                }
            });
        }
    }

    private void showTakingPanorDialog(boolean isWide) {
        dialogShowingAndStart(true, true);
        this.mIsWide = isWide;
        maxCount = !mIsWide ? 15 : 6;
        mYawCount = 1;//??????????????????1??????
        mIPMHelper.createTakingPanorDg(isWide, new TakingPanorDialog.OnClickCallback() {
            @Override
            public void onStart() {
                /**????????????*/
                if (mCameraSetting.photoISOAuto && mCameraSetting.photoAELock)
                    if (mCallback != null) mCallback.onEvChange(false);
            }


            @Override
            public void onInterrupt() {
                //?????????????????????????????????????????????,????????????
            }

            @Override
            public void onCountChanged(int count) {
                sendPanorMessage(isWide, count);
            }

            @Override
            public void onComplete() {
                //????????????,??????????????????????????????????????????????????????
                takingPanorComplete();
            }

            @Override
            public void onDestroy() {
                //?????????????????????????????????????????????
                mCurrentVrTime = 0;
                mLastVrTime = 0;
                cancelTakingPanor(1);
            }
        }).showTakingPanorD();
    }

    public boolean isTakingDialogShown() {
        return mIPMHelper.isTakingPanorDShowing();
    }


    /**
     * ???????????????????????????
     */
    public void changePanorModeSuccess(boolean isPanorSuccess) {
        this.isPanorSuccess = !isPanorSuccess;
    }


    /**
     * ??????????????????
     */
    public void yawSuccess(boolean suceeess) {
        this.isStart = !suceeess;
    }


    /**
     * ???????????????????????????
     */
    private void sendPanorMessage(boolean isWide, int count) {
        LogUtils.d("????????????", "????????????====???" + count);
        if (count == 1) {
            startSendServo(1, true, false, count, !isWide ? FIRST_SERVO_PANOR : FIRST_SERVO_WIDE);
        } else if (count > maxCount) {
            LogUtils.d("????????????", "?????????????????????????????????");
            startSendServo(2, true, false, count, CENTER_SERVO);
        }
    }

    /**
     * ????????????????????????
     *
     * @param start    ?????????????????????????????????
     * @param curCount ?????????????????????
     * @param servo    ??????????????????
     */
    private void startSendServo(int where, boolean start, boolean isAutoPhoto, int curCount, int servo) {
        LogUtils.d("????????????", "??????????????????====>" + where + "::::" + start + ":::::" + servo);
        this.isStart = start;
        this.mCurPanorCount = curCount;
        this.mServoAngle = servo;
        if (isStart) {
            RxLoopSchedulers.composeLoop(mbv, 0, 1000, new Function() {
                @Override
                public Boolean apply(Object o) throws Exception {
                    PlaneCommand.getInstance().sendServo(servo);
                    return isStart;
                }
            }).subscribe(new RxLoopObserver<Boolean>() {
                @Override
                public void onNext(Boolean start) {
                    super.onNext(start);
                    if (!start || !isTakingDialogShown() || isPaused) {
                        //???????????????????????????
                        this.disposeDisposables();
                        LogUtils.d("????????????", "??????????????????===>" + curCount);
                        if (!isPaused) {
                            if (curCount <= maxCount) {
                                startTakePanorPhoto(1, isAutoPhoto);
                            } else {
                                /**???????????????????????????*/
                                LogUtils.d("????????????", "??????????????????");
                                mIPMHelper.dismissTakingPanorD(2);
                            }
                        }
                    }
                }
            });
        }
    }

    private void startSendYaw(int where, boolean start, boolean isPositiveYaw, int yawAngle, int yawCount) {
        LogUtils.d("????????????", "??????????????????====>" + where + "::::" + isStart + ":::::" + yawAngle + ":::::" + yawCount);
        this.isStart = start;
//        this.mIsPositiveYaw = isPositiveYaw;
        this.mYawCount = yawCount;
        if (this.isStart && yawCount != 40) {
            RxLoopSchedulers.composeLoop(mbv, 0, 2000, new Function() {
                @Override
                public Boolean apply(Object o) throws Exception {
                    if (!isPaused && isStart)
                        PlaneCommand.getInstance().sendYaw(isPositiveYaw, yawAngle, mYawCount);
                    return isStart;
                }
            }).subscribe(new RxLoopObserver<Boolean>() {
                @Override
                public void onNext(Boolean start) {
                    super.onNext(start);
                    if (!isTakingDialogShown() || !isStart || isPaused) {
                        //???????????????????????????
                        this.disposeDisposables();
                        if (mYawCount != 106 && !isPaused)
                            startTakePanorPhoto(2, false);
                        else {
                            //????????????
                        }
                    }
                }
            });
        }

    }

    private void startTakePanorPhoto(int where, boolean isAutoPhoto) {
        if (mCallback != null) mCallback.onModeChange(PhotoModePano, 0);
        LogUtils.d("????????????", "??????????????????" + mCurPanorCount + "???" + "|||" + where);
        time = 0;
        //???????????????
        RxLoopSchedulers.composeLoop(mbv, 0, 1000, new Function() {
            @Override
            public Integer apply(Object o) throws Exception {
                time++;
                return time;
            }
        }).subscribe(new RxLoopObserver<Integer>() {
            @Override
            public void onNext(Integer time) {
                super.onNext(time);
                if (isTakingDialogShown()) {
                    if (time >= delayTime) {
                        //?????????????????????
                        this.disposeDisposables();
                        LogUtils.d("????????????", "????????????" + delayTime + "|||" + mCurPanorCount);
                        takeSinglePanorPhoto(isAutoPhoto);
                    } else {
                        //?????????...
                        LogUtils.d("????????????", "?????????" + time);
                        if (mCallback != null) {
                            mCallback.onDelayTimer(delayTime - time);
                        }
                    }
                } else {
                    //????????????????????????????????????
                    this.disposeDisposables();
                    LogUtils.d("????????????", "????????????" + time);
                }
            }
        });
    }

    private void takeSinglePanorPhoto(boolean isAutoPhoto) {
        LogUtils.d("????????????", "???????????????" + mCurPanorCount + "???");
        if (mCallback != null) mCallback.onStopVideo(mIsWide, true);
        CameraCommand.getCmdInstance().takePhotoInPanoComplete(mbv, mCurPanorCount == 1, new CameraCommandCallback<String>() {
            @Override
            public void onComplete(String vrName) {
                if (mCallback != null) mCallback.onPlayVideo(true);
                if (!StringUtils.isEmpty(vrName)) {
                    takePanorPhotoSuccess(isAutoPhoto, vrName);
                } else {
                    dismissDialogAndCancelFunction(2);
                    ToastUtils.showLongToast(ResourceUtils.getString(mIsWide ? R.string.take_wide_error : R.string.take_panorama_error));
                }
            }
        });
    }

    private void takePanorPhotoSuccess(boolean isAutoPhoto, String vrName) {
        if (mCallback != null) mCallback.onModeChange(PhotoModePano, 0);
        mLastVrTime = TimeUtils.lastVrTime(vrName);
        mCurrentVrTime = mLastVrTime;
        if (!isPaused) {
            //????????????
            mCurPanorCount++;
            Log.d("????????????", "??????????????????????????????" + mCurPanorCount + "???");
            mIPMHelper.updateTakingPanorD(mCurPanorCount);
            if (mCurPanorCount == 2) {
                if (mCameraSetting.photoISOAuto) {
                    // TODO: 2019/7/20 ?????????????????????
                    if (mCallback != null) mCallback.onEvChange(false);
//                    aELockStatusChange(false);
                } else {
                    // TODO: 2019/7/20 ??????
                    continueTakingPanorWithAeLockStatus();

                }
            } else if (mCurPanorCount == 14) {
                //??????????????????
                mYawAngle = THIRD_YAW_PANOR;
                mServoAngle = THIRD_SERVO_PANOR;
                startSendServo(3, true, false, mCurPanorCount, mServoAngle);
            } else {
                if (!mIsWide) {
                    //????????????
                    if (mCurPanorCount == 8) {
                        //???????????????
                        mYawAngle = SECOND_YAW_PANOR;
                        mServoAngle = SECOND_SERVO_PANOR;
                        startSendServo(4, true, false, mCurPanorCount, mServoAngle);
                    } else {
                        if (mCurPanorCount <= 15) {
                            startSendYaw(2, true, false, mYawAngle, mYawCount);
                        } else {
                            takingPanorComplete();
                        }
                    }
                } else {
                    //??????
                    switch (mCurPanorCount) {
                        case 3:
                            //?????????????????????????????????
                            mYawAngle = FIRST_YAW_WIDE;
                            startSendYaw(11, true, false, mYawAngle, mYawCount);
                            break;
                        case 4:
                            //???????????????
                            mServoAngle = FIRST_SERVO_WIDE;
                            startSendServo(12, true, false, mCurPanorCount, mServoAngle);
                            break;
                        case 5:
                            //?????????????????????????????????
                            mYawAngle = SECOND_YAW_WIDE;
                            startSendYaw(13, true, true, mYawAngle, mYawCount);
                            break;
                        case 6:
                            //???????????????
                            mServoAngle = SECOND_SERVO_WIDE;
                            startSendServo(14, true, false, mCurPanorCount, mServoAngle);
                            break;
                        case 7:
                            //??????
                            LogUtils.d("????????????", "????????????????????????");
                            break;
                    }
                }
            }
        }
    }

    private void aELockStatusChange(boolean open) {

        CameraCommand.getCmdInstance().swithAELock(mbv, open, true, new CameraCommandCallback<Boolean>() {
            @Override
            public void onComplete(Boolean success) {
                if (success) {
                    if (isTakingDialogShown()) {
                        if (open) {
                            /**??????????????????????????????**/
                        } else {
                            /**?????????????????????*/
                            continueTakingPanorWithAeLockStatus();
                        }
                    }
                    mCameraSetting.photoAELock = open;
                }
            }
        });
    }

    /**
     * ?????????AE?????????????????????
     */
    private void continueTakingPanorWithAeLockStatus() {
        if (!mIsWide) {
            //??????
            mYawAngle = FIRST_YAW_PANOR;
            startSendYaw(1, true, false, mYawAngle, mYawCount);
        } else {
            //??????
            mServoAngle = SECOND_SERVO_WIDE;
            startSendServo(10, true, false, mCurPanorCount, mServoAngle);
        }
    }


    /**
     * ????????????
     */
    private void cancelTakingPanor(int where) {
        if (mCallback != null) mCallback.onDestory();
        LogUtils.d("????????????", "????????????????????????====>" + where);
        dialogShowingAndStart(false, false);
        startSendYaw(3, true, false, 0, 106);
    }

    /**
     * ??????????????????
     */
    private void takingPanorComplete() {
        dialogShowingAndStart(false, false);
        sendPanorMessage(mIsWide, mCurPanorCount);
    }

    /**
     * @param isPhoto      ????????????????????????????????????
     * @param isStartPanor ???????????????????????????????????????
     */
    public void getLastFileName(boolean isPhoto, boolean isStartPanor) {
        CameraCommand.getCmdInstance().getLastFileName(mbv, isPhoto, new CameraCommandCallback<ABCmdValue<String>>() {
            @Override
            public void onComplete(ABCmdValue<String> data) {
                if (data.getRval() == 0) {
                    long lastFileTime = TimeUtils.lastVrTime(data.getParam());
                    LogUtils.d("????????????", "??????????????????????????????=" + lastFileTime);
                    if (isStartPanor) {
                        // TODO: 2019/7/25 ?????????????????????????????? ?????????currentVrTime;
                        mCurrentVrTime = lastFileTime;
                        LogUtils.d("????????????", "????????????");
                        startTakingPanor(mIsWide);
                    } else {
                        // TODO: 2019/7/25 ??????????????????
                        if (mYawMoveStatus == mYawCount) {
                            LogUtils.d("????????????", "(???????????????)????????????,????????????");
                            startTakePanorPhoto(3, isAutoPhoto);
                        } else {
                            LogUtils.d("????????????", "(???????????????)????????????" + mYawMoveStatus + "||" + mYawCount);
                            if (mLastVrTime == 0) {
                                if (mCurrentVrTime != lastFileTime) {
                                    LogUtils.d("????????????", "(???????????????)????????????????????????????????????????????????");
                                    takePanorPhotoSuccess(isAutoPhoto, data.getParam());
                                } else {
                                    //2:???????????????
                                    LogUtils.d("????????????", "(???????????????)?????????????????????????????????????????????" + mYawMoveStatus);
                                    startTakePanorPhoto(4, isAutoPhoto);
                                }
                            } else {
                                //??????????????????
                                if (mYawMoveStatus == 0) {
                                    if ((lastFileTime == mCurrentVrTime)) {
                                        //??????????????????????????????????????????
                                        LogUtils.d("????????????", "(???????????????)???????????????????????????,???????????????????????????,?????????????????????");
                                    } else {
                                        //??????????????????????????????????????????
                                        LogUtils.d("????????????", "(???????????????)???????????????????????????,???????????????,?????????????????????");
                                        takePanorPhotoSuccess(isAutoPhoto, data.getParam());
//                                        takePanorSingleSucces(parma);
                                    }
                                } else {
                                    if (lastFileTime == mCurrentVrTime) {
                                        LogUtils.d("????????????", "(???????????????)???????????????????????????,?????????????????????,????????????????????????");
                                        startTakePanorPhoto(5, isAutoPhoto);
                                    } else {
                                        LogUtils.d("????????????", "(???????????????)???????????????????????????,?????????????????????,????????????????????????????????????");
                                        takePanorPhotoSuccess(isAutoPhoto, data.getParam());
//                                        takePanorSingleSucces(parma);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (isStartPanor) {
                        // TODO: 2019/7/25 ?????????????????????????????? ?????????currentVrTime;
                        LogUtils.d("????????????", "????????????,??????????????????????????????");
                        startTakingPanor(mIsWide);
                    }
                }
            }
        });
    }


    /*??????end***********************************************************/
    /*??????start*********************************************************/
    private void showAroundDialog() {
        dialogShowingAndStart(true, true);
        mIPMHelper.createAroundD(new AroundDialog.OnClickCallback() {
            @Override
            public void onSetting() {
                //???????????????????????????
                mapVToFullScreen();
                PlaneCommand.getInstance().initAroundMode();
                mapV.addAroundCenterMarker();
            }

            @Override
            public void onStart(float speed, double radius) {
                PlaneCommand.getInstance().starAround(Math.abs(speed) > 0 ? speed * 100 : 0);
            }

            @Override
            public void onSpeedChanged(float speed) {
                PlaneCommand.getInstance().starAround(Math.abs(speed) > 0 ? speed * 100 : 0);

            }

            //            @Override
//            public void onPause() {
//                PlaneCommand.getInstance().starAround(0);
//            }
//
//            @Override
//            public void onResume(float speed) {
//                PlaneCommand.getInstance().starAround(Math.abs(speed) > 0 ? speed : 0);
//            }

            @Override
            public void onDestroy() {
                //????????????????????????????????????
                PlaneCommand.getInstance().starAround(0);
                mapV.removeAroundCenter();
                dialogShowingAndStart(false, false);
            }
        }).showAroundD();
    }

    /**
     * ????????????
     */
    public void updateAroundR(float radius) {
        mIPMHelper.updateAroundR(radius);
    }

    public void updateAroundHeight(String height) {
        mIPMHelper.updateAroundH(height);
    }
    /*??????end***********************************************************/

    /*??????start*********************************************************/

    /**
     * ?????????????????????
     */
    private void startDelayTime() {
        PlaneCommand.getInstance().sendYaw(false, 0, 106);
        if (mCallback != null)
            mCallback.onStopVideo(false, false);
        showAndHintFunctionLy(false, 5);
        CameraCommand.getCmdInstance().takePhotoInDelayModeComplete(mbv,
                mCameraSetting.lastUseMode == CameraSetConstaint.CameraModeMovie,
                ConnectManager.getInstance().mProductModel.productType, true, new CameraCommandCallback<Boolean>() {
                    @Override
                    public void onComplete(Boolean success) {
                        if (mCallback != null) mCallback.onPlayVideo(false);
                        if (success) {
                            if (mCallback != null) {
                                mCallback.onModeChange(PhotoModeDelay, 1);
                            }
                            dialogShowingAndStart(true, true);
                            showDelayTimeDialog();
                        }
                    }
                });
    }

    long startDelayTime = 0;

    public void showDelayTimeDialog() {
        //???????????????????????????????????????????????????12??????????????????
        startDelayTime = TimeUtils.getNowTimeMills();
        mIPMHelper.createDelayTimeD(new DelayTimeDialog.OnEventCallback() {
            @Override
            public void onConfirmSpeed() {

            }

            @Override
            public void onStart(boolean start, int vx, int vy) {
                startSendDelayTimeV(start, 2, vx, vy);
            }

            @Override
            public void onStop(boolean start, int vx, int vy) {
                isStart = start;
                startSendDelayTimeV(start, 2, vx, vy);
                dialogShowingAndStart(false, false);
            }

            @Override
            public void onDestory(boolean start) {
                PlaneCommand.getInstance().sendYaw(false, 0, 106);
                isStart = start;
                stopDelayTimer();
                dismissDialogAndCancelFunction(9);
            }
        }).showDelayTimeD();
    }

    private void startSendDelayTimeV(boolean start, int delayTime, int vx, int vy) {
        isStart = start;
        RxLoopSchedulers.composeLoop(mbv, 0, 800, new Function() {
            @Override
            public Object apply(Object o) throws Exception {
                if (isStart) {
                    PlaneCommand.getInstance().executeDelayTime(isStart, vx, vy);
                } else {
                    PlaneCommand.getInstance().executeDelayTime(isStart, 30, 30);
                }
                return isStart;
            }
        }).subscribe(new RxLoopObserver<Object>() {
            @Override
            public void onNext(Object obj) {
                super.onNext(obj);
                if (!isStart) {
                    //????????????
                    this.disposeDisposables();
                }
            }
        });
        if (isStart) {
            openDelayTimer(delayTime, isStart);
        }
    }

    int time = 0;
    boolean mIsDelayTaking;
    int delayTime = 0;

    /**
     * ???????????????????????????
     */
    public void openDelayTimer(int delayTime, boolean isStart) {
        this.isStart = isStart;
        mIsDelayTaking = true;
        this.delayTime = delayTime;
        if (delayTime > 0) {
            dialogShowingAndStart(true, false);
        }
        mCurPanorCount = 1;
        if (mCameraSetting != null && mCameraSetting.photoISOAuto && mCameraSetting.photoAELock) {
            if (mCallback != null) mCallback.onEvChange(true);
        } else {
            startTakeDelayPhoto(delayTime);
        }

    }

    public void stopDelayTimer() {
        mIsDelayTaking = false;
        isStart = false;
        delayTime = 0;
        startDelayTime = 0;
        mCurPanorCount = 0;
    }


    public void startTakeDelayPhoto(int delayTime) {
        LogUtils.d("????????????====>??????" + delayTime + "||" + isStart);
        this.delayTime = delayTime;
        time = 0;
        //???????????????
        if (isStart) {
            RxLoopSchedulers.composeLoop(mbv, 0, 1000, new Function() {
                @Override
                public Integer apply(Object o) throws Exception {
                    time++;
                    return time;
                }
            }).subscribe(new RxLoopObserver<Integer>() {
                @Override
                public void onNext(Integer time) {
                    super.onNext(time);
                    if (mIsDelayTaking) {
                        if (time >= delayTime) {
                            if (mCallback == null || !mCallback.canTakeNextPhoto()) return;

                            //?????????????????????
                            this.disposeDisposables();
                            if (isStart) {
                                LogUtils.d("????????????", "????????????" + delayTime + "|||" + mCurPanorCount);
                                takeTimeLapsePhoto(delayTime, mCurPanorCount);
                            }
                        } else {
                            //?????????...
                            LogUtils.d("????????????", "?????????" + time);
                            if (mCallback != null) {
                                mCallback.onDelayTimer(delayTime - time);
                            }
                        }
                    } else {
                        //????????????????????????????????????
                        this.disposeDisposables();
                        LogUtils.d("????????????", "????????????" + time);
                    }
                }
            });
        }

    }


    /**
     * @param delayTime ????????????
     * @param count     ????????????
     */
    private void takeTimeLapsePhoto(int delayTime, int count) {
        LogUtils.d("????????????", "???????????????" + count + "???");
        if (mCallback != null) mCallback.onStopVideo(false, true);
        CameraCommand.getCmdInstance().takePhotoInDelayComplete(mbv, count == 1, new CameraCommandCallback<Boolean>() {
            @Override
            public void onComplete(Boolean success) {
                if (mCallback != null) mCallback.onPlayVideo(true);
                if (success) {
                    mCurPanorCount++;
                    LogUtils.d("?????????" + mCurPanorCount + "??????????????????," + delayTime);
                    if (delayTime == 2 && (/**mCurPanorCount > 400 ||   //????????????**/
                            (mStatusBean != null && mStatusBean.getCurBattey() <= 20)
                            /*||(startDelayTime != 0 && TimeUtils.getNowTimeMills() - startDelayTime >= 12 * 60 * 1000)*/)) {//????????????????????????
                        /**?????????????????????????????????240*/
                        dismissDialogAndCancelFunction(3);
                    } else {
                        LogUtils.d("????????????", "???????????????" + count + "????????????????????????" + mCurPanorCount + "???");
                        updateDelayCount(mCurPanorCount);
                        if (count == 1) {
                            /**?????????????????????????????????*/
                            if (mCallback != null) mCallback.onEvChange(true);
                        } else {
                            startTakeDelayPhoto(delayTime);
                        }
                    }
                } else {
                    dismissDialogAndCancelFunction(4);
                    ToastUtils.showLongToast(ResourceUtils.getString(R.string.take_delaytime_error));
                }
            }
        });
    }

    /**
     * ?????????????????????????????????
     */
    public void updateDelayCount(int count) {
        mIPMHelper.updateDelayTimeD(count);
    }

    public void updateFlyDistance(String distance) {
        mIPMHelper.updateDelayDistance(distance);
    }


    /**
     * ????????????????????????????????????
     */
    public boolean isDelayTimeTaking() {
        return mIPMHelper.isDelayTimeDShowing();
    }

    public void dismissDelayTime() {
        if (isDelayTimeTaking())
            mIPMHelper.dismissDelayTimeD();
    }

    /*??????end***********************************************************/

    /*??????start*********************************************************/
    public void showWayPointDialog() {
        dialogShowingAndStart(true, true);
        mIPMHelper.createWaypointD(new WayPointDialog.OnEventCallback() {

            @Override
            public void confirmPoints(int total) {
                PlaneCommand.getInstance().writeWaypoinit(total);
            }

            @Override
            public void startWaypoints(float speed, int total) {
                //???????????????????????????>????????????????????????
                PlaneCommand.getInstance().setMavlinkParam(MAV_DATA_STREAM.WPNAV_SPEED, (int) speed * 100);
                executeWaypointFly(true);
            }

            @Override
            public void startedChangeSpeed(float speed) {
                PlaneCommand.getInstance().setMavlinkParam(MAV_DATA_STREAM.WPNAV_SPEED, (int) speed * 100);
            }

            @Override
            public void onPause() {
                dismissDialogAndCancelFunction(5);
            }

            @Override
            public void onDestoryFlying() {
                if (mapV.isZh()) {
                    if (waypointModel != null && waypointModel.getWaypointMarkers() != null)
                        PlaneCommand.getInstance().clearWayponit(waypointModel.getWaypointMarkers().size());
                } else {
                    if (mapboxWayPointModel != null && mapboxWayPointModel.getWaypointMarkers() != null)
                        PlaneCommand.getInstance().clearWayponit(mapboxWayPointModel.getWaypointMarkers().size());
                }
                executeWaypointFly(false);
                mapV.removeWaypoint();
            }

        }).showWaypointD();
        mapVToFullScreen();
    }

    public void startWaypoint(int flyAlt) {
        wpIndex = -1;
        writeWaypoint(flyAlt);
    }


    /**
     * ???????????????????????????
     */
    public void writeWaypoint(int flyAlt) {
        wpIndex++;
        if (mapV.isZh()) {
            if (wpIndex >= 0 && waypointModel != null && waypointModel.getWaypointMarkers().size() > wpIndex) {
                LatLng latlng = GPSUtils.gcj2WGSExactly(
                        waypointModel.getWaypointMarkers().get(wpIndex).getPosition().latitude,
                        waypointModel.getWaypointMarkers().get(wpIndex).getPosition().longitude);
                PlaneCommand.getInstance().writhWaypointLg(latlng.latitude, latlng.longitude, wpIndex + 1, flyAlt);
            }
        } else {
            if (wpIndex >= 0 && mapboxWayPointModel != null && mapboxWayPointModel.getWaypointMarkers().size() > wpIndex) {
                LogUtils.d("??????????????????");
                MapboxLatLng latlng = mapboxWayPointModel.getWaypointMarkers().get(wpIndex).getLatLng();
                PlaneCommand.getInstance().writhWaypointLg(latlng.getLatitude(), latlng.getLongitude(), wpIndex + 1, flyAlt);
            }
        }
    }

    public void executeWaypointFly(boolean isStart) {
        if (mapV.isZh()) {
            if (waypointModel != null && waypointModel.getWaypointMarkers() != null && waypointModel.getWaypointMarkers().size() > 0) {
                //???????????????????????????????????????
                LogUtils.d("??????????????????" + isStart);
                PlaneCommand.getInstance().executeWaypointFly(isStart ? 1 : 0);
            }
        } else {
            if (mapboxWayPointModel != null && mapboxWayPointModel.getWaypointMarkers() != null && mapboxWayPointModel.getWaypointMarkers().size() > 0) {
                //???????????????????????????????????????
                LogUtils.d("??????????????????" + isStart);
                PlaneCommand.getInstance().executeWaypointFly(isStart ? 1 : 0);
            }
        }
        wpIndex = -1;
    }

    public void executeWaypointFlySuccess(boolean isSuccess) {
        if (mIPMHelper.isWaypointDShowing()) {

        }
    }

    /**
     * ??????????????????????????????
     */
    public void writeWaypointsCountSuccess(int flyAlt) {
        if (mapV.isZh()) {
            if (waypointModel != null && waypointModel.getWaypointMarkers().size() > 0) {
                LatLng latlng = GPSUtils.gcj2WGSExactly(waypointModel.getWaypointMarkers().get(wpIndex - 1).getPosition().latitude,
                        waypointModel.getWaypointMarkers().get(wpIndex - 1).getPosition().longitude);
                PlaneCommand.getInstance().writhWaypointLg(latlng.latitude, latlng.longitude, wpIndex, flyAlt);
            }
        } else {
            if (wpIndex >= 0 && mapboxWayPointModel != null && mapboxWayPointModel.getWaypointMarkers().size() > wpIndex) {
                MapboxLatLng latlng = mapboxWayPointModel.getWaypointMarkers().get(wpIndex).getLatLng();
                PlaneCommand.getInstance().writhWaypointLg(latlng.getLatitude(), latlng.getLongitude(), wpIndex + 1, flyAlt);
            }
        }
    }

    /*??????end***********************************************************/

    /*??????start*********************************************************/
    public void showFollowDialog() {
        dialogShowingAndStart(true, true);
        mIPMHelper.createFollowD(new FollowSnapDialog.OnEventCallback() {

            @Override
            public void onStart(LatLng latLng) {
                //????????????????????????????????????????????????
                PlaneCommand.getInstance().startFollow(true);
            }

            @Override
            public void onStartedLatLngChanged(LatLng curLatLng, LatLng oldLatLng) {
                //??????????????????????????????,????????????????????????vx??? vy
                int vx = (int) (GPSUtils.vxSpeedWithLoc1(oldLatLng, curLatLng) * 100);
                int vy = (int) (GPSUtils.vySpeedWithLoc1(oldLatLng, curLatLng) * 100);
                PlaneCommand.getInstance().followGPSPosition(curLatLng.latitude, curLatLng.longitude, vx, vy);
            }

            @Override
            public void onDistory() {
                /**????????????*/
                PlaneCommand.getInstance().startFollow(false);
            }
        }).showFollowD();
    }

    public boolean isFollowDShown() {
        return mIPMHelper.isFollowDShowing();
    }

    public void updateFollowHeight(String alt) {
        mIPMHelper.updateFollowDHeight(alt);
    }

    public void updateFollowGps(float mobileGPS, LatLng mobileLatlng, float distance) {
        mIPMHelper.updateFollowDGps(mobileGPS, mobileLatlng, distance);
    }
    /*??????end***********************************************************/

    public void mapVToFullScreen() {
        if (mapV != null && mapV.isSmall()) mapV.toFullScreen();
    }

    /**
     * map????????????
     */
    public void onMapClick(LatLng latLng, int fanceR) {
        if (mIPMHelper.isWaypointSetPoints() && mapV != null) {
            waypointModel = mapV.addWaypointMarker(mbv.getPageActivity(), latLng, fanceR);
            if (waypointModel != null) {
                mIPMHelper.updateWayponitD(waypointModel.getWaypointMarkers().size(), ConvertUtils.floatToString(waypointModel.getTotalMileages()));
            }

        }
    }

    public void onBoxMapClick(com.mapbox.mapboxsdk.geometry.LatLng latLng, int fanceR) {
        if (mIPMHelper.isWaypointSetPoints() && mapV != null) {
            mapboxWayPointModel = mapV.addWayPointMarkerOnBoxMap(mbv.getPageActivity(), latLng, fanceR);
            if (mapboxWayPointModel != null) {
                mIPMHelper.updateWayponitD(mapboxWayPointModel.getWaypointMarkers().size(), ConvertUtils.floatToString(mapboxWayPointModel.getTotalMileages()));
            }

        }
    }


    public void setWaypoitSpeed(int wayPointSpeed) {
        mIPMHelper.setWaypointSpeed(wayPointSpeed);
    }

    /**
     * ??????????????????
     **/
    public void readLocationData(LocationBean lBean) {
//        LogUtils.d("????????????===>" + lBean.getcRadius());
        mapV.movePlaneMarker(lBean);
    }

    /**
     * ??????????????????
     */
    public void servoSuccess() {
        LogUtils.d("??????????????????====???");
        if (isTakingDialogShown() && isStart)
            startSendServo(5, false, isAutoPhoto, mCurPanorCount, mServoAngle);
    }

    public void setCameraSetting(FPCameraSettingBase cameraSetting) {
        this.mCameraSetting = cameraSetting;
        this.isAutoPhoto = cameraSetting.photoISOAuto;
        showAndHintFunctionLy(false, 6);
    }


    public void yawStatus(int status) {
//        LogUtils.d("????????????", "" + status + "||" + mYawCount);
        if (isTakingDialogShown() || isDelayTimeTaking()) {
            this.mYawMoveStatus = status;
            if (status == mYawCount) {
                //????????????
                LogUtils.d("????????????", "????????????....");
                yawSuccess(true);
                if (isTakingDialogShown()) {
                    mYawCount++;
                }
            } else if (status == 0) {
                //????????????
//                LogUtils.d("????????????", "????????????....");
            } else if (status == 20) {
                //?????????????????????????????????
                //????????????20
                isPanorSuccess = true;
                mIPMHelper.dismissTakingPanorD(3);
                dismissDelayTime();
                dialogShowingAndStart(false, false);
            } else if (status == 50) {
                //????????????????????????????????????
            }
            if ((status == 106 || status == 50 || status == 40)) {
                //??????????????????
            }
        }

    }

    public void setFlyAlt(String flyAlt) {
        if (!StringUtils.isEmpty(flyAlt)) {
            updateFollowHeight(flyAlt);
            updateAroundHeight(flyAlt);
        }
    }

    public void setFlyDistance(String flyDistance) {
        if (!StringUtils.isEmpty(flyDistance)) {
            updateFlyDistance(flyDistance);
        }
    }

    public boolean isSmartWithPhoto() {
        return isTakingDialogShown() || isDelayTimeTaking();
    }


    /*****????????????end********************************************************************/

    public boolean isStarted() {
        return isStart;
    }

    public void onPause() {
        isPaused = true;
    }

    public void onResume() {
        isPaused = false;
    }

    public void onDestroy() {

    }

    /**
     * ??????????????????
     */
    public void aeLockChangedSuccess(FPCameraSettingBase cameraSetting, boolean photoAeLock) {
        LogUtils.d("????????????", "????????????====>" + photoAeLock + "||||" + mIsDelayTaking);
        setCameraSetting(cameraSetting);
        if (photoAeLock) {
            if (isTakingDialogShown()) {
                /**??????????????????????????????????????????????????????**/
                continueTakingPanorWithAeLockStatus();
            }

        }
        if (mIsDelayTaking) {
            startTakeDelayPhoto(delayTime);
        }

    }

    /**
     * ????????????
     */
    public void setReturning(boolean isRetrun) {
        cameraTurnBackBtn.setImageResource(isRetrun ? R.mipmap.smart_cancel_img : R.mipmap.camera_turn_back);
        cameraTurnBackBtnTv.setVisibility(isRetrun ? VISIBLE : GONE);
    }

    public void setPlaneStatus(BatteryBean bean) {
        this.mStatusBean = bean;
        yawStatus(bean.getYawMoveStatus());
        setFlyAlt(bean.getFlyAlt());
        setFlyDistance(bean.getFlyDistance());
        if (bean.isRcConnented()) {
            if (bean.getCustomMode() == 6) {
                // TODO: 2019/8/9 ?????????(?????????????????????)..
//                ToastUtils.showLongToast("????????????????????????");
                setReturning(true);
                dismissDialogAndCancelFunction(6);
            } else {
                // TODO: 2019/8/9 ??????????????????
                setReturning(false);
                if (bean.getFlyStatus() == 2) {
                    // TODO: 2019/8/9 ????????????
                    if (bean.getCustomMode() == 2) {
//                        ToastUtils.showLongToast("??????????????????????????????");
                        dismissDialogAndCancelFunction(7);
                    }
                } else if (bean.getFlyStatus() == 24) {
                    // TODO: 2019/8/9 ????????????
//                    ToastUtils.showLongToast("????????????????????????");
                    dismissDialogAndCancelFunction(8);
                }
            }
        }
    }

    /**
     * ??????????????????????????????????????????????????????????????????
     */
    public void outOfTouch(MotionEvent event) {
        if (!ViewUtils.inRangeOfView(abControlFunctionLy, event) && !ViewUtils.inRangeOfView(cameraSmartButtonLy, event)) {
            if (abControlFunctionLy.getVisibility() == VISIBLE) {
                showAndHintFunctionLy(false, 7);
            }
        }
    }


    public void setAdjustControlShown(boolean isShown) {
        adjustControlShowBtn.setImageDrawable(ResourceUtils.getDrawabe(isShown ?
                R.drawable.adjust_control_hint_button_selcetor : R.drawable.adjust_control_show_button_selcetor));
    }

    /**
     * ???????????????????????????
     */
    public void setAdjustControlBntVisibility(boolean isShown) {
        if (adjustControlShowBtn != null)
            adjustControlShowBtn.setVisibility(isShown ? VISIBLE : INVISIBLE);
    }

    /**
     * ???????????????????????????
     */
    public void setAdjustControlBntEnable(boolean isEnable) {
        if (adjustControlShowBtn != null) {
            adjustControlShowBtn.setEnabled(isEnable);
            adjustControlShowBtn.setAlpha(isEnable ? 1f : 0.5f);
        }
    }

    public boolean getAdjustControlBntEnabled() {
        return adjustControlShowBtn.isEnabled();
    }


    /***?????????activity??????**************************************/

    public interface OnActionCallback {

        void onIPMDialogShown(boolean isShown);

        void onReturnBack(boolean isStart);

        void onStart(int status);

        void onAdjustControlShown();

        /**
         * ??????????????????
         */
        void onModeChange(int photoMode, int photoSize);

        void onDelayTimer(int time);

        /**
         * ??????????????????
         *
         * @param isEnabled ???????????????AE???
         */
        void onEvChange(boolean isEnabled);

        void onPlayVideo(boolean isTakePhotoEnd);

        boolean canTakeNextPhoto();

        void onStopVideo(boolean isWide, boolean isTakingPhoto);

        void onMapLacationResult(boolean isSuccess);

        void onDestory();

        void mapTypeChange(int type);

    }
}
