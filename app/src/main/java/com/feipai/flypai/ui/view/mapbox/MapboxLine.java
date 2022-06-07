package com.feipai.flypai.ui.view.mapbox;

import android.support.annotation.NonNull;

import com.feipai.flypai.R;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.LINE_LAYER_TAG;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.LOCATION_MARKER;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.MARKER_LAYER_TAG;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.PLANE_ICON;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillAntialias;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineDasharray;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class MapboxLine {
    private MapboxMap mapboxMap;
    private String lineTag;
    private GeoJsonSource source;
    private FeatureCollection featureCollection;
    private final List<Feature> featureList = new ArrayList<>();
    private LineLayer lineLayer;
    private List<Point> linePoints = new ArrayList<>();//存放线段的两点


    public MapboxLine(MapboxMap mapboxMap, String lineTag) {
        this.mapboxMap = mapboxMap;
        this.lineTag = lineTag;
        mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                setupSource(style, lineTag);
                setupLineLayer(style, lineTag);
            }
        });

    }

    public String getLineTag() {
        return lineTag;
    }

    /**
     * 将GeoJSON源添加到地图
     */
    private void setupSource(@NonNull Style loadedStyle, String lineTag) {

        featureCollection = FeatureCollection.fromFeatures(featureList);
        source = new GeoJsonSource(lineTag, featureCollection);
        loadedStyle.addSource(source);
    }

    private void setupLineLayer(Style style, String lineTag) {
        lineLayer = new LineLayer(LINE_LAYER_TAG + lineTag, lineTag);
        lineLayer.withProperties(
                fillAntialias(true),
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND)
        );
        if (style.getLayer(MARKER_LAYER_TAG + PLANE_ICON) != null)
            style.addLayerBelow(lineLayer, MARKER_LAYER_TAG + PLANE_ICON);
    }

    /***
     * 线宽
     *
     */
    public MapboxLine setLineWidth(float width) {
        lineLayer.setProperties(PropertyFactory.lineWidth(width));
        return this;
    }

    /**
     * 线段颜色
     */
    public MapboxLine setLineColor(int lineColor) {
        lineLayer.setProperties(lineColor(ResourceUtils.getColor(lineColor)));
        return this;
    }

    /**
     * 设置线段为虚线
     * 可不做设置
     */
    public MapboxLine setLineDotted(boolean isDotted) {
        lineLayer.setProperties(PropertyFactory.lineDasharray(isDotted ? new Float[]{1.2f, 2f} : null));
        setLineColor(isDotted ? R.color.color_f34235 : R.color.color_4097e1);
        return this;
    }

    public void createLine(LatLng oldLatlng, LatLng newLatlng) {
        featureList.clear();
        if (linePoints.size() > 0) linePoints.clear();
        linePoints.add(Point.fromLngLat(oldLatlng.getLongitude(), oldLatlng.getLatitude()));
        linePoints.add(Point.fromLngLat(newLatlng.getLongitude(), newLatlng.getLatitude()));
        LineString lineString = LineString.fromLngLats(linePoints);
        Feature feature = Feature.fromGeometry(lineString);
        featureList.add(feature);
        featureCollection = FeatureCollection.fromFeatures(featureList);
        refreshGeoJson(featureCollection);
    }

    /**
     * 刷新数据源
     */
    public void refreshGeoJson(FeatureCollection featureCollection) {
        this.featureCollection = featureCollection;
        source.setGeoJson(featureCollection);
    }

    public void removeByTag(String lineTag) {
        mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                if (style.getSourceAs(lineTag) != null)
                    style.removeSource(lineTag);
                if (style.getLayer(LINE_LAYER_TAG + lineTag) != null)
                    style.removeLayer(LINE_LAYER_TAG + lineTag);
            }
        });

    }


}
