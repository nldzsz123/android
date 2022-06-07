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
import com.umeng.commonsdk.statistics.common.MLog;

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

    //智能功能
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
    private int mServoAngle;//云台旋转角度
    private int mYawAngle;//飞机旋转角度
    private int mYawCount = 1;//旋转参数（拍摄过程中张数与之一一对应）
    private int mYawMoveStatus = -1;
    //    private boolean mIsPositiveYaw;
    private final static int CENTER_SERVO = 1242;

    //全景第一圈云台
    private final static int FIRST_SERVO_PANOR = 1260;
    //全景第二圈云台
    private final static int SECOND_SERVO_PANOR = 1510;
    //全景第三圈云台
    private final static int THIRD_SERVO_PANOR = 1754;
    //全景第一圈飞机
    private final static int FIRST_YAW_PANOR = 360 / 7;
    //全景第二圈飞机
    private final static int SECOND_YAW_PANOR = 360 / 6;
    //全景第三圈飞机
    private final static int THIRD_YAW_PANOR = 360 / 2;

    /*** 实际拍摄顺序
     * 5  1  4
     * 6  2  3
     */
    //广角第一排
    private final static int FIRST_SERVO_WIDE = 1347;
    //广角第二排
    private final static int SECOND_SERVO_WIDE = 1138;
    //全景第一圈飞机
    private final static int FIRST_YAW_WIDE = 380 / 8;
    private final static int SECOND_YAW_WIDE = 380 / 8 * 2;

    /**
     * 已经拍摄好的全景照片时间
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
     * 与地图绑定
     */
    public void bindMapView(BaseView baseView, MapViewLayout mapViewLy, OnActionCallback callback) {
        this.mCallback = callback;
        this.mbv = baseView;
        this.mapV = mapViewLy;
        this.mapV.setMapChangeListener(new MapViewLayout.OnMapChangedCallback() {
            @Override
            public void onWaypointRemoved(Marker marker) {
                LogUtils.d("航点移出回调");
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
                    // TODO: 2019/7/27 持续发送手机经纬度坐标给飞机,需要转换
                }
//                LogUtils.d("跟随GPS更新" + mobileGPS + "||" + mobileLatlng.latitude + "||" + mobileLatlng.longitude);
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
     * 开始智能功能
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
     * 功能按键是否可点击
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
                        //未解锁
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
                    //用于取消对话框
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
                //未解锁
                ToastUtils.showShortToast(ResourceUtils.getString(R.string.plane_unlocked));
            } else {
                if (mStatusBean.getFlyStatus() == 100 && mStatusBean.getCustomMode() == 5) {
                    switch (type) {
                        case R.id.smart_vr_btn:
                            if (mCallback != null) mCallback.onStart(CameraStatusInPano);
                            mIsWide = false;
                            //延时拍摄时间，按照白天黑夜计算
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
                                LogUtils.d("跟随的间距===>" + mapV.distanceBetweenMobileWithPlane);
                                if (mapV.distanceBetweenMobileWithPlane > 80) {
                                    /**距离小于80米*/
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
     * 关闭对话框，并结束智能功能
     */
    private void dismissDialogAndCancelFunction(int where) {
        dialogShowingAndStart(false, false);
        if (isTakingDialogShown()) {
            //全景显示
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
//        LogUtils.d("显示===>" + isShow + where);
        if (isShow) {
            IAnimationUtils.performShowViewAnim(getContext(), abControlFunctionLy, R.anim.enter_left_anim);
        } else {
            IAnimationUtils.performHideViewAnim(getContext(), abControlFunctionLy, R.anim.exit_left_anim);
        }
    }


    /*****智能功能start******************************************************************/

    /*全景start*********************************************************/
    private void startTakingPanor(boolean isWide) {
        if (mCallback != null) mCallback.onStopVideo(isWide, false);
        CameraCommand.getCmdInstance().changeToPanorMode(mbv,
                ConnectManager.getInstance().mProductModel.productType, mCameraSetting.lastUseMode == CameraSetConstaint.CameraModeMovie,
                true, new CameraCommandCallback<Boolean>() {
                    @Override
                    public void onComplete(Boolean isSuccess) {
                        if (isSuccess) {
                            //切换到全景，尺寸切换成功
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
     * 切换全景模式，朝北向，直到成功
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
                        //结束循环
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
        mYawCount = 1;//初始化必须从1开始
        mIPMHelper.createTakingPanorDg(isWide, new TakingPanorDialog.OnClickCallback() {
            @Override
            public void onStart() {
                /**直接开拍*/
                if (mCameraSetting.photoISOAuto && mCameraSetting.photoAELock)
                    if (mCallback != null) mCallback.onEvChange(false);
            }


            @Override
            public void onInterrupt() {
                //终止拍摄全景，并切回到正常模式,云台回中
            }

            @Override
            public void onCountChanged(int count) {
                sendPanorMessage(isWide, count);
            }

            @Override
            public void onComplete() {
                //完成拍摄,切换到正常模式，此步骤之前云台已回中
                takingPanorComplete();
            }

            @Override
            public void onDestroy() {
                //发送指令回复角度和取消全景状态
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
     * 切换到全景模式成功
     */
    public void changePanorModeSuccess(boolean isPanorSuccess) {
        this.isPanorSuccess = !isPanorSuccess;
    }


    /**
     * 飞机旋转成功
     */
    public void yawSuccess(boolean suceeess) {
        this.isStart = !suceeess;
    }


    /**
     * 当前拍摄第几张照片
     */
    private void sendPanorMessage(boolean isWide, int count) {
        MLog.d("全景拍摄", "当前拍摄====》" + count);
        if (count == 1) {
            startSendServo(1, true, false, count, !isWide ? FIRST_SERVO_PANOR : FIRST_SERVO_WIDE);
        } else if (count > maxCount) {
            MLog.d("全景拍摄", "全景拍摄完成，云台回中");
            startSendServo(2, true, false, count, CENTER_SERVO);
        }
    }

    /**
     * 发送云台旋转指令
     *
     * @param start    是否执行循环，结束循环
     * @param curCount 当前拍摄的张数
     * @param servo    当前云台角度
     */
    private void startSendServo(int where, boolean start, boolean isAutoPhoto, int curCount, int servo) {
        MLog.d("全景拍摄", "云台旋转角度====>" + where + "::::" + start + ":::::" + servo);
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
                        //云台旋转成功，拍照
                        this.disposeDisposables();
                        MLog.d("全景拍摄", "云台旋转成功===>" + curCount);
                        if (!isPaused) {
                            if (curCount <= maxCount) {
                                startTakePanorPhoto(1, isAutoPhoto);
                            } else {
                                /**完成全景，云台回中*/
                                MLog.d("全景拍摄", "云台回中成功");
                                mIPMHelper.dismissTakingPanorD(2);
                            }
                        }
                    }
                }
            });
        }
    }

    private void startSendYaw(int where, boolean start, boolean isPositiveYaw, int yawAngle, int yawCount) {
        MLog.d("全景拍摄", "飞机旋转角度====>" + where + "::::" + isStart + ":::::" + yawAngle + ":::::" + yawCount);
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
                        //飞机旋转成功，拍照
                        this.disposeDisposables();
                        if (mYawCount != 106 && !isPaused)
                            startTakePanorPhoto(2, false);
                        else {
                            //取消全景
                        }
                    }
                }
            });
        }

    }

    private void startTakePanorPhoto(int where, boolean isAutoPhoto) {
        if (mCallback != null) mCallback.onModeChange(PhotoModePano, 0);
        MLog.d("全景拍摄", "计时拍摄延时" + mCurPanorCount + "张" + "|||" + where);
        time = 0;
        //拍照计时器
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
                        //计时结束，开拍
                        this.disposeDisposables();
                        MLog.d("全景拍摄", "开始拍照" + delayTime + "|||" + mCurPanorCount);
                        takeSinglePanorPhoto(isAutoPhoto);
                    } else {
                        //计时中...
                        MLog.d("全景拍摄", "计时中" + time);
                        if (mCallback != null) {
                            mCallback.onDelayTimer(delayTime - time);
                        }
                    }
                } else {
                    //直接结束了，恢复拍摄状态
                    this.disposeDisposables();
                    MLog.d("延时拍摄", "直接结束" + time);
                }
            }
        });
    }

    private void takeSinglePanorPhoto(boolean isAutoPhoto) {
        MLog.d("全景拍摄", "当前拍摄第" + mCurPanorCount + "张");
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
            //拍照成功
            mCurPanorCount++;
            Log.d("全景拍摄", "拍摄成功后，继续拍第" + mCurPanorCount + "张");
            mIPMHelper.updateTakingPanorD(mCurPanorCount);
            if (mCurPanorCount == 2) {
                if (mCameraSetting.photoISOAuto) {
                    // TODO: 2019/7/20 锁定曝光后继续
                    if (mCallback != null) mCallback.onEvChange(false);
//                    aELockStatusChange(false);
                } else {
                    // TODO: 2019/7/20 继续
                    continueTakingPanorWithAeLockStatus();

                }
            } else if (mCurPanorCount == 14) {
                //全景最后一圈
                mYawAngle = THIRD_YAW_PANOR;
                mServoAngle = THIRD_SERVO_PANOR;
                startSendServo(3, true, false, mCurPanorCount, mServoAngle);
            } else {
                if (!mIsWide) {
                    //全景限定
                    if (mCurPanorCount == 8) {
                        //折腾第二圈
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
                    //广角
                    switch (mCurPanorCount) {
                        case 3:
                            //转飞机，向右，一个跨度
                            mYawAngle = FIRST_YAW_WIDE;
                            startSendYaw(11, true, false, mYawAngle, mYawCount);
                            break;
                        case 4:
                            //向上转云台
                            mServoAngle = FIRST_SERVO_WIDE;
                            startSendServo(12, true, false, mCurPanorCount, mServoAngle);
                            break;
                        case 5:
                            //转飞机，向左，两个跨度
                            mYawAngle = SECOND_YAW_WIDE;
                            startSendYaw(13, true, true, mYawAngle, mYawCount);
                            break;
                        case 6:
                            //向下转云台
                            mServoAngle = SECOND_SERVO_WIDE;
                            startSendServo(14, true, false, mCurPanorCount, mServoAngle);
                            break;
                        case 7:
                            //完成
                            MLog.d("全景拍摄", "广角已经拍摄完成");
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
                            /**开始时，打开曝光锁定**/
                        } else {
                            /**继续下一张全景*/
                            continueTakingPanorWithAeLockStatus();
                        }
                    }
                    mCameraSetting.photoAELock = open;
                }
            }
        });
    }

    /**
     * 判定完AE锁之后继续拍摄
     */
    private void continueTakingPanorWithAeLockStatus() {
        if (!mIsWide) {
            //全景
            mYawAngle = FIRST_YAW_PANOR;
            startSendYaw(1, true, false, mYawAngle, mYawCount);
        } else {
            //广角
            mServoAngle = SECOND_SERVO_WIDE;
            startSendServo(10, true, false, mCurPanorCount, mServoAngle);
        }
    }


    /**
     * 退出全景
     */
    private void cancelTakingPanor(int where) {
        if (mCallback != null) mCallback.onDestory();
        MLog.d("全景拍摄", "退出飞机旋转角度====>" + where);
        dialogShowingAndStart(false, false);
        startSendYaw(3, true, false, 0, 106);
    }

    /**
     * 全景拍摄完成
     */
    private void takingPanorComplete() {
        dialogShowingAndStart(false, false);
        sendPanorMessage(mIsWide, mCurPanorCount);
    }

    /**
     * @param isPhoto      获取最后一张照片的文件名
     * @param isStartPanor 主动开启获取最后一个文件名
     */
    public void getLastFileName(boolean isPhoto, boolean isStartPanor) {
        CameraCommand.getCmdInstance().getLastFileName(mbv, isPhoto, new CameraCommandCallback<ABCmdValue<String>>() {
            @Override
            public void onComplete(ABCmdValue<String> data) {
                if (data.getRval() == 0) {
                    long lastFileTime = TimeUtils.lastVrTime(data.getParam());
                    MLog.d("全景拍摄", "断开连上，文件名时间=" + lastFileTime);
                    if (isStartPanor) {
                        // TODO: 2019/7/25 主动开启全景，初始化 全局的currentVrTime;
                        mCurrentVrTime = lastFileTime;
                        MLog.d("全景拍摄", "主动开拍");
                        startTakingPanor(mIsWide);
                    } else {
                        // TODO: 2019/7/25 外界原因获取
                        if (mYawMoveStatus == mYawCount) {
                            MLog.d("全景拍摄", "(断开再连上)旋转完成,直接拍照");
                            startTakePanorPhoto(3, isAutoPhoto);
                        } else {
                            MLog.d("全景拍摄", "(断开再连上)其他状态" + mYawMoveStatus + "||" + mYawCount);
                            if (mLastVrTime == 0) {
                                if (mCurrentVrTime != lastFileTime) {
                                    MLog.d("全景拍摄", "(断开再连上)第一张照片发送了拍照却未收到回复");
                                    takePanorPhotoSuccess(isAutoPhoto, data.getParam());
                                } else {
                                    //2:未发送拍照
                                    MLog.d("全景拍摄", "(断开再连上)第一张照片未发送拍照先不管状态" + mYawMoveStatus);
                                    startTakePanorPhoto(4, isAutoPhoto);
                                }
                            } else {
                                //已拍过照片了
                                if (mYawMoveStatus == 0) {
                                    if ((lastFileTime == mCurrentVrTime)) {
                                        //拍照指令发出去了，收到了回复
                                        MLog.d("全景拍摄", "(断开再连上)刚好拍摄过这张照片,并且收到了照片回复,并且相机旋转中");
                                    } else {
                                        //拍照指令发出去了，未收到回复
                                        MLog.d("全景拍摄", "(断开再连上)刚好拍摄过这张照片,未收到回复,并且相机旋转中");
                                        takePanorPhotoSuccess(isAutoPhoto, data.getParam());
//                                        takePanorSingleSucces(parma);
                                    }
                                } else {
                                    if (lastFileTime == mCurrentVrTime) {
                                        MLog.d("全景拍摄", "(断开再连上)刚好拍摄过这张照片,收到了照片回复,旋转指令发送完成");
                                        startTakePanorPhoto(5, isAutoPhoto);
                                    } else {
                                        MLog.d("全景拍摄", "(断开再连上)刚好拍摄过这张照片,未收到照片回复,那这时候就直接旋转指令咯");
                                        takePanorPhotoSuccess(isAutoPhoto, data.getParam());
//                                        takePanorSingleSucces(parma);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (isStartPanor) {
                        // TODO: 2019/7/25 主动开启全景，初始化 全局的currentVrTime;
                        MLog.d("全景拍摄", "主动开拍,获取最后一个文件失败");
                        startTakingPanor(mIsWide);
                    }
                }
            }
        });
    }


    /*全景end***********************************************************/
    /*环绕start*********************************************************/
    private void showAroundDialog() {
        dialogShowingAndStart(true, true);
        mIPMHelper.createAroundD(new AroundDialog.OnClickCallback() {
            @Override
            public void onSetting() {
                //全屏，并设立中心点
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
                //取消环绕，清除环绕中心点
                PlaneCommand.getInstance().starAround(0);
                mapV.removeAroundCenter();
                dialogShowingAndStart(false, false);
            }
        }).showAroundD();
    }

    /**
     * 环绕半径
     */
    public void updateAroundR(float radius) {
        mIPMHelper.updateAroundR(radius);
    }

    public void updateAroundHeight(String height) {
        mIPMHelper.updateAroundH(height);
    }
    /*环绕end***********************************************************/

    /*延时start*********************************************************/

    /**
     * 开始延时拍摄啦
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
        //拍摄延时开始的时间，记录时间不超过12分钟，判断下
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
                    //结束循环
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
     * 开启延时模式计时器
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
        LogUtils.d("延时摄影====>开拍" + delayTime + "||" + isStart);
        this.delayTime = delayTime;
        time = 0;
        //拍照计时器
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

                            //计时结束，开拍
                            this.disposeDisposables();
                            if (isStart) {
                                MLog.d("延时拍摄", "开始拍照" + delayTime + "|||" + mCurPanorCount);
                                takeTimeLapsePhoto(delayTime, mCurPanorCount);
                            }
                        } else {
                            //计时中...
                            MLog.d("延时拍摄", "计时中" + time);
                            if (mCallback != null) {
                                mCallback.onDelayTimer(delayTime - time);
                            }
                        }
                    } else {
                        //直接结束了，恢复拍摄状态
                        this.disposeDisposables();
                        MLog.d("延时拍摄", "直接结束" + time);
                    }
                }
            });
        }

    }


    /**
     * @param delayTime 延时时间
     * @param count     当前张数
     */
    private void takeTimeLapsePhoto(int delayTime, int count) {
        MLog.d("延时拍摄", "正在拍摄第" + count + "张");
        if (mCallback != null) mCallback.onStopVideo(false, true);
        CameraCommand.getCmdInstance().takePhotoInDelayComplete(mbv, count == 1, new CameraCommandCallback<Boolean>() {
            @Override
            public void onComplete(Boolean success) {
                if (mCallback != null) mCallback.onPlayVideo(true);
                if (success) {
                    mCurPanorCount++;
                    LogUtils.d("已拍摄" + mCurPanorCount + "张，最大张数," + delayTime);
                    if (delayTime == 2 && (/**mCurPanorCount > 400 ||   //张数限定**/
                            (mStatusBean != null && mStatusBean.getCurBattey() <= 20)
                            /*||(startDelayTime != 0 && TimeUtils.getNowTimeMills() - startDelayTime >= 12 * 60 * 1000)*/)) {//最大拍摄时长限定
                        /**限定延时拍摄的最大张数240*/
                        dismissDialogAndCancelFunction(3);
                    } else {
                        MLog.d("延时拍摄", "拍摄成功第" + count + "张，接下来拍摄第" + mCurPanorCount + "张");
                        updateDelayCount(mCurPanorCount);
                        if (count == 1) {
                            /**第一张拍摄完，锁定曝光*/
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
     * 暴露给外界更新延时张数
     */
    public void updateDelayCount(int count) {
        mIPMHelper.updateDelayTimeD(count);
    }

    public void updateFlyDistance(String distance) {
        mIPMHelper.updateDelayDistance(distance);
    }


    /**
     * 是否已经在延时拍摄模式中
     */
    public boolean isDelayTimeTaking() {
        return mIPMHelper.isDelayTimeDShowing();
    }

    public void dismissDelayTime() {
        if (isDelayTimeTaking())
            mIPMHelper.dismissDelayTimeD();
    }

    /*延时end***********************************************************/

    /*航点start*********************************************************/
    public void showWayPointDialog() {
        dialogShowingAndStart(true, true);
        mIPMHelper.createWaypointD(new WayPointDialog.OnEventCallback() {

            @Override
            public void confirmPoints(int total) {
                PlaneCommand.getInstance().writeWaypoinit(total);
            }

            @Override
            public void startWaypoints(float speed, int total) {
                //写入总数，回复——>循序写入航点坐标
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
     * 循环向飞控写入航点
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
                LogUtils.d("循环写入航点");
                MapboxLatLng latlng = mapboxWayPointModel.getWaypointMarkers().get(wpIndex).getLatLng();
                PlaneCommand.getInstance().writhWaypointLg(latlng.getLatitude(), latlng.getLongitude(), wpIndex + 1, flyAlt);
            }
        }
    }

    public void executeWaypointFly(boolean isStart) {
        if (mapV.isZh()) {
            if (waypointModel != null && waypointModel.getWaypointMarkers() != null && waypointModel.getWaypointMarkers().size() > 0) {
                //航点写入完成，执行航点飞行
                LogUtils.d("开始执行航点" + isStart);
                PlaneCommand.getInstance().executeWaypointFly(isStart ? 1 : 0);
            }
        } else {
            if (mapboxWayPointModel != null && mapboxWayPointModel.getWaypointMarkers() != null && mapboxWayPointModel.getWaypointMarkers().size() > 0) {
                //航点写入完成，执行航点飞行
                LogUtils.d("开始执行航点" + isStart);
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
     * 写入航点个数成功回调
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

    /*航点end***********************************************************/

    /*跟随start*********************************************************/
    public void showFollowDialog() {
        dialogShowingAndStart(true, true);
        mIPMHelper.createFollowD(new FollowSnapDialog.OnEventCallback() {

            @Override
            public void onStart(LatLng latLng) {
                //开始时，发送手机当前坐标点给飞机
                PlaneCommand.getInstance().startFollow(true);
            }

            @Override
            public void onStartedLatLngChanged(LatLng curLatLng, LatLng oldLatLng) {
                //实时传递坐标点给飞机,根据新老坐标计算vx， vy
                int vx = (int) (GPSUtils.vxSpeedWithLoc1(oldLatLng, curLatLng) * 100);
                int vy = (int) (GPSUtils.vySpeedWithLoc1(oldLatLng, curLatLng) * 100);
                PlaneCommand.getInstance().followGPSPosition(curLatLng.latitude, curLatLng.longitude, vx, vy);
            }

            @Override
            public void onDistory() {
                /**取消跟随*/
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
    /*跟随end***********************************************************/

    public void mapVToFullScreen() {
        if (mapV != null && mapV.isSmall()) mapV.toFullScreen();
    }

    /**
     * map点击事件
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
     * 当前位置更新
     **/
    public void readLocationData(LocationBean lBean) {
//        LogUtils.d("飞控数据===>" + lBean.getcRadius());
        mapV.movePlaneMarker(lBean);
    }

    /**
     * 云台旋转成功
     */
    public void servoSuccess() {
        LogUtils.d("云台旋转成功====》");
        if (isTakingDialogShown() && isStart)
            startSendServo(5, false, isAutoPhoto, mCurPanorCount, mServoAngle);
    }

    public void setCameraSetting(FPCameraSettingBase cameraSetting) {
        this.mCameraSetting = cameraSetting;
        this.isAutoPhoto = cameraSetting.photoISOAuto;
        showAndHintFunctionLy(false, 6);
    }


    public void yawStatus(int status) {
//        MLog.d("全景拍摄", "" + status + "||" + mYawCount);
        if (isTakingDialogShown() || isDelayTimeTaking()) {
            this.mYawMoveStatus = status;
            if (status == mYawCount) {
                //旋转完成
                MLog.d("全景拍摄", "旋转完成....");
                yawSuccess(true);
                if (isTakingDialogShown()) {
                    mYawCount++;
                }
            } else if (status == 0) {
                //正在旋转
//                MLog.d("全景拍摄", "正在旋转....");
            } else if (status == 20) {
                //用户动了摇杆，直接停掉
                //无故出现20
                isPanorSuccess = true;
                mIPMHelper.dismissTakingPanorD(3);
                dismissDelayTime();
                dialogShowingAndStart(false, false);
            } else if (status == 50) {
                //表示定点成功，可拍摄全景
            }
            if ((status == 106 || status == 50 || status == 40)) {
                //退出全景拍摄
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


    /*****智能功能end********************************************************************/

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
     * 曝光锁定成功
     */
    public void aeLockChangedSuccess(FPCameraSettingBase cameraSetting, boolean photoAeLock) {
        MLog.d("智能拍摄", "曝光锁定====>" + photoAeLock + "||||" + mIsDelayTaking);
        setCameraSetting(cameraSetting);
        if (photoAeLock) {
            if (isTakingDialogShown()) {
                /**锁定曝光的时候，用于全景就是继续拍摄**/
                continueTakingPanorWithAeLockStatus();
            }

        }
        if (mIsDelayTaking) {
            startTakeDelayPhoto(delayTime);
        }

    }

    /**
     * 正在返航
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
                // TODO: 2019/8/9 返航中(或许是主动返航)..
//                ToastUtils.showLongToast("收到一次主动返航");
                setReturning(true);
                dismissDialogAndCancelFunction(6);
            } else {
                // TODO: 2019/8/9 不管什么模式
                setReturning(false);
                if (bean.getFlyStatus() == 2) {
                    // TODO: 2019/8/9 姿态模式
                    if (bean.getCustomMode() == 2) {
//                        ToastUtils.showLongToast("收到一次切了姿态模式");
                        dismissDialogAndCancelFunction(7);
                    }
                } else if (bean.getFlyStatus() == 24) {
                    // TODO: 2019/8/9 返航模式
//                    ToastUtils.showLongToast("收到一次返航模式");
                    dismissDialogAndCancelFunction(8);
                }
            }
        }
    }

    /**
     * 点击位置非智能功能菜单，自动隐藏智能功能菜单
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
     * 现在智能功能不开放
     */
    public void setAdjustControlBntVisibility(boolean isShown) {
        if (adjustControlShowBtn != null)
            adjustControlShowBtn.setVisibility(isShown ? VISIBLE : INVISIBLE);
    }

    /**
     * 现在智能功能不开放
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


    /***回调到activity页面**************************************/

    public interface OnActionCallback {

        void onIPMDialogShown(boolean isShown);

        void onReturnBack(boolean isStart);

        void onStart(int status);

        void onAdjustControlShown();

        /**
         * 模式改变回调
         */
        void onModeChange(int photoMode, int photoSize);

        void onDelayTimer(int time);

        /**
         * 曝光锁定回调
         *
         * @param isEnabled 是否可点击AE锁
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
