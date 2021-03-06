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
 * Mapbox??????????????????????????????????????????????????????
 */
public class MapBoxViewHelper {

    //???????????????????????????
    public static final float DEFAULT_DISPLACEMENT = 0f;
    //????????????????????????????????????????????????????????????
    public static final long DEFAULT_MAX_WAIT_TIME = 5000L;
    //???????????????????????????????????????????????????
    public static final long DEFAULT_FASTEST_INTERVAL = 500L;
    //?????????????????????????????????
    public static final long DEFAULT_INTERVAL = 500L;


    private List<Point> linePoints = new ArrayList<>();//?????????????????????


    private Context mContext;
    private PermissionsManager permissionsManager;

    private MyMapBoxMap mMapView;
    private MapboxMap mBoxMap;
    private LocationComponent locationComponent;
    private boolean isMaploaded;
    private Map<String, SymbolLayer> markerList = new HashMap<>();//??????????????????????????????
    private Map<String, LineLayer> lineList = new HashMap<>();//???????????????????????????
    private LocationEngine locationEngine;
    private LocationEngineCallback<LocationEngineResult> mLocationEngineCallback;
    private MapboxMarkersManager markersManager;
    private MapboxWayPointModel mWaypointModel;

    private int mSolidWidth;//?????????
    private int mSolidColor;//????????????
    private int mDottedWidth;//?????????
    private int mDottedColor;//????????????
    private int mFindPlaneTakeOffLineColor;
    private int mLocationRes;//????????????(????????????)

