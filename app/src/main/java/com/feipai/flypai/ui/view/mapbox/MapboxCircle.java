package com.feipai.flypai.ui.view.mapbox;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfMeta;

import java.util.ArrayList;
import java.util.List;

import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.CALLOUT_LAYER_TAG;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.FILL_LAYER_TAG;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.HIDDEN_RANGE_CIRCLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;

public class MapboxCircle {
    private MapboxMap mapboxMap;
    private String circleTag;

    private GeoJsonSource source;
    private FeatureCollection featureCollection;
    private final List<Feature> featureList = new ArrayList<>();


    public MapboxCircle(MapboxMap mapboxMap, String circleTag, int fillColor) {
        this.mapboxMap = mapboxMap;
        this.circleTag = circleTag;
        mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                setupSource(style, circleTag);
                setupFillColor(style, circleTag, fillColor);
            }
        });

    }


    public void setupSource(@NonNull Style loadedStyle, String circleTag) {
        featureCollection = FeatureCollection.fromFeatures(featureList);
        source = new GeoJsonSource(circleTag, featureCollection);
        loadedStyle.addSource(source);
    }

    public void setupFillColor(Style style, String circleTag, int color) {
        FillLayer fillLayer = new FillLayer(FILL_LAYER_TAG + circleTag, circleTag);
        fillLayer.setProperties(
                fillColor(ResourceUtils.getColor(color)));
        style.addLayer(fillLayer);
    }

    public MapboxCircle createCircle(LatLng centerLatLng, @NonNull double radius) {
        featureList.clear();
        Polygon outerCircle = MapBoxUtils.getTurfPolygon(radius, centerLatLng);
        Feature feature = Feature.fromGeometry(Polygon.fromOuterInner(
                LineString.fromLngLats(TurfMeta.coordAll(outerCircle, false))));
        featureList.add(feature);
        featureCollection = FeatureCollection.fromFeatures(featureList);
        refreshGeoJson(featureCollection);
        return this;

    }


    public MapboxCircle createRing(LatLng centerLatLng, @NonNull double radius, float ringWide) {
        if (radius <= .1)
            return null;
        featureList.clear();
        //外圆
        Polygon outerCirclePolygon = MapBoxUtils.getTurfPolygon(radius, centerLatLng);
        //内圆
        LogUtils.d("半径===>外圆=" + radius + "，内圆=" + (radius - .1));
        Polygon innerCirclePolygon = MapBoxUtils.getTurfPolygon(
                radius - ringWide, centerLatLng);
        Feature feature = Feature.fromGeometry(Polygon.fromOuterInner(
                LineString.fromLngLats(TurfMeta.coordAll(outerCirclePolygon, false)),
                LineString.fromLngLats(TurfMeta.coordAll(innerCirclePolygon, false))
        ));
        featureList.add(feature);
        featureCollection = FeatureCollection.fromFeatures(featureList);
        refreshGeoJson(featureCollection);
        return this;
    }

    public MapboxCircle updateRing(GeoJsonSource outerCircleSource, LatLng centerLatLng, @NonNull double radius, float ringWide) {
        if (radius <= ringWide)
            return null;
        featureList.clear();
        //外圆
        Polygon outerCirclePolygon = MapBoxUtils.getTurfPolygon(radius, centerLatLng);
        //内圆
        Polygon innerCirclePolygon = MapBoxUtils.getTurfPolygon(
                radius - ringWide, centerLatLng);
        if (outerCircleSource != null) {
            LogUtils.d("更新圆形半径====>" + radius);
            // Use the two Polygon objects above to create the final Polygon that visually represents the ring.
            outerCircleSource.setGeoJson(Polygon.fromOuterInner(
                    // Create outer LineString
                    LineString.fromLngLats(TurfMeta.coordAll(outerCirclePolygon, false)),
                    // Create inter LineString
                    LineString.fromLngLats(TurfMeta.coordAll(innerCirclePolygon, false))
            ));
        }
//        setupSource(loadedStyle, circleTag);
        return this;
    }


    public void removeByTag(String circleTag) {
        mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                if (style.getSourceAs(circleTag) != null)
                    style.removeSource(circleTag);
                if (style.getLayer(FILL_LAYER_TAG + circleTag) != null)
                    style.removeLayer(FILL_LAYER_TAG + circleTag);
            }
        });

    }

    /**
     * 刷新数据源
     */
    public void refreshGeoJson(FeatureCollection featureCollection) {
        this.featureCollection = featureCollection;
        source.setGeoJson(featureCollection);
    }

    public boolean contains(MapboxLatLng latLng) {
        if (mapboxMap.getStyle().getLayer(FILL_LAYER_TAG + HIDDEN_RANGE_CIRCLE) != null) {
            LogUtils.d("圆圈已绘制====");
        }
        PointF pointF = mapboxMap.getProjection().toScreenLocation(latLng);
        List<Feature> features = mapboxMap.queryRenderedFeatures(pointF, FILL_LAYER_TAG + HIDDEN_RANGE_CIRCLE);
        return !features.isEmpty();
    }

}
