package com.feipai.flypai.mvp.contract.fragcontract;

import android.content.Intent;
import android.net.wifi.ScanResult;

import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.mvp.BaseView;

import java.util.List;


public interface MainFragContract {


    interface View extends BaseView {

        /**
         * 刷新wifi列表
         */
        void refreshWifiLv();

        /**
         * 刷新连接按钮的状态
         */
        void refreshConnectButtonStatus(int where,String text, int bgRes, int textColor);

        void intoCamera();

        /**
         * 显示wifi列表对话框
         */
        void showWifiListDialog(List<ScanResult> wifiList);

        /**
         * 显示普通对话框
         */
        void showGotoSettingDialog(String title, String content);

        /**
         * 区分当前连接状态
         */
        String getConnectButtonText();

        void startActivity(Intent intent);
    }

    interface Presenter {

        /**
         * 接受rxbus消息
         */
        void handleEvent(RxbusBean bean);

        /**
         * 搜索wifi
         */
        void searchWifi();

        /**
         * 进入相机按钮
         */
        void connectButtonToCamera();

        /**
         * 获取指定飞行器wifi列表
         */
        List<ScanResult> getFlyPieWifiList();


    }
}
