package com.feipai.flypai.ui.view.ipm;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.feipai.flypai.base.basedialog.BaseDialog;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.zhy.autolayout.AutoLinearLayout;
import com.zhy.autolayout.utils.AutoUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 1.切换到延时模式，拍照尺寸需要固定
 * 2.调整飞行速度，选择飞行方向
 * 3.不间断的拍摄（拍摄时间判断，开始时计时，拍一张判断一次，张数累积不超过最大张数）
 * 4.结束时将模式切换到正常的拍摄模式
 * <p>
 * 注：刚连接上，不是延时拍照下，切记取消噢
 */
public class DelayTimeDialog {
    @BindView(R.id.delay_time_dg_title)
    TextView mDelayTimeDgTitle;
    @BindView(R.id.delay_time_fly_speed_value)
    TextView mDelayTimeFlySpeedValue;
    @BindView(R.id.delay_time_distance_value_tv)
    TextView mDelayTimeDistanceValueTv;
    @BindView(R.id.delay_time_speed_seekbar)
    SeekBar mDelayTimeSpeedSeekbar;
    @BindView(R.id.delay_time_left)
    TextView mDelayTimeLeft;
    @BindView(R.id.delay_time_front)
    TextView mDelayTimeFront;
    @BindView(R.id.delay_time_back)
    TextView mDelayTimeBack;
    @BindView(R.id.delay_time_right)
    TextView mDelayTimeRight;
    @BindView(R.id.delay_time_dg_tv_header)
    TextView mDelayTimeDgTvHeader;
    @BindView(R.id.delay_time_dg_count)
    TextView mDelayTimeDgCount;
    @BindView(R.id.delay_time_dg_total)
    TextView mDelayTimeDgTotal;
    @BindView(R.id.delay_time_dg_tv_end)
    TextView mDelayTimeDgTvEnd;
    @BindView(R.id.delay_time_start_center_ly)
    LinearLayout mDelayTimeStartCenterLy;
    @BindView(R.id.delay_time_dg_hint_tv)
    TextView mDelayTimeDgHintTv;
    @BindView(R.id.delay_time_dg_btn)
    TextView mDelayTimeDgBtn;
    @BindView(R.id.delay_time_dg_speed_ly)
    AutoLinearLayout mDelayTimeDgSpeedLy;
    @BindView(R.id.delay_time_dg_direction_ly)
    AutoLinearLayout mDelayTimeDgDirectionLy;
    private BaseDialog mBuilder;
    private Context mContext;
    private OnEventCallback mCallback;

    private final static int START_SETTING = 0;
    private final static int SETTING_SPEED = 1;
    private final static int SETING_ORIENTATION = 2;
    private final static int INTERRUPT = 3;
    private final static int COMPLETE = 4;

    private final static int LEFT = 5;
    private final static int RIGHT = 6;
    private final static int FRONT = 7;
    private final static int BACK = 8;
    private int bearing = FRONT;
    private int vx = 0;
    private int vy = 0;
    private int mStep = START_SETTING;
    private int speed = 2;

    private int totalSize = 240;
    private int count = 1;

    public DelayTimeDialog(Context context) {
        LogUtils.d("移动延时速度" + speed);
        this.mContext = context;
    }

    public DelayTimeDialog onCreate(int gravity, int x, int y) {
        mBuilder = new BaseDialog.Builder(mContext, R.style.dialog_for_ipm)
                .setContentView(R.layout.delay_time_dialog_layout)
                .setWidthAndHeight(AutoUtils.getPercentWidthSize(672), AutoUtils.getPercentHeightSize(417))
                .setParamsXAndY(gravity, x, y)
                .setNotTouchModal(true)
                .setCancelable(false)
                .create();
        ButterKnife.bind(this, mBuilder);
        mDelayTimeSpeedSeekbar.setMax(9);
        mDelayTimeSpeedSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        return this;
    }

    private void updateProgress(int progress) {
        mDelayTimeFlySpeedValue.setText(String.valueOf(((float) (progress + 1)) / 10));
        mDelayTimeDistanceValueTv.setText((String.format("%.1f", ((float) (progress + 1) / 10) * 12 * 60)));
    }


