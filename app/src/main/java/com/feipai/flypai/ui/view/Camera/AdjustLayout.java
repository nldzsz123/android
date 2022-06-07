package com.feipai.flypai.ui.view.Camera;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.feipai.flypai.api.RxLoopObserver;
import com.feipai.flypai.api.RxLoopSchedulers;
import com.feipai.flypai.beans.mavlinkbeans.BatteryBean;
import com.feipai.flypai.beans.mavlinkbeans.PlaneParamsBean;
import com.feipai.flypai.connect.ConnectManager;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.ui.view.VerticalSeekBar;
import com.feipai.flypai.utils.PlaneCommand;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.StringUtils;
import com.feipai.flypai.utils.global.ToastUtils;
import com.feipai.flypai.utils.global.ViewUtils;
import com.zhy.autolayout.AutoLinearLayout;
import com.zhy.autolayout.AutoRelativeLayout;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Function;

public class AdjustLayout extends AutoRelativeLayout implements VerticalSeekBar.OnSlideChangeListener {

    private BaseView mbv;

    @BindView(R.id.yuntai_adjust_up)
    ImageView yuntaiAdjustUp;
    @BindView(R.id.yuntai_adjust_seekbar)
    VerticalSeekBar yuntaiAdjustSeekbar;
    @BindView(R.id.yuntai_adjust_down)
    ImageView yuntaiAdjustDown;
    @BindView(R.id.yuntai_adjust_ly)
    AutoLinearLayout yuntaiAdjustLy;
    @BindView(R.id.zoom_adjust_add_img)
    ImageView zoomAdjustAddImg;
    @BindView(R.id.zoom_adjust_seekbar)
    VerticalSeekBar zoomAdjustSeekbar;
    @BindView(R.id.zoom_adjust_minus_img)
    ImageView zoomAdjustMinusImg;
    @BindView(R.id.zoom_adjust_ly)
    AutoLinearLayout zoomAdjustLy;
    @BindView(R.id.adjust_layout_root)
    AutoRelativeLayout adjustLayoutRoot;

    @BindView(R.id.adjust_direction_layout)
    AutoRelativeLayout adjustDirectionLayout;

    @BindView(R.id.adjust_rising)
    TextView adjustRising;
    @BindView(R.id.adjust_turn_left)
    TextView adjustTurnLeft;
    @BindView(R.id.adjust_turn_right)
    TextView adjustTurnRight;
    @BindView(R.id.adjust_falling)
    TextView adjustFalling;
    @BindView(R.id.adjust_front_fly)
    TextView adjustFrontFly;
    @BindView(R.id.adjust_left_fly)
    TextView adjustLeftFly;
    @BindView(R.id.adjust_right_fly)
    TextView adjustRightFly;
    @BindView(R.id.adjust_behind_fly)
    TextView adjustBehindFly;
    @BindView(R.id.adjust_control_start_but)
    Button adjustStart;
    @BindView(R.id.adjust_control_stop_hint_tv)
    TextView adjustStopHintTv;

    private RxLoopObserver<String> mIObservable;
    private RxLoopObserver<String> mYtIObservable;
    private boolean isStartFly;//开始执行控制飞行
    private int upAndDownSpeed = 0;//上升与下降
    private int turnRightAndLeft = 0;//左转与右转
    private int frontAndBehindFly = 0;//前飞与后飞
    private int rightAndLeftFly = 0;//左飞与右飞
    private BatteryBean mStatusBean;
    private List<Integer> contorlList = new ArrayList<>();

    private final static int YUNTAI_UP = -1;
    private final static int YUNTAI_DOWN = 1;
    private final static int YUNTAI_STOP = 0;
    private int yuntaiType = 0;


    // 正在自动变焦
    public boolean isAutoZoom;
    // 自动变焦时间间隔
    public int internaltimer;
    public boolean isAutoAddZoom; // true为自动增加
    public boolean isAutoReduceZoom; // true为自动减少

    private int val;    // 当前滑动条的值
    private int inteval = 1;
    private PlaneParamsBean planeParamsBean;
    private BatteryBean batteryBean;
//    private int maxUpAndDown=10;
//    private int maxFAnd=5;

    public void setmListioner(Callback mListioner) {
        this.mListioner = mListioner;
    }

    public void setPlaneStatus(BatteryBean batteryBean) {
        this.mStatusBean = batteryBean;
        if (batteryBean.getYawMoveStatus() == 20) {
            /**用户动了摇杆*/
            if (isStartFly) {
                stopAdjustControl(true);
                yuntaiAdjust(true, YUNTAI_STOP);
            }
        }
    }

