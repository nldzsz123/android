package com.feipai.flypai.ui.view.ipm;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.feipai.flypai.base.basedialog.BaseDialog;
import com.feipai.flypai.ui.view.CenterPointSeekBar;
import com.feipai.flypai.utils.global.ConvertUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.StringUtils;
import com.zhy.autolayout.utils.AutoUtils;


/**
 * 1.地图添加当前飞机位置为环绕中心，标记地图，发送指定到飞机
 * 2.提示移动飞机，调整半径（环绕半径必须大于5m）
 * 3.调整速度
 * 5.开始飞行
 **/
public class AroundDialog {
    private BaseDialog mBuilder;
    private Context mContext;
    private OnClickCallback mCallback;
    private TextView mTitle;
    private TextView mBnt;
    private TextView mStartCenterTv;
    private TextView mStartHintTv;
    private TextView mArounHeightTv;
    private TextView mArounRadiusTv;
    private LinearLayout mAroundCenterSpeedLy;
    private CenterPointSeekBar mAroundSpeedSeekbar;
    private LinearLayout mAroundSpeedValueLy;
    private TextView mArounSpeedValue;
    private final static int SETTING = 0;
    private final static int START = 1;
    private final static int DESTORY = 2;
    private int mStep = SETTING;

    private double mRadius = 0;
    private final static int MIN_RADIUS = 0;//最小环绕半径
    private float mSpeed = 1f;

    public AroundDialog(Context context) {
        this.mContext = context;
    }

    public AroundDialog onCreate(int gravity, int x, int y) {
        mBuilder = new BaseDialog.Builder(mContext, R.style.dialog_for_ipm)
                .setContentView(R.layout.round_dg_layout)
                .setWidthAndHeight(AutoUtils.getPercentHeightSize(672), AutoUtils.getPercentHeightSize(417))
                .setParamsXAndY(gravity, x, y)
                .setNotTouchModal(true)
                .setCancelable(false)
                .create();
        initView();
        return this;
    }

    private void initView() {
        mBnt = mBuilder.findViewById(R.id.around_comfirm_bnt);
        mTitle = mBuilder.findViewById(R.id.around_dg_title);
        mStartCenterTv = mBuilder.findViewById(R.id.around_start_center_tv);
        mStartHintTv = mBuilder.findViewById(R.id.around_dg_start_hint_tv);
        mArounHeightTv = mBuilder.findViewById(R.id.around_height_tv);
        mArounRadiusTv = mBuilder.findViewById(R.id.around_radius_tv);
        mAroundCenterSpeedLy = mBuilder.findViewById(R.id.around_speed_ly);
        mAroundSpeedSeekbar = mBuilder.findViewById(R.id.around_speed_seekbar);
        mAroundSpeedValueLy = mBuilder.findViewById(R.id.around_speed_value_ly);
        mArounSpeedValue = mBuilder.findViewById(R.id.around_speed_value);
        mBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStep == SETTING) {
                    mStep = START;
                    if (mCallback != null) mCallback.onSetting();
                } else if (mStep == START) {
                    mStep = DESTORY;
                    if (mCallback != null) mCallback.onStart(mSpeed, mRadius);
                } else if (mStep == DESTORY) {
                    mStep = -1;
                    dismiss();
//                    if (mCallback != null) mCallback.onDestroy();
                }
                updateClues(mStep);
            }
        });
        mAroundSpeedSeekbar.setOnSeekBarChangeListener(new CenterPointSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onOnSeekBarValueChange(CenterPointSeekBar bar, double value) {
                mSpeed = (float) (value / 100);
                LogUtils.d("环绕步骤===" + mStep);
                if (mStep == DESTORY)
                    if (mCallback != null) mCallback.onSpeedChanged(mSpeed);
                mArounSpeedValue.setText(ConvertUtils.floatToString(Math.abs((float) (value / 100))));
            }
        });
    }

    public void updateClues(int step) {
        if (mBuilder.isShowing()) {
            if (step == SETTING) {
                mBnt.setText(ResourceUtils.getString(R.string.setting));
                mBnt.setEnabled(true);
                mBnt.setBackgroundResource(R.drawable.login_button_selcetor);
                mTitle.setText(ResourceUtils.getString(R.string.surrounded_by_arget));
                mStartCenterTv.setText(ResourceUtils.getString(R.string.move_plane_over_canter_circle));
                mStartCenterTv.setVisibility(View.VISIBLE);
                mAroundCenterSpeedLy.setVisibility(View.GONE);
                mAroundSpeedValueLy.setVisibility(View.GONE);
                mStartHintTv.setText(ResourceUtils.getString(R.string.around_make_sure_sufficient_power));
                mStartHintTv.setVisibility(View.VISIBLE);
            } else if (step == START) {
                mBnt.setText(ResourceUtils.getString(R.string.start));
                mBnt.setBackgroundResource(R.drawable.login_button_selcetor);
                mTitle.setText(ResourceUtils.getString(R.string.move_plane_over_canter_circle));
                mStartCenterTv.setVisibility(View.GONE);
                mAroundSpeedSeekbar.setProgress(100);
                mAroundCenterSpeedLy.setVisibility(View.VISIBLE);
                mAroundSpeedValueLy.setVisibility(View.VISIBLE);
                mStartHintTv.setVisibility(View.GONE);
                mArounSpeedValue.setText(String.valueOf(1.0));
                mArounHeightTv.setText(String.valueOf(0));
                mArounRadiusTv.setText(String.valueOf(0));
            } else if (step == DESTORY) {
                mBnt.setText(ResourceUtils.getString(R.string.interrupt));
                mBnt.setBackgroundResource(R.drawable.fe9700_btn_selcetor);
                mTitle.setText(ResourceUtils.getString(R.string.arounding));
            }
        }
    }

    public int getStep() {
        return mStep;
    }

    /**
     * 设置环绕半径
     */
    public void setArounRadius(double radius) {
        this.mRadius = radius;
        mArounRadiusTv.setText(String.format("%.1f", radius));
        if (mStep == DESTORY) {
            mBnt.setEnabled(true);
        } else {
            mBnt.setEnabled(radius > MIN_RADIUS);
        }

        mBnt.setBackgroundResource(mStep == DESTORY ? R.drawable.fe9700_btn_selcetor :
                (radius > MIN_RADIUS ? R.drawable.login_button_selcetor : R.drawable.unenabled_button_selcetor));
    }

    /**
     * 设置环绕高度，即未当前飞行器高度
     */
    public void setAroundHeight(String height) {
        mArounHeightTv.setText(height);
    }

    /**
     * 初始化时，设置环绕速度
     */
    public void setAroundSpeed(float speed) {
        mArounSpeedValue.setText(String.valueOf(speed));
        mAroundSpeedSeekbar.setProgress(speed);
    }


    public void show() {
        if (!isShowing()) {
            mBuilder.show();
            mStep = SETTING;
            updateClues(mStep);
        }
    }

    public boolean isShowing() {
        return mBuilder != null && mBuilder.isShowing();
    }

    public void dismiss() {
        if (mCallback != null) mCallback.onDestroy();
        if (isShowing())
            mBuilder.dismiss();
    }

    public AroundDialog setCallback(OnClickCallback mCallback) {
        this.mCallback = mCallback;
        return this;
    }

    public interface OnClickCallback {

        void onSetting();


        void onStart(float speed, double radius);

        void onSpeedChanged(float speed);

        void onDestroy();
    }

}
