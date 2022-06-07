package com.feipai.flypai.ui.view;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.feipai.flypai.api.RxLoopObserver;
import com.feipai.flypai.api.RxLoopSchedulers;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.base.basedialog.BaseDialog;
import com.feipai.flypai.beans.FileBean;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.utils.global.IAnimationUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.NetworkUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.zhy.autolayout.utils.AutoUtils;

import io.reactivex.functions.Function;

public class ActionDialog {

    private BaseDialog mActionDialog;

    private ActionDialogListener mListener;
    private ActionDialogCancelListener mCancelListener;
    private FileBean mActionBean;
    BaseView mbv;
    private int mAction;

    public ActionDialog(BaseView baseView, ActionDialogListener listener) {
        this.mbv = baseView;
        if (mActionDialog == null) {
            mActionDialog = new BaseDialog.Builder(baseView.getPageActivity())
                    .setWidthAndHeight(AutoUtils.getPercentHeightSize(672), AutoUtils.getPercentHeightSize(408))
                    .setContentView(R.layout.confirm_action_dialog)
                    .create();
            mActionDialog.setCancelable(false);
            mActionDialog.setOnclickListener(R.id.confirm_action_cancel_tv, v -> {
                mActionDialog.dismiss();
            });
        }
        this.mListener = listener;
    }

    public ActionDialog(Context context) {
        if (mActionDialog == null) {
            mActionDialog = new BaseDialog.Builder(context)
                    .setWidthAndHeight(AutoUtils.getPercentHeightSize(672), AutoUtils.getPercentHeightSize(408))
                    .setContentView(R.layout.confirm_action_dialog)
                    .create();
            mActionDialog.setCancelable(false);
        }
    }

    /**
     * @param bean 响应操作的bean文件
     */
    public void setActionBean(FileBean bean) {
        mActionBean = bean;
    }

    public FileBean getActionBean() {
        return mActionBean;
    }

    public int getAction(){
        return mAction;
    }

    public String message() {
        if (isShowing()) {
            TextView tv = mActionDialog.getView(R.id.confirm_action_tv);
            return tv.getText().toString().trim();
        }
        return null;
    }

