package com.feipai.flypai.ui.view.ipm;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.feipai.flypai.base.basedialog.BaseDialog;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.StringUtils;
import com.zhy.autolayout.utils.AutoUtils;

/**
 * 1.准备工作：检查是否解锁，是否GPS模式
 * 2.6k切换到3:2，其他切换到4：3
 * 3.锁定曝光
 * 4.开启全景模式，朝北
 * 5.获取全景最后生成的文件名
 * 判断是否拍摄中中断
 * 6.开始拍摄
 * <p>
 * 自动曝光第一张拍完锁定曝光,手动曝光别管
 * 广角：
 * 1：转飞机380/8，云台1347拍；
 * 2转云台1138拍；
 * 3转飞机380/8拍；
 * 4转云台1347拍；
 * 5转飞机360 / 8 * 2拍；
 * 6转云台1138拍；
 * <p>
 * <p>
 * 全景：飞机角度：360/7,360/6，360/2
 * 云台角度1260,1510,1754，水平是1242
 * <p>
 * <p>
 **/
public class TakingPanorDialog {
    private BaseDialog mBuilder;
    private Context mContext;
    private OnClickCallback mCallback;
    private TextView mTitle;
    private TextView mBnt;
    private LinearLayout mTotalTimeLy;
    private TextView mTotalTime;
    private LinearLayout mTotalCountLy;
    private TextView mTotalStartTv;
    private TextView mCountTv;
    private TextView mTotalTv;
    private TextView mHintTv;
    private final static int START = 0;
    private final static int INTERRUPT = 1;
    private final static int COMPLETE = 2;
    private int mStep = START;
    private boolean isWide;
    private int mCount = 1;
    private int maxTotal = 0;


    public TakingPanorDialog(Context context) {
        this.mContext = context;
    }

    public TakingPanorDialog onCreate(int gravity, int x, int y) {
        mBuilder = new BaseDialog.Builder(mContext, R.style.dialog_for_ipm)
                .setContentView(R.layout.taking_panor_photo_layout)
                .setWidthAndHeight(AutoUtils.getPercentWidthSize(672), AutoUtils.getPercentHeightSize(417))
                .setParamsXAndY(gravity, x, y)
                .setNotTouchModal(true)
                .setCancelable(false)
                .create();
        initView();
        return this;
    }

