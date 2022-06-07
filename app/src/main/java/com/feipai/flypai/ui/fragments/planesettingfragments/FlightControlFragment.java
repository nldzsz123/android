package com.feipai.flypai.ui.fragments.planesettingfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.enums.MAV_DATA_STREAM;
import com.feipai.flypai.R;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.base.BaseMvpFragment;
import com.feipai.flypai.beans.NotifyMessageMode;
import com.feipai.flypai.beans.ProductModel;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.beans.mavlinkbeans.BatteryBean;
import com.feipai.flypai.beans.mavlinkbeans.PlaneParamsBean;
import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.ui.view.ActionDialog;
import com.feipai.flypai.ui.view.SwitchView;
import com.feipai.flypai.ui.view.VirtualKeyboardView;
import com.feipai.flypai.utils.PlaneCommand;
import com.feipai.flypai.utils.global.IAnimationUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.RxBusUtils;
import com.feipai.flypai.utils.global.StringUtils;
import com.feipai.flypai.utils.global.Utils;
import com.feipai.flypai.utils.global.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.common.msg_battery_status.MAVLINK_MSG_ID_BATTERY_STATUS;
import static com.common.msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE;

public class FlightControlFragment extends BaseMvpFragment<BasePresenter> implements BaseView, SwitchView.OnStateChangedListener {
    @BindView(R.id.ab_return_hight_ed)
    EditText mReturnHightEd;
    @BindView(R.id.ab_return_hight_limit_tv)
    TextView mReturnHightLimitTv;
    @BindView(R.id.flight_control_root_ly)
    LinearLayout mRootLy;
    @BindView(R.id.limit_height_root_ly)
    RelativeLayout mLimitHeightRootLy;
    @BindView(R.id.speed_limit_ly)
    LinearLayout mSpeedRootLy;
    @BindView(R.id.dw_tv)
    TextView mDwTv;
    @BindView(R.id.return_point_button)
    TextView mReturnPointButton;
    @BindView(R.id.distance_limit_eidtor)
    EditText mDistanceLimitEd;
    @BindView(R.id.distance_limit_tv)
    TextView mDistanceLimitTv;
    @BindView(R.id.distance_limit_max)
    TextView mDistanceLimitMaxTv;

    @BindView(R.id.hight_limit_eidtor)
    EditText mHightLimitEd;
    @BindView(R.id.limit_hight_tv)
    TextView mLimitHightTv;
    @BindView(R.id.safe_mode_switchview)
    SwitchView mSafeModeSv;
    @BindView(R.id.eight_down_outside_switchview)
    SwitchView mEightDownOutsideSv;
    @BindView(R.id.rising_add_tv)
    TextView mRisingAddTv;
    @BindView(R.id.rising_edittv)
    EditText mRisingEdittv;
    @BindView(R.id.rising_minus_tv)
    TextView mRisingMinusTv;
    @BindView(R.id.falling_add_tv)
    TextView mFallingAddTv;
    @BindView(R.id.falling_edittv)
    EditText mFallingEdittv;
    @BindView(R.id.falling_minus_tv)
    TextView mFallingMinusTv;
    @BindView(R.id.standard_add_tv)
    TextView mStandardAddTv;
    @BindView(R.id.standard_edittv)
    EditText mStandardEdittv;
    @BindView(R.id.standard_minus_tv)
    TextView mStandardMinusTv;
    @BindView(R.id.keyboard_view)
    VirtualKeyboardView mKeyboard;

    private ActionDialog mActionDialog;

    private final static int MIN_UP_DN_SPEED = 1;
    private final static int MAX_UP_DN_SPEED = 3;
    private final static int MIN_STANDARD_SPEED = 1;
    private final static int MAX_STANDARD_SPEED = 10;
    private final static int MIN_ALT = 30;
    private final static int MAX_ALT = 500;
    private final static int MIN_DISTANCE = 30;
    private int MAX_DISTANCE = 9999;
            //Utils.isDebug ? 5000 : 2000;
    private final static int MIN_RETURN = 30;
    private int MAX_RETURN = 200;


    @Override
    protected void initInject() {

    }

    @Override
    protected boolean isUseRxBus() {
        return true;
    }