    public void updateClues(int step) {
        if (step == START_SETTING) {
            mDelayTimeDgBtn.setText(ResourceUtils.getString(R.string.next_step));
            mDelayTimeDgBtn.setBackgroundResource(R.drawable.login_button_selcetor);
            mDelayTimeDgTitle.setText(ResourceUtils.getString(R.string.make_sure_sufficient_power_and_barrier_free));
            mDelayTimeStartCenterLy.setVisibility(View.VISIBLE);
            mDelayTimeDgTvHeader.setText(ResourceUtils.getString(R.string.total_shooting_size));
            mDelayTimeDgTvHeader.setVisibility(View.VISIBLE);
            mDelayTimeDgCount.setText(String.valueOf(240));
            mDelayTimeDgTotal.setVisibility(View.GONE);
            mDelayTimeDgTvEnd.setText(ResourceUtils.getString(R.string.amount_of_sheets));
            mDelayTimeDgDirectionLy.setVisibility(View.GONE);
            mDelayTimeDgSpeedLy.setVisibility(View.GONE);
            mDelayTimeDgHintTv.setText(ResourceUtils.getString(R.string.make_sure_sufficient_power_and_barrier_free));
            mDelayTimeDgHintTv.setVisibility(View.VISIBLE);
        } else if (step == SETTING_SPEED) {
            //速度设置
            mDelayTimeDgBtn.setText(ResourceUtils.getString(R.string.next_step));
            mDelayTimeDgBtn.setBackgroundResource(R.drawable.login_button_selcetor);
            mDelayTimeDgTitle.setText(ResourceUtils.getString(R.string.fly_speed_and_distance_setting));
            mDelayTimeDgHintTv.setText(ResourceUtils.getString(R.string.fly_at_constant_speed));
            updateProgress(speed);
            mDelayTimeSpeedSeekbar.setProgress(speed);
            mDelayTimeDgHintTv.setVisibility(View.VISIBLE);
            mDelayTimeStartCenterLy.setVisibility(View.GONE);
            mDelayTimeDgSpeedLy.setVisibility(View.VISIBLE);
            mDelayTimeDgDirectionLy.setVisibility(View.GONE);
        } else if (step == SETING_ORIENTATION) {
            //方向选择
            mDelayTimeDgBtn.setText(ResourceUtils.getString(R.string.start));
            mDelayTimeDgBtn.setBackgroundResource(R.drawable.login_button_selcetor);
            mDelayTimeDgTitle.setText(ResourceUtils.getString(R.string.camera_picture_as_front_fly));
            mDelayTimeDgHintTv.setVisibility(View.GONE);
            mDelayTimeStartCenterLy.setVisibility(View.GONE);
            mDelayTimeDgSpeedLy.setVisibility(View.GONE);
            mDelayTimeDgDirectionLy.setVisibility(View.VISIBLE);
        } else if (step == INTERRUPT) {
            mDelayTimeDgBtn.setText(ResourceUtils.getString(R.string.interrupt));
            mDelayTimeDgBtn.setBackgroundResource(R.drawable.fe9700_btn_selcetor);
            mDelayTimeDgTitle.setText(ResourceUtils.getString(R.string.delay_time_shooting));
            mDelayTimeStartCenterLy.setVisibility(View.VISIBLE);
            mDelayTimeDgTvHeader.setText(ResourceUtils.getString(R.string.shooting));
            mDelayTimeDgCount.setText(String.valueOf(count));
            mDelayTimeDgTotal.setText("/" + totalSize);
            mDelayTimeDgTotal.setVisibility(View.VISIBLE);
            mDelayTimeDgTvEnd.setText(ResourceUtils.getString(R.string.amount_of_sheets));
            mDelayTimeDgDirectionLy.setVisibility(View.GONE);
            mDelayTimeDgSpeedLy.setVisibility(View.GONE);
            mDelayTimeDgHintTv.setText(ResourceUtils.getString(R.string.during_will_finish_shooting));
            mDelayTimeDgHintTv.setVisibility(View.VISIBLE);
        } else if (step == COMPLETE) {
            mDelayTimeDgBtn.setText(ResourceUtils.getString(R.string.complete));
            mDelayTimeDgBtn.setBackgroundResource(R.drawable.login_button_selcetor);
            mDelayTimeDgTitle.setText(ResourceUtils.getString(R.string.wrap));
            mDelayTimeStartCenterLy.setVisibility(View.VISIBLE);
            mDelayTimeDgTvHeader.setVisibility(View.GONE);
            mDelayTimeDgCount.setText(String.valueOf(240));
            mDelayTimeDgTotal.setText("/240");
            mDelayTimeDgTotal.setVisibility(View.VISIBLE);
            mDelayTimeDgTvEnd.setText(ResourceUtils.getString(R.string.amount_of_sheets));
            mDelayTimeDgDirectionLy.setVisibility(View.GONE);
            mDelayTimeDgSpeedLy.setVisibility(View.GONE);
            mDelayTimeDgHintTv.setVisibility(View.INVISIBLE);
        }
    }

