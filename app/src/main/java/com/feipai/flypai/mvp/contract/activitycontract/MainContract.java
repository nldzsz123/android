package com.feipai.flypai.mvp.contract.activitycontract;

import com.feipai.flypai.beans.ABCmdValue;
import com.feipai.flypai.beans.FileBean;
import com.feipai.flypai.beans.PlaneVersionBean;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.utils.CameraCommand;


/**
 * MainActivity总管理
 */
public class MainContract {
    // 日志Tag
    public String TAG = getClass().getSimpleName();

    public interface View extends BaseView {
        /**
         * 获取当前页面
         */
        int getBottomBarIndex();

        /**
         * 线程切换
         */
        void sendHandlerMessage(int what, Object object);

        /**
         * 切换底部button
         */
        void switchBottomBar(int tab);

        /**
         * 更新版本号
         */
        void updateVersion();

        /**
         * 连接相机datasocket
         */
        void connectCameraDataTcp();

        /**
         * 准备上传文件到相机
         */
        void sendFilePrepare();

        /**
         * 开始上传文件到相机
         */
        void sendFileStart();

        /**
         * 完成上传文件到相机
         */
        void sendFileComplete();


        /**
         * 更新飞控上传进度
         */
        void uploadPlaneFw(int porgress);

        /**
         * 更新云台上传进度
         */
        void uploadYuntaiFw(int porgress);


        /**
         * 获取cmd命令
         */
        CameraCommand getCmd();

        /**
         * 获取版本号对象
         */
        PlaneVersionBean getVersionBean();


    }

    public interface Presenter extends BasePresenter<View> {

        /**
         * 从服务器获取版本号
         */
        boolean getVersionFromServier();


        /**
         * 处理由Fragment发来的RXbus消息
         */
        void handlerEvent(RxbusBean event);


//        void readCameraMainSocket(ABCmdValue cb);

        void readCameraDataSocket(byte[] buffer);

        /**
         * Plane固件检测
         *
         * @param type 飞控固件检测，云台固件检测，飞控升级
         */
        void checkPlaneFwVersion(int type);

        /**
         * 初始化飞控升级
         */
        void initUpdateFirm();

        void resetUpdateFirm();

        /**
         * 上传固件到飞机
         *
         * @param type 云台 or 飞控
         */
        void updateFwToPlane(int type);

        /**
         * 从服务器请求所有的版本号
         */
        void requestAllVersionFromServer();


        /**
         * 准备上传文件到相机
         */
        void sendFilePrepare();

        /**
         * 请求服务器激活状态
         * */
        void requestAckForServer();

    }
}
