package com.feipai.flypai.api;

import com.amap.api.maps.model.Marker;

public interface MapChangeListener {
    void onRemoveMakrerFromUser(Marker marker);

    void onAroundChange(double aroundRadius);

    /**
     * 定位结果回调
     *
     * @param isSuccess 成功与否
     */
    void onLocationResult(boolean isSuccess);
}
