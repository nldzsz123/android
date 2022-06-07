package com.feipai.flypai.ui.fragments.planesettingfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.base.BaseMvpFragment;
import com.feipai.flypai.beans.NotifyMessageMode;
import com.feipai.flypai.beans.ProductModel;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.beans.mavlinkbeans.BatteryBean;
import com.feipai.flypai.beans.mavlinkbeans.CalibrationProgressBean;
import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.ui.view.CalibrationDialog;
import com.feipai.flypai.utils.PlaneCommand;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.StringUtils;
import com.feipai.flypai.utils.global.ToastUtils;
import com.feipai.flypai.utils.global.Utils;
import com.zhy.autolayout.AutoLinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.ardupilotmega.msg_mag_cal_report.MAVLINK_MSG_ID_MAG_CAL_REPORT;
import static com.common.msg_battery_status.MAVLINK_MSG_ID_BATTERY_STATUS;
import static com.common.msg_cal_progress_decode.MAG_CAL_PROGRESS;
import static com.feipai.flypai.app.ConstantFields.ACTION_PARAM.CALI_COMPASS_PROGRESS;
import static com.feipai.flypai.app.ConstantFields.ACTION_PARAM.COMPASS_STATE;
import static com.feipai.flypai.app.ConstantFields.ACTION_PARAM.GYROSCOPE_STATE;

public class CalibrationFragment extends BaseMvpFragment<BasePresenter> implements BaseView {


    @BindView(R.id.calib_gryo)
    TextView mCalibGryo;
    @BindView(R.id.calib_compass)
    TextView mCalibCompass;
    @BindView(R.id.calib_yuntai)
    TextView mCalibYuntai;
    @BindView(R.id.yuntai_cal_ly)
    AutoLinearLayout mYuntaiCalLy;
    @BindView(R.id.yuntai_cal_ly_for_4k)
    AutoLinearLayout mYuntaiCalFor4KLy;
    @BindView(R.id.gyroscope_state)
    TextView mGyroscopeStateTv;
    @BindView(R.id.compass_state)
    TextView mCompassStateTv;
    @BindView(R.id.calib_gryo_offset)
    TextView mCalibGryoOffset;
    @BindView(R.id.calib_gryo_offset_ly)
    AutoLinearLayout mCalibGryoOffsetLy;


    private CalibrationDialog mCalibDialog;

    @Override
    protected void initInject() {

    }

    @Override
    protected boolean isUseButterKnife() {
        return true;
    }

//    @Override
//    protected boolean isUseRxBus() {
//        return true;
//    }

    @Override
    protected void initView() {
        super.initView();
        setYuntaiCalLyVisibility(false);
        setGyroOffsetLyVisibility(Utils.isDebug);
    }

    public void setYuntaiCalLyVisibility(boolean isVisibility) {
        mYuntaiCalLy.setVisibility(isVisibility ? View.VISIBLE : View.GONE);
    }

