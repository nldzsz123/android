package com.feipai.flypai.ui.view.ipm;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.enums.MAV_DATA_STREAM;
import com.feipai.flypai.R;
import com.feipai.flypai.base.basedialog.BaseDialog;
import com.feipai.flypai.ui.view.CenterPointSeekBar;
import com.feipai.flypai.utils.PlaneCommand;
import com.feipai.flypai.utils.global.ConvertUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.ToastUtils;
import com.zhy.autolayout.AutoLinearLayout;
import com.zhy.autolayout.AutoRelativeLayout;
import com.zhy.autolayout.utils.AutoUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 1.添加航点，一次性写入所有航点到飞机（是否要循环写入，考虑丢包问题；航点个数不得超过10个；总里程不得超过2km）
 * 2.调整速度
 * 3.开始飞行
 */
public class WayPointDialog {
    @BindView(R.id.waypoint_dg_title)
    TextView mWaypointDgTitle;
    @BindView(R.id.waypoint_center_hint_tv)
    TextView mWaypointCenterHintTv;
    @BindView(R.id.waypoint_points_value_tv)
    TextView mWaypointPointsValueTv;
    @BindView(R.id.wappoint_total_distance_value_tv)
    TextView mWappointTotalDistanceValueTv;
    @BindView(R.id.wappoint_seekbar)
    SeekBar mWappointSeekbar;
    @BindView(R.id.waypoint_bottom_hint_tv)
    TextView mWaypointBottomHintTv;
    @BindView(R.id.waypoint_speed_value)
    TextView mWaypointSpeedValue;
    @BindView(R.id.waypoint_speed_value_ly)
    AutoLinearLayout mWaypointSpeedValueLy;
    @BindView(R.id.waypoint_comfirm_bnt)
    TextView mWaypointComfirmBnt;
    private BaseDialog mBuilder;
    private Context mContext;
    private OnEventCallback mCallback;

    public final static int SET_POINTS = 0;
    private final static int SET_SPEED_AND_START = 1;
    private final static int PAUSE = 2;
    private final static int RESUME = 3;
    private int mStep = SET_POINTS;
    private int wayPointSpeed;

    public WayPointDialog(Context context) {
        this.mContext = context;
    }

    public WayPointDialog onCreate(int gravity, int x, int y) {
        mBuilder = new BaseDialog.Builder(mContext, R.style.dialog_for_ipm)
                .setContentView(R.layout.waypoint_dg_layout)
                .setWidthAndHeight(AutoUtils.getPercentWidthSize(672), AutoUtils.getPercentHeightSize(417))
                .setParamsXAndY(gravity, x, y)
                .setNotTouchModal(true)
                .setCancelable(false)
                .create();
        ButterKnife.bind(this, mBuilder);
        mWappointSeekbar.setMax(10);
        mWappointSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mWaypointSpeedValue.setText(String.valueOf(((float) progress * 50f + 500) / 100f));
                    if (mCallback != null && mStep == PAUSE)
                        mCallback.startedChangeSpeed(Float.valueOf(mWaypointSpeedValue.getText().toString().trim()));
                }
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

