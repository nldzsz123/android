package com.feipai.flypai.ui.view.mapbox;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.Utils;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.BubbleLayout;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.CALLOUT_LAYER_TAG;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.INFO_WINDOW_FOR_LOCATION_TAG;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.PROPERTY_NAME;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.PROPERTY_SELECTED;
import static com.mapbox.mapboxsdk.location.LocationComponentConstants.FOREGROUND_LAYER;
import static com.mapbox.mapboxsdk.style.expressions.Expression.e;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

/***
 * tag:INFO_WINDOW_FOR_LOCATION_TAG
 */
public class MapboxInfoWindowForPhone {

    private final static String PHONE_INFO_WINDOW_TAG = "PHONE_INFO_WINDOW_TAG";
    private final static String PHONE_LOCA = "PHONE_LOCA";

    private MapboxMap mapboxMap;
    private Style loadedStyle;
    private String markerTag;
    private String text;
    private List<Feature> featureList = new ArrayList<>();
    private final HashMap<String, Bitmap> infoWindowMap = new HashMap<>();
    private final HashMap<String, View> viewMap = new HashMap<>();
    private GeoJsonSource source;
    private FeatureCollection featureCollection;

    public MapboxInfoWindowForPhone(MapboxMap mapboxMap, Style style, String markerTag) {
        this.mapboxMap = mapboxMap;
        this.loadedStyle = style;
        this.markerTag = markerTag;
//        setupSource(loadedStyle, this.markerTag);
        setUpInfoWindowLayer(loadedStyle, markerTag);
    }

    private void setupSource(@NonNull Style loadedStyle, String markerTag) {
        featureCollection = FeatureCollection.fromFeatures(featureList);
        source = new GeoJsonSource(markerTag, featureCollection);
        loadedStyle.addSource(source);
    }

    private void setUpInfoWindowLayer(@NonNull Style loadedStyle, String markerTag) {
        loadedStyle.addLayerAbove(new SymbolLayer(INFO_WINDOW_FOR_LOCATION_TAG, markerTag)
                .withProperties(
                        /* 根据名称要素属性的值显示具有ID标题的图像 */
                        iconImage("{" + PROPERTY_NAME + "}"),

                        /* 将图标的锚点设置为左下 */
                        iconAnchor(ICON_ANCHOR_BOTTOM),

                        /* 所有信息窗口和标记图像同时显示*/
                        iconAllowOverlap(true),

                        /* 将信息窗口偏移到标记上方*/
                        iconOffset(new Float[]{-2f, -25f})
                )
                /* 添加过滤器以仅在选定要素属性为隐藏 */
                .withFilter(eq((get(PROPERTY_SELECTED)), literal(true))), FOREGROUND_LAYER);
    }


    public void updataPhoneInfoWindow(MapboxLatLng tagLatLng, String text) {
        featureList.clear();
        Feature feature = Feature.fromGeometry(Point.fromLngLat(tagLatLng.getLongitude(), tagLatLng.getLatitude()));
        LogUtils.d("定位====》" + text);
//        if (text == null) {
//            feature.addBooleanProperty(PROPERTY_SELECTED, false);
//        } else {
        feature.addBooleanProperty(PROPERTY_SELECTED, true);
        feature.addStringProperty(PROPERTY_NAME, text);
//        }
        featureList.add(feature);
        infoWindowMap.put(PHONE_INFO_WINDOW_TAG, generateBitmap(feature));
        //用addImages更快，因为每个位图分别调用addImage。
        if (loadedStyle != null && loadedStyle.isFullyLoaded())
            loadedStyle.addImages(infoWindowMap);
        //重新赋值featureCollection
        featureCollection = FeatureCollection.fromFeatures(featureList);
        //更新source
        refreshGeoJson(featureCollection);
    }

    public void refreshGeoJson(FeatureCollection featureCollection) {
        if (loadedStyle == null || !loadedStyle.isFullyLoaded())
            return;
        source = loadedStyle.getSourceAs(markerTag);
        if (source != null) {
            LogUtils.d("source刷新" + markerTag);
            source.setGeoJson(featureCollection);
        } else {
            LogUtils.d("source未找到" + markerTag);
        }
    }

    private Bitmap generateBitmap(Feature singleFeature) {
        BubbleLayout bubbleLayout = (BubbleLayout)
                LayoutInflater.from(Utils.context).inflate(R.layout.map_info_window_layout_mapbox, null);

        TextView textView = bubbleLayout.findViewById(R.id.mapbox_inforwindow_text);
        textView.setText(singleFeature.getStringProperty(PROPERTY_NAME));
        ImageView imageView = bubbleLayout.findViewById(R.id.mapbox_inforwindow_btn);
        imageView.setVisibility(View.GONE);

        int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        bubbleLayout.measure(measureSpec, measureSpec);

        float measuredWidth = bubbleLayout.getMeasuredWidth();

        bubbleLayout.setArrowPosition(measuredWidth / 2 - 5);
        viewMap.put(PHONE_INFO_WINDOW_TAG, bubbleLayout);
        return MapBoxUtils.generate(bubbleLayout);
    }
}
