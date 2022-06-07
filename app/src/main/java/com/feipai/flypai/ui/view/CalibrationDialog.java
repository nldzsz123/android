package com.feipai.flypai.ui.view;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.feipai.flypai.R;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.base.basedialog.BaseDialog;
import com.feipai.flypai.connect.ConnectManager;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.Utils;
import com.feipai.flypai.utils.imageloader.GlideApp;
import com.feipai.flypai.utils.imageloader.IImageLoader;
import com.feipai.flypai.utils.imageloader.ImageLoaderFactory;
import com.zhy.autolayout.utils.AutoUtils;

public class CalibrationDialog {

    private ActionDialog acDialog;
    private BaseDialog mCaliD;
    private Context mContext;
    private int mAction = -1;

    public CalibrationDialog(BaseView baseView, CalibrationDialogListener listener) {
        this.mContext = baseView.getPageActivity();
        if (acDialog == null) {
            acDialog = new ActionDialog(baseView, new ActionDialog.ActionDialogListener() {
                @Override
                public void onConfirmCallback(int act) {
                    showCaliDialog(act, listener);
                }
            });
        }
    }

    public void startShowAcDialog(int action) {
        mAction = action;
        acDialog.showDialog(action);
    }

    public boolean isShowing() {
        return mCaliD != null && mCaliD.isShowing();
    }

