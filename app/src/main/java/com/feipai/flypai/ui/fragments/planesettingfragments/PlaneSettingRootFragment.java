package com.feipai.flypai.ui.fragments.planesettingfragments;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.feipai.flypai.R;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.base.BaseMvpFragment;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.utils.global.IAnimationUtils;
import com.feipai.flypai.utils.global.StringUtils;
import com.zhy.autolayout.AutoRadioGroup;

import me.yokeyword.fragmentation.SupportFragment;

import static com.feipai.flypai.app.ConstantFields.PLANE_SETTING_TAB.*;

public class PlaneSettingRootFragment extends BaseMvpFragment<BasePresenter> implements BaseView {

    private SupportFragment[] mFragments = new SupportFragment[4];
    private AutoRadioGroup radioGroup;
    private FrameLayout mRootContainer;
    private int index = FLIGHT_CONTROL_TAB;
    private RadioButton mFlightControlTab;
    private RadioButton mCalibrationTab;
    private RadioButton mBatteryTab;
    private RadioButton mFindPlaneTab;

    @Override
    protected void initInject() {

    }

    @Override
    protected boolean isUseRxBus() {
        return true;
    }

    @Override
    protected void initRxbusListener(RxbusBean msg) {
        super.initRxbusListener(msg);
        if (msg != null && StringUtils.equals(msg.TAG, ConstantFields.BusEventType.SWITCH_BATTERY_TAB)) {
            if (index != BATTERY_TAB) {
                showHideFragment(mFragments[BATTERY_TAB], mFragments[index]);
                index = BATTERY_TAB;
                mBatteryTab.setChecked(true);
//                radioGroup.check(R.id.battery_tab);
            }
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected int initLayout() {
        return R.layout.planesetting_root_fragment;
    }

    @Override
    protected void initView() {
        super.initView();
        mRootContainer = findViewById(R.id.plane_setting_container);
        initFragments();
        radioGroup = findViewById(R.id.plane_setting_root_radiogroup);
        mFlightControlTab = findViewById(R.id.flight_control_tab);
        mCalibrationTab = findViewById(R.id.calibration_tab);
        mBatteryTab = findViewById(R.id.battery_tab);
        mFindPlaneTab = findViewById(R.id.find_plane_tab);
        mFlightControlTab.setChecked(true);
//        radioGroup.check(R.id.flight_control_tab);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                IAnimationUtils.performAnim(getPageActivity(), findViewById(checkedId), R.anim.view_up_scale_anim);
                switch (checkedId) {
                    case R.id.flight_control_tab:
                        showHideFragment(mFragments[FLIGHT_CONTROL_TAB], mFragments[index]);
                        index = FLIGHT_CONTROL_TAB;
                        break;
                    case R.id.calibration_tab:
                        showHideFragment(mFragments[CALIBRATION_TAB], mFragments[index]);
                        index = CALIBRATION_TAB;
                        break;
                    case R.id.battery_tab:
                        showHideFragment(mFragments[BATTERY_TAB], mFragments[index]);
                        index = BATTERY_TAB;
                        break;
                    case R.id.find_plane_tab:
                        showHideFragment(mFragments[SEARCH_PLANE_TAB], mFragments[index]);
                        index = SEARCH_PLANE_TAB;
                        break;
                }
            }
        });
    }

    private void initFragments() {
        SupportFragment firstFragment = findChildFragment(FlightControlFragment.class);
        if (firstFragment == null) {
            mFragments[FLIGHT_CONTROL_TAB] = new FlightControlFragment();
            mFragments[CALIBRATION_TAB] = new CalibrationFragment();
            mFragments[BATTERY_TAB] = new BatteryFragment();
            mFragments[SEARCH_PLANE_TAB] = new SearchPlaneFragment();

            loadMultipleRootFragment(R.id.plane_setting_container, FLIGHT_CONTROL_TAB,
                    mFragments[FLIGHT_CONTROL_TAB],
                    mFragments[CALIBRATION_TAB],
                    mFragments[BATTERY_TAB],
                    mFragments[SEARCH_PLANE_TAB]);
        } else {
            // 这里库已经做了Fragment恢复,所有不需要额外的处理了, 不会出现重叠问题
            // 这里我们需要拿到mFragments的引用
            mFragments[FLIGHT_CONTROL_TAB] = firstFragment;
            mFragments[CALIBRATION_TAB] = new CalibrationFragment();
            mFragments[BATTERY_TAB] = new BatteryFragment();
            mFragments[SEARCH_PLANE_TAB] = new SearchPlaneFragment();
        }
    }
}
