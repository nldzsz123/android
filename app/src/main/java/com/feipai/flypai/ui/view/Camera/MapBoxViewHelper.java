package com.feipai.flypai.ui.view.Camera;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;

import com.feipai.flypai.R;
import com.feipai.flypai.api.MapChangeListener;
import com.feipai.flypai.beans.UserBean;
import com.feipai.flypai.ui.view.mapbox.MapboxLatLng;
import com.feipai.flypai.ui.view.mapbox.MapboxMarker;
import com.feipai.flypai.ui.view.mapbox.MapboxMarkersManager;
import com.feipai.flypai.ui.view.mapbox.MapboxWayPointModel;
import com.feipai.flypai.utils.GPSUtils;
import com.feipai.flypai.utils.MLog;
import com.feipai.flypai.utils.daoutils.DBClient;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.ToastUtils;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.UiSettings;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.turf.TurfMeta;
import com.mapbox.turf.TurfTransformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.Looper.getMainLooper;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.AROUND_CIRCLE;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.AROUND_LINE;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.AROUND_MARKER;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.FIND_PLANE_LINE_FOR_PHONE;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.FIND_PLANE_LINE_FOR_TAKE_OFF;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.HIDDEN_RANGE_CIRCLE;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.HIDDEN_RANGE_FILL_CIRCLE;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.PLANE_ICON;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.PLANE_LINE;
import static com.feipai.flypai.ui.view.mapbox.MapBoxTag.TAKE_OFF;
import static com.mapbox.mapboxsdk.location.LocationComponentConstants.BACKGROUND_LAYER;
import static com.mapbox.mapboxsdk.location.LocationComponentConstants.FOREGROUND_LAYER;
import static com.mapbox.mapboxsdk.location.LocationComponentConstants.LOCATION_SOURCE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillAntialias;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineDasharray;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.turf.TurfConstants.UNIT_METERS;


/**
 * Mapbox与飞机属于同一坐标系，都是地理坐标系
 */
public class MapBoxViewHelper {

    //位置更新之间的距离
    public static final float DEFAULT_DISPLACEMENT = 0f;
    //位置更新的最大等待时间（以毫秒为单位）。
    public static final long DEFAULT_MAX_WAIT_TIME = 5000L;
    //位置更新的最快间隔（以毫秒为单位）
    public static final long DEFAULT_FASTEST_INTERVAL = 500L;
    //位置更新之间的默认间隔
    public static final long DEFAULT_INTERVAL = 500L;


    private List<Point> linePoints = new ArrayList<>();//存放线段的两点


    private Context mContext;
    private PermissionsManager permissionsManager;

    private MyMapBoxMap mMapView;
    private MapboxMap mBoxMap;
    private LocationComponent locationComponent;
    private boolean isMaploaded;
    private Map<String, SymbolLayer> markerList = new HashMap<>();//存储地图上所有的图标
    private Map<String, LineLayer> lineList = new HashMap<>();//存储地图上所有线段
    private LocationEngine locationEngine;
    private LocationEngineCallback<LocationEngineResult> mLocationEngineCallback;
    private MapboxMarkersManager markersManager;
    private MapboxWayPointModel mWaypointModel;

    private int mSolidWidth;//实线宽
    private int mSolidColor;//实线颜色
    private int mDottedWidth;//虛线宽
    private int mDottedColor;//虚线颜色
    private int mFindPlaneTakeOffLineColor;
    private int mLocationRes;//定位图标(手机位置)

    private boolean isFindMode;//找飞机模式

    private boolean isFirstLocation = true;
    private MapChangeListener listener;

    public MapBoxViewHelper(Context context, AttributeSet attrs, int defStyleAttr, MyMapBoxMap mapView, MapChangeListener listener) {
        this.mContext = context;
        Mapbox.getInstance(mContext, "access_token");
        initAttr(context, attrs, defStyleAttr);
        this.mMapView = mapView;
        mMapView.addOnWillStartLoadingMapListener(new MapView.OnWillStartLoadingMapListener() {
            @Override
            public void onWillStartLoadingMap() {
                LogUtils.d("地图====>开始添加");
            }
        });
        mMapView.addOnCameraWillChangeListener(new MapView.OnCameraWillChangeListener() {
            @Override
            public void onCameraWillChange(boolean animated) {
                LogUtils.d("地图====>发生改变" + animated);
            }
        });
        mapView.addOnDidFinishLoadingMapListener(new MapView.OnDidFinishLoadingMapListener() {
            @Override
            public void onDidFinishLoadingMap() {
                LogUtils.d("地图====>结束添加");
            }
        });
        this.listener = listener;
        mLocationRes = R.mipmap.phone_ponit_img;
    }

