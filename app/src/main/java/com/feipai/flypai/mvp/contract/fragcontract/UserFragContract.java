package com.feipai.flypai.mvp.contract.fragcontract;

import android.content.Context;

import com.feipai.flypai.beans.ABCmdValue;
import com.feipai.flypai.beans.PlaneVersionBean;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.utils.CameraCommand;


public interface UserFragContract {


    interface View extends BaseView {

        CameraCommand getCmd();

        void showDefDialog(String text);

        void dismissDefDialog();

        void notifyAdapter();

        void showActionDialog(int action);

        void dismissActionDialog();

        void dismissUpgradeDialog();

        void showUpgradeProgress(String type, int progress);

        /**
         * 固件安装成功
         */
        void upgradeSuccess();

        /**
         * 安装过程中SD卡出现问题
         */
        void upgradeSDCardError();

        /**
         * 固件安装失败
         *
         * @param errorCode 错误代码
         */
        void upgradeFailed(int errorCode);

        void initVersionBean(PlaneVersionBean bean);

        /**
         * @param requestCode 请求码，标识页
         */
        void startToActivity(int requestCode, int requestType);

        /**
         * 更新APP版本号以及激活状态
         *
         * @param ack 激活状态
         */
        void refreshAppAck(int ack);

        /**
         * 刷新安心计划时效
         */
        void refreshPeaceTime(String time);

        void showToast(String text);


    }

    interface Presenter {

        void handleEvent(RxbusBean bean);

        /**
         * 刷新版本号
         *
         * @param planeVersionBean 所有版本存储对象
         */
        void refreshVersion(PlaneVersionBean planeVersionBean);

        /**
         * 上传相机固件
         */
        void updateCameraFw(Context context, String path);

        /**
         * 上传固件到飞机
         */
        void updatePlaneFw(int type);

        /**
         * 刷新安新计划
         */
        void refreshPeaceStatus();

        void startUpdateCameraBin();

        void sendCameraBinComplete(ABCmdValue cb);
    }
}