    public void updateClues(int step) {
        if (mBuilder.isShowing()) {
            if (step == SET_POINTS) {
                mWaypointComfirmBnt.setText(ResourceUtils.getString(R.string.setting));
                mWaypointComfirmBnt.setBackgroundResource(R.drawable.login_button_selcetor);
                mWaypointDgTitle.setText(ResourceUtils.getString(R.string.smart_waypoint_fly));
                mWaypointCenterHintTv.setText(ResourceUtils.getString(R.string.single_click_map_set_point));
                mWaypointCenterHintTv.setVisibility(View.VISIBLE);
                updateTotalPoints(0, String.valueOf(0));
                mWappointSeekbar.setVisibility(View.GONE);
                mWaypointSpeedValueLy.setVisibility(View.GONE);
                mWaypointBottomHintTv.setText(ResourceUtils.getString(R.string.waypoint_make_sure_sufficient_power));
                mWaypointBottomHintTv.setVisibility(View.VISIBLE);
            } else if (step == SET_SPEED_AND_START) {
                mWaypointComfirmBnt.setText(ResourceUtils.getString(R.string.start));
                mWaypointComfirmBnt.setBackgroundResource(R.drawable.login_button_selcetor);
                mWaypointDgTitle.setText(ResourceUtils.getString(R.string.set_waypoint_speed));
                mWaypointCenterHintTv.setVisibility(View.GONE);
                mWaypointSpeedValue.setText(ConvertUtils.floatToString(((float) (wayPointSpeed / 100))));
                mWappointSeekbar.setProgress(wayPointSpeed / 100);
                mWappointSeekbar.setVisibility(View.VISIBLE);
                mWaypointSpeedValueLy.setVisibility(View.VISIBLE);
                mWaypointBottomHintTv.setVisibility(View.GONE);
            } else if (step == PAUSE) {
                /**速度仍然可以调节*/
                mWaypointComfirmBnt.setText(ResourceUtils.getString(R.string.pause));
                mWaypointComfirmBnt.setBackgroundResource(R.drawable.fe9700_btn_selcetor);
                mWaypointDgTitle.setText(ResourceUtils.getString(R.string.waypoint_flying));
            } else if (step == RESUME) {
                mWaypointComfirmBnt.setText(ResourceUtils.getString(R.string.continues));
                mWaypointComfirmBnt.setBackgroundResource(R.drawable.fe9700_btn_selcetor);
                mWaypointDgTitle.setText(ResourceUtils.getString(R.string.waypoint_flying_pasued));
            }
        }
    }

    public int getStep() {
        return mStep;
    }

    public boolean isWaypointSet(){
        return mStep==SET_POINTS;
    }

    /**
     * 更新总里程和总航点总个数
     */
    public void updateTotalPoints(int pointsSize, String totalDistance) {
        mWaypointPointsValueTv.setText(String.valueOf(pointsSize));
        mWappointTotalDistanceValueTv.setText(totalDistance);
    }

    public void show() {
        if (wayPointSpeed != 500) {
            wayPointSpeed = 500;
        }
        PlaneCommand.getInstance().setMavlinkParam(MAV_DATA_STREAM.WPNAV_SPEED, wayPointSpeed);
        if (!isShowing()) {
            mBuilder.show();
            mStep = SET_POINTS;
            updateClues(mStep);
        }
    }

    public boolean isShowing() {
        return mBuilder != null && mBuilder.isShowing();
    }


    public void dismiss() {
        if (isShowing())
            mBuilder.dismiss();
//        if (mStep == PAUSE) {//已经在执行飞行，消失对话框则清除航点
        if (mCallback != null) mCallback.onDestoryFlying();
//        }

    }

    public WayPointDialog setCallback(OnEventCallback mCallback) {
        this.mCallback = mCallback;
        return this;
    }

    @OnClick(R.id.waypoint_comfirm_bnt)
    public void onViewClicked() {
        if (mStep == SET_POINTS) {
            //航点已设置完，需要判定航点数是否满足要求
            int totalWaypoints = Integer.parseInt(mWaypointPointsValueTv.getText().toString().trim());
            if (totalWaypoints > 1) {
                mStep = SET_SPEED_AND_START;
                if (mCallback != null)
                    mCallback.confirmPoints(totalWaypoints);
            } else {
                ToastUtils.showLongToast(ResourceUtils.getString(R.string.at_least_two_waypoints));
            }

        } else if (mStep == SET_SPEED_AND_START) {
            //设置完航点飞行速度，直接开始飞行
            mStep = PAUSE;
            if (mCallback != null)
                mCallback.startWaypoints(Float.valueOf(mWaypointSpeedValue.getText().toString().trim()),
                        Integer.parseInt(mWaypointPointsValueTv.getText().toString().trim()));
        } else if (mStep == PAUSE) {
            //结束航点飞行
            mStep = SET_POINTS;
            if (mCallback != null) mCallback.onPause();
        }
        updateClues(mStep);
    }

    public void setWaypointSpeed(int wayPointSpeed) {
        this.wayPointSpeed = wayPointSpeed;
        LogUtils.d("航点速度" + wayPointSpeed);
    }

    public interface OnEventCallback {
        /**
         * 航点已确认
         */
        void confirmPoints(int total);

        /**
         * 开始航点飞行
         *
         * @param speed 速度
         * @param total 航点总数
         */
        void startWaypoints(float speed, int total);

        /***已开始后，改变航点速度*/
        void startedChangeSpeed(float speed);

        void onPause();

        /**
         * 暂停航点
         */
        void onDestoryFlying();


    }

}