    public void showDialog(int action) {
        this.mAction=action;
        mActionDialog.setOnclickListener(R.id.confirm_action_confirm_tv, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionDialogDismiss();
                if (mListener != null) mListener.onConfirmCallback(action);
            }
        });
        mActionDialog.setViewVisibility(R.id.confirm_action_second_tv, View.GONE);
        showCancelButton(true);
        mActionDialog.setViewVisibility(R.id.confirm_action_bottom_ly, View.VISIBLE);
        if (action == ConstantFields.ACTION_PARAM.DOWNLOAD_SINGLE_FILE) {
            mActionDialog.setText(R.id.confirm_action_titile, ResourceUtils.getString(R.string.confirm_download));
            mActionDialog.setText(R.id.confirm_action_tv, ResourceUtils.getString(R.string.download_file_form_flypai));
            mActionDialog.setText(R.id.confirm_action_confirm_tv, ResourceUtils.getString(R.string.confirm));
            mActionDialog.setTextColor(R.id.confirm_action_confirm_tv, ResourceUtils.getColor(R.color.color_5098e4));
        } else if (action == ConstantFields.ACTION_PARAM.DOWNLOAD_FILES) {
            mActionDialog.setText(R.id.confirm_action_titile, ResourceUtils.getString(R.string.download_files_form_flypai));
            mActionDialog.setText(R.id.confirm_action_tv, ResourceUtils.getString(R.string.download_file_form_flypai_prompt));
            mActionDialog.setText(R.id.confirm_action_confirm_tv, ResourceUtils.getString(R.string.confirm));
            mActionDialog.setTextColor(R.id.confirm_action_confirm_tv, ResourceUtils.getColor(R.color.color_5098e4));
        } else if (action == ConstantFields.ACTION_PARAM.DELECT_FILE) {
            mActionDialog.setText(R.id.confirm_action_titile, ResourceUtils.getString(R.string.confirm_deletion));
            mActionDialog.setText(R.id.confirm_action_tv, ResourceUtils.getString(R.string.are_u_sure_delect_selected_file));
            mActionDialog.setText(R.id.confirm_action_confirm_tv, ResourceUtils.getString(R.string.delete));
            mActionDialog.setTextColor(R.id.confirm_action_confirm_tv, ResourceUtils.getColor(R.color.color_f34235));
        } else if (action == ConstantFields.ACTION_PARAM.SYN_PANOR || action == ConstantFields.ACTION_PARAM.SYN_DELAY || action == ConstantFields.ACTION_PARAM.SYN_WIDE_ANGLE) {
            mActionDialog.setText(R.id.confirm_action_titile, ResourceUtils.getString(R.string.confirm_synthetic));
            mActionDialog.setText(R.id.confirm_action_tv, ResourceUtils.getString(R.string.synthetic_long_time_prompt));
            mActionDialog.setText(R.id.confirm_action_confirm_tv, ResourceUtils.getString(R.string.confirm));
            mActionDialog.setTextColor(R.id.confirm_action_confirm_tv, ResourceUtils.getColor(R.color.color_5098e4));
        } else if (action == ConstantFields.ACTION_PARAM.RESET_RETURN_POINT) {
            mActionDialog.setText(R.id.confirm_action_titile, ResourceUtils.getString(R.string.return_point_reset));
            mActionDialog.setText(R.id.confirm_action_tv, ResourceUtils.getString(R.string.confirm_reset_return_point_to_phone_position));
            mActionDialog.setText(R.id.confirm_action_confirm_tv, ResourceUtils.getString(R.string.reset));
            mActionDialog.setTextColor(R.id.confirm_action_confirm_tv, ResourceUtils.getColor(R.color.color_5098e4));
        } else if (action == ConstantFields.ACTION_PARAM.START_CALIBRATION_GYRO) {
            mActionDialog.setText(R.id.confirm_action_titile, ResourceUtils.getString(R.string.gyroscope_calibration));
            mActionDialog.setText(R.id.confirm_action_tv, ResourceUtils.getString(R.string.gyroscope_need_calibration));
            mActionDialog.setText(R.id.confirm_action_confirm_tv, ResourceUtils.getString(R.string.confirm));
            mActionDialog.setTextColor(R.id.confirm_action_confirm_tv, ResourceUtils.getColor(R.color.color_5098e4));
        } else if (action == ConstantFields.ACTION_PARAM.START_CALIBRATION_COMPASS) {
            mActionDialog.setText(R.id.confirm_action_titile, ResourceUtils.getString(R.string.compass_alignment));
            mActionDialog.setText(R.id.confirm_action_tv, ResourceUtils.getString(R.string.compass_need_alignment));
            mActionDialog.setText(R.id.confirm_action_confirm_tv, ResourceUtils.getString(R.string.confirm));
            mActionDialog.setTextColor(R.id.confirm_action_confirm_tv, ResourceUtils.getColor(R.color.color_5098e4));
        } else if (action == ConstantFields.ACTION_PARAM.START_UPGRADE_CAMERA_FW) {
            mActionDialog.setText(R.id.confirm_action_titile, ResourceUtils.getString(R.string.confirm_updating_camera_fw));
            mActionDialog.setText(R.id.confirm_action_tv, ResourceUtils.getString(R.string.dont_close_or_disconnect));
            mActionDialog.setText(R.id.confirm_action_confirm_tv, ResourceUtils.getString(R.string.confirm));
//            showCancelButton(false);
            mActionDialog.setTextColor(R.id.confirm_action_confirm_tv, ResourceUtils.getColor(R.color.color_5098e4));
        } else if (action == ConstantFields.ACTION_PARAM.START_UPGRADE_PLANE_FW) {
            mActionDialog.setText(R.id.confirm_action_titile, ResourceUtils.getString(R.string.confirm_updating_plane_fw));
            mActionDialog.setText(R.id.confirm_action_tv, ResourceUtils.getString(R.string.dont_close_or_disconnect));
            mActionDialog.setText(R.id.confirm_action_confirm_tv, ResourceUtils.getString(R.string.confirm));
//            showCancelButton(false);
            mActionDialog.setTextColor(R.id.confirm_action_confirm_tv, ResourceUtils.getColor(R.color.color_5098e4));
        } else if (action == ConstantFields.ACTION_PARAM.START_UPGRADE_YUNTAI_FW) {
            mActionDialog.setText(R.id.confirm_action_titile, ResourceUtils.getString(R.string.confirm_updating_yuntai_fw));
            mActionDialog.setText(R.id.confirm_action_tv, ResourceUtils.getString(R.string.dont_close_or_disconnect));
            mActionDialog.setText(R.id.confirm_action_confirm_tv, ResourceUtils.getString(R.string.confirm));
//            showCancelButton(false);
            mActionDialog.setTextColor(R.id.confirm_action_confirm_tv, ResourceUtils.getColor(R.color.color_5098e4));
        } else if (action == ConstantFields.ACTION_PARAM.START_UPGRADE_NET_ERROR) {
            mActionDialog.setText(R.id.confirm_action_titile, ResourceUtils.getString(R.string.cont_updating));
            mActionDialog.setText(R.id.confirm_action_tv, ResourceUtils.getString(R.string.connect_plane_retry));
            mActionDialog.setText(R.id.confirm_action_confirm_tv, ResourceUtils.getString(R.string.confirm));
            showCancelButton(false);
            mActionDialog.setTextColor(R.id.confirm_action_confirm_tv, ResourceUtils.getColor(R.color.color_5098e4));
        } else if (action == ConstantFields.ACTION_PARAM.UPGRADE_IN_FLYING_ERROR) {
            mActionDialog.setText(R.id.confirm_action_titile, ResourceUtils.getString(R.string.cont_updating));
            mActionDialog.setText(R.id.confirm_action_tv, ResourceUtils.getString(R.string.do_not_upgrade_flying));
            mActionDialog.setText(R.id.confirm_action_confirm_tv, ResourceUtils.getString(R.string.confirm));
            showCancelButton(false);
            mActionDialog.setTextColor(R.id.confirm_action_confirm_tv, ResourceUtils.getColor(R.color.color_5098e4));
        } else if (action == ConstantFields.ACTION_PARAM.UPGRADE_NET_WORK_ERROR) {
            mActionDialog.setText(R.id.confirm_action_titile, ResourceUtils.getString(R.string.updating_failed));
            mActionDialog.setText(R.id.confirm_action_tv, ResourceUtils.getString(R.string.connect_plane_retry));
            mActionDialog.setText(R.id.confirm_action_confirm_tv, ResourceUtils.getString(R.string.confirm));
            showCancelButton(false);
            mActionDialog.setTextColor(R.id.confirm_action_confirm_tv, ResourceUtils.getColor(R.color.color_5098e4));
        } else if (action == ConstantFields.ACTION_PARAM.UPGRADE_ERROR) {
            mActionDialog.setText(R.id.confirm_action_titile, ResourceUtils.getString(R.string.updating_failed));
            mActionDialog.setText(R.id.confirm_action_tv, ResourceUtils.getString(R.string.check_sd_or_connection_retry));
            mActionDialog.setText(R.id.confirm_action_confirm_tv, ResourceUtils.getString(R.string.confirm));
            showCancelButton(false);
            mActionDialog.setTextColor(R.id.confirm_action_confirm_tv, ResourceUtils.getColor(R.color.color_5098e4));
        } else if (action == ConstantFields.ACTION_PARAM.UPGRADE_SD_ERROR_FOR_6KA) {
            mActionDialog.setText(R.id.confirm_action_titile, ResourceUtils.getString(R.string.updating_failed));
            mActionDialog.setText(R.id.confirm_action_tv, ResourceUtils.getString(R.string.insert_external_card_and_retry));
            mActionDialog.setText(R.id.confirm_action_confirm_tv, ResourceUtils.getString(R.string.confirm));
            showCancelButton(false);
            mActionDialog.setTextColor(R.id.confirm_action_confirm_tv, ResourceUtils.getColor(R.color.color_5098e4));
        } else if (action == ConstantFields.ACTION_PARAM.UPGRADE_COMPLETE) {
            mActionDialog.setText(R.id.confirm_action_titile, ResourceUtils.getString(R.string.updated_complete));
            mActionDialog.setText(R.id.confirm_action_tv, ResourceUtils.getString(R.string.fw_uploaded_successfully_and_restart));
            startTimerEnable(ResourceUtils.getString(R.string.confirm));
            mActionDialog.setTextColor(R.id.confirm_action_confirm_tv, ResourceUtils.getColor(R.color.color_5098e4));
            showCancelButton(false);
        } else if (action == ConstantFields.ACTION_PARAM.UPGRADE_COMPLETE_RESTART) {
            mActionDialog.setText(R.id.confirm_action_titile, ResourceUtils.getString(R.string.updated_complete));
            mActionDialog.setText(R.id.confirm_action_tv, ResourceUtils.getString(R.string.fw_uploaded_successfully_and_restart_by_yourself));
            startTimerEnable(ResourceUtils.getString(R.string.confirm));
            mActionDialog.setTextColor(R.id.confirm_action_confirm_tv, ResourceUtils.getColor(R.color.color_5098e4));
            showCancelButton(false);
        } else if (action == ConstantFields.ACTION_PARAM.UNPACK_CAMERA_BIN_ZIP) {
            mActionDialog.setText(R.id.confirm_action_titile, ResourceUtils.getString(R.string.unpacking_zip));
            mActionDialog.setText(R.id.confirm_action_tv, ResourceUtils.getString(R.string.unpacking_zip_and_dont_close_app));
            mActionDialog.setViewVisibility(R.id.confirm_action_bottom_ly, View.GONE);
            mActionDialog.setTextColor(R.id.confirm_action_confirm_tv, ResourceUtils.getColor(R.color.color_5098e4));
            showCancelButton(false);
        }
        LogUtils.d("显示对话框");
        if (!mActionDialog.isShowing()) {
            LogUtils.d("显示对话框2");
            mActionDialog.show();
        }
    }

    /**
     * 倒计时确认完成按钮
     */
    int time = 10;

    private void startTimerEnable(String string) {
        time = 10;
        RxLoopSchedulers.composeLoop(mbv, 0, 1000, new Function() {
            @Override
            public Integer apply(Object o) throws Exception {
                time--;
                return time;
            }
        }).subscribe(new RxLoopObserver<Integer>() {
            @Override
            public void onNext(Integer count) {
                super.onNext(count);
                LogUtils.d("倒计时确定" + count);
                if (count <= 0) {
                    this.disposeDisposables();
                    mActionDialog.setText(R.id.confirm_action_confirm_tv, string);
                    mActionDialog.setViewEnabled(R.id.confirm_action_confirm_tv, true);
                    mActionDialog.setViewAlpha(R.id.confirm_action_confirm_tv, 1f);
                } else {
                    mActionDialog.setText(R.id.confirm_action_confirm_tv, string + "（" + count + "）");
                    mActionDialog.setViewEnabled(R.id.confirm_action_confirm_tv, false);
                    mActionDialog.setViewAlpha(R.id.confirm_action_confirm_tv, 0.5f);
                }
            }
        });
        mActionDialog.setText(R.id.confirm_action_confirm_tv, ResourceUtils.getString(R.string.confirm));
    }

    public void showCancelButton(boolean isShow) {
        mActionDialog.setViewVisibility(R.id.download_dlg_bottom_ly_canter_view, isShow ? View.VISIBLE : View.GONE);
        mActionDialog.setViewVisibility(R.id.confirm_action_cancel_tv, isShow ? View.VISIBLE : View.GONE);
    }

    public void actionDialogDismiss() {
        if (mActionDialog != null && mActionDialog.isShowing())
            mActionDialog.dismiss();
    }

    public boolean isShowing() {
        return mActionDialog != null && mActionDialog.isShowing();
    }

    public void showWithTitle(boolean isShowCancelBtn, String title, String message, String secondMessage, String confirmText,
                              ActionDialogListener calback, ActionDialogCancelListener cancelCallback) {
        mListener = calback;
        mCancelListener = cancelCallback;
        mActionDialog.setOnclickListener(R.id.confirm_action_confirm_tv, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionDialogDismiss();
                if (mListener != null) mListener.onConfirmCallback(0);
            }
        });
        mActionDialog.setText(R.id.confirm_action_confirm_tv, confirmText);
        mActionDialog.setOnclickListener(R.id.confirm_action_cancel_tv, v -> {
            mActionDialog.dismiss();
            if (mCancelListener != null) mCancelListener.onCancelCallback();
        });
        showCancelButton(isShowCancelBtn);
        if (secondMessage == null) {
            mActionDialog.setViewVisibility(R.id.confirm_action_second_tv, View.GONE);
        } else {
            mActionDialog.setText(R.id.confirm_action_second_tv, secondMessage);
            mActionDialog.setViewVisibility(R.id.confirm_action_second_tv, View.VISIBLE);
        }
        mActionDialog.setText(R.id.confirm_action_titile, title);
        mActionDialog.setViewVisibility(R.id.confirm_action_tv, View.VISIBLE);
        mActionDialog.setText(R.id.confirm_action_tv, message);
        mActionDialog.setText(R.id.confirm_action_confirm_tv, ResourceUtils.getString(R.string.confirm));
        mActionDialog.setTextColor(R.id.confirm_action_confirm_tv, ResourceUtils.getColor(R.color.color_5098e4));

        if (!mActionDialog.isShowing()) {
            mActionDialog.show();
        }
    }

    public interface ActionDialogListener {
        void onConfirmCallback(int action);
    }

    public interface ActionDialogCancelListener {
        void onCancelCallback();
    }

}