    private void initAttr(Context context, AttributeSet attrs, int defStyleAttr) {
        if (null != attrs || defStyleAttr != 0) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MapViewLy, defStyleAttr, 0);
            int count = ta.getIndexCount();
            for (int i = 0; i < count; ++i) {
                int index = ta.getIndex(i);
                if (index == R.styleable.MapViewLy_polyline_solid_width) {
                    mSolidWidth = ta.getInt(R.styleable.MapViewLy_polyline_solid_width, 4);
                } else if (index == R.styleable.MapViewLy_polyline_solid_color) {
                    mSolidColor = ta.getColor(R.styleable.MapViewLy_polyline_solid_color, ResourceUtils.getColor(R.color.color_4097e1));
                } else if (index == R.styleable.MapViewLy_polyline_dotted_width) {
                    mDottedWidth = ta.getInt(R.styleable.MapViewLy_polyline_dotted_width, 4);
                } else if (index == R.styleable.MapViewLy_polyline_dotted_color) {
                    mDottedColor = ta.getColor(R.styleable.MapViewLy_polyline_dotted_color, ResourceUtils.getColor(R.color.color_4097e1));
                } else if (index == R.styleable.MapViewLy_polyline_dotted_color) {
                    mFindPlaneTakeOffLineColor = ta.getColor(R.styleable.MapViewLy_polyline_find_plane_takeoff_color, ResourceUtils.getColor(R.color.color_4097e1));
                }
            }
            ta.recycle();
        }
    }

    public void setTouch(View.OnTouchListener tapDetector) {
        if (mMapView != null)
            mMapView.setOnTouchListener(tapDetector);
    }


    public void removeTouch() {
        if (mMapView != null)
            mMapView.setOnTouchListener(null);
    }

    public void initMapBoxMap(Bundle savedInstanceState, MapboxMap.OnMapClickListener mapClickListener, LocationEngineCallback<LocationEngineResult> locationEngineCallback, int type) {
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                //mapbox地图加载完成
                mBoxMap = mapboxMap;
                mBoxMap.setMinZoomPreference(8);
                mBoxMap.setMaxZoomPreference(22);
                mBoxMap.addOnMapClickListener(mapClickListener);
                updateBoxMapStyle(locationEngineCallback, type);
            }
        });
    }

    public Style getBoxMapStyle() {
        if (mBoxMap == null)
            return null;
        return mBoxMap.getStyle();
    }

    public void updateBoxMapStyle(LocationEngineCallback<LocationEngineResult> locationEngineCallback, int type) {
        String typeStr = Style.MAPBOX_STREETS;
        if (type == 1) {
            typeStr = Style.SATELLITE;
        }
        this.mLocationEngineCallback = locationEngineCallback;
        mBoxMap.setStyle(typeStr, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                isMaploaded = true;
                UiSettings uiSettings = mBoxMap.getUiSettings();
                uiSettings.setRotateGesturesEnabled(false);
                if (markersManager == null) {
                    markersManager = new MapboxMarkersManager(mBoxMap, listener);
                } else {
                    markersManager.changeMapStyle();
                }
                if (locationEngine != null)
                    locationEngine.removeLocationUpdates(mLocationEngineCallback);
//                else {
//                    markersManager.updateMapStyle(style);
//                }
                initLocationComponent(style, mLocationEngineCallback);
            }
        });
    }

    /**
     * 激活定位
     */
    @SuppressWarnings({"MissingPermission"})
    private void initLocationComponent(Style loadedMapStyle, LocationEngineCallback<LocationEngineResult> locationEngineCallback) {
        if (PermissionsManager.areLocationPermissionsGranted(mContext)) {
            locationComponent = mBoxMap.getLocationComponent();

//            LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(mContext)
//                    .elevation(0)
//                    .accuracyColor(Color.TRANSPARENT)
//                    .backgroundDrawable(R.drawable.transparent_icon)
//                    .foregroundDrawable(R.drawable.transparent_icon)
//                    .build();//用于隐藏原始的定位图标

            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(mContext, loadedMapStyle)
                            .useDefaultLocationEngine(false)
//                            .locationComponentOptions(customLocationComponentOptions)
                            .build();

            locationComponent.activateLocationComponent(locationComponentActivationOptions);

            locationComponent.setLocationComponentEnabled(false);
            locationComponent.onStart();
            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING_COMPASS);
            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.NORMAL);

            initLocationChangeListener(locationEngineCallback);
        }

    }

    /**
     * 初始化定位监听
     */
    @SuppressWarnings({"MissingPermission"})
    private void initLocationChangeListener(LocationEngineCallback<LocationEngineResult> locationEngineCallback) {
        locationEngine = LocationEngineProvider.getBestLocationEngine(mContext);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL)
                //要求最准确的位置
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                //请求经过电池优化的粗略位置
//            .setPriority(LocationEngineRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                //要求粗略〜10 km的准确位置
//            .setPriority(LocationEngineRequest.PRIORITY_LOW_POWER)
                //被动位置：除非其他客户端请求位置更新，否则不会返回任何位置