    public void setPitch(double pitch) {
//        LogUtils.d("调节云台角度" + pitch);
        yuntaiAdjustSeekbar.onlySetProgress((int) Math.round(80 - pitch));
        yuntaiAdjustSeekbar.setProgressText((int) Math.round(pitch) + "°");
    }

    public interface Callback {
        void autoZoomEndBlock();

        void valueChangeBlock(int value);

        void autoReduceZoomBlock(boolean begin, int value);

        void autoAddZoomBlock(boolean begin, int value);

        void adjustContorlAvailable(boolean isAvailable);
    }

    private Callback mListioner;

    // 隐藏并调用
    public void hideSliderAndreleaseTimer() {
        stopAutoZoomForSlider();
        setZoomVisibility(false);
//        setVisibility(INVISIBLE);
    }

    // 更新顶部与底部可按下状态信息
    public void updateEnableStatus() {
        if (zoomAdjustSeekbar.getProgress() <= zoomAdjustSeekbar.getMin()) {
            zoomAdjustAddImg.setAlpha(1.0f);
            zoomAdjustMinusImg.setAlpha(0.5f);
            zoomAdjustAddImg.setEnabled(true);
            zoomAdjustMinusImg.setEnabled(false);
        } else if (zoomAdjustSeekbar.getProgress() >= zoomAdjustSeekbar.getMax()) {
            zoomAdjustAddImg.setAlpha(0.5f);
            zoomAdjustMinusImg.setAlpha(1.0f);
            zoomAdjustAddImg.setEnabled(false);
            zoomAdjustMinusImg.setEnabled(true);
        } else {
            zoomAdjustAddImg.setAlpha(1.0f);
            zoomAdjustMinusImg.setAlpha(1.0f);
            zoomAdjustAddImg.setEnabled(true);
            zoomAdjustMinusImg.setEnabled(true);
        }
    }

    public AdjustLayout(Context context) {
        super(context);
        initView(context);
    }

