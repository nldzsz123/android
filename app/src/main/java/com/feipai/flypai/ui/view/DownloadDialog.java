package com.feipai.flypai.ui.view;

import android.content.Context;

import com.feipai.flypai.R;
import com.feipai.flypai.base.basedialog.BaseDialog;
import com.zhy.autolayout.utils.AutoUtils;

public class DownloadDialog {
    private BaseDialog mBaseDialog;
    private OnClickListener mListener;

    public DownloadDialog(Context context, OnClickListener listener) {
        this.mListener = listener;
        if (mBaseDialog == null) {
            mBaseDialog = new BaseDialog.Builder(context)
                    .setWidthAndHeight(AutoUtils.getPercentHeightSize(672), AutoUtils.getPercentHeightSize(408))
                    .addDefaultAnimation()
                    .setContentView(R.layout.download_dialog_layout)
                    .create();
            mBaseDialog.setCancelable(false);
            mBaseDialog.setOnclickListener(R.id.download_cancel_tv, v -> {
                if (mListener != null) {
                    mListener.onCancelListener();
                }
//                mPresenter.cancelDownloading();
            });
        }
    }

    public void updateProgressWithText(int progress, String centerText) {
        if (mBaseDialog != null && mBaseDialog.isShowing()) {
            mBaseDialog.setProgressWithCenterText(R.id.download_circle_progress, progress,
                    centerText);
        }
    }

    public void updateProgress(int progress) {
        if (mBaseDialog != null && mBaseDialog.isShowing())
            mBaseDialog.setCircleProgress(R.id.download_circle_progress, progress);
    }

    public void show() {
        if (mBaseDialog != null && !mBaseDialog.isShowing())
            mBaseDialog.show();
    }

    public boolean isShowing() {
        return mBaseDialog != null && mBaseDialog.isShowing();
    }


    public void dismissDownloadDialog() {
        updateProgress(0);
        if (mBaseDialog != null && mBaseDialog.isShowing())
            mBaseDialog.dismiss();
    }

    public interface OnClickListener {
        void onCancelListener();
    }
}
