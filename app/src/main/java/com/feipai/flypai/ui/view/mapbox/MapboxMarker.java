package com.feipai.flypai.ui.view.mapbox;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.StringUtils;
import com.feipai.flypai.utils.global.Utils;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.BubbleLayout;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.PropertyValue;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.CALLOUT_LAYER_TAG;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.IMAGE_TAG;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.LAYER_TAG;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.LOCATION_MARKER;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.MARKER_FILTER_LATLNG;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.MARKER_LAYER_TAG;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.PLANE_ICON;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.PROPERTY_NAME;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.PROPERTY_SELECTED;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.WAYPOINT_MARRKER;
import static com.mapbox.mapboxsdk.style.expressions.Expression.all;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM_LEFT;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_MAP;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconRotate;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;

public class MapboxMarker {

    private static final String PROPERTY_CAPITAL = "capital";

    private MapboxMap mapboxMap;
    private String markerTag;
    private GeoJsonSource source;
    private FeatureCollection featureCollection;
    private String name = "";
    private boolean isNeedWindowShow;
    private boolean isBinding;

    private List<Feature> featureList = new ArrayList<>();
    private final HashMap<String, Bitmap> imageMap = new HashMap<>();
    private final HashMap<String, View> viewMap = new HashMap<>();

    private SymbolLayer markerLayer;
    private MapboxLatLng latLng;
    private ValueAnimator markerMoveAnimator;
    private float markerYaw = -1;
    private MarkerMoveAnimatorCallback mMarkerMoveAnimatorCallback;