    private void initView() {
        mBnt = mBuilder.findViewById(R.id.taking_panor_photo_dg_btn);
        mTitle = mBuilder.findViewById(R.id.taking_panor_photo_dg_title);
        mTotalTimeLy = mBuilder.findViewById(R.id.taking_panor_time_ly);
        mTotalTime = mBuilder.findViewById(R.id.taking_panor_time_value);
        mTotalCountLy = mBuilder.findViewById(R.id.taking_panor_photo_dg_total_count_ly);
        mTotalStartTv = mBuilder.findViewById(R.id.taking_panor_photo_total_count_start);
        mCountTv = mBuilder.findViewById(R.id.taking_panor_photo_dg_count);
        mTotalTv = mBuilder.findViewById(R.id.taking_panor_photo_dg_total);
        mHintTv = mBuilder.findViewById(R.id.taking_panor_photo_dg_hint_tv);
        mBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StringUtils.equals(mBnt.getText().toString().trim(), ResourceUtils.getString(R.string.start))) {
                    //开始
                    mStep = INTERRUPT;
                    updateClues(mStep, 1, "/" + (isWide ? 6 : 15));
                    if (mCallback != null) mCallback.onStart();
                } else if (StringUtils.equals(mBnt.getText().toString().trim(), ResourceUtils.getString(R.string.interrupt))) {
                    //中断
                    dismiss();
                    if (mCallback != null) mCallback.onInterrupt();
                } else if (StringUtils.equals(mBnt.getText().toString().trim(), ResourceUtils.getString(R.string.complete))) {
                    //完成
                    dismiss();
                    if (mCallback != null) mCallback.onComplete();
                }
            }
        });
    }

    private void updateClues(int step, int count, String total) {
        mCountTv.setText(String.valueOf(count));
        if (step == START) {
            mBnt.setText(ResourceUtils.getString(R.string.start));
            mBnt.setBackgroundResource(R.drawable.login_button_selcetor);
            mTitle.setText(ResourceUtils.getString(isWide ? R.string.smart_wide_photo_mode : R.string.move_plane_to_take_panor));
            if (isWide) {
                mCountTv.setText(String.valueOf(6));
                mTotalCountLy.setVisibility(View.VISIBLE);
                mTotalTv.setVisibility(View.GONE);
            } else {
                mTotalCountLy.setVisibility(View.GONE);
            }

            mTotalTime.setText(String.valueOf(isWide ? 5 : 10));
            mTotalTimeLy.setVisibility(View.VISIBLE);
            mHintTv.setText(ResourceUtils.getString(R.string.make_sure_sufficient_power));
        } else if (step == INTERRUPT) {
            mBnt.setText(ResourceUtils.getString(R.string.interrupt));
            mBnt.setBackgroundResource(R.drawable.fe9700_btn_selcetor);
            mTitle.setText(ResourceUtils.getString(isWide ? R.string.wide_angle_taking : R.string.vr_taking));
            mTotalTimeLy.setVisibility(View.GONE);
            mTotalStartTv.setVisibility(View.VISIBLE);
            mTotalCountLy.setVisibility(View.VISIBLE);
            updateCountForPanor(1);
            mTotalTv.setText("/" + String.valueOf(maxTotal));
            mTotalTv.setVisibility(View.VISIBLE);
            mHintTv.setText(ResourceUtils.getString(R.string.during_will_finish_shooting));
        } else {
            mBnt.setText(ResourceUtils.getString(R.string.complete));
            mBnt.setBackgroundResource(R.drawable.login_button_selcetor);
            mTitle.setText(ResourceUtils.getString(R.string.wrap));
            mTotalStartTv.setVisibility(View.GONE);
            mCountTv.setText(String.valueOf(count));
            mTotalTv.setText(total);
            mTotalTv.setVisibility(View.VISIBLE);
            mHintTv.setVisibility(isWide ? View.INVISIBLE : View.VISIBLE);
            mHintTv.setText(ResourceUtils.getString(R.string.return_to_synthesize));
        }
    }

    public void updateCountForPanor(int count) {
        if (mStep == INTERRUPT) {
            this.mCount = count;
            if (mCount <= maxTotal) {
                mCountTv.setText(String.valueOf(count));
                if (mCallback != null) mCallback.onCountChanged(count);
            } else {
                LogUtils.d("全景", "更新全景对话框----" + count);
                mStep = COMPLETE;
                updateClues(mStep, maxTotal, "/" + String.valueOf(maxTotal));
            }
        }
    }

    public int getStep() {
        return mStep;
    }

    public void show() {
        if (!isShowing()) {
            mStep = START;
            updateClues(mStep, 1, "/" + (isWide ? 6 : 15));
            mBuilder.show();
        }
    }

    public boolean isShowing() {
        return mBuilder != null && mBuilder.isShowing();
    }

    public void dismiss() {
        if (isShowing())
            mBuilder.dismiss();
        if (mCallback != null) mCallback.onDestroy();
    }

    public TakingPanorDialog setWide(boolean isWide) {
        this.isWide = isWide;
        maxTotal = isWide ? 6 : 15;
        return this;
    }

    public TakingPanorDialog setCallback(OnClickCallback mCallback) {
        this.mCallback = mCallback;
        return this;
    }

    public interface OnClickCallback {
        void onStart();

        void onInterrupt();

        /**
         * 监听变化，毕竟要从外面更新的当前拍摄张数
         */
        void onCountChanged(int count);

        void onComplete();

        void onDestroy();
    }

}
