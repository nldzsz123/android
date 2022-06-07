package com.feipai.flypai.mvp.contract.fragcontract;

import com.feipai.flypai.beans.PlaneInfo;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.utils.daoutils.DBClient;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.NetworkUtils;

public class RealNameRegistrationFragContract {
    // 日志Tag
    public String TAG = getClass().getSimpleName();

    public interface View extends BaseView {
        /**
         * 获取序列号成功，刷新
         */
        void refreshSerialNumb(String serialNumb);

        /**
         * 连接状态提醒
         */
        void isConnectedPrompt(int type, boolean isConnected);

    }

    public interface Presenter extends BasePresenter<View> {
        void handleEvent(RxbusBean bean);

        void initUpdateFirm(String ip);

    }
}