    public MapboxMarker(MapboxMap mapboxMap, String markerTag, boolean isBinding) {
        this.isBinding = isBinding;
        this.mapboxMap = mapboxMap;
        this.markerTag = markerTag;
        mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                setupSource(style, markerTag);
                setUpMarkerLayer(style, markerTag);
                if (isBinding)
                    setUpInfoWindowLayer(style, markerTag);
            }
        });

    }

    /**
     * 将GeoJSON源添加到地图
     */
    private void setupSource(@NonNull Style loadedStyle, String markerTag) {
        featureCollection = FeatureCollection.fromFeatures(featureList);
        source = new GeoJsonSource(markerTag, featureCollection);
        loadedStyle.addSource(source);
    }

    /**
     * 将标记图像添加到地图中，以用作SymbolLayer图标
     */
    public MapboxMarker bindIcon(int resId) {
        if (mapboxMap.getStyle() != null && mapboxMap.getStyle().isFullyLoaded())
            mapboxMap.getStyle().addImage(IMAGE_TAG + markerTag, BitmapUtils.getBitmapFromDrawable(
                    ResourceUtils.getDrawabe(resId)));
        return this;
    }

    public MapboxMarker bindIcon(Bitmap bitmap) {
        if (mapboxMap.getStyle() != null && mapboxMap.getStyle().isFullyLoaded())
            mapboxMap.getStyle().addImage(IMAGE_TAG + markerTag, bitmap);
        return this;
    }

    public MapboxMarker setWindowName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public MapboxMarker setMarkerYaw(float yaw) {
        this.markerYaw = yaw;
        if (markerLayer != null) {
            if (markerYaw != -1) {//图标旋转
                markerLayer.setProperties(iconRotate(yaw));
            }
        }
        return this;
    }

    /**
     * 设置带有mark图标的图层
     * 如果带有info window则统一使用同一个marker tag，否则单独使用一个marker tag
     */
    private void setUpMarkerLayer(@NonNull Style loadedStyle, String markerTag) {
        markerLayer = new SymbolLayer(MARKER_LAYER_TAG + markerTag, markerTag)
                .withProperties(
                        iconImage(IMAGE_TAG + markerTag),
                        iconAllowOverlap(true),//是否可以重叠
                        iconOffset(markerTag.contains(WAYPOINT_MARRKER) ? new Float[]{0f, -8f} : new Float[]{0f, -0f})
                );
        loadedStyle.addLayer(markerLayer);
    }

    /**
     * 使用Android SDK标注设置图层
     * <p>
     * Feature name用作iconImage的键
     * </ p>
     */
    private void setUpInfoWindowLayer(@NonNull Style loadedStyle, String markerTag) {
        loadedStyle.addLayer(new SymbolLayer(CALLOUT_LAYER_TAG + markerTag, markerTag)
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
                /* 添加过滤器以仅在选定要素属性为true时显示 */
                .withFilter(eq((get(PROPERTY_SELECTED)), literal(true))));

    }

    public void updateWindowInfoName(String name) {
        if (isBindInfoWindow() && featureList != null && !featureList.isEmpty()) {
            View view = viewMap.get(this.name);
            this.name = name;
            viewMap.clear();
            viewMap.put(name, view);
            Feature feature = featureList.get(0);
            feature.addStringProperty(PROPERTY_NAME, name);
            feature.addBooleanProperty(PROPERTY_SELECTED, false);
            refreshGeoJson(featureCollection);
        }
    }

    /**
     * 绑定经纬度
     * 刷新数据显示
     */
    public void createMarker(MapboxLatLng latLng) {
        this.latLng = latLng;
        featureList.clear();
        Feature feature = Feature.fromGeometry(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()));
        if (isBindInfoWindow()) {
            feature.addBooleanProperty(PROPERTY_SELECTED, false);
            if (name != null) {
                feature.addStringProperty(PROPERTY_NAME, name);
                imageMap.put(name, generateBitmap(feature));
                //用addImages更快，因为每个位图分别调用addImage。
                if (mapboxMap.getStyle() != null && mapboxMap.getStyle().isFullyLoaded())
                    mapboxMap.getStyle().addImages(imageMap);

            }
        }
        featureList.add(feature);
        //重新赋值featureCollection
        featureCollection = FeatureCollection.fromFeatures(featureList);
        //更新source
        refreshGeoJson(featureCollection);
    }


    public void refreshGeoJson(FeatureCollection featureCollection) {
        this.featureCollection = featureCollection;
        List<Feature> features = featureCollection.features();
        if (!features.isEmpty()) {
            latLng = MapBoxUtils.convertToLatLng(features.get(0));
        }
        source.setGeoJson(featureCollection);
    }

    /**
     * 更新位置
     *
     * @param lat
     * @param lng
     */
    public void updatePosition(double lat, double lng) {
        featureList.clear();
        featureList.add(Feature.fromGeometry(Point.fromLngLat(lng, lat)));
        featureCollection = FeatureCollection.fromFeatures(featureList);
        refreshGeoJson(featureCollection);
    }

    public void setRotateAngle(float yaw) {
        mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                if (style != null && style.isFullyLoaded()) {
                    SymbolLayer iconSymbolLayer = style.getLayerAs(MARKER_LAYER_TAG + markerTag);
                    if (iconSymbolLayer != null) {
                        iconSymbolLayer.setProperties(
                                iconRotate(yaw)
                        );
                    }
                }
            }
        });

    }

    public float getRotateAngle() {
        Style style = mapboxMap.getStyle();
        if (style != null && style.isFullyLoaded()) {
            SymbolLayer iconSymbolLayer = style.getLayerAs(MARKER_LAYER_TAG + markerTag);
            if (iconSymbolLayer != null) {
                PropertyValue<Float> iconRotate = iconSymbolLayer.getIconRotate();
                if (iconRotate != null && !iconRotate.isNull())
                    return iconRotate.getValue();
            }
        }
        return -1;
    }


    public void removeByTag(String markerTag) {
        mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                if (style.getSourceAs(markerTag) != null)
                    style.removeSource(markerTag);
                if (style.getLayer(MARKER_LAYER_TAG) != null)
                    style.removeLayer(MARKER_LAYER_TAG);
                if (style.getImage(IMAGE_TAG + markerTag) != null)
                    style.removeImage(IMAGE_TAG + markerTag);
                if (style.getLayer(MARKER_LAYER_TAG + markerTag) != null)
                    style.removeLayer(MARKER_LAYER_TAG + markerTag);
                if (style.getLayer(CALLOUT_LAYER_TAG + markerTag) != null)
                    style.removeLayer(CALLOUT_LAYER_TAG + markerTag);
            }
        });

        stopMarkerMoveAnimator();

    }

    public SymbolLayer getMarkerLayer() {
        return markerLayer;
    }

    public List<Feature> getMarkerFeature() {
        return featureCollection.features();
    }

    public FeatureCollection getFeatureCollection() {
        return featureCollection;
    }

    public MapboxLatLng getLatLng() {
        List<Feature> features = getMarkerFeature();
        if (!features.isEmpty()) {
            return MapBoxUtils.convertToLatLng(features.get(0));
        }
        return null;
    }

    public String getMarkerTag() {
        return markerTag;
    }

    public boolean isWindowInfoShow() {
        if (!isBindInfoWindow())
            return false;
        for (Feature feature : featureList) {
            if (feature.getBooleanProperty(PROPERTY_SELECTED)) {
                return true;
            }
        }
        return false;
    }

    public View getView(Feature feature) {
        return viewMap.get(feature.getStringProperty(PROPERTY_NAME));
    }

    public boolean isBindInfoWindow() {
        return isBinding;
    }

    /**
     * 自定义View转Bitmap
     *
     * @param singleFeature Feature
     * @return Bitmap
     */
    private Bitmap generateBitmap(Feature singleFeature) {
        BubbleLayout bubbleLayout = (BubbleLayout)
                LayoutInflater.from(Utils.context).inflate(R.layout.map_info_window_layout_mapbox, null);

        TextView textView = bubbleLayout.findViewById(R.id.mapbox_inforwindow_text);
        textView.setText(singleFeature.getStringProperty(PROPERTY_NAME));
        bubbleLayout.findViewById(R.id.mapbox_inforwindow_btn).setVisibility(
                markerTag.equals(LOCATION_MARKER) ? View.GONE : View.VISIBLE);

        int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        bubbleLayout.measure(measureSpec, measureSpec);

        float measuredWidth = bubbleLayout.getMeasuredWidth();

        bubbleLayout.setArrowPosition(measuredWidth / 2 - 5);
        viewMap.put(name, bubbleLayout);
        return MapBoxUtils.generate(bubbleLayout);
    }


    public void moveMarkerWithWindow(MapboxLatLng newLngLat, String name, boolean isWindowShow) {
        this.latLng = newLngLat;
        this.isNeedWindowShow = isWindowShow;
        this.name = name;
        featureList.clear();
        Feature feature = Feature.fromGeometry(Point.fromLngLat(newLngLat.getLongitude(), newLngLat.getLatitude()));
        if (isBindInfoWindow()) {
            feature.addBooleanProperty(PROPERTY_SELECTED, isWindowShow);
            if (name != null) {
                feature.addStringProperty(PROPERTY_NAME, name);
                Bitmap bitmap = imageMap.get(name);
                if (bitmap != null) {
                    bitmap.recycle();
                    bitmap = null;
                }
                imageMap.clear();
                imageMap.put(name, generateBitmap(feature));
                //用addImages更快，因为每个位图分别调用addImage。
                if (mapboxMap.getStyle() != null)
                    mapboxMap.getStyle().addImages(imageMap);
            }
        }
        featureList.add(feature);
        //重新赋值featureCollection
        featureCollection = FeatureCollection.fromFeatures(featureList);
        //更新source
        refreshGeoJson(featureCollection);
    }


    /**
     * 控制marker平滑移动
     */
    public void startMarkerMoveAnimator(MapboxLatLng newLngLat, String name, boolean isWindowShow, MarkerMoveAnimatorCallback callback) {
        this.name = name;
        this.isNeedWindowShow = isWindowShow;
        this.mMarkerMoveAnimatorCallback = callback;
        if (markerMoveAnimator != null && markerMoveAnimator.isStarted()) {
            latLng = (MapboxLatLng) markerMoveAnimator.getAnimatedValue();
            markerMoveAnimator.cancel();
        }
        markerMoveAnimator = ObjectAnimator
                .ofObject(latLngEvaluator, latLng, newLngLat)
                .setDuration(1000);
        markerMoveAnimator.addUpdateListener(animatorUpdateListener);
        markerMoveAnimator.start();
    }

    private ValueAnimator.AnimatorUpdateListener animatorUpdateListener =
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    if (mapboxMap.getStyle() == null) {
                        return;
                    }
                    MapboxLatLng animatedPosition = (MapboxLatLng) valueAnimator.getAnimatedValue();
                    if (mMarkerMoveAnimatorCallback != null)
                        mMarkerMoveAnimatorCallback.animatorListener(animatedPosition, isNeedWindowShow);
                    moveMarkerWithWindow(animatedPosition, name, isNeedWindowShow);
                }
            };


    private TypeEvaluator<MapboxLatLng> latLngEvaluator = new TypeEvaluator<MapboxLatLng>() {

        private final MapboxLatLng latLng = new MapboxLatLng();

        @Override
        public MapboxLatLng evaluate(float fraction, MapboxLatLng startValue, MapboxLatLng endValue) {
            latLng.setLatitude(startValue.getLatitude()
                    + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude()
                    + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    };

    public void stopMarkerMoveAnimator() {
        if (markerMoveAnimator != null) {
            markerMoveAnimator.removeUpdateListener(animatorUpdateListener);
            markerMoveAnimator.cancel();
        }
    }

    public interface MarkerMoveAnimatorCallback {
        void animatorListener(MapboxLatLng animatedPosition, boolean showInfoWindow);
    }


}
