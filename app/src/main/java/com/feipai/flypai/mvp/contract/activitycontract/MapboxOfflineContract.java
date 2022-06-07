package com.feipai.flypai.mvp.contract.activitycontract;

import com.feipai.flypai.beans.PlaneVersionBean;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.utils.CameraCommand;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;


/**
 * MapboxOfflineActivity总管理
 */
public class MapboxOfflineContract {
    // 日志Tag
    public String TAG = getClass().getSimpleName();

    public interface View extends BaseView {

        OfflineRegion getOfflineRegion();

        void setOfflineRegion(OfflineRegion region);

        void showToast(String text);

        void updateProgress(int percentage);

        void downloadSuccess();

        void setRegionSelected(int regionSelectedId);

        int getRegionSelected();

        void onRegionDeleteSuccess();

        void onRegionDeletedError(String error);

        void showProgressBar(boolean isShow);
    }

    public interface Presenter extends BasePresenter<View> {

        /**
         * 下载离线地图
         */
        void downloadOfflineMap(MapboxMap map, OfflineManager offlineManager, String name);

        /**
         * 获取已下载地图列表
         */
        void downloadedMapList(MapboxMap map,OfflineManager offlineManager);


    }
}
