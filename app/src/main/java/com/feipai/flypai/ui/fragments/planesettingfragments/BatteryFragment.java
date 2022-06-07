package com.feipai.flypai.ui.fragments.planesettingfragments;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.feipai.flypai.base.BaseMvpFragment;
import com.feipai.flypai.beans.NotifyMessageMode;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.beans.mavlinkbeans.BatteryBean;
import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.utils.global.ConvertUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.StringUtils;
import com.feipai.flypai.utils.global.ViewUtils;

import butterknife.BindView;

import static com.common.msg_battery_status.MAVLINK_MSG_ID_BATTERY_STATUS;

public class BatteryFragment extends BaseMvpFragment<BasePresenter> implements BaseView {

    @BindView(R.id.voltage_value)
    TextView mVoltageValue;
    @BindView(R.id.temperature_value)
    TextView mTemperatureValue;
    @BindView(R.id.battery_capacity_value)
    TextView mCapacityValue;
    @BindView(R.id.electricity_value)
    TextView mElectricityValue;
    @BindView(R.id.cycles_value)
    TextView mCyclesValue;
    @BindView(R.id.first_voltage_ly)
    RelativeLayout batteryLy1;
    @BindView(R.id.first_voltage_img)
    ImageView batteryImg1;
    @BindView(R.id.first_voltage_value)
    TextView mFirstVoltageValue;
    @BindView(R.id.second_voltage_ly)
    RelativeLayout batteryLy2;
    @BindView(R.id.second_voltage_img)
    ImageView batteryImg2;
    @BindView(R.id.second_voltage_value)
    TextView mSecondVoltageValue;
    @BindView(R.id.third_voltage_ly)
    RelativeLayout batteryLy3;
    @BindView(R.id.third_voltage_img)
    ImageView batteryImg3;
    @BindView(R.id.third_voltage_value)
    TextView mThirdVoltageValue;
    @BindView(R.id.fourth_voltage_ly)
    RelativeLayout batteryLy4;
    @BindView(R.id.fourth_voltage_img)
    ImageView batteryImg4;
    @BindView(R.id.fourth_voltage_value)
    TextView mFourthVoltageValue;

    private int maxHeight;


    @Override
    protected void initInject() {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected int initLayout() {
        return R.layout.battery_fragment;
    }

    @Override
    protected void initView() {
        super.initView();
        maxHeight = batteryLy1.getHeight() - batteryLy1.getPaddingTop() - batteryLy1.getPaddingBottom();
        LogUtils.d("最大高度====>" + maxHeight);
    }

//    @Override
//    protected boolean isUseRxBus() {
//        return true;
//    }

    @Override
    protected boolean isUseButterKnife() {
        return true;
    }

//    @Override
//    protected void initListener() {
//        super.initListener();
//    }

    @Override
    protected boolean isRegisterNotifyReceiver() {
        return true;
    }

    @Override
    protected void subscribeNotify(NotifyMessageMode msg) {
        super.subscribeNotify(msg);
        if (batteryImg1 == null)
            return;
        int maxHeight = batteryLy1.getHeight() - batteryLy1.getPaddingTop() - batteryImg1.getPaddingBottom();
        if (msg != null && msg.receiver == MAVLINK_MSG_ID_BATTERY_STATUS && msg.object != null && msg.object instanceof BatteryBean) {
            BatteryBean bean = (BatteryBean) msg.object;
//            LogUtils.d(bean.toString());
            mVoltageValue.setText(bean.getVoltage());
            mTemperatureValue.setText(String.valueOf(bean.getTemperature()));
            mCapacityValue.setText(String.valueOf(bean.getCapacity()));
            mElectricityValue.setText(bean.getElectricity());
            mCyclesValue.setText(String.valueOf(bean.getBatteryCycle()));
            mFirstVoltageValue.setText(ConvertUtils.floatToString("%.2fV", bean.getBattery()[0]));
            mSecondVoltageValue.setText(ConvertUtils.floatToString("%.2fV", bean.getBattery()[1]));
            mThirdVoltageValue.setText(ConvertUtils.floatToString("%.2fV", bean.getBattery()[2]));
            mFourthVoltageValue.setText(ConvertUtils.floatToString("%.2fV", bean.getBattery()[3]));
            RelativeLayout.LayoutParams params1 = ViewUtils.getNewLayoutParams(batteryImg1,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (bean.getFirstVoltageScale() * maxHeight));
            params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            batteryImg1.setBackground(ResourceUtils.getDrawabe(bean.getFirstBatteryBg()));
            batteryImg1.setLayoutParams(params1);

            RelativeLayout.LayoutParams params2 = ViewUtils.getNewLayoutParams(batteryImg2,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (bean.getSecondVoltageScale() * maxHeight));
            params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            batteryImg2.setLayoutParams(params2);
            batteryImg2.setBackground(ResourceUtils.getDrawabe(bean.getSecondBatteryBg()));

            RelativeLayout.LayoutParams params3 = ViewUtils.getNewLayoutParams(batteryImg3,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (bean.getThirdVoltageScale() * maxHeight));
            params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            batteryImg3.setLayoutParams(params3);
            batteryImg3.setBackground(ResourceUtils.getDrawabe(bean.getThirdBatteryBg()));

            RelativeLayout.LayoutParams params4 = ViewUtils.getNewLayoutParams(batteryImg4,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (bean.getFourthVoltageScale() * maxHeight));
            params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            batteryImg4.setLayoutParams(params4);
            batteryImg4.setBackground(ResourceUtils.getDrawabe(bean.getFourthBatteryBg()));
        }
    }


}