    /***显示校准对话框*/
    public void showCaliDialog(int action, CalibrationDialogListener listener) {
        this.mAction = action;
        if (mCaliD == null) createCaliDialog(mContext);
        mCaliD.setOnclickListener(R.id.cali_cancel_button, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    if (tv.getText().toString().equals(ResourceUtils.getString(R.string.start_calibration))) {
                        //开始校准
                        if (listener != null) listener.onStartCali(action);
                    } else if (tv.getText().toString().equals(ResourceUtils.getString(R.string.cancel_calibration))) {
                        //取消校准,直接取消，无需指令
                        if (listener != null) listener.onCancleCali(action);
                        mAction = -1;
                        mCaliD.dismiss();
                    }
                }
            }
        });
        if (action == ConstantFields.ACTION_PARAM.START_CALIBRATION_GYRO) {
            //开始陀螺仪校准
            mCaliD.setText(R.id.cali_title_tv, ResourceUtils.getString(R.string.gyroscope_calibration));
            mCaliD.setText(R.id.cali_content_tv, ResourceUtils.getString(R.string.start_gyroscope_calibration_dg_hint));
            mCaliD.setText(R.id.cali_cancel_button, ResourceUtils.getString(R.string.start_calibration));
            mCaliD.setViewVisibility(R.id.cali_img, View.VISIBLE);
            mCaliD.setViewVisibility(R.id.cali_img_gif, View.GONE);
            mCaliD.setViewVisibility(R.id.cali_progress, View.INVISIBLE);
            mCaliD.setTextColor(R.id.cali_cancel_button, ResourceUtils.getColor(R.color.color_5098e4));
        } else if (action == ConstantFields.ACTION_PARAM.START_CALIBRATION_COMPASS) {
            //开始指南针校准
            mCaliD.setText(R.id.cali_title_tv, ResourceUtils.getString(R.string.compass_alignment));
            mCaliD.setText(R.id.cali_content_tv, ResourceUtils.getString(R.string.start_compass_calibration_dg_hint));
            mCaliD.setText(R.id.cali_cancel_button, ResourceUtils.getString(R.string.start_calibration));
            mCaliD.setViewVisibility(R.id.cali_img, View.VISIBLE);
            mCaliD.setViewVisibility(R.id.cali_img_gif, View.GONE);
            mCaliD.setViewVisibility(R.id.cali_progress, View.INVISIBLE);
            mCaliD.setTextColor(R.id.cali_cancel_button, ResourceUtils.getColor(R.color.color_5098e4));
        } else if (action == ConstantFields.ACTION_PARAM.CANCEL_CALIBRATION_GYRO) {
            //陀螺仪校准中
            mCaliD.setText(R.id.cali_title_tv, ResourceUtils.getString(R.string.gyroscope_calibrationing));
            mCaliD.setText(R.id.cali_content_tv, ResourceUtils.getString(R.string.gyroscope_calibrationing_dg_hint));
            mCaliD.setText(R.id.cali_cancel_button, ResourceUtils.getString(R.string.cancel_calibration));
            mCaliD.setViewVisibility(R.id.cali_progress, View.INVISIBLE);
            mCaliD.setViewVisibility(R.id.cali_img, View.VISIBLE);
            mCaliD.setViewVisibility(R.id.cali_img_gif, View.GONE);
            mCaliD.setTextColor(R.id.cali_cancel_button, ResourceUtils.getColor(R.color.color_333333));
        } else if (action == ConstantFields.ACTION_PARAM.CANCEL_CALIBRATION_COMPASS) {
            //指南针校准中
            mCaliD.setText(R.id.cali_title_tv, ResourceUtils.getString(R.string.compass_alignmenting));
            mCaliD.setText(R.id.cali_content_tv, ResourceUtils.getString(R.string.compass_calibrationing_dg_hint));
            mCaliD.setText(R.id.cali_cancel_button, ResourceUtils.getString(R.string.cancel_calibration));
            mCaliD.setViewVisibility(R.id.cali_progress, View.VISIBLE);
            mCaliD.setViewVisibility(R.id.cali_img, View.GONE);
            mCaliD.setViewVisibility(R.id.cali_img_gif, View.VISIBLE);
            mCaliD.setTextColor(R.id.cali_cancel_button, ResourceUtils.getColor(R.color.color_333333));
            ImageView imageView = mCaliD.getView(R.id.cali_img_gif);
            int caliGif = R.mipmap.cali_compass_4k;
            if (ConnectManager.getInstance().mProductModel.productType == ConstantFields.ProductType_4kAir) {
                caliGif = R.mipmap.cali_compass_4ka;
            } else if (ConnectManager.getInstance().mProductModel.productType == ConstantFields.ProductType_6kAir) {
                caliGif = R.mipmap.cali_compass_6ka;
            }
            GlideApp.with(Utils.context).asGif().load(caliGif).diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);
        }
        if (!mCaliD.isShowing()) {
            mCaliD.show();
        }
    }

    private void createCaliDialog(Context context) {
        mCaliD = new BaseDialog.Builder(context)
                .setWidthAndHeight(AutoUtils.getPercentHeightSize(1032), AutoUtils.getPercentHeightSize(755))
                .setContentView(R.layout.calibration_dialog_layout)
                .create();
        mCaliD.setProgressMax(R.id.cali_progress, 100);
        mCaliD.setCancelable(false);
    }

    /**
     * 校准成功或失败
     */
    public void creteSuccessDialog(Context context, int action, boolean isSuccess) {
        if (action == -1)
            return;
        if (mCaliD != null) {
            if (mCaliD.isShowing()) {
                if (mCaliD.getViewVisibility(R.id.cali_progress) != View.VISIBLE || getProgress() >= 99) {
                    mCaliD.dismiss();
                    mCaliD = null;
                } else {
                    return;
                }
            }
        }
        if (mCaliD == null) {
            mCaliD = new BaseDialog.Builder(context)
                    .setWidthAndHeight(AutoUtils.getPercentHeightSize(672), AutoUtils.getPercentHeightSize(408))
                    .setContentView(R.layout.calibration_success_dialog_layout)
                    .create();
            mCaliD.setOnclickListener(R.id.cali_success_confirm_button, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAction = -1;
                    mCaliD.dismiss();
                    mCaliD = null;
                }
            });
            if (action == ConstantFields.ACTION_PARAM.CANCEL_CALIBRATION_COMPASS) {
                mCaliD.setText(R.id.cali_success_title_tv, ResourceUtils.getString(
                        isSuccess ? R.string.compass_calibration_complete : R.string.compass_calibration_failure));
            } else if (action == ConstantFields.ACTION_PARAM.CANCEL_CALIBRATION_GYRO) {
                mCaliD.setText(R.id.cali_success_title_tv, ResourceUtils.getString(R.string.gyroscope_calibration_complete));
            }
        }
        mAction = -1;
        if (!mCaliD.isShowing()) {
            mCaliD.show();
        }
    }

    /**
     * 指南针更新校准进度条
     */
    public void updateProgress(int progress) {
        mCaliD.setProgress(R.id.cali_progress, progress);
    }

    public int getProgress() {
        return mCaliD.getProgress(R.id.cali_progress);
    }

    /**
     * 指南针和陀螺仪校验结果
     */
    public void calibRustle(boolean isSuccess) {
        creteSuccessDialog(mContext, mAction, isSuccess);
    }

    public interface CalibrationDialogListener {
        void onStartCali(int action);

        void onCancleCali(int action);

        void onCompleteConfirm(int action);
    }
}