    public void updeCount(int count) {
        this.count = count;
        mDelayTimeDgCount.setText(String.valueOf(count));
        mDelayTimeDgTotal.setText("/" + totalSize);
    }

    public void updateDelayTimeDistance(String distance) {
        if (mStep == START_SETTING)
            mDelayTimeDistanceValueTv.setText(distance);
    }

//    public void setWaypointSpees(int speed) {
//        this.speed = speed;
//        LogUtils.d("移动延时速度" + speed);
////        if (mDelayTimeFlySpeedValue!=null){
////            mDelayTimeFlySpeedValue.setText(String.valueOf(speed));
////            mDelayTimeSpeedSeekbar.setProgress(speed );
////        }
//
//
//    }

    public int getStep() {
        return mStep;
    }

    public void show() {
        if (!isShowing()) {
            mStep = START_SETTING;
            updateClues(mStep);
            mBuilder.show();
        }
    }

    public boolean isShowing() {
        return mBuilder != null && mBuilder.isShowing();
    }

    public void dismiss() {
        totalSize = 240;
        count = 1;
        if (isShowing())
            mBuilder.dismiss();
        if (mCallback != null) mCallback.onDestory(false);
    }

    public DelayTimeDialog setCallback(OnEventCallback mCallback) {
        this.mCallback = mCallback;
        return this;
    }

    @OnClick({R.id.delay_time_dg_btn
            , R.id.delay_time_left
            , R.id.delay_time_right
            , R.id.delay_time_front
            , R.id.delay_time_back
    })
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.delay_time_dg_btn:
                if (mStep != COMPLETE) {
                    if (mStep == START_SETTING) {
                        mStep = SETTING_SPEED;
                    } else if (mStep == SETTING_SPEED) {
                        if (mCallback != null) mCallback.onConfirmSpeed();
                        mStep = SETING_ORIENTATION;
                    } else if (mStep == SETING_ORIENTATION) {
                        switch (bearing) {
                            case FRONT:
                                vx = (mDelayTimeSpeedSeekbar.getProgress() + 1) * 10;
                                vy = 0;
                                break;
                            case BACK:
                                vx = -(mDelayTimeSpeedSeekbar.getProgress() + 1) * 10;
                                vy = 0;
                                break;
                            case LEFT:
                                vx = 0;
                                vy = -(mDelayTimeSpeedSeekbar.getProgress() + 1) * 10;
                                break;
                            case RIGHT:
                                vx = 0;
                                vy = (mDelayTimeSpeedSeekbar.getProgress() + 1) * 10;
                                break;
                        }
                        if (mCallback != null) {
                            mCallback.onStart(true, vx, vy);
                        }
                        mStep = INTERRUPT;
                    } else if (mStep == INTERRUPT) {
                        if (mCallback != null) mCallback.onStop(false, 30, 30);
                        dismiss();
//                        mStep = COMPLETE;
                    }
                    updateClues(mStep);
                } else {
                    dismiss();
                }
                break;
            case R.id.delay_time_left:
                switchDirection(LEFT);
                break;
            case R.id.delay_time_right:
                switchDirection(RIGHT);
                break;
            case R.id.delay_time_front:
                switchDirection(FRONT);
                break;
            case R.id.delay_time_back:
                switchDirection(BACK);
                break;
        }
    }

    public void switchDirection(int direction) {
        this.bearing = direction;
        mDelayTimeLeft.setBackground(ResourceUtils.getDrawabe(direction == LEFT ? R.drawable.round5098e4_bg : R.drawable.borderffffff_bg));
        mDelayTimeRight.setBackground(ResourceUtils.getDrawabe(direction == RIGHT ? R.drawable.round5098e4_bg : R.drawable.borderffffff_bg));
        mDelayTimeFront.setBackground(ResourceUtils.getDrawabe(direction == FRONT ? R.drawable.round5098e4_bg : R.drawable.borderffffff_bg));
        mDelayTimeBack.setBackground(ResourceUtils.getDrawabe(direction == BACK ? R.drawable.round5098e4_bg : R.drawable.borderffffff_bg));
    }

    public interface OnEventCallback {

        void onConfirmSpeed();

        void onStart(boolean start, int vx, int vy);

        void onStop(boolean start, int vx, int vy);

        void onDestory(boolean start);
    }

}