    @Override
    protected boolean isUseButterKnife() {
        return true;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected int initLayout() {
        return R.layout.flight_control_fragment;
    }

    @Override
    protected void initView() {
        super.initView();
        mDistanceLimitMaxTv.setText(ResourceUtils.getString(Utils.isDebug ? R.string.distance_limit_max5 : R.string.distance_limit_max2));
        ViewUtils.banSystemSoft(getPageActivity(), mRootLy, mReturnHightEd);
        ViewUtils.banSystemSoft(getPageActivity(), mRootLy, mDistanceLimitEd);
        ViewUtils.banSystemSoft(getPageActivity(), mRootLy, mHightLimitEd);
        ViewUtils.banSystemSoft(getPageActivity(), mRootLy, mRisingEdittv);
        ViewUtils.banSystemSoft(getPageActivity(), mRootLy, mFallingEdittv);
        ViewUtils.banSystemSoft(getPageActivity(), mRootLy, mStandardEdittv);
        mKeyboard.setKeyboardListener(new VirtualKeyboardView.OnKeyboardListener() {
            @Override
            public void onTextAppend(EditText ed, String text) {
                String edText = ed.getText().toString().trim();
                edText = edText + text;
                ed.setText(edText);
                ed.setSelection(ed.getText().length());
                mKeyboard.getEditQuery().setText(edText);
            }

            @Override
            public void onCloseCallback(EditText ed) {
                ed.setCursorVisible(false);
                if (ed.getId() == R.id.ab_return_hight_ed) {
                    /**实际返航高度不能高于当前的最大飞行高度*/
                    String maxHeightTv = mHightLimitEd.getText().toString().trim();
                    if (StringUtils.isEmpty(maxHeightTv) || Integer.parseInt(maxHeightTv) >= MAX_RETURN) {
                        ViewUtils.edittextLimit(ed, MIN_RETURN, MAX_RETURN);
                    } else {
                        ViewUtils.edittextLimit(ed, MIN_RETURN, Integer.parseInt(maxHeightTv));
                    }
                    PlaneCommand.getInstance().setMavlinkParam(MAV_DATA_STREAM.RTL_ALT,
                            Integer.parseInt(ed.getText().toString().trim()) * 100);
                } else if (ed.getId() == R.id.distance_limit_eidtor) {
                    ViewUtils.edittextLimit(ed, MIN_DISTANCE, MAX_DISTANCE);//最大值根据飞控类型判断
                    PlaneCommand.getInstance().setMavlinkParam(MAV_DATA_STREAM.FENCE_RADIUS,
                            Integer.parseInt(ed.getText().toString().trim()));
                } else if (ed.getId() == R.id.hight_limit_eidtor) {
                    /**实际在最小取值必须大于返航高*/
                    String retureHeightTv = mReturnHightEd.getText().toString().trim();
                    if (StringUtils.isEmpty(retureHeightTv)) {
                        ViewUtils.edittextLimit(ed, MIN_ALT, MAX_ALT);
                    } else {
                        ViewUtils.edittextLimit(ed, Integer.parseInt(retureHeightTv), MAX_ALT);
                    }
                    PlaneCommand.getInstance().setMavlinkParam(MAV_DATA_STREAM.FENCE_ALT_MAX,
                            Integer.parseInt(ed.getText().toString().trim()));
                } else if (ed.getId() == R.id.rising_edittv) {
                    ViewUtils.edittextLimit(ed, MIN_UP_DN_SPEED, MAX_UP_DN_SPEED);
                    PlaneCommand.getInstance().setMavlinkParam(MAV_DATA_STREAM.PILOT_SPEED_UP,
                            Integer.parseInt(ed.getText().toString().trim()) * 100);
                } else if (ed.getId() == R.id.falling_minus_tv) {
                    ViewUtils.edittextLimit(ed, MIN_UP_DN_SPEED, MAX_UP_DN_SPEED);
                    PlaneCommand.getInstance().setMavlinkParam(MAV_DATA_STREAM.PILOT_SPEED_DN,
                            Integer.parseInt(ed.getText().toString().trim()) * 100);
                } else if (ed.getId() == R.id.standard_edittv) {
                    ViewUtils.edittextLimit(ed, MIN_STANDARD_SPEED, MAX_STANDARD_SPEED);
                    PlaneCommand.getInstance().setMavlinkParam(MAV_DATA_STREAM.WPNAV_LOIT_SPEED,
                            Integer.parseInt(ed.getText().toString().trim()) * 100);
                }
                showBoard(ed, false);
            }

            @Override
            public void onDelectAppend(EditText ed) {
                String text = ed.getText().toString().trim();
                if (text.length() > 0) {
                    text = text.substring(0, text.length() - 1);
                    ed.setText(text);
                    ed.setSelection(ed.getText().length());
                    mKeyboard.getEditQuery().setText(text);
                }

            }
        });
        mSafeModeSv.setOnStateChangedListener(this);
        mEightDownOutsideSv.setOnStateChangedListener(this);
    }

    @OnClick({R.id.rising_add_tv,
            R.id.ab_return_hight_ed,
            R.id.distance_limit_eidtor,
            R.id.hight_limit_eidtor,
            R.id.return_point_button,
            R.id.rising_minus_tv,
            R.id.rising_edittv,
            R.id.falling_add_tv,
            R.id.falling_minus_tv,
            R.id.standard_add_tv,
            R.id.standard_minus_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ab_return_hight_ed:
                showKeyboard(mReturnHightEd);
                break;
            case R.id.distance_limit_eidtor:
                showKeyboard(mDistanceLimitEd);
                break;
            case R.id.hight_limit_eidtor:
                showKeyboard(mHightLimitEd);
                break;
            case R.id.rising_edittv:
                showKeyboard(mRisingEdittv);
                break;
            case R.id.rising_add_tv:
                speedMinus(mRisingEdittv, mRisingAddTv, mRisingMinusTv, MAX_UP_DN_SPEED, MIN_UP_DN_SPEED, true);
                break;
            case R.id.rising_minus_tv:
                speedMinus(mRisingEdittv, mRisingAddTv, mRisingMinusTv, MAX_UP_DN_SPEED, MIN_UP_DN_SPEED, false);
                break;
            case R.id.falling_add_tv:
                speedMinus(mFallingEdittv, mFallingAddTv, mFallingMinusTv, MAX_UP_DN_SPEED, MIN_UP_DN_SPEED, true);
                break;
            case R.id.falling_minus_tv:
                speedMinus(mFallingEdittv, mFallingAddTv, mFallingMinusTv, MAX_UP_DN_SPEED, MIN_UP_DN_SPEED, false);
                break;
            case R.id.standard_add_tv:
                speedMinus(mStandardEdittv, mStandardAddTv, mStandardMinusTv, MAX_STANDARD_SPEED, MIN_STANDARD_SPEED, true);
                break;
            case R.id.standard_minus_tv:
                speedMinus(mStandardEdittv, mStandardAddTv, mStandardMinusTv, MAX_STANDARD_SPEED, MIN_STANDARD_SPEED, false);
                break;
            case R.id.return_point_button:
                /**重置返航点*/
                showActionDialog(ConstantFields.ACTION_PARAM.RESET_RETURN_POINT);
                break;
        }
    }

    private void showActionDialog(int action) {
        if (mActionDialog == null) {
            mActionDialog = new ActionDialog(this, new ActionDialog.ActionDialogListener() {
                @Override
                public void onConfirmCallback(int action) {
                    if (action == ConstantFields.ACTION_PARAM.RESET_RETURN_POINT) {
                        // TODO: 2019/7/8 返航点重置
                        RxBusUtils.getDefault().post(new RxbusBean(ConstantFields.BusEventType.RESET_HOME_POINT, 0));
//                        PlaneCommand.getInstance().resetHomePoint(phoneLat, phoneLng);
                    }
                }
            });
        }
        mActionDialog.showDialog(action);
    }

    private void showKeyboard(EditText editText) {
        editText.setCursorVisible(true);
        editText.setSelection(editText.getText().length());
        showBoard(editText, true);
    }


    @Override
    public void onConnected(ProductModel productModel) {
        super.onConnected(productModel);
        PlaneCommand.getInstance().initMavlinkSpeedParams();
        MAX_DISTANCE = Utils.isDebug ? 9999 : (productModel.productType == ConstantFields.ProductType_6kAir ? 9999 : 9999);
        mDistanceLimitMaxTv.setText(ResourceUtils.getString(Utils.isDebug ? R.string.distance_limit_max5 :
                (productModel.productType == ConstantFields.ProductType_6kAir ? R.string.distance_limit_max4 : R.string.distance_limit_max2)));
        mReturnHightLimitTv.setText(ResourceUtils.getString(
                productModel.productType == ConstantFields.ProductType_4k ? R.string.return_hight_limit_max120 : R.string.return_hight_limit_max200));
        MAX_RETURN = productModel.productType == ConstantFields.ProductType_4k ? 120 : 200;
    }

    /**
     * 数字键盘显示与隐藏
     */
    public void showBoard(EditText editText, boolean isShow) {
        mKeyboard.setFocusable(isShow);
        mKeyboard.bindEidttext(editText);
        mKeyboard.setFocusableInTouchMode(isShow);
        mKeyboard.bringToFront();
        mKeyboard.getEditQuery().setText("");
        if (isShow) {
            IAnimationUtils.performShowViewAnim(getPageActivity(), mKeyboard, R.anim.enter_bottom_anim);
        } else {
            IAnimationUtils.performHideViewAnim(getPageActivity(), mKeyboard, R.anim.exit_bottom_anim);
        }
    }

    public void speedMinus(EditText ed, TextView addView, TextView minView, int max, int min, boolean isAdd) {
        if (StringUtils.isEmpty(ed.getText().toString()))
            return;
        int text = Integer.parseInt(ed.getText().toString().trim());
        if (isAdd) {
            if (text < max) {
                text++;
                if (text >= max) {
                    text = max;
                    setViewEnabled(addView, false);
                }
                ed.setText(String.valueOf(text));
                setViewEnabled(minView, true);

            } else {
                /**停止加*/
                setViewEnabled(addView, false);
            }
        } else {
            if (text > min) {
                text--;
                ed.setText(String.valueOf(text));
                if (text <= min) {
                    text = min;
                    setViewEnabled(minView, false);
                }
                ed.setText(String.valueOf(text));
                setViewEnabled(addView, true);
            } else {
                /**停止减*/
                text = min;
                ed.setText(String.valueOf(text));
                setViewEnabled(minView, false);
            }
        }
        if (ed.getId() == R.id.rising_edittv) {
            PlaneCommand.getInstance().setMavlinkParam(MAV_DATA_STREAM.PILOT_SPEED_UP, text * 100);
        } else if (ed.getId() == R.id.falling_edittv) {
            PlaneCommand.getInstance().setMavlinkParam(MAV_DATA_STREAM.PILOT_SPEED_DN, text * 100);
        } else if (ed.getId() == R.id.standard_edittv) {
            PlaneCommand.getInstance().setMavlinkParam(MAV_DATA_STREAM.WPNAV_LOIT_SPEED, text * 100);
        }
    }

    private void setViewEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        view.setAlpha(enabled ? 1f : 0.5f);
    }