    public AdjustLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.camera_adjust_layout, this, true);
        AutoUtils.auto(view);
        ButterKnife.bind(this, view);
        yuntaiAdjustSeekbar.setOnSlideChangeListener(this);
        yuntaiAdjustSeekbar.setDragAble(false);
        zoomAdjustSeekbar.setOnSlideChangeListener(this);
        yuntaiAdjustSeekbar.setMax(90);
        yuntaiAdjustSeekbar.setMin(-10);
        ViewUtils.setLayoutParams(adjustRising, 158);
        ViewUtils.setLayoutParams(adjustTurnLeft, 148);
        ViewUtils.setLayoutParams(adjustTurnRight, 148);
        ViewUtils.setLayoutParams(adjustFalling, 148);
        ViewUtils.setLayoutParams(adjustFrontFly, 148);
        ViewUtils.setLayoutParams(adjustLeftFly, 148);
        ViewUtils.setLayoutParams(adjustRightFly, 148);
        ViewUtils.setLayoutParams(adjustBehindFly, 148);

    }

    public void bindBaseView(BaseView baseView) {
        this.mbv = baseView;
    }

    public void setMinValue(int value) {
        zoomAdjustSeekbar.setMin(value);
    }

    public int getMinValue() {
        return zoomAdjustSeekbar.getMin();
    }

    public void setMaxValue(int value) {
        zoomAdjustSeekbar.setMax(value);
    }

    public int getMaxValue() {
        return zoomAdjustSeekbar.getMax();
    }

    public void setCurrentProgressText(String value) {
        zoomAdjustSeekbar.setProgressText(value);
    }

    public void setCurrentValue(int value) {
        zoomAdjustSeekbar.onlySetProgress(value);
    }

    public int getCurrentValue() {
        return zoomAdjustSeekbar.getProgress();
    }

    @OnClick({R.id.yuntai_adjust_up,
            R.id.yuntai_adjust_down,
            R.id.zoom_adjust_add_img,
            R.id.zoom_adjust_minus_img,
            R.id.adjust_rising,
            R.id.adjust_falling,
            R.id.adjust_turn_left,
            R.id.adjust_turn_right,
            R.id.adjust_front_fly,
            R.id.adjust_left_fly,
            R.id.adjust_right_fly,
            R.id.adjust_behind_fly,
            R.id.adjust_control_start_but})
    public void onViewClicked(View view) {
        switch (view.getId()) {
//            case R.id.yuntai_adjust_up:
//                //云台+，换算成需要的数据
//                yuntaiAdjust(YUNTAI_UP);
////                int yuntaiProgressUp = yuntaiAdjustSeekbar.getProgress();
////                yuntaiProgressUp++;
////                yuntaiAdjustSeekbar.onlySetProgress(yuntaiProgressUp);
//                //处理数据会到change回调中，直接做处理即可
//                break;
//            case R.id.yuntai_adjust_down:
//                yuntaiAdjust(YUNTAI_DOWN);
////                int yuntaiProgressDown = yuntaiAdjustSeekbar.getProgress();
////                yuntaiProgressDown--;
////                yuntaiAdjustSeekbar.onlySetProgress(yuntaiProgressDown);
//                //处理数据会到change回调中，直接做处理即可
//                break;
            case R.id.yuntai_adjust_up:
                yuntaiAdjust(false, yuntaiType != YUNTAI_UP ? YUNTAI_UP : YUNTAI_STOP);
                break;
            case R.id.yuntai_adjust_down:
                yuntaiAdjust(false, yuntaiType != YUNTAI_DOWN ? YUNTAI_DOWN : YUNTAI_STOP);
                break;
            case R.id.zoom_adjust_add_img:
                val = zoomAdjustSeekbar.getProgress();

                if (isAutoAddZoom) {    // 暂停
                    zoomAdjustAddImg.setImageResource(R.mipmap.zoom_out_img);
                    zoomAdjustMinusImg.setImageResource(R.mipmap.zoom_in_img);
                    zoomAdjustAddImg.setAlpha(1.0f);
                    zoomAdjustMinusImg.setAlpha(1.0f);
                    zoomAdjustAddImg.setEnabled(true);
                    zoomAdjustMinusImg.setEnabled(true);
                    isAutoAddZoom = false;
                    isAutoReduceZoom = false;
                } else {    // 自动增加
                    zoomAdjustAddImg.setImageResource(R.mipmap.zoom_pause);
                    zoomAdjustMinusImg.setImageResource(R.mipmap.zoom_in_img);
                    zoomAdjustAddImg.setAlpha(1.0f);
                    zoomAdjustMinusImg.setAlpha(0.5f);
                    zoomAdjustAddImg.setEnabled(true);
                    zoomAdjustMinusImg.setEnabled(false);
                    isAutoAddZoom = true;
                    isAutoReduceZoom = false;

                    RxLoopSchedulers.composeLoop(mbv, 0, internaltimer, new Function() {
                        @Override
                        public Object apply(Object o) throws Exception {
                            return isAutoAddZoom;
                        }
                    }).subscribe(new RxLoopObserver<Object>() {
                        @Override
                        public void onNext(Object obj) {
                            super.onNext(obj);
                            val += inteval;
                            if (val >= zoomAdjustSeekbar.getMax() && zoomAdjustSeekbar.getProgress() >= zoomAdjustSeekbar.getMax()) {
                                LogUtils.d("已经到了最大值了 %d" + val);
                                stopAutoZoomForSlider();
                                zoomAdjustAddImg.setImageResource(R.mipmap.zoom_out_img);
                                zoomAdjustAddImg.setAlpha(1.0f);
                                zoomAdjustAddImg.setEnabled(true);
                            } else {
                                zoomAdjustSeekbar.onlySetProgress(val);
                                if (mListioner != null) {
                                    mListioner.autoAddZoomBlock(false, val);
                                }
                            }

                            if (!isAutoAddZoom) {
                                //结束循环
                                LogUtils.d("结束自动 增加了");
                                this.disposeDisposables();
                                if (!isAutoReduceZoom && mListioner != null) {
                                    mListioner.autoZoomEndBlock();
                                }
                            }
                        }
                    });

                    if (mListioner != null) {
                        mListioner.autoAddZoomBlock(true, val);
                    }
                }

                break;
            case R.id.zoom_adjust_minus_img:

                val = zoomAdjustSeekbar.getProgress();

                if (isAutoReduceZoom) {    // 暂停
                    zoomAdjustAddImg.setImageResource(R.mipmap.zoom_out_img);
                    zoomAdjustMinusImg.setImageResource(R.mipmap.zoom_in_img);
                    zoomAdjustAddImg.setAlpha(1.0f);
                    zoomAdjustMinusImg.setAlpha(1.0f);
                    zoomAdjustAddImg.setEnabled(true);
                    zoomAdjustMinusImg.setEnabled(true);
                    isAutoAddZoom = false;
                    isAutoReduceZoom = false;
                } else {    // 自动减少
                    zoomAdjustAddImg.setImageResource(R.mipmap.zoom_out_img);
                    zoomAdjustMinusImg.setImageResource(R.mipmap.zoom_pause);
                    zoomAdjustAddImg.setAlpha(0.5f);
                    zoomAdjustMinusImg.setAlpha(1.0f);
                    zoomAdjustAddImg.setEnabled(false);
                    zoomAdjustMinusImg.setEnabled(true);
                    isAutoAddZoom = false;
                    isAutoReduceZoom = true;

                    RxLoopSchedulers.composeLoop(mbv, 0, internaltimer, new Function() {
                        @Override
                        public Object apply(Object o) throws Exception {
                            return isAutoReduceZoom;
                        }
                    }).subscribe(new RxLoopObserver<Object>() {
                        @Override
                        public void onNext(Object obj) {
                            super.onNext(obj);
                            val -= inteval;
                            if (val <= zoomAdjustSeekbar.getMin() && zoomAdjustSeekbar.getProgress() <= zoomAdjustSeekbar.getMin()) {
                                LogUtils.d("已经到了最小值了 %d" + val);
                                stopAutoZoomForSlider();
                                zoomAdjustMinusImg.setImageResource(R.mipmap.zoom_in_img);
                                zoomAdjustMinusImg.setAlpha(1.0f);
                                zoomAdjustMinusImg.setEnabled(true);
                            } else {
                                zoomAdjustSeekbar.onlySetProgress(val);
                                if (mListioner != null) {
                                    mListioner.autoReduceZoomBlock(false, val);
                                }
                            }

                            if (!isAutoReduceZoom) {
                                //结束循环
                                LogUtils.d("结束自动 减少了");
                                this.disposeDisposables();
                                if (!isAutoAddZoom && mListioner != null) {
                                    mListioner.autoZoomEndBlock();
                                }
                            }
                        }
                    });

                    if (mListioner != null) {
                        mListioner.autoReduceZoomBlock(true, val);
                    }
                }
                break;
            case R.id.adjust_rising:
            case R.id.adjust_falling:
            case R.id.adjust_turn_left:
            case R.id.adjust_turn_right:
            case R.id.adjust_front_fly:
            case R.id.adjust_behind_fly:
            case R.id.adjust_right_fly:
            case R.id.adjust_left_fly:
            case R.id.adjust_control_start_but:
//            case R.id.yuntai_adjust_up:
//            case R.id.yuntai_adjust_down:
                adjustControl(view.getId());
                break;
        }
    }

    /**
     * 云台调节
     */
    private void yuntaiAdjust(boolean moveYG, int type) {
        if (mYtIObservable != null)
            mYtIObservable.disposeDisposables();
        if (type == YUNTAI_UP) {
            yuntaiAdjustUp.setImageDrawable(ResourceUtils.getDrawabe(R.mipmap.zoom_pause));
            yuntaiAdjustDown.setImageDrawable(ResourceUtils.getDrawabe(R.mipmap.yuntai_down));
        } else if (type == YUNTAI_DOWN) {
            yuntaiAdjustDown.setImageDrawable(ResourceUtils.getDrawabe(R.mipmap.zoom_pause));
            yuntaiAdjustUp.setImageDrawable(ResourceUtils.getDrawabe(R.mipmap.yuntai_up));
        } else {
            //停止
            yuntaiAdjustDown.setImageDrawable(ResourceUtils.getDrawabe(R.mipmap.yuntai_down));
            yuntaiAdjustUp.setImageDrawable(ResourceUtils.getDrawabe(R.mipmap.yuntai_up));
        }
        yuntaiType = type;
        if (type == YUNTAI_STOP) {
            if (mYtIObservable != null)
                mYtIObservable.disposeDisposables();
            if (!moveYG) {      // 用户未操作遥感，点击APP上按钮停止
                if (!isStartFly) {
                    LogUtils.d("停止飞行2");
                    PlaneCommand.getInstance().adjustFly(false, 0, 0, 0, 0);
                }
                PlaneCommand.getInstance().adjustYuntai(0);
            }

        } else {
            startAdjustYuntaiWithLooper();
        }
    }

    private void startAdjustYuntaiWithLooper() {

        if (mYtIObservable == null) {
            mYtIObservable = new RxLoopObserver<String>() {
                @Override
                public void onNext(String str) {
                    super.onNext(str);
                    if (ConnectManager.getInstance().isConneted()) {
                        if (!isStartFly) {
                            LogUtils.d("开始==停止飞行1");
                            PlaneCommand.getInstance().adjustFly(true, 0, 0, 0, 0);
                        }
                        PlaneCommand.getInstance().adjustYuntai(yuntaiType);
                    } else {
                        this.disposeDisposables();
                    }
                }
            };
        }
        RxLoopSchedulers.composeLoop(mbv, 0, 1000, new Function() {
            @Override
            public String apply(Object o) throws Exception {
                return "";
            }
        }).subscribe(mYtIObservable);
    }


    public void stopYuntaiAdjust() {
        yuntaiAdjust(false, YUNTAI_STOP);
    }

    /**
     * 这些view统一管理
     */
    private void adjustControl(int viewId) {
        switch (viewId) {
            case R.id.adjust_rising:
                // TODO: 2019/11/15 上升 vz
                if (upAndDownSpeed > -3 * 100) {
                    upAndDownSpeed -= 50;
                    if (upAndDownSpeed > 0) {
                        adjustFalling.setText(Math.abs((float) upAndDownSpeed / 100f) + "m/s");
                        adjustBottonSelected(adjustFalling, true);
                    } else if (upAndDownSpeed < 0) {
                        adjustRising.setText(Math.abs((float) upAndDownSpeed / 100f) + "m/s");
                        adjustBottonSelected(adjustRising, true);
                    } else {
                        removeControlClick(R.id.adjust_falling);
                        adjustFalling.setText(ResourceUtils.getString(R.string.falling));
                        adjustBottonSelected(adjustFalling, false);
                    }
                }
                break;
            case R.id.adjust_falling:
                // TODO: 2019/11/15 下降 vz
                if (upAndDownSpeed < 3 * 100) {
                    upAndDownSpeed += 50;
                    if (upAndDownSpeed > 0) {
                        if (addContorlClick(viewId) > 3) {
                            upAndDownSpeed -= 50;
                            removeControlClick(viewId);
                            return;
                        }
                        if (flyAlt() <= 5) {
                            upAndDownSpeed -= 50;
                            ToastUtils.showShortToast(R.string.smart_down_height_shall_not_be_less_than5);
                            return;
                        }
                        adjustFalling.setText(Math.abs((float) upAndDownSpeed / 100f) + "m/s");
                        adjustBottonSelected(adjustFalling, true);
                    } else if (upAndDownSpeed < 0) {
                        adjustRising.setText(Math.abs((float) upAndDownSpeed / 100f) + "m/s");
                        adjustBottonSelected(adjustRising, true);
                    } else {
                        adjustRising.setText(ResourceUtils.getString(R.string.rising));
                        adjustBottonSelected(adjustRising, false);
                    }
                }
                break;
            case R.id.adjust_turn_left:
                // TODO: 2019/11/15 左转 yaw
                if (turnRightAndLeft > -8 * 2) {
                    turnRightAndLeft -= 2;
                    if (turnRightAndLeft > 0) {
                        adjustTurnRight.setText(Math.abs(turnRightAndLeft / 2) + "x");
                        adjustBottonSelected(adjustTurnRight, true);
                    } else if (turnRightAndLeft < 0) {
                        if (addContorlClick(viewId) > 3) {
                            turnRightAndLeft += 2;
                            removeControlClick(viewId);
                            return;
                        }
                        adjustTurnLeft.setText(Math.abs(turnRightAndLeft / 2) + "x");
                        adjustBottonSelected(adjustTurnLeft, true);
                    } else {
                        adjustTurnRight.setText(ResourceUtils.getString(R.string.turn_right));
                        adjustBottonSelected(adjustTurnRight, false);
                    }
                }
                break;
            case R.id.adjust_turn_right:

                // TODO: 2019/11/15 右转 yaw
                if (turnRightAndLeft < 8 * 2) {
                    turnRightAndLeft += 2;
                    if (turnRightAndLeft > 0) {
                        adjustTurnRight.setText(Math.abs(turnRightAndLeft / 2) + "x");
                        adjustBottonSelected(adjustTurnRight, true);
                    } else if (turnRightAndLeft < 0) {
                        adjustTurnLeft.setText(Math.abs(turnRightAndLeft / 2) + "x");
                        adjustBottonSelected(adjustTurnLeft, true);
                    } else {
                        removeControlClick(R.id.adjust_turn_left);
                        adjustTurnLeft.setText(ResourceUtils.getString(R.string.turn_left));
                        adjustBottonSelected(adjustTurnLeft, false);
                    }
                }
                break;
            case R.id.adjust_front_fly:
                // TODO: 2019/11/15 前飞  vx
                if (frontAndBehindFly < 10 * 100) {
                    frontAndBehindFly += 100;
                    if (frontAndBehindFly > 0) {
                        adjustFrontFly.setText(Math.abs(frontAndBehindFly / 100) + "m/s");
                        adjustBottonSelected(adjustFrontFly, true);
                    } else if (frontAndBehindFly < 0) {
                        adjustBehindFly.setText(Math.abs(frontAndBehindFly / 100) + "m/s");
                        adjustBottonSelected(adjustBehindFly, true);
                    } else {
                        removeControlClick(R.id.adjust_behind_fly);
                        adjustBehindFly.setText(ResourceUtils.getString(R.string.behind_fly));
                        adjustBottonSelected(adjustBehindFly, false);
                    }
                } else {
                    // TODO: 2019/11/16 超过最大
                }
                break;
            case R.id.adjust_behind_fly:
                // TODO: 2019/11/15 后飞 vx
                if (frontAndBehindFly > -10 * 100) {
                    frontAndBehindFly -= 100;
                    if (frontAndBehindFly > 0) {
                        adjustFrontFly.setText(Math.abs(frontAndBehindFly / 100) + "m/s");
                        adjustBottonSelected(adjustFrontFly, true);
                    } else if (frontAndBehindFly < 0) {
                        if (addContorlClick(viewId) > 3) {
                            frontAndBehindFly += 100;
                            removeControlClick(viewId);
                            return;
                        }
                        adjustBehindFly.setText(Math.abs(frontAndBehindFly / 100) + "m/s");
                        adjustBottonSelected(adjustBehindFly, true);
                    } else {
                        adjustFrontFly.setText(ResourceUtils.getString(R.string.front_fly));
                        adjustBottonSelected(adjustFrontFly, false);
                    }
                } else {
                    // TODO: 2019/11/16 超出最大
                }
                break;
            case R.id.adjust_right_fly:
                // TODO: 2019/11/15 右飞 vy
                if (rightAndLeftFly < 10 * 100) {
                    rightAndLeftFly += 100;
                    if (rightAndLeftFly > 0) {
                        if (addContorlClick(viewId) > 3) {
                            rightAndLeftFly -= 100;
                            removeControlClick(viewId);
                            return;
                        }
                        adjustRightFly.setText(Math.abs(rightAndLeftFly / 100) + "m/s");
                        adjustBottonSelected(adjustRightFly, true);
                    } else if (rightAndLeftFly < 0) {
                        adjustLeftFly.setText(Math.abs(rightAndLeftFly / 100) + "m/s");
                        adjustBottonSelected(adjustLeftFly, true);
                    } else {
                        adjustLeftFly.setText(ResourceUtils.getString(R.string.left_fly));
                        adjustBottonSelected(adjustLeftFly, false);
                    }
                } else {

                }
                break;
            case R.id.adjust_left_fly:
                // TODO: 2019/11/15 左飞 vy
                if (rightAndLeftFly > -10 * 100) {
                    rightAndLeftFly -= 100;
                    if (rightAndLeftFly > 0) {
                        adjustRightFly.setText(Math.abs(rightAndLeftFly / 100) + "m/s");
                        adjustBottonSelected(adjustRightFly, true);
                    } else if (rightAndLeftFly < 0) {
                        adjustLeftFly.setText(Math.abs(rightAndLeftFly / 100) + "m/s");
                        adjustBottonSelected(adjustLeftFly, true);
                    } else {
                        removeControlClick(R.id.adjust_right_fly);
                        adjustRightFly.setText(ResourceUtils.getString(R.string.right_fly));
                        adjustBottonSelected(adjustRightFly, false);
                    }
                }
                break;
//            case R.id.yuntai_adjust_up:
//                yuntaiAdjust(yuntaiType != YUNTAI_UP ? YUNTAI_UP : YUNTAI_STOP);
//                break;
//            case R.id.yuntai_adjust_down:
//                yuntaiAdjust(yuntaiType != YUNTAI_DOWN ? YUNTAI_DOWN : YUNTAI_STOP);
//                break;
            case R.id.adjust_control_start_but:
//                if (!isStartFly) {
//                    if (mStatusBean == null) {
//                        ToastUtils.showShortToast(ResourceUtils.getString(R.string.plane_not_connected));
//                        break;
//                    } else {
//                        if (!mStatusBean.isUnlocked()) {
//                            //未解锁
//                            ToastUtils.showShortToast(ResourceUtils.getString(R.string.plane_unlocked));
//                            break;
//                        } else {
//                        }
//                    }
//                }
//                if (flyAlt() < 5 && !isStartFly) {
//                    if (mListioner != null) {
//                        mListioner.adjustContorlAvailable(false);
//                    }
////                    ToastUtils.showLongToast(ResourceUtils.getString(R.string.disable_smart_mode));
//                    return;
//
//                }
                isStartFly = !isStartFly;
                adjustStartFly(false, isStartFly);
                break;
        }
    }

    private void startFlyWithLooper() {
        if (mIObservable == null) {
            mIObservable = new RxLoopObserver<String>() {
                @Override
                public void onNext(String str) {
                    super.onNext(str);
                    if (ConnectManager.getInstance().isConneted()) {
                        PlaneCommand.getInstance().adjustFly(isStartFly, frontAndBehindFly, rightAndLeftFly, upAndDownSpeed, turnRightAndLeft);
                    } else {
                        this.disposeDisposables();
                    }
                }
            };
        }
        RxLoopSchedulers.composeLoop(mbv, 0, 1000, new Function() {
            @Override
            public String apply(Object o) throws Exception {
                return "";
            }
        }).subscribe(mIObservable);
    }

    private void adjustBottonSelected(TextView view, boolean isSelect) {
        view.setBackground(ResourceUtils.getDrawabe(isSelect ? R.mipmap.control_bg_select : R.mipmap.control_bg_def));
        view.setTextColor(ResourceUtils.getColor(isSelect ? R.color.color_4097e1 : R.color.color_ffffff));
    }

    private void adjustStartFly(boolean moveYg, boolean isStartFly) {
        adjustStart.setText(ResourceUtils.getString(isStartFly ? R.string.stop_fly : R.string.start_fly));
        adjustStart.setBackground(ResourceUtils.getDrawabe(isStartFly ? R.drawable.fe9700_btn_selcetor : R.drawable.login_button_selcetor));
        adjustStopHintTv.setVisibility(isStartFly ? VISIBLE : GONE);
        if (!isStartFly) {
            if (mIObservable != null)
                mIObservable.disposeDisposables();
            if (yuntaiType == YUNTAI_STOP && !moveYg) {
                LogUtils.d("停止飞行1");
                PlaneCommand.getInstance().adjustFly(isStartFly, frontAndBehindFly, rightAndLeftFly, upAndDownSpeed, turnRightAndLeft);
            }
        } else {
            startFlyWithLooper();
        }
    }

    public void stopAutoZoomForSlider() {
        zoomAdjustAddImg.setImageResource(R.mipmap.zoom_out_img);
        zoomAdjustMinusImg.setImageResource(R.mipmap.zoom_in_img);
        zoomAdjustAddImg.setAlpha(1.0f);
        zoomAdjustMinusImg.setAlpha(1.0f);
        zoomAdjustAddImg.setEnabled(true);
        zoomAdjustMinusImg.setEnabled(true);
        isAutoAddZoom = false;
        isAutoReduceZoom = false;
    }

    public void stopRecord() {
        stopAutoZoomForSlider();
        if (mListioner != null) {
            mListioner.autoZoomEndBlock();
        }
    }


    @Override
    public void OnSlideChangeListener(View view, int progress) {
        if (view.getId() == R.id.zoom_adjust_seekbar) {
            if (mListioner != null) {
                mListioner.autoZoomEndBlock();
            }
            if (mListioner != null) {
                mListioner.valueChangeBlock(progress);
            }
        } else if (view.getId() == R.id.yuntai_adjust_seekbar) {
            yuntaiAdjustSeekbar.setProgressText(progress + "°");
        }
    }

    public void setDragAble(boolean dragAble) {
        LogUtils.d("变焦条可拖动===>" + dragAble);
        zoomAdjustSeekbar.setDragAble(dragAble);
    }

    @Override
    public void onSlideStopTouch(View view, int progress) {

    }

    /**
     * 设置缩放条显示与隐藏
     */
    public void setZoomVisibility(boolean isShow) {
        if (zoomAdjustLy != null) {
            zoomAdjustLy.setVisibility(isShow ? VISIBLE : INVISIBLE);
        }

    }


    /**
     * 缩放条是否显示
     */
    public boolean isZoomShow() {
        return zoomAdjustLy != null && zoomAdjustLy.getVisibility() == VISIBLE;
    }


    /**
     * 方向控制中心是否显示
     */
    public boolean isAdjustControlShow(boolean isShow) {
        return adjustDirectionLayout != null && adjustDirectionLayout.getVisibility() == VISIBLE;
    }

    /**
     * 方向控制中心隐藏
     */
    public void setAdjustControlVisibility(boolean isShow) {
        if (adjustDirectionLayout != null) {
            adjustDirectionLayout.setVisibility(isShow ? VISIBLE : INVISIBLE);
        }
    }

    public boolean showAdjustControl() {
        if (adjustDirectionLayout != null) {
            setAdjustControlVisibility(adjustDirectionLayout.getVisibility() != VISIBLE);
            return adjustDirectionLayout.getVisibility() == VISIBLE;
        }
        return false;
    }

    /**
     * 暂不开放云台控制条
     */
    public void setYuntaiAdjustVisibility(boolean isShown) {
        if (yuntaiAdjustLy != null)
            yuntaiAdjustLy.setVisibility(isShown ? VISIBLE : INVISIBLE);
    }

    public void stopAdjustControl(boolean moveYg) {
//        LogUtils.d("停止手动调节飞行");
        if (isStartFly) {
            isStartFly = false;
            adjustStartFly(moveYg, false);
        }
    }

    public void setPlaneParam(PlaneParamsBean planeParam) {
        this.planeParamsBean = planeParam;
//        this.maxUpAndDown=planeParam.getFenceMaxAlt();

    }


    /**
     * 根据飞行高度限定智能按键功能是否可用
     */
    public void upDatePlaneInfo(BatteryBean batteryBean) {
        this.batteryBean = batteryBean;

        if (batteryBean != null) {
            String alt = batteryBean.getFlyAlt();
            if (!StringUtils.isEmpty(alt)) {
                if (isStartFly) {
                    //如果低于10米，禁用智能下降
                    if (upAndDownSpeed > 0 && Float.parseFloat(alt) <= 5) {
                        //已经在下降了,直接恢复到0
                        upAndDownSpeed = 0;
                        adjustFalling.setText(ResourceUtils.getString(R.string.falling));
                        adjustBottonSelected(adjustFalling, false);
                        ToastUtils.showShortToast("高度低于5m,不可继续使用智能下降");
                    }
                    if (upAndDownSpeed < 0 && (planeParamsBean != null && Float.parseFloat(alt) >= planeParamsBean.getFenceMaxAlt())) {
                        upAndDownSpeed = 0;
                        adjustRising.setText(ResourceUtils.getString(R.string.rising));
                        adjustBottonSelected(adjustRising, false);
                        ToastUtils.showShortToast("高度超过围栏高度,不可继续使用智能上升");
                    }
//                    stopAdjustControl(false);
//                    if (mListioner != null) mListioner.adjustContorlAvailable(false);
                } else {
//                    setAdjustStartBtnEnabled(false);
                }
            } else {
//                setAdjustStartBtnEnabled(true);
            }
        }
    }

    private float flyAlt() {
        if (batteryBean != null) {
            String alt = batteryBean.getFlyAlt();
            LogUtils.d("飞行高度为=" + alt);
            if (!StringUtils.isEmpty(alt)) {
                return Float.parseFloat(alt);
            }
        }
        return 0;
    }

    private void setAdjustStartBtnEnabled(boolean isEnable) {
        adjustStart.setEnabled(isEnable);
        adjustStart.setAlpha(isEnable ? 1f : 0.5f);
    }

    private int addContorlClick(int id) {
        if (!contorlList.contains(id)) {
            contorlList.add(id);
        }
        if (contorlList.size() == 4) {
            ToastUtils.showShortToast(ResourceUtils.getString(R.string.prohibited_use));
        }
        return contorlList.size();
    }

    private void removeControlClick(int id) {
        if (contorlList.size() > 0) {
            for (int i = 0; i < contorlList.size(); i++) {
                if (contorlList.get(i) == id) {
                    LogUtils.d("移除一个ViewId" + i);
                    contorlList.remove(i);
                    break;
                }
            }
        }
    }
}
