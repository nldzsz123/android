package com.feipai.flypai.mvp.contract.activitycontract;

import android.widget.EditText;

import com.feipai.flypai.beans.ABCmdValue;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.utils.CameraCommand;

public class CameraActivityContract {
    // 日志Tag
    public String TAG = getClass().getSimpleName();

    public interface View extends BaseView {
    }

    public interface Presenter extends BasePresenter<View> {

        void sendMessageUpdateUi(int what, Object obj);

        void startCameraSuccess();

        void readCameraData(ABCmdValue cb);

        void handlerEvent(RxbusBean msg);


    }
}