//            .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
                //设置位置更新之间的距离
//                .setDisplacement(DEFAULT_DISPLACEMENT)
                //设置位置更新的最大等待时间（以毫秒为单位）。
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                //设置位置更新的最快间隔（以毫秒为单位）
                .setFastestInterval(DEFAULT_FASTEST_INTERVAL)
                .build();


        locationEngine.requestLocationUpdates(request, locationEngineCallback, getMainLooper());
        locationEngine.getLastLocation(locationEngineCallback);
        locationComponent.setLocationEngine(locationEngine);
        UserBean userBean = DBClient.findObjById(UserBean.class, 0);
        if (userBean != null) {
            LogUtils.d("用户信息====>" + userBean.toString());
            if (userBean.getPlaneLat() != 0 && userBean.getPlaneLng() != 0) {
                smoothMoveMarker(0, new MapboxLatLng(userBean.getPlaneLat(), userBean.getPlaneLng()), 0);
            }
            if (userBean.getHomeLat() != 0 && userBean.getHomeLng() != 0) {
                addTakeOffMarker(new MapboxLatLng(userBean.getHomeLat(), userBean.getHomeLng()));
            }
        }
    }

    /**
     * 监听mapbox地图定位数据改变
     */
    public void onLocationSuccess(Location location) {
        if (mBoxMap != null && isFirstLocation) {
            CameraPosition position = new CameraPosition.Builder()
                    .target(new MapboxLatLng(location.getLatitude(), location.getLongitude())) // Sets the new camera position
                    .zoom(17) // Sets the zoom
                    .bearing(0) // Rotate the camera
//                    .tilt(30) // Set the camera tilt
                    .build(); // Creates a CameraPosition from the builder
            mBoxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position)
                    , 1000);
            isFirstLocation = false;
        }