    private boolean isFindMode;//???????????????

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
                LogUtils.d("??????====>????????????");
            }
        });
        mMapView.addOnCameraWillChangeListener(new MapView.OnCameraWillChangeListener() {
            @Override
            public void onCameraWillChange(boolean animated) {
                LogUtils.d("??????====>????????????" + animated);
            }
        });
        mapView.addOnDidFinishLoadingMapListener(new MapView.OnDidFinishLoadingMapListener() {
            @Override
            public void onDidFinishLoadingMap() {
                LogUtils.d("??????====>????????????");
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
                //mapbox??????????????????
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
     * ????????????
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
//                    .build();//?????????????????????????????????

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
     * ?????????????????????
     */
    @SuppressWarnings({"MissingPermission"})
    private void initLocationChangeListener(LocationEngineCallback<LocationEngineResult> locationEngineCallback) {
        locationEngine = LocationEngineProvider.getBestLocationEngine(mContext);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL)
                //????????????????????????
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                //???????????????????????????????????????
//            .setPriority(LocationEngineRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                //???????????????10 km???????????????
//            .setPriority(LocationEngineRequest.PRIORITY_LOW_POWER)
                //???????????????????????????????????????????????????????????????????????????????????????
//            .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
                //?????????????????????????????????
//                .setDisplacement(DEFAULT_DISPLACEMENT)
                //??????????????????????????????????????????????????????????????????
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                //?????????????????????????????????????????????????????????
                .setFastestInterval(DEFAULT_FASTEST_INTERVAL)
                .build();


        locationEngine.requestLocationUpdates(request, locationEngineCallback, getMainLooper());
        locationEngine.getLastLocation(locationEngineCallback);
        locationComponent.setLocationEngine(locationEngine);
        UserBean userBean = DBClient.findObjById(UserBean.class, 0);
        if (userBean != null) {
            LogUtils.d("????????????====>" + userBean.toString());
            if (userBean.getPlaneLat() != 0 && userBean.getPlaneLng() != 0) {
                smoothMoveMarker(0, new MapboxLatLng(userBean.getPlaneLat(), userBean.getPlaneLng()), 0);
            }
            if (userBean.getHomeLat() != 0 && userBean.getHomeLng() != 0) {
                addTakeOffMarker(new MapboxLatLng(userBean.getHomeLat(), userBean.getHomeLng()));
            }
        }
    }

    /**
     * ??????mapbox????????????????????????
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
//        LogUtils.d("????????????====>" + location.getLatitude() + "||" + location.getLongitude());
        if (mBoxMap.getStyle() != null && mBoxMap.getStyle().isFullyLoaded())
            markersManager.addLocMarker(isFindMode, location);
    }

    public void onLocationError(Exception exception) {
        isFirstLocation = true;
        LogUtils.d("mapbox ????????????" + exception.getMessage());
    }


    /**
     * marker????????????
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
     * ????????????
     *
     * @param circleTag    ????????????
     * @param fillTag      ???????????????
     * @param centerLatLng ???????????????
     * @param radius       ?????????
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
     * ???????????????
     */
    public void removeFillCircle(String circleTag, String fillTag) {
        removeSource(circleTag);
        removeLayer(fillTag);
    }

    /**
     * ???????????????????????????
     *
     * @param fillTag      ???????????????
     * @param ringTag      ????????????
     * @param centerLatLng ???????????????
     * @param radius       ?????????????????????
     * @param colorSrc     ?????????
     */
    public void drawRing(String fillTag, String ringTag, LatLng centerLatLng, @NonNull double radius, int colorSrc) {
        //??????
        Polygon outerCirclePolygon = getTurfPolygon(radius, centerLatLng);
        //??????,5?????????
        Polygon innerCirclePolygon = getTurfPolygon(
                radius - 5, centerLatLng);
        //???????????????
        GeoJsonSource ringSource = new GeoJsonSource(ringTag);
        ringSource.setGeoJson(Polygon.fromOuterInner(
                LineString.fromLngLats(TurfMeta.coordAll(outerCirclePolygon, false)),
                LineString.fromLngLats(TurfMeta.coordAll(innerCirclePolygon, false))
        ));
        //????????????
        addSource(ringSource);
        //?????????
        FillLayer fillLayer = new FillLayer(fillTag, ringTag);
        fillLayer.setProperties(
                fillColor(ResourceUtils.getColor(colorSrc)));
        //????????????
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
     * ?????????????????????
     * ??????????????????
     *
     * @param srcLineTag ??????????????????????????????
     * @param oldData    ???????????????
     * @param newData    ???????????????
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
     * ???????????????
     * ????????????
     *
     * @param srcLineTag   ?????????????????????????????????
     * @param isDottedLine true=??????
     * @param lineWidth    ??????
     * @param lineColor    ???????????????
     * @param lineTag      ?????????????????????
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
     * ??????line
     *
     * @param tag ???line????????????
     */
    public void removeLine(String tag) {
        if (lineList.containsKey(tag)) {
            removeLayer(lineList.get(tag));
            lineList.remove(tag);
        }
    }

    /**
     * ??????marker
     *
     * @param markerTag           ????????????marker????????????
     * @param markerJsonSourceTag ?????????????????????
     * @param markerIconTag       marker???????????????
     * @param latLng              marker??????????????????
     */
    public void addMarker(String markerTag, String markerJsonSourceTag, String markerIconTag, int resId, LatLng latLng) {
        // ??????maker??????
        GeoJsonSource iconGeoJsonSource = new GeoJsonSource(markerJsonSourceTag,
                Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()));
        addSource(iconGeoJsonSource);
        // ??????maker??????
        addImage(markerIconTag, BitmapUtils.getBitmapFromDrawable(
                ResourceUtils.getDrawabe(resId)));
        //???????????????marker
        SymbolLayer marker = new SymbolLayer(markerTag, markerJsonSourceTag).withProperties(
                iconImage(markerIconTag),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconOffset(new Float[]{0f, -9f}));
        // ??????marker???????????????
        addLayer(marker);
        markerList.put(markerTag, marker);
    }

    /**
     * ??????marker
     *
     * @param tag ???marker????????????
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
     * ????????????????????????marker???
     */
    public void addTakeOffMarker(MapboxLatLng latLng) {
        if (isMaploaded()) {
            markersManager.addTakeOffMarkerToMap(latLng);
            //?????????????????????
            UserBean bean = DBClient.findObjById(UserBean.class, 0);
            if (bean != null) {
                bean.setHomeLat(latLng.getLatitude());
                bean.setHomeLng(latLng.getLongitude());
                bean.setUserId(0);
                DBClient.updateObject(bean);
//                LogUtils.d("??????????????????===>" + bean.toString());
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
     * ????????????????????????marker???
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
     * ????????????
     * 1.???????????????
     * 2.?????????
     */
    public void clearAround() {
        markersManager.removeMakerByTag(AROUND_MARKER);
        markersManager.removeCircleByTag(AROUND_CIRCLE);
        markersManager.removeLineByTag(AROUND_LINE);
    }

    /**
     * ?????????????????????
     * ????????????????????????????????????????????????
     */
    public void changePhoneHeader(float angle) {
        if (markersManager != null)
            markersManager.changeLocaHeader(angle);
    }

    //===????????????==================================================

    /**
     * ????????????
     */
    public MapboxWayPointModel addWaypointMarker(Activity ac, LatLng lastClickPoint, int fanceRadius) {
        return markersManager.addWayPointToMap(ac, new MapboxLatLng(lastClickPoint.getLatitude(), lastClickPoint.getLongitude()), fanceRadius);
    }

    /**
     * ??????????????????
     */
    public void notifyWayPointData() {

    }

    /**
     * ????????????????????????????????????
     */
    public void removeSigleWaypointFormUser(String markerTag) {

    }

    /**
     * ??????????????????
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

    //==?????????====================================================

    public void changeToFindPlane(boolean isFindMode) {
        this.isFindMode = isFindMode;
        if (!isFindMode) {
            //?????????????????????
            markersManager.enterFindPlaneMode();
            MapboxLatLng planeLatLng = markersManager.getMarkerLatLngByTag(PLANE_ICON);
            MapboxLatLng takeOffLatLng = markersManager.getMarkerLatLngByTag(TAKE_OFF);
            if (planeLatLng != null && takeOffLatLng != null) {
                //??????????????????
                markersManager.drawPlaneLine(false, planeLatLng);
            }
        } else {
            //?????????????????????
            MapboxLatLng planeLatLng = markersManager.getMarkerLatLngByTag(PLANE_ICON);
            if (planeLatLng != null) {
                MapboxLatLng takeOffLatLng = markersManager.getMarkerLatLngByTag(TAKE_OFF);
//                MapboxLatLng takeOffMarker = markersManager.getMarkerLatLngByTag(TAKE_OFF);//????????????Marker
                if (takeOffLatLng != null) {
                    //??????????????????
                    markersManager.drawPlaneLine(true, planeLatLng);
                }
                if (mBoxMap.getCameraPosition() != null) {
                    //???????????????
                    markersManager.startFindPlaneMode();
                }

            }
        }
    }

    //==????????????===================================================

    public void onResume() {
        LogUtils.d("mapbox====>onResume");
        mMapView.onResume();
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                LogUtils.d("mapbox====>????????????");
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
        //??????????????????
        if (locationEngine != null)
            locationEngine.removeLocationUpdates(mLocationEngineCallback);
        mMapView.onDestroy();
        if (markersManager != null) {
            markersManager.stopPlaneMove();
            markersManager.stopLocationMarkerMove();
        }
    }

    public void onMapChange() {
        //??????????????????
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
