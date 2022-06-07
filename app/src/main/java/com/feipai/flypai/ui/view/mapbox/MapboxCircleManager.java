package com.feipai.flypai.ui.view.mapbox;

import android.support.annotation.NonNull;

import com.feipai.flypai.R;
import com.feipai.flypai.utils.global.LogUtils;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfMeta;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.AROUND_CIRCLE;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.HIDDEN_RANGE_CIRCLE;

public class MapboxCircleManager {

    private MapboxMap mapboxMap;
    private Map<String, MapboxCircle> circles = new HashMap<>();
    private String circleTag;

    public MapboxCircleManager(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
    }

    /**
     * 添加环绕圆环
     *
     * @param centerLatLng 环绕中心点
     * @param radius       环绕半径
     * @param ringWide     圆环宽度
     */
    public void addAroundRingToMap(LatLng centerLatLng, @NonNull double radius, float ringWide) {
        mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                MapboxCircle circle = circles.get(AROUND_CIRCLE);
                if (isCircleAdded(AROUND_CIRCLE)) {
//            LogUtils.d("绘制圆圈====》" + radius);
                    GeoJsonSource outerCircleSource = style.getSourceAs(AROUND_CIRCLE);
                    circle.updateRing(outerCircleSource, centerLatLng, radius, ringWide);
                    if (circle != null)
                        circles.put(AROUND_CIRCLE, circle);
                } else {
//            LogUtils.d("绘制圆圈====》改变圆半径");
                    circle = new MapboxCircle(mapboxMap, AROUND_CIRCLE, R.color.color_00cf00_50);
                    circle.createRing(centerLatLng, radius, ringWide);
                    if (circle != null)
                        circles.put(AROUND_CIRCLE, circle);
                }
            }
        });

    }


    public MapboxCircle addHiddenRangeCircle(LatLng centerLatLng, @NonNull double radius) {
        MapboxCircle circle = circles.get(HIDDEN_RANGE_CIRCLE);
        if (circle != null && isCircleAdded(HIDDEN_RANGE_CIRCLE)) {
            circle.createCircle(centerLatLng, radius);
        } else {
            circle = new MapboxCircle(mapboxMap, HIDDEN_RANGE_CIRCLE, R.color.color_f34235_50);
            circle.createCircle(centerLatLng, radius);
        }
        circles.put(HIDDEN_RANGE_CIRCLE, circle);
        return circle;
    }

    public void removeHiddenCircle() {
        removeCircleByTagFormMap(HIDDEN_RANGE_CIRCLE);
    }


    public boolean isCircleAdded(String tag) {
        MapboxCircle circle = circles.get(tag);
        if (mapboxMap.getStyle() != null) {
            GeoJsonSource source = mapboxMap.getStyle().getSourceAs(tag);
            return circle != null && source != null;
        }
        return false;
    }

    /**
     * 从地图移除circle
     *
     * @param circleTag 唯一标识
     */
    public void removeCircleByTagFormMap(String circleTag) {
        for (Iterator<Map.Entry<String, MapboxCircle>> it = circles.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, MapboxCircle> item = it.next();
            if (circleTag.equals(item.getKey())) {
                item.getValue().removeByTag(circleTag);
                it.remove();
            }
        }
        mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                style.removeSource(circleTag);
            }
        });

    }


    public void changeMapStyle() {
        circles.clear();
    }
}
