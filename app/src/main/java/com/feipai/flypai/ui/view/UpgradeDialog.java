package com.feipai.flypai.ui.view;

import android.content.Context;
import android.view.View;

import com.feipai.flypai.R;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.base.basedialog.BaseDialog;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.zhy.autolayout.utils.AutoUtils;

public class UpgradeDialog {
    private BaseDialog mUpgradeDialog;
    private CancelListener mListener;
    private String mAction;

    public UpgradeDialog(Context context, CancelListener listener) {
        if (mUpgradeDialog == null) {
            mUpgradeDialog = new BaseDialog.Builder(context)
                    .setWidthAndHeight(AutoUtils.getPercentHeightSize(672), AutoUtils.getPercentHeightSize(408))
                    .setContentView(R.layout.upgrade_dialog)
                    .create();
            mUpgradeDialog.setCancelable(false);
        }
        this.mListener = listener;
    }

    public void showDialog(String action) {
        this.mAction = action;
//        mUpgradeDialog.setOnclickListener(R.id.confirm_action_confirm_tv, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialogDismiss();
//            }
//        });
        switch (action) {
            case ConstantFields.UPGRADE_FW.UPDATE_CAMERA_PROGRESS:
                mUpgradeDialog.setText(R.id.base_dialog_title, ResourceUtils.getString(R.string.updating_camera_fw));
                break;
            case ConstantFields.UPGRADE_FW.UPDATE_PLANE_PROGRESS:
                mUpgradeDialog.setText(R.id.base_dialog_title, ResourceUtils.getString(R.string.updating_plane_fw));
                break;
            case ConstantFields.UPGRADE_FW.UPDATE_YUNTAI_PROGRESS:
                mUpgradeDialog.setText(R.id.base_dialog_title, ResourceUtils.getString(R.string.updating_yuntai_fw));
                break;
        }

        if (!mUpgradeDialog.isShowing()) {
            mUpgradeDialog.show();
        }
    }

    public void updateProgress(int progress) {
        if (mUpgradeDialog != null)
            mUpgradeDialog.setCircleProgress(R.id.circle_progress, progress);
    }

    public void dialogDismiss() {
        if (mListener != null) mListener.cancelCallback(mAction);
        if (isShowing()) mUpgradeDialog.dismiss();
    }

    public boolean isShowing() {
        return mUpgradeDialog != null && mUpgradeDialog.isShowing();
    }

    public interface CancelListener {
        void cancelCallback(String action);
    }

}