    public void setGyroOffsetLyVisibility(boolean isVisibility) {
        mCalibGryoOffsetLy.setVisibility(isVisibility ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onConnected(ProductModel productModel) {
        super.onConnected(productModel);
        setYuntaiCalLyVisibility((productModel.productType != ConstantFields.ProductType_4k && Utils.isDebug));
        mYuntaiCalFor4KLy.setVisibility(productModel.productType == ConstantFields.ProductType_4k && Utils.isDebug
                ? View.VISIBLE : View.GONE);
//        setGyroOffsetLyVisibility(Utils.isDebug);
    }

    @Override
    public void onDisConnected() {
        super.onDisConnected();
    }

//    @Override
//    protected void initRxbusListener(RxbusBean msg) {
//        super.initRxbusListener(msg);
//        if (StringUtils.equals(msg.TAG, String.valueOf(MAG_CAL_PROGRESS))) {
//            CalibrationProgressBean bean = (CalibrationProgressBean) msg.object;
//            if (mCalibDialog != null && mCalibDialog.isShowing()) {
//                mCalibDialog.updateProgress(bean.getProgress());
//            }
//        } else if (StringUtils.equals(msg.TAG, String.valueOf(MAVLINK_MSG_ID_MAG_CAL_REPORT))) {
//            if (mCalibDialog != null && mCalibDialog.isShowing()) {
//                mCalibDialog.calibRustle((Boolean) msg.object);
//            }
//        }
//    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected int initLayout() {
        return R.layout.calibration_fragment;
    }


    @OnClick({R.id.calib_gryo, R.id.calib_compass, R.id.calib_yuntai,
            R.id.cali_yt_for4k01, R.id.cali_yt_for4k02, R.id.cali_yt_for4k03, R.id.cali_yt_for4k04, R.id.cali_yt_for4k05, R.id.cali_yt_for4k06,
            R.id.calib_gryo_offset})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.calib_gryo:
                showCalibDialog(ConstantFields.ACTION_PARAM.START_CALIBRATION_GYRO);
                break;
            case R.id.calib_compass:
                showCalibDialog(ConstantFields.ACTION_PARAM.START_CALIBRATION_COMPASS);
                break;
            case R.id.calib_yuntai:
                ToastUtils.showLongToast("开始6K云台校准");
                PlaneCommand.getInstance().yunTai6KCalib();
//                showCalibDialog(ConstantFields.ACTION_PARAM.START_CALIBRATION_YUNTAI);
                break;
            case R.id.cali_yt_for4k01:
                ToastUtils.showLongToast("开始校准");
                caliYtFor4K(5);
                break;
            case R.id.cali_yt_for4k02:
                ToastUtils.showLongToast("校准R");
                caliYtFor4K(1);
                break;
            case R.id.cali_yt_for4k03:
                ToastUtils.showLongToast("校准P");
                caliYtFor4K(2);
                break;
            case R.id.cali_yt_for4k04:
                ToastUtils.showLongToast("校准Y");
                caliYtFor4K(3);
                break;
            case R.id.cali_yt_for4k05:
                ToastUtils.showLongToast("执行校准");
                caliYtFor4K(4);
                break;
            case R.id.cali_yt_for4k06:
                ToastUtils.showLongToast("完成校准");
                caliYtFor4K(6);
                break;
            case R.id.calib_gryo_offset:
                ToastUtils.showLongToast("开始陀螺仪偏移校准");
                PlaneCommand.getInstance().gyroOffsetCalib();
                break;
        }
    }

    public void caliYtFor4K(int value) {
        PlaneCommand.getInstance().yunTaiCalibration(value);
    }

    private void showCalibDialog(int action) {
        if (mCalibDialog == null) {
            mCalibDialog = new CalibrationDialog(this, new CalibrationDialog.CalibrationDialogListener() {
                @Override
                public void onStartCali(int act) {
                    if (act == ConstantFields.ACTION_PARAM.START_CALIBRATION_COMPASS) {
                        PlaneCommand.getInstance().startCompassCalib();
                        mCalibDialog.showCaliDialog(ConstantFields.ACTION_PARAM.CANCEL_CALIBRATION_COMPASS, this);
                    } else if (act == ConstantFields.ACTION_PARAM.START_CALIBRATION_GYRO) {
                        PlaneCommand.getInstance().startGyroCalib();
                        mCalibDialog.showCaliDialog(ConstantFields.ACTION_PARAM.CANCEL_CALIBRATION_GYRO, this);
                    } else if (act == ConstantFields.ACTION_PARAM.START_CALIBRATION_YUNTAI) {
                        /**6k校准精确校准*/
                        PlaneCommand.getInstance().yunTai6KCalib();
                    }
                }

                @Override
                public void onCancleCali(int act) {
                    if (act == ConstantFields.ACTION_PARAM.CANCEL_CALIBRATION_GYRO) {

                    } else if (act == ConstantFields.ACTION_PARAM.CANCEL_CALIBRATION_COMPASS) {

                    }
                }

                @Override
                public void onCompleteConfirm(int act) {

                }
            });
        }
        mCalibDialog.startShowAcDialog(action);
    }

    @Override
    protected boolean isRegisterNotifyReceiver() {
        return true;
    }

    @Override
    protected void subscribeNotify(NotifyMessageMode msg) {
        super.subscribeNotify(msg);
        if (msg == null || mGyroscopeStateTv == null)
            return;
        if (msg.receiver == COMPASS_STATE && msg.object != null && msg.object instanceof Boolean) {
            mCompassStateTv.setText(ResourceUtils.getString((Boolean) msg.object ? R.string.abnormal : R.string.normal));
            mCompassStateTv.setTextColor(ResourceUtils.getColor((Boolean) msg.object ? R.color.color_f34235 : R.color.color_00cf00));
        } else if (msg.receiver == GYROSCOPE_STATE && msg.object != null && msg.object instanceof Boolean) {
            mGyroscopeStateTv.setText(ResourceUtils.getString((Boolean) msg.object ? R.string.abnormal : R.string.normal));
            mGyroscopeStateTv.setTextColor(ResourceUtils.getColor((Boolean) msg.object ? R.color.color_f34235 : R.color.color_00cf00));
        }
        if (msg.receiver == CALI_COMPASS_PROGRESS && msg.object != null && msg.object instanceof CalibrationProgressBean) {
            LogUtils.d("接受指南针校准进度");
            CalibrationProgressBean bean = (CalibrationProgressBean) msg.object;
            if (mCalibDialog != null && mCalibDialog.isShowing()) {
                mCalibDialog.updateProgress(bean.getProgress());
            }
        } else if (msg.receiver == CALI_COMPASS_PROGRESS && msg.object != null && msg.object instanceof Boolean) {
            if (mCalibDialog != null && mCalibDialog.isShowing()) {
                mCalibDialog.calibRustle((Boolean) msg.object);
            }
        }
    }

}

