package com.feipai.flypai.mvp.presenters.activitypresenters;

import android.os.Message;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.beans.ABCmdValue;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.mvp.contract.activitycontract.CameraActivityContract;
import com.feipai.flypai.mvp.contract.activitycontract.CameraActivityContract.View;
import com.feipai.flypai.utils.global.HandlerUtils;

public class CameraActivityPresenter implements CameraActivityContract.Presenter, BasePresenter<View>
        , HandlerUtils.OnReceiveMessageListener {
    private View mView;

    private HandlerUtils.HandlerHolder handlerHolder;

    @Override
    public void attachView(View view) {
        this.mView = view;
        handlerHolder = new HandlerUtils.HandlerHolder(this);
    }

    @Override
    public void detachView() {
    }


    @Override
    public void sendMessageUpdateUi(int what, Object obj) {
        Message msg = new Message();
        msg.what = what;
        if (obj != null) msg.obj = obj;
        handlerHolder.sendMessage(msg);
    }

    @Override
    public void handlerMessage(Message msg) {
    }

    @Override
    public void startCameraSuccess() {

    }

    @Override
    public void readCameraData(ABCmdValue cb) {
        switch (cb.getMsg_id()) {

        }
    }

    public void handlerEvent(RxbusBean msg) {
        switch (msg.TAG) {
            case ConstantFields.BusEventType.FIND_PLANE:

                break;
        }
    }
}
