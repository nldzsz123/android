package com.feipai.flypai.ui.view.Camera;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.feipai.flypai.ui.view.FPButton;
import com.feipai.flypai.utils.global.ViewUtils;
import com.zhy.autolayout.AutoRelativeLayout;
import com.zhy.autolayout.utils.AutoUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OperateView extends RelativeLayout {

    private Context mContext;

    @BindView(R.id.camera_mode_exchange_btn)
    public FPButton modeExchangeButton;
    @BindView(R.id.function_btn)
    public FPButton functionButton;
    @BindView(R.id.taking_photo_progressbar)
    public ProgressBar taking_photo_bar;
    @BindView(R.id.record_time_tv)
    public TextView timeLabel;
    @BindView(R.id.camera_set_btn)
    public FPButton setButton;

    public OperateView(Context context) {
        super(context);
        initView(context);
    }

    public OperateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.camera_operateview, this, true);
        AutoUtils.auto(view);
        ButterKnife.bind(this);
        ViewUtils.setLayoutParams(modeExchangeButton, 105);
        ViewUtils.setLayoutParams(functionButton, 180);
    }

    public void setIsSelected(boolean selected) {
        if (selected) {
            setBackground(mContext.getResources().getDrawable(R.drawable.camera_set_right_round_sel));
        } else {
            setBackground(mContext.getResources().getDrawable(R.drawable.camera_set_right_round));
        }
    }

    public void startTakePhotoAnimation() {
        Log.d("延时拍摄", "转圈圈");
        taking_photo_bar.setVisibility(VISIBLE);
        functionButton.setAlpha(0f);
//        functionButton.setVisibility(GONE);
    }

    public void stopTakePhotoAnimation() {
        Log.d("延时拍摄", "停止转圈圈");
        taking_photo_bar.setVisibility(GONE);
        functionButton.setAlpha(1f);
//        functionButton.setVisibility(VISIBLE);
    }

    public void delayTimerChange(int time) {
        functionButton.setText(time > 0 ? String.valueOf(time) : "");
    }

    public void setViewStatus(@CameraSetConstaint.CameraStatus int status) {
        if (status == CameraSetConstaint.CameraStatusDisconnect) {
            modeExchangeButton.setEnabled(false);
            functionButton.setEnabled(false);
            setButton.setEnabled(true);
            modeExchangeButton.setAlpha(0.5f);
            functionButton.setAlpha(0.5f);
            setButton.setAlpha(1.0f);
        } else if (status == CameraSetConstaint.CameraStatusSDCardNotInsert || status == CameraSetConstaint.CameraStatusSDCardFull) {
            modeExchangeButton.setEnabled(true);
            functionButton.setEnabled(false);
            setButton.setEnabled(true);
            modeExchangeButton.setAlpha(1.0f);
            functionButton.setAlpha(0.5f);
            setButton.setAlpha(1.0f);
        } else if (status == CameraSetConstaint.CameraStatusDisable) {
            modeExchangeButton.setEnabled(false);
            functionButton.setEnabled(false);
            setButton.setEnabled(false);
            modeExchangeButton.setAlpha(0.5f);
            functionButton.setAlpha(0.5f);
            setButton.setAlpha(0.5f);
        } else if (status == CameraSetConstaint.CameraStatusPhotoContinue) {
            setCameraStatus(CameraSetConstaint.CameraStatusPhotoModeNormal);
            modeExchangeButton.setEnabled(false);
            functionButton.setEnabled(true);
            setButton.setEnabled(false);
            modeExchangeButton.setAlpha(0.5f);
            functionButton.setAlpha(1.0f);
            setButton.setAlpha(0.5f);
        } else if (status == CameraSetConstaint.CameraStatusPhotoModeNormal) {
            setCameraStatus(CameraSetConstaint.CameraStatusPhotoModeNormal);
        } else if (status == CameraSetConstaint.CameraStatusMovieModeNormal) {
            setCameraStatus(CameraSetConstaint.CameraStatusMovieModeNormal);
        } else if (status == CameraSetConstaint.CameraStatusRecord) {
            setCameraStatus(CameraSetConstaint.CameraStatusRecord);
        } else if (status == CameraSetConstaint.CameraStatusInPano || status == CameraSetConstaint.CameraStatusInYanshi) {
            setCameraStatus(CameraSetConstaint.CameraStatusPhotoModeNormal);
            modeExchangeButton.setAlpha(0.5f);
            functionButton.setAlpha(0.5f);
            setButton.setAlpha(0.5f);
            modeExchangeButton.setEnabled(false);
            functionButton.setEnabled(false);
            setButton.setEnabled(false);
        }
    }

    private void setCameraStatus(@CameraSetConstaint.CameraStatus int cameraStatus) {
        if (cameraStatus == CameraSetConstaint.CameraStatusMovieModeNormal) {
            modeExchangeButton.setEnabled(true);
            modeExchangeButton.setBackground(mContext.getResources().getDrawable(R.mipmap.camera_mode_exchange_to_photo));
            functionButton.setEnabled(true);
            functionButton.setBackground(mContext.getResources().getDrawable(R.mipmap.camera_video_take_normal));
            setButton.setEnabled(true);
            modeExchangeButton.setAlpha(1.0f);
            functionButton.setAlpha(1.0f);
            setButton.setAlpha(1.0f);
        } else if (cameraStatus == CameraSetConstaint.CameraStatusPhotoModeNormal) {
            modeExchangeButton.setEnabled(true);
            modeExchangeButton.setBackground(mContext.getResources().getDrawable(R.mipmap.camera_mode_exchange_to_video));
            functionButton.setEnabled(true);
            setButton.setEnabled(true);
            functionButton.setBackground(mContext.getResources().getDrawable(R.mipmap.camera_photo_take));
            setButton.setEnabled(true);
            modeExchangeButton.setAlpha(1.0f);
            functionButton.setAlpha(1.0f);
            setButton.setAlpha(1.0f);
        } else if (cameraStatus == CameraSetConstaint.CameraStatusRecord) {
            modeExchangeButton.setBackground(mContext.getResources().getDrawable(R.mipmap.camera_mode_exchange_to_photo));
            modeExchangeButton.setEnabled(false);

            functionButton.setEnabled(true);
            functionButton.setBackground(mContext.getResources().getDrawable(R.mipmap.camera_video_take));

            setButton.setEnabled(false);
            modeExchangeButton.setAlpha(0.5f);
            functionButton.setAlpha(1.0f);
            setButton.setAlpha(0.5f);
        }
    }


    /**
     * 点击位置非智能功能菜单，自动隐藏智能功能菜单
     */
    public boolean outOfTouchSetBnt(MotionEvent event) {
        return !ViewUtils.inRangeOfView(setButton, event);
    }


}
