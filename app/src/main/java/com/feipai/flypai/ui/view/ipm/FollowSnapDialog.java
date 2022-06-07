package com.feipai.flypai.ui.view.ipm;

import android.content.Context;
import android.widget.TextView;

import com.amap.api.maps.model.LatLng;
import com.feipai.flypai.R;
import com.feipai.flypai.base.basedialog.BaseDialog;
import com.feipai.flypai.utils.GPSUtils;
import com.feipai.flypai.utils.global.ConvertUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.zhy.autolayout.utils.AutoUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 传手机经纬度给飞机吗？逻辑比较不太熟，可以备注一下，后面方便追溯
 */
public class FollowSnapDialog {
    @BindView(R.id.follow_dg_title)
    TextView followDgTitle;
    @BindView(R.id.follow_dg_gps_value)
    TextView followDgGpsValue;
    @BindView(R.id.follow_dg_height_tv)
    TextView followDgHeightValue;
    @BindView(R.id.follow_distance_value)
    TextView followDistanceValue;
    @BindView(R.id.follow_dg_hint_tv)
    TextView followDgHintTv;
    @BindView(R.id.follow_dg_comfirm_bnt)
    TextView followDgComfirmBnt;
    private BaseDialog mBuilder;
    private Context mContext;
    private OnEventCallback mCallback;
    private final static int START = 0;
    private final static int PAUSE = 2;
    private final static int RESUME = 3;
    private int mStep = START;

    public LatLng mMobileLatLng = null;
    public LatLng mOldLatlng = null;

    public FollowSnapDialog(Context context) {
        this.mContext = context;
    }

    public FollowSnapDialog onCreate(int gravity, int x, int y) {
        mBuilder = new BaseDialog.Builder(mContext, R.style.dialog_for_ipm)
                .setContentView(R.layout.follow_snap_dg_layout)
                .setWidthAndHeight(AutoUtils.getPercentWidthSize(672), AutoUtils.getPercentHeightSize(417))
                .setParamsXAndY(gravity, x, y)
                .setNotTouchModal(true)
                .setCancelable(false)
                .create();
        ButterKnife.bind(this, mBuilder);
        return this;
    }


    public void updateClues(int step) {
        if (mBuilder.isShowing()) {
            if (step == START) {
                followDgTitle.setText(ResourceUtils.getString(R.string.push_rocker_adjust_following_position));
                followDgComfirmBnt.setText(ResourceUtils.getString(R.string.start));
                limitDistance();

            } else if (step == PAUSE) {
                followDgTitle.setText(ResourceUtils.getString(R.string.intelligent_following));
                followDgComfirmBnt.setText(ResourceUtils.getString(R.string.pause));
                followDgComfirmBnt.setBackgroundResource(R.drawable.fe9700_btn_selcetor);
            } else if (step == RESUME) {
                followDgTitle.setText(ResourceUtils.getString(R.string.intelligent_follow_pause));
                followDgComfirmBnt.setText(ResourceUtils.getString(R.string.continues));
                followDgComfirmBnt.setBackgroundResource(R.drawable.login_button_selcetor);
            }
        }
    }

    public int getStep() {
        return mStep;
    }

    public void show() {
        if (!isShowing()) {
            mBuilder.show();
            mStep = START;
            updateClues(mStep);
        }
    }

    public boolean isShowing() {
        return mBuilder != null && mBuilder.isShowing();
    }

    public void dismiss() {
        mMobileLatLng = null;
        if (mCallback != null)
            mCallback.onDistory();
        if (isShowing())
            mBuilder.dismiss();
    }


    public FollowSnapDialog setCallback(OnEventCallback mCallback) {
        this.mCallback = mCallback;
        return this;
    }

    @OnClick(R.id.follow_dg_comfirm_bnt)
    public void onViewClicked() {
        if (mStep == START) {
            if (mCallback != null) mCallback.onStart(mMobileLatLng);
            mStep = PAUSE;
        } else if (mStep == PAUSE) {
            dismiss();
        }
        updateClues(mStep);
    }


    public void updateFollowHeight(String height) {
        if (mBuilder != null && mBuilder.isShowing()) {
            followDgHeightValue.setText(height);
            limitDistance();
        }
    }

    private void limitDistance() {
        if (Float.parseFloat(followDistanceValue.getText().toString().trim()) < 4
                && (Float.parseFloat(followDgHeightValue.getText().toString().trim()) < 4) && mStep == START) {
            //限制高度和距离，不达标则不允许开始
            followDgComfirmBnt.setEnabled(false);
            followDgComfirmBnt.setBackgroundResource(R.drawable.unenabled_button_def);
        } else {
            followDgComfirmBnt.setEnabled(true);
            followDgComfirmBnt.setBackgroundResource(R.drawable.login_button_selcetor);
        }
    }

    /**
     * 更新手机GPS强弱，监听手机坐标点位置
     */
    public void updateGpsValue(float mobileGPS, LatLng mobileLatlng, float distance) {

        if (mBuilder != null && mBuilder.isShowing()) {
            if (mMobileLatLng != null) {
                mOldLatlng = mMobileLatLng;
            }
            this.mMobileLatLng = GPSUtils.gcj2WGSExactly(mobileLatlng.latitude, mobileLatlng.longitude);
            followDgGpsValue.setText(ResourceUtils.getString(mobileGPS <= 15 ? R.string.weak : R.string.strong));
//            LogUtils.d("跟随步骤----》" + mStep);
            if (mStep == PAUSE && mCallback != null && mMobileLatLng != null && mOldLatlng != null) {
                mCallback.onStartedLatLngChanged(mMobileLatLng, mOldLatlng);
            }
            followDistanceValue.setText(ConvertUtils.floatToString(distance));
            limitDistance();
        }
    }

    public interface OnEventCallback {
        void onStart(LatLng latLng);

        void onStartedLatLngChanged(LatLng curLatLng, LatLng oldLatLng);

        void onDistory();
    }

}
