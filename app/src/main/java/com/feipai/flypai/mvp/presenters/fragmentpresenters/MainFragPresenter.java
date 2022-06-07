package com.feipai.flypai.mvp.presenters.fragmentpresenters;

import android.content.Intent;
import android.net.wifi.ScanResult;

import com.feipai.flypai.R;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.connect.ConnectManager;
import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.mvp.contract.fragcontract.MainFragContract;
import com.feipai.flypai.ui.activity.CameraActivity;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.NetworkUtils;
import com.feipai.flypai.utils.global.ResourceUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.feipai.flypai.app.ConstantFields.BusEventType.*;

public class MainFragPresenter implements MainFragContract.Presenter, BasePresenter<MainFragContract.View> {

    private MainFragContract.View mView;

    @Inject
    public MainFragPresenter() {

    }


    @Override
    public void attachView(MainFragContract.View view) {
        this.mView = view;
    }

    @Override
    public void detachView() {

    }

    @Override
    public void handleEvent(RxbusBean bean) {
        switch (bean.TAG) {
            case WIFI_CONNTENTED:
                LogUtils.d("MainFragment连上飞机--》wifiConntentedObserveOn");
                if (!ConnectManager.getInstance().isConneted()) {
//                    mView.refreshConnectButtonStatus(3,ResourceUtils.getString(R.string.wifi_connectting),
//                            R.drawable.connect_plane_button_bg_def
//                            , R.color.color_4097e1);
                }
                break;
            case WIFI_BREAK_IN_BACKGRAOUND:
                LogUtils.d("MainFragment与飞机断开--》wifiBreakInBackgroundObserveOn");
//                mView.refreshConnectButtonStatus(4,ResourceUtils.getString(R.string.plane_unconnect), R.drawable.connect_plane_button_bg_def, R.color.color_4097e1);
                break;
            case WIFI_RSSI_CHANGE:
                LogUtils.d("MainFragment---》wifi信号强弱" + bean.object);
                break;
            case WIFI_OTHER_CONNECTED:
                LogUtils.d("MainrFragment与其他网络连接上--》otherWifiConnectedObserveOn");
                break;
            case WIFI_DISCONNECT:
                LogUtils.d("MainFragment无wifi网络连接--》wifiDisconnectObserveOn");
                break;
            case REFRESH_WIFI_DATA:
//                LogUtils.d("MainFragment开始刷新wifi列表");
                mView.refreshWifiLv();
                break;
            case CAMERA_SOCKET_CLOSE:
//                mView.refreshConnectButtonStatus(5,ResourceUtils.getString(NetworkUtils.getCurConnetWifiName().startsWith(ConstantFields.DATA_CONFIG.FLYPAI_NAME_START) ?
//                        R.string.wifi_connectting : R.string.plane_unconnect), R.drawable.connect_plane_button_bg_def, R.color.color_4097e1);
                break;
            case ACT_RESPONSE:
                LogUtils.d("激活状态===>" + (int) bean.object);
                mView.intoCamera();
                break;
        }
    }

    @Override
    public void connectButtonToCamera() {
        Intent intent = new Intent();
        intent.setClass(mView.getPageActivity(), CameraActivity.class);
        mView.startActivity(intent);

    }

    @Override
    public void searchWifi() {
        if (NetworkUtils.isGpsOpen()) {
            // TODO: 2019/4/24 GPS已打开
            if (!NetworkUtils.isWifiOpened()) {
                //打开wifi
                NetworkUtils.toggleWiFi(true);
            }
            mView.showWifiListDialog(getFlyPieWifiList());
        } else {
            // TODO: 2019/4/24 GPS未开启
            mView.showGotoSettingDialog(ResourceUtils.getString(R.string.prompt),
                    ResourceUtils.getString(R.string.turn_on_gps_before_search_wifi));
        }

    }

    @Override
    public List<ScanResult> getFlyPieWifiList() {
        List<ScanResult> mWifiList = NetworkUtils.getWifiList();
        List<ScanResult> mFlyPieWifi = new ArrayList<>();
        for (ScanResult result : mWifiList) {
            if (result.SSID.startsWith(ConstantFields.DATA_CONFIG.FLYPAI_NAME_START)) {
                mFlyPieWifi.add(result);
            }
        }
        return mFlyPieWifi;
    }
}