//        LogUtils.d("定位成功====>" + location.getLatitude() + "||" + location.getLongitude());
        if (mBoxMap.getStyle() != null && mBoxMap.getStyle().isFullyLoaded())
            markersManager.addLocMarker(isFindMode, location);
    }

    public void onLocationError(Exception exception) {
        isFirstLocation = true;
        LogUtils.d("mapbox 定位失败" + exception.getMessage());
    }


    /**
     * marker顺滑移动
     */
    public void smoothMoveMarker(float planeYaw, MapboxLatLng latLng, float aroundR) {
        if (mBoxMap != null && mBoxMap.getStyle() != null && mBoxMap.getStyle().isFullyLoaded()) {
            markersManager.smoothMovePlaneMarker(latLng, planeYaw, aroundR);
        }
    }

    public boolean isMaploaded() {
        return mBoxMap != null && mBoxMap.getStyle() != null && mBoxMap.getStyle().isFullyLoaded();
    }

    public MapboxLatLng getPlaneMarkerLatLng() {
        return markersManager.getMarkerLatLngByTag(PLANE_ICON);
    }

    public MapboxLatLng getMobileLatLng() {
        Location lastKnownLocation = mBoxMap.getLocationComponent().getLastKnownLocation();
        return new MapboxLatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
    }

    /**
     * 绘制圆形
     *
     * @param circleTag    圆形标识
     * @param fillTag      填充色标识
     * @param centerLatLng 中心点坐标
     * @param radius       圆半径
     */
    public void drawFillCircle(String circleTag, String fillTag, LatLng centerLatLng, @NonNull double radius) {
        Polygon outerCircle = getTurfPolygon(radius, centerLatLng);
        GeoJsonSource outerCircleSource = new GeoJsonSource(circleTag);
        outerCircleSource.setGeoJson(Polygon.fromOuterInner(
                LineString.fromLngLats(TurfMeta.coordAll(outerCircle, false))));
        addSource(outerCircleSource);
        FillLayer fillLayer = new FillLayer(fillTag, circleTag);
        fillLayer.setProperties(
                fillColor(Color.TRANSPARENT));
        addLayer(fillLayer);
    }

    /**
     * 移除填充圆
     */
    public void removeFillCircle(String circleTag, String fillTag) {
        removeSource(circleTag);
        removeLayer(fillTag);
    }

    /**
     * 绘制圆环，即为圆圈
     *
     * @param fillTag      填充色标识
     * @param ringTag      圆环标识
     * @param centerLatLng 中心点坐标
     * @param radius       圆环最外层半径
     * @param colorSrc     填充色
     */
    public void drawRing(String fillTag, String ringTag, LatLng centerLatLng, @NonNull double radius, int colorSrc) {
        //外圆
        Polygon outerCirclePolygon = getTurfPolygon(radius, centerLatLng);
        //内圆,5为线宽
        Polygon innerCirclePolygon = getTurfPolygon(
                radius - 5, centerLatLng);
        //添加数据源
        GeoJsonSource ringSource = new GeoJsonSource(ringTag);
        ringSource.setGeoJson(Polygon.fromOuterInner(
                LineString.fromLngLats(TurfMeta.coordAll(outerCirclePolygon, false)),
                LineString.fromLngLats(TurfMeta.coordAll(innerCirclePolygon, false))
        ));
        //加入地图
        addSource(ringSource);
        //填充色
        FillLayer fillLayer = new FillLayer(fillTag, ringTag);
        fillLayer.setProperties(
                fillColor(ResourceUtils.getColor(colorSrc)));
        //加入地图
        addLayer(fillLayer);
    }

    private void removeRing(String ringTag, String fillTag) {
        removeSource(ringTag);
        removeLayer(fillTag);
    }

    private Polygon getTurfPolygon(@NonNull double radius, LatLng centerLatLng) {
        Point centerPoint = Point.fromLngLat(centerLatLng.getLongitude(), centerLatLng.getLatitude());
        return TurfTransformation.circle(centerPoint, radius, 360, UNIT_METERS);
    }


    /**
     * 绘制两点间距离
     * 保存固定直线
     *
     * @param srcLineTag 绘制的线段数据源标识
     * @param oldData    起始经纬度
     * @param newData    结束经纬度
     */
    private void drawLineSource(String srcLineTag, LatLng oldData, LatLng newData) {
        if (linePoints.size() > 0) linePoints.clear();
        linePoints.add(Point.fromLngLat(oldData.getLongitude(), oldData.getLatitude()));
        linePoints.add(Point.fromLngLat(newData.getLongitude(), newData.getLatitude()));
        addSource(new GeoJsonSource(srcLineTag,
                FeatureCollection.fromFeatures(new Feature[]{
                        Feature.fromGeometry(LineString.fromLngLats(linePoints))
                })));
    }

    /**
     * 根据数据源
     * 绘制直线
     *
     * @param srcLineTag   线段两点坐标数据源标识
     * @param isDottedLine true=虚线
     * @param lineWidth    线宽
     * @param lineColor    线段绘制色
     * @param lineTag      绘制的线段标识
     */
    private void drawLine(String srcLineTag, String lineTag, boolean isDottedLine, float lineWidth, int lineColor) {
        LineLayer lineLayer = new LineLayer(lineTag, srcLineTag);
        lineLayer.withProperties(
                fillAntialias(true),
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(lineWidth),
                lineColor(ResourceUtils.getColor(lineColor))
        );
        if (isDottedLine) {
            lineLayer.setProperties(lineDasharray(new Float[]{1.2f, 2f}));
        }
        lineList.put(lineTag, lineLayer);
        addLayer(lineLayer);
    }

    /**
     * 移除line
     *
     * @param tag 与line一一对应
     */
    public void removeLine(String tag) {
        if (lineList.containsKey(tag)) {
            removeLayer(lineList.get(tag));
            lineList.remove(tag);
        }
    }

    /**
     * 添加marker
     *
     * @param markerTag           与真实的marker一一对应
     * @param markerJsonSourceTag 经纬度坐标标识
     * @param markerIconTag       marker的图标标识
     * @param latLng              marker的经纬度坐标
     */
    public void addMarker(String markerTag, String markerJsonSourceTag, String markerIconTag, int resId, LatLng latLng) {
        // 添加maker坐标
        GeoJsonSource iconGeoJsonSource = new GeoJsonSource(markerJsonSourceTag,
                Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()));
        addSource(iconGeoJsonSource);
        // 添加maker图标
        addImage(markerIconTag, BitmapUtils.getBitmapFromDrawable(
                ResourceUtils.getDrawabe(resId)));
        //创建真实的marker
        SymbolLayer marker = new SymbolLayer(markerTag, markerJsonSourceTag).withProperties(
                iconImage(markerIconTag),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconOffset(new Float[]{0f, -9f}));
        // 添加marker到地图显示
        addLayer(marker);
        markerList.put(markerTag, marker);
    }

    /**
     * 移除marker
     *
     * @param tag 与marker一一对应
     */
    public void removeMarker(String tag) {
        if (markerList.containsKey(tag)) {
            removeLayer(markerList.get(tag));
            markerList.remove(tag);
        }
    }


    private void addSource(Source source) {
        mBoxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                style.addSource(source);
            }
        });

    }

    private void removeSource(String sourceTag) {
        mBoxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                style.removeSource(sourceTag);
            }
        });
    }

    private void addImage(String iconTag, Bitmap bitmap) {
        mBoxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                style.addImage(iconTag, bitmap);
            }
        });
    }

    private void addLayer(Layer layer) {
        mBoxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                style.addLayer(layer);
            }
        });
    }

    private void removeLayer(String layerTag) {
        mBoxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                style.removeLayer(layerTag);
            }
        });
    }

    public void removeLayer(Layer layer) {
        mBoxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                style.removeLayer(layer);
            }
        });
    }


    /**
     * 添加起飞点位置（marker）
     */
    public void addTakeOffMarker(MapboxLatLng latLng) {
        if (isMaploaded()) {
            markersManager.addTakeOffMarkerToMap(latLng);
            //存储起飞点位置
            UserBean bean = DBClient.findObjById(UserBean.class, 0);
            if (bean != null) {
                bean.setHomeLat(latLng.getLatitude());
                bean.setHomeLng(latLng.getLongitude());
                bean.setUserId(0);
                DBClient.updateObject(bean);
//                LogUtils.d("存储家的位置===>" + bean.toString());
            } else {
                bean = new UserBean();
                bean.setHomeLat(latLng.getLatitude());
                bean.setHomeLng(latLng.getLongitude());
                bean.setUserId(0);
                DBClient.addObject(bean);
            }
        }
    }

    /**
     * 添加环绕中心点（marker）
     */
    public void addAroundCenterMarker() {
        if (mBoxMap.getStyle() != null && mBoxMap.getStyle().isFullyLoaded()) {
            MapboxLatLng loc = getPlaneMarkerLatLng();
            if (loc == null)
                loc = getMobileLatLng();
            markersManager.addAroundCentorMarkerToMap(loc);
        }
    }

    /**
     * 清除环绕
     * 1.环绕中心点
     * 2.环绕圆
     */
    public void clearAround() {
        markersManager.removeMakerByTag(AROUND_MARKER);
        markersManager.removeCircleByTag(AROUND_CIRCLE);
        markersManager.removeLineByTag(AROUND_LINE);
    }

    /**
     * 改变手机头朝向
     * （定位点的朝向，自定义手机图标）
     */
    public void changePhoneHeader(float angle) {
        if (markersManager != null)
            markersManager.changeLocaHeader(angle);
    }

    //===航点相关==================================================

    /**
     * 添加航点
     */
    public MapboxWayPointModel addWaypointMarker(Activity ac, LatLng lastClickPoint, int fanceRadius) {
        return markersManager.addWayPointToMap(ac, new MapboxLatLng(lastClickPoint.getLatitude(), lastClickPoint.getLongitude()), fanceRadius);
    }

    /**
     * 刷新航点数据
     */
    public void notifyWayPointData() {

    }

    /**
     * 用户手动移除某一指定航点
     */
    public void removeSigleWaypointFormUser(String markerTag) {

    }

    /**
     * 清除所有航点
     */
    public void clearWaypoint() {
        markersManager.clearWaypoint();
    }

    public void changeMapTypeToSatality() {
        mBoxMap.setStyle(Style.SATELLITE, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
//                markersManager = new MapboxMarkersManager(mBoxMap, style);
//                isMaploaded = true;
//                initLocationComponent(style, locationEngineCallback);
            }
        });
    }

    public void changeMapTypeToNormal() {
        mBoxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
//                markersManager = new MapboxMarkersManager(mBoxMap, style);
//                isMaploaded = true;
//                initLocationComponent(style, locationEngineCallback);
            }
        });
    }

    //==寻飞机====================================================

    public void changeToFindPlane(boolean isFindMode) {
        this.isFindMode = isFindMode;
        if (!isFindMode) {
            //移除寻飞机航线
            markersManager.enterFindPlaneMode();
            MapboxLatLng planeLatLng = markersManager.getMarkerLatLngByTag(PLANE_ICON);
            MapboxLatLng takeOffLatLng = markersManager.getMarkerLatLngByTag(TAKE_OFF);
            if (planeLatLng != null && takeOffLatLng != null) {
                //起飞点到飞机
                markersManager.drawPlaneLine(false, planeLatLng);
            }
        } else {
            //绘制寻飞机航线
            MapboxLatLng planeLatLng = markersManager.getMarkerLatLngByTag(PLANE_ICON);
            if (planeLatLng != null) {
                MapboxLatLng takeOffLatLng = markersManager.getMarkerLatLngByTag(TAKE_OFF);
//                MapboxLatLng takeOffMarker = markersManager.getMarkerLatLngByTag(TAKE_OFF);//定位点的Marker
                if (takeOffLatLng != null) {
                    //起飞点到飞机
                    markersManager.drawPlaneLine(true, planeLatLng);
                }
                if (mBoxMap.getCameraPosition() != null) {
                    //手机到飞机
                    markersManager.startFindPlaneMode();
                }

            }
        }
    }

    //==生命周期===================================================

    public void onResume() {
        LogUtils.d("mapbox====>onResume");
        mMapView.onResume();
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                LogUtils.d("mapbox====>添加成功");
            }
        });
    }

    protected void onStart() {
        LogUtils.d("mapbox====>onStart");
        mMapView.onStart();
    }

    public void onStop() {
        LogUtils.d("mapbox====>onStop");
        mMapView.onStop();
        if (markersManager != null) {
            markersManager.stopPlaneMove();
            markersManager.stopLocationMarkerMove();

        }
    }

    public void onPause() {
        LogUtils.d("mapbox====>onPause");
        mMapView.onPause();
        if (markersManager != null) {
            markersManager.stopPlaneMove();
            markersManager.stopLocationMarkerMove();
        }
    }


    protected void onSaveInstanceState(Bundle outState) {
        LogUtils.d("mapbox====>onSaveInstanceState");
        mMapView.onSaveInstanceState(outState);
    }

    protected void onDestroy() {
        isMaploaded = false;
        LogUtils.d("mapbox====>onDestroy");
        //移除定位监听
        if (locationEngine != null)
            locationEngine.removeLocationUpdates(mLocationEngineCallback);
        mMapView.onDestroy();
        if (markersManager != null) {
            markersManager.stopPlaneMove();
            markersManager.stopLocationMarkerMove();
        }
    }

    public void onMapChange() {
        //移除定位监听
        if (locationEngine != null)
            locationEngine.removeLocationUpdates(mLocationEngineCallback);
        if (markersManager != null) {
            markersManager.stopPlaneMove();
            markersManager.stopLocationMarkerMove();
        }
    }

    public void onLowMemory() {
        LogUtils.d("mapbox====>onLowMemory");
        mMapView.onLowMemory();
        if (markersManager != null) {
            markersManager.stopPlaneMove();
            markersManager.stopLocationMarkerMove();
        }
    }

}