    @Override
    protected boolean isRegisterNotifyReceiver() {
        return true;
    }

    @Override
    protected void subscribeNotify(NotifyMessageMode msg) {
        super.subscribeNotify(msg);
        if (mReturnHightEd == null)
            return;
        if (msg != null && msg.receiver == MAVLINK_MSG_ID_PARAM_VALUE && msg.object != null && msg.object instanceof PlaneParamsBean) {
            PlaneParamsBean bean = (PlaneParamsBean) msg.object;
            mReturnHightEd.setText(String.valueOf(bean.getReturnAlt()));
            mDistanceLimitEd.setText(String.valueOf(bean.getFenceDistance()));
            mHightLimitEd.setText(String.valueOf(bean.getFenceMaxAlt()));
            mSafeModeSv.setOpened(bean.isSafeModeOpen());

            int rising = bean.getPolotSpeedUp();
            mRisingEdittv.setText(String.valueOf(rising));
            setViewEnabled(mRisingMinusTv, rising > MIN_UP_DN_SPEED);
            setViewEnabled(mRisingAddTv, rising < MAX_UP_DN_SPEED);

            int falling = bean.getPolotSpeedDn();
            mFallingEdittv.setText(String.valueOf(falling));
            setViewEnabled(mFallingMinusTv, falling > MIN_UP_DN_SPEED);
            setViewEnabled(mFallingAddTv, falling < MAX_UP_DN_SPEED);

            int standard = bean.getWpnavLoitSpeed();
            mStandardEdittv.setText(String.valueOf(standard));
            setViewEnabled(mStandardMinusTv, standard > MIN_STANDARD_SPEED);
            setViewEnabled(mStandardAddTv, standard < MAX_STANDARD_SPEED);
        }
    }

    @Override
    public void toggleToOn(SwitchView view) {
        view.toggleSwitch(true);
        switch (view.getId()) {
            case R.id.safe_mode_switchview:
                PlaneCommand.getInstance().setSafeMode(true);
                break;
            case R.id.eight_down_outside_switchview:
                /**指令目前不知*/
                break;
        }
    }

    @Override
    public void toggleToOff(SwitchView view) {
        view.toggleSwitch(false);
        switch (view.getId()) {
            case R.id.safe_mode_switchview:
                PlaneCommand.getInstance().setSafeMode(false);
                break;
            case R.id.eight_down_outside_switchview:
                /**指令目前不知*/
                break;
        }
    }
}
