package com.feipai.flypai.ui.view.Camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipai.flypai.BuildConfig;
import com.feipai.flypai.R;
import com.feipai.flypai.connect.ConnectManager;
import com.feipai.flypai.utils.MLog;
import com.feipai.flypai.utils.PlaneCommand;
import com.feipai.flypai.utils.global.IAnimationUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.Utils;
import com.videoplayer.NativeCode;
import com.zhy.autolayout.AutoRelativeLayout;
import com.zhy.autolayout.utils.AutoUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TopbarInfoView extends AutoRelativeLayout {
    private Context mContext;

    // 返回按钮
    @BindView(R.id.ab_back_ly)
    public ImageButton turnbackImageBtn;

    // 分辨率 标题
    @BindView(R.id.ab_control_resolution_title)
    public TextView res_title_textView;

    @BindView(R.id.ab_control_resolution_value)
    public TextView res_value_textView;

    // iso
    @BindView(R.id.ab_iso_value)
    public TextView iso_value_textView;

    // 快门
    @BindView(R.id.ab_shutter_title)
    public TextView shut_title_textView;

    @BindView(R.id.ab_shutter_value)
    public TextView shut_value_textView;

    @BindView(R.id.ab_gps_img)
    ImageView gps_imageView;

    @BindView(R.id.ab_wifi_img)
    ImageView wifi_imageView;
    @BindView(R.id.ab_wifi_tv)
    TextView wifi_value_textView;

    // GPS信息
    @BindView(R.id.ab_gps_tv)
    TextView gps_value_textView;

    // 电池信息
    @BindView(R.id.ab_battery_img)
    ImageView battry_imageView;

    @BindView(R.id.ab_contr_battery_value)
    TextView battry_value_textView;

    @BindView(R.id.gps_hint_ly)
    LinearLayout gpsStatusLy;

    @BindView(R.id.ab_gps_hint_but)
    TextView status_textView;

    // 飞控设置按钮
    @BindView(R.id.ab_cotr_more_ly)
    ImageButton planset_imagebtton;

    private int mFlyStatus = 0;
    private boolean isWideAngle;
    private TopStatusCallback  callback;

    public TopbarInfoView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public TopbarInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.camera_top_bar_view, this, true);
        AutoUtils.auto(view);
        ButterKnife.bind(this, view);
        if (Utils.isDebug) {
            status_textView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mFlyStatus == 25) {
                        PlaneCommand.getInstance().activatePlaneAck();
                    } else {
                        PlaneCommand.getInstance().resetPlaneAck();
                    }
                    return false;
                }
            });
        }
    }

    public void setTopStatusListener(TopStatusCallback callback){
        this.callback=callback;
    }


    /**
     * 更新电池状态
     */
    public void updateBatteryImg(int load, int remainTime) {
        int batteryImg = R.mipmap.camera_top_charge_0;
        int batteryColor = R.color.color_b8b8b8;
        if (ConnectManager.getInstance().isConneted()) {
            if (load <= 15) {
                batteryImg = R.mipmap.camera_top_charge_1;
                batteryColor = R.color.color_f34235;
            } else if (load > 15 && load < 30) {
                batteryImg = R.mipmap.camera_top_charge_2;
                batteryColor = R.color.color_fe9700;
            } else {
                batteryImg = R.mipmap.camera_top_charge_03;
                batteryColor = R.color.color_00cf00;
            }
//            LogUtils.d("剩余飞行时间====>" + remainTime);
            if (remainTime > 0 && remainTime <= 5) {
                batteryImg = R.mipmap.camera_top_charge_1;
                batteryColor = R.color.color_f34235;
                battry_value_textView.setText(String.valueOf(remainTime) + ResourceUtils.getString(R.string.minutes));
            } else {
                battry_value_textView.setText(String.valueOf(load) + "%");
            }
        } else {
            battry_value_textView.setText(String.valueOf(0) + "%");
        }
        battry_value_textView.setTextColor(ResourceUtils.getColor(batteryColor));
        battry_imageView.setImageResource(batteryImg);
    }

    /**
     * 更新GPS状态
     */
    public void updateGpsImg(int satellites) {
        int gpsImg = R.mipmap.camera_top_gps_0;
        int gpsValueColor = R.color.color_b8b8b8;
        if (ConnectManager.getInstance().isConneted()) {
            if (satellites == 0) {
                gpsImg = R.mipmap.camera_top_gps_0;
                gpsValueColor = R.color.color_b8b8b8;
            } else if (satellites > 0 && satellites <= 5) {
                gpsImg = R.mipmap.camera_top_gps_1;
                gpsValueColor = R.color.color_fe9700;
            } else if (satellites == 6) {
                gpsImg = R.mipmap.camera_top_gps_2;
                gpsValueColor = R.color.color_fe9700;
            } else if (satellites == 7) {
                gpsImg = R.mipmap.camera_top_gps_3;
                gpsValueColor = R.color.color_fe9700;
            } else if (satellites == 8 || satellites == 9) {
                gpsImg = R.mipmap.camera_top_gps_4;
                gpsValueColor = R.color.color_00cf00;
            } else {
                gpsImg = R.mipmap.camera_top_gps_5;
                gpsValueColor = R.color.color_00cf00;
            }
            gps_value_textView.setText(String.valueOf(satellites));
        } else {
            gps_value_textView.setText(String.valueOf(0));
        }
        gps_imageView.setImageResource(gpsImg);
        gps_value_textView.setTextColor(ResourceUtils.getColor(gpsValueColor));

    }

    /**
     * 更新WIFI
     */
    public void updateWifiLevel(int level,int[] types) {
        int state = R.mipmap.wifi05;
        if (ConnectManager.getInstance().isConneted()) {
            if (level > 0 && level < types[0]) {
                // 弱
                state = R.mipmap.wifi04;
            } else if (level >= types[0] && level < types[1]) {
//                // 好
                state = R.mipmap.wifi03;
            } else if (level >= types[1] && level < types[2]) {
//                // 很好
                state = R.mipmap.wifi02;
            } else if (level >= types[2] && level <= types[3]) {
                // 非常好
                state = R.mipmap.wifi01;
            } else if (level > types[3]) {
                // 非常好
                state = R.mipmap.wifi00;
            }
//            if (level <= 30) {
//                // 非常好
//                state = R.mipmap.wifi00;
//            } else if (level > 30 && level <= 40) {
//                // 很好
//                state = R.mipmap.wifi01;
//            } else if (level > 40 && level <= 50) {
//                // 好
//                state = R.mipmap.wifi02;
//            } else if (level > 50 && level <= 60) {
//                // 弱
//                state = R.mipmap.wifi03;
//            } else {
//                state = R.mipmap.wifi04;
//            }
        } else {
            state = R.mipmap.wifi05;
        }
//        if (level >= 100) {
//            level = 100;
//        }
        if (wifi_imageView != null)
            wifi_imageView.setImageResource(state);
//        if (wifi_value_textView != null)
//            wifi_value_textView.setText(String.valueOf(level));
    }

    /**
     * 更新飞行状态
     */
    public void updateFlyStatus(boolean isRcConncented, int flyStatus, int customMode) {
        this.mFlyStatus = flyStatus;
        String hintText = getResources().getString(R.string.plane_not_connected);
        int topStatusBg = R.mipmap.camera_top_drone_status_0;
        if (!ConnectManager.getInstance().isConneted()) {

        } else {
            if (!isRcConncented) {
                topStatusBg = R.mipmap.camera_top_drone_status_1;
                hintText = getResources().getString(R.string.remote_control_not_connected);
                if (callback!=null)callback.stopSmart(true);
            } else {
                if (customMode == 6) {
                    topStatusBg = R.mipmap.camera_top_drone_status_1;
                    hintText = getResources().getString(R.string.being_shadowed);
                    if (callback!=null)callback.stopSmart(true);
//                    hintFuncationDialog();
//                    if (commandAction == PlaneCommand.TYPE_DELAY_TIME_MODE) {
//                        stopDelayTimeShooting();
//                    }
//                    if (!isReturnning)
//                        swichTurnBackImg(true, false);
                } else {
//                    if (isReturnning) {
//                        swichTurnBackImg(false, false);
//                    }

                    switch (flyStatus) {
                        case 100:
                            /**可解锁,区分姿态或定位状态*/
//                        lockImg.setImageResource(R.mipmap.lock_img);
                            switch (customMode) {
                                case 2:
                                    topStatusBg = R.mipmap.camera_top_drone_status_1;
                                    hintText = getResources().getString(R.string.cautious_flight_for_posture);
//                                    hintFuncationDialog();
                                    if (callback!=null)callback.stopSmart(true);
                                    break;
                                case 3:
                                    /**航点*/
                                    topStatusBg = R.mipmap.camera_top_drone_status_1;
                                    hintText = getResources().getString(R.string.waypoint_flying);
                                    break;
                                case 4:
                                    /**全景模式*/
                                    topStatusBg = R.mipmap.camera_top_drone_status_3;
                                    if (isWideAngle) {
                                        hintText = getResources().getString(R.string.wide_photo_mode);
                                    } else {
                                        hintText = getResources().getString(R.string.panoramic_mode);
                                    }
                                    break;
                                case 5:
                                    topStatusBg = R.mipmap.camera_top_drone_status_3;
                                    hintText = getResources().getString(R.string.safe_flight_for_gps);
                                    if (callback!=null)callback.stopSmart(false);
                                    break;
                                case 7:
                                    topStatusBg = R.mipmap.camera_top_drone_status_3;
                                    hintText = getResources().getString(R.string.around_flight);
                                    break;
                                case 9:
                                    topStatusBg = R.mipmap.camera_top_drone_status_1;
                                    hintText = getResources().getString(R.string.auto_falling);
                                    if (callback!=null)callback.stopSmart(true);
                                    break;
                                case 23:
                                    topStatusBg = R.mipmap.camera_top_drone_status_3;
                                    hintText = getResources().getString(R.string.following);
                                    break;
                                case 24:
                                    topStatusBg = R.mipmap.camera_top_drone_status_3;
                                    hintText = getResources().getString(R.string.delay_model);
                                    break;
                                case 25:
                                    topStatusBg = R.mipmap.camera_top_drone_status_3;
                                    hintText = getResources().getString(R.string.control_model);
                                    break;
                            }
                            break;
                        default:
                            if (flyStatus <= 21 && flyStatus >= 0)//排除降落模式落地瞬间，还未切换到非降落模式时解锁所产生的模式异常(出现过-39)
                                hintText = ResourceUtils.getStringArray(R.array.UNLOCK)[flyStatus];
                            else if (flyStatus == 22) {
                                //未激活解锁
                                hintText = getResources().getString(R.string.compass_not_calibrated);
                            } else if (flyStatus == 23) {
                                //新手模式开关打开（姿态模式下解锁会返回）
                                hintText = getResources().getString(R.string.beginner_mode);
                            } else if (flyStatus == 24) {
                                hintText = getResources().getString(R.string.return_mode);
                                if (callback!=null)callback.stopSmart(true);
//                                showPanorDialog(ABFlyingFactory.PLANE_RETURNED);
                            } else if (flyStatus == 25) {
                                //未激活解锁
                                hintText = getResources().getString(R.string.plane_not_activated);
                                if (!BuildConfig.DEBUG) {
//                                    showPanorDialog(ABFlyingFactory.NOTICE_ACK);
                                }
                            }
                            if (flyStatus == 0 || flyStatus == 3 || flyStatus == 4 || flyStatus == 6 || flyStatus == 7 || flyStatus == 15 || flyStatus == 18 || flyStatus == 21 || flyStatus == 23 || flyStatus == 24 || flyStatus == 25) {
                                topStatusBg = R.mipmap.camera_top_drone_status_1;
                            } else {
                                topStatusBg = R.mipmap.camera_top_drone_status_2;
                            }
                            break;
                    }
                }
            }

        }
        status_textView.setText(hintText);
        gpsStatusLy.setBackgroundResource(topStatusBg);

    }

    public void loadAnimatation(boolean reset) {
        planset_imagebtton.setImageResource(reset ? R.mipmap.ab_cotr_more_ly : R.mipmap.plane_setting_close_img);
        IAnimationUtils.performAnim(mContext, planset_imagebtton, R.anim.view_up_scale_anim);
    }

    public void setWide(boolean isWide) {
        isWideAngle = isWide;
    }

    public boolean isCompassAnomaly() {
        return
                status_textView.getText().toString().equals(ResourceUtils.getStringArray(R.array.UNLOCK)[6]) ||
                        status_textView.getText().toString().equals(ResourceUtils.getStringArray(R.array.UNLOCK)[8]) ||
                        status_textView.getText().toString().equals(ResourceUtils.getStringArray(R.array.UNLOCK)[14]);
    }

    public boolean isGyroscopeAnomaly() {
        return status_textView.getText().toString().equals(ResourceUtils.getStringArray(R.array.UNLOCK)[5]) ||
                status_textView.getText().toString().equals(ResourceUtils.getStringArray(R.array.UNLOCK)[12]);
    }


    public interface  TopStatusCallback{
        void stopSmart(boolean isStop);
    }
}
