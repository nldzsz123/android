package com.feipai.flypai.ui.view.Camera;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.feipai.flypai.beans.mavlinkbeans.BatteryBean;
import com.feipai.flypai.ui.view.FPButton;
import com.feipai.flypai.utils.MLog;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.StringUtils;
import com.zhy.autolayout.AutoLinearLayout;
import com.zhy.autolayout.AutoRelativeLayout;
import com.zhy.autolayout.utils.AutoUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BottomInfoView extends AutoRelativeLayout implements View.OnClickListener {
    private Context mContext;
    @BindView(R.id.camera_up_sp_tv)
    TextView mUpDownSpeed;
    @BindView(R.id.camera_up_ho_tv)
    TextView mHSpeed;
    @BindView(R.id.camera_up_high_tv)
    TextView mHeight;
    @BindView(R.id.camera_up_distance_tv)
    TextView mDistance;
    @BindView(R.id.camera_ae_btn)
    FPButton aeButton;
    @BindView(R.id.camera_sd_tv)
    public TextView sdCardTV;
    @BindView(R.id.camera_drone_head_tv)
    public TextView droneHeadTv;

    @BindView(R.id.yuntai_left_btn)
    FPButton yt_left_btn;
    @BindView(R.id.yuntai_right_btn)
    FPButton yt_right_btn;
    @BindView(R.id.ab_yuntai_cal)
    public RelativeLayout ytLY;


    public BottomInfoView(Context context) {
        super(context);
        initView(context);
    }

    public BottomInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.camera_bottominfoview, this, true);
        ButterKnife.bind(this, view);
        AutoUtils.auto(view);
        yt_left_btn.setOnClickListener(this);
        yt_right_btn.setOnClickListener(this);
        yt_left_btn.setOnTouchListener(yuntaiHoriCalListioner);
        yt_right_btn.setOnTouchListener(yuntaiHoriCalListioner);
    }

    /**
     * 更新状态数据从电池包
     */
    public void upDatePlaneInfo(BatteryBean bean) {
        if (bean != null) {
            if (!StringUtils.isEmpty(bean.getFlyAlt())) {
                mHeight.setText(bean.getFlyAlt());
            }
            if (!StringUtils.isEmpty(bean.getFlyDistance())) {
                mDistance.setText(bean.getFlyDistance());
            }
            if (!StringUtils.isEmpty(bean.getHorizontalVelocity())) {
                mHSpeed.setText(bean.getHorizontalVelocity());
            }

        }
    }

    public void upDateLiftSpeed(String liftSpeed) {
        mUpDownSpeed.setText(liftSpeed);
    }

    public void setAEButtonLock(boolean lock, boolean isShow) {
        if (isShow) {
            aeButton.setVisibility(VISIBLE);
        } else {
            aeButton.setVisibility(GONE);
            return;
        }
        if (lock) {
            aeButton.setBackGroundDrawable(mContext.getResources().getDrawable(R.mipmap.camera_bottom_lock));
        } else {
            aeButton.setBackGroundDrawable(mContext.getResources().getDrawable(R.mipmap.camera_bottom_unlock));
        }
    }

    public void setAEButtonEnabled(boolean isEnabled) {
        aeButton.setEnabled(isEnabled);
    }

    public void hideSDCardLabel() {
        sdCardTV.setVisibility(INVISIBLE);
    }

    public void showSDCardLabelWithDes(String string) {
        sdCardTV.setVisibility(VISIBLE);
        sdCardTV.setText(string);
    }

    /**
     * 机头方向调换
     */
    public void showDroneHeadTv(boolean isShow) {
        droneHeadTv.setVisibility(isShow ? VISIBLE : INVISIBLE);
    }

    public boolean isDroneHeadShow() {
        return droneHeadTv.getVisibility() == VISIBLE;
    }

    @Override
    public void onClick(View v) {

    }


    public interface YTHoriSetLisitioner {
        void sendCMDValue(int val);
    }

    private YTHoriSetLisitioner mLisioner;

    public void setmLisioner(YTHoriSetLisitioner lisioner) {
        mLisioner = lisioner;
    }

    private Runnable yuntaiTimer;
    private Handler mHandler = new Handler();

    private void startYuntaiTimer(int val) {
        if (yuntaiTimer != null) {
            mHandler.removeCallbacks(yuntaiTimer);
            yuntaiTimer = null;
        }

        yuntaiTimer = new Runnable() {
            @Override
            public void run() {
                if (yuntaiTimer == null) {
                    return;
                }
                if (mLisioner != null) {
                    mLisioner.sendCMDValue(val);
                }
                mHandler.postDelayed(yuntaiTimer, 100);
            }
        };
        mHandler.postDelayed(yuntaiTimer, 0);
    }

    private View.OnTouchListener yuntaiHoriCalListioner = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                MLog.log("ACTION_DOWN");
                if (v.getId() == R.id.yuntai_left_btn) {
                    startYuntaiTimer(10);
                } else {
                    startYuntaiTimer(-10);
                }
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                MLog.log("ACTION_UP");
                if (yuntaiTimer != null) {
                    mHandler.removeCallbacks(yuntaiTimer);
                    yuntaiTimer = null;
                }
            }
            return true;
        }
    };
}
