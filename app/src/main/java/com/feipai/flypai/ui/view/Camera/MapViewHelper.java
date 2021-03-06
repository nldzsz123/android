package com.feipai.flypai.ui.view.Camera;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.MavMessageHelp;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.LocationSource.OnLocationChangedListener;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.utils.overlay.SmoothMoveMarker;
import com.feipai.flypai.R;
import com.feipai.flypai.api.MapChangeListener;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.beans.UserBean;
import com.feipai.flypai.beans.WaypointModel;
import com.feipai.flypai.ui.adapter.MapInfoWindowAdapter;
import com.feipai.flypai.ui.view.mapbox.MapboxMarker;
import com.feipai.flypai.ui.view.mapbox.MapboxWayPointModel;
import com.feipai.flypai.utils.GPSUtils;
import com.feipai.flypai.utils.MLog;
import com.feipai.flypai.utils.daoutils.DBClient;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.NetworkUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.StringUtils;
import com.feipai.flypai.utils.global.ToastUtils;
import com.feipai.flypai.utils.global.Utils;
import com.feipai.flypai.utils.languageutils.LanguageUtil;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapViewHelper {
    private final static String LOC_MARKER = "locMarker";
    private final static String TAKE_OFF_MARKER = "takeOffMarker";
    private final static String AROUND_CENTER_MARKER = "aroundCenterMarker";
    private final static String FIND_LINE = "findLine";
    private final static String TAKE_OFF_LINE = "takeOffLine";
    private final static String AROUND_LIDE = "aroundLine";

    private Context mContext;

    private int mSolidWidth;//?????????
    private int mSolidColor;//????????????
    private int mDottedWidth;//?????????
    private int mDottedColor;//????????????
    private int mFindPlaneTakeOffLineColor;
    private int mLocationRes;//????????????(????????????)

    GaoDeMapView mMapView;
    private AMap mAMap;
    private MapInfoWindowAdapter mapInfoWinAdapter;
    private UiSettings mapSettings;
    private LatLng phoneLatlng;
    private Map<String, Marker> markerMap = new HashMap<>();//???????????????marker
    private Map<String, Polyline> linesMap = new HashMap<>();//???????????????????????????
    private WaypointModel mWaypointModel;
    private MapboxWayPointModel mMapboxWayPointModel;
    //    private List<Polyline> waypointPolylines = new ArrayList<>();//??????????????????
    private SmoothMoveMarker planeMarker = null;//??????????????????marker?????????
    private float planeMoveDistance = 0;
    //??????????????????????????????????????????
    private Circle circle;
    private LatLng planeNewLatlng;//???????????????????????????
    private LatLng planeOldLatlng;//??????????????????????????????
    private View planeView;
    private View wayPointView;

    private AMapLocationClient mLocationClient = null;//???????????????
    private AMapLocationClientOption mLocationOption = null;//????????????
    private LocationSource.OnLocationChangedListener mListener = null;//???????????????

    private boolean isFindMode;

    private MapChangeListener mapChangeListener;
    private boolean isLocationSuccess;
    private boolean isFirstLocation = true;


    public MapViewHelper(Context context, AttributeSet attrs, int defStyleAttr, GaoDeMapView mapView, MapChangeListener listener) {
        this.mContext = context;
        initAttr(context, attrs, defStyleAttr);
        this.mMapView = mapView;
        this.mapChangeListener = listener;
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

    public void setTouch(AMap.OnMapTouchListener tapDetector) {
        if (mAMap != null)
            mAMap.setOnMapTouchListener(tapDetector);
    }


    public void removeTouch() {
        if (mAMap != null)
            mAMap.setOnMapTouchListener(null);
    }

    public void initMapView(Bundle savedInstanceState, AMap.OnMapClickListener mapClickListener, LocationSource locationSource) {
        mMapView.onCreate(savedInstanceState);
        if (mAMap == null) {
            mAMap = mMapView.getMap();
            mapSettings = mAMap.getUiSettings();
            //??????????????????
            mapSettings.setRotateGesturesEnabled(true);
//            setMapCustomStyleFile(this);
//            mAMap.setLoadOfflineData(false);
            mAMap.setLoadOfflineData(true);
            mAMap.setMapLanguage(LanguageUtil.isEnglish() ? AMap.ENGLISH : AMap.CHINESE);
//            mAMap.reloadMap();
            mapSettings = mAMap.getUiSettings();
            mapSettings.setRotateGesturesEnabled(true);///??????????????????
            mapInfoWinAdapter = new MapInfoWindowAdapter(mContext, new MapInfoWindowAdapter.DelectWaypointListener() {
                @Override
                public void delectWaypoinit(Marker marker) {
                    MLog.log("??????????????????..." + marker.getTitle());
                    if (mWaypointModel != null) {
                        if (mapChangeListener != null)
                            mapChangeListener.onRemoveMakrerFromUser(marker);
                    }
                }
            });
            mAMap.setInfoWindowAdapter(mapInfoWinAdapter);
//            mapSettings.setAllGesturesEnabled(true);///??????????????????
            initAmap(mapClickListener, locationSource);
        }

    }

    /**
     * ???????????????
     */
    private void initAmap(AMap.OnMapClickListener mapClickListener, LocationSource locationSource) {
        mAMap.setOnMapClickListener(mapClickListener);
        mAMap.setLocationSource(locationSource);// ??????????????????
        // ???????????????????????????
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // ???????????????????????????
//        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.phone_ponit_img));
        // ??????????????????????????????????????????
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
        // ??????????????????????????????????????????
        myLocationStyle.strokeWidth(0);
        // ???????????????????????????
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
        //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????false????????????????????????????????????????????????????????????????????????????????????
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);
        myLocationStyle.showMyLocation(false);
        // ??????????????? myLocationStyle ????????????????????????
        mAMap.setMyLocationStyle(myLocationStyle);
        // ?????????true??????????????????????????????????????????false??????????????????????????????????????????????????????false
        mAMap.setMyLocationEnabled(true);
        //?????? aMap.setMapTextZIndex(2) ????????????????????????????????????????????????????????????
        mAMap.setMapTextZIndex(2);
        UserBean userBean = DBClient.findObjById(UserBean.class, 0);
        if (userBean != null) {
            LogUtils.d("????????????====>" + userBean.toString());
            if (userBean.getPlaneLat() != 0 && userBean.getPlaneLng() != 0) {
                smoothMoveMarker(0, GPSUtils.gcj2WGSExactly(userBean.getPlaneLat(), userBean.getPlaneLng()), 0);
            }
//            if (waypointDbDao.getPlanePointById(0) != null)
//                movePlaneMarker(waypointDbDao.getPlanePointById(0));
            if (userBean.getHomeLat() != 0 && userBean.getHomeLng() != 0) {
                LatLng latLng = GPSUtils.gcj2WGSExactly(userBean.getHomeLat(), userBean.getHomeLng());
                LogUtils.d("???????????????????????????====>" + latLng.latitude + "||||" + latLng.longitude);
                addTakeOffMarker(latLng);
            }
        }
    }

    /**
     * ???????????????
     */
    private void initLoc(AMapLocationListener aMapLocationListener) throws Exception {
        if (mLocationClient == null) {
            //???????????????
            mLocationClient = new AMapLocationClient(Utils.context);
            //????????????????????????
            mLocationClient.setLocationListener(aMapLocationListener);
            //?????????????????????
            mLocationOption = new AMapLocationClientOption();
            //???????????????????????????????????????Battery_Saving?????????????????????Device_Sensors??????????????????????????????????????????Device_Sensors
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //????????????????????????????????????????????????????????????
            mLocationOption.setNeedAddress(true);
            //???????????????????????????,?????????false
            mLocationOption.setOnceLocation(false);
            //????????????????????????WIFI????????????????????????
//            mLocationOption.setWifiActiveScan(true);
            //??????????????????????????????,?????????false????????????????????????
            mLocationOption.setMockEnable(true);
            //??????????????????,????????????,?????????2000ms
            mLocationOption.setInterval(2000);
            //????????????????????????????????????
            mLocationOption.setLocationCacheEnable(true);
            //??????????????????????????????????????????
            mLocationClient.setLocationOption(mLocationOption);
            if (mLocationClient.getLastKnownLocation() != null) {
                mAMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
                        new LatLng(mLocationClient.getLastKnownLocation().getLatitude(),
                                mLocationClient.getLastKnownLocation().getLongitude()), 19, 0, 0)));
            }
            //????????????
            mLocationClient.startLocation();
        }
    }

    /**
     * ????????????????????????
     */
    public void startLoc(OnLocationChangedListener onLocationChangedListener, AMapLocationListener aMapLocationListener) throws Exception {
        mListener = onLocationChangedListener;
        initLoc(aMapLocationListener);
    }

    /**
     * ????????????
     */
    public void stopLoc() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }


    /**
     * ??????????????????
     */
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                if (!isLocationSuccess) {
                    isLocationSuccess = true;
                    if (mapChangeListener != null)
                        mapChangeListener.onLocationResult(isLocationSuccess);
                }
                mListener.onLocationChanged(aMapLocation);// ?????????????????????,?????????????????????
                double latitudeFirst = aMapLocation.getLatitude();//????????????
                double longitudeFirst = aMapLocation.getLongitude();//????????????
                phoneLatlng = new LatLng(latitudeFirst, longitudeFirst);
                MarkerOptions markerOption = new MarkerOptions();
                markerOption.position(phoneLatlng);

                if (markerMap.get(LOC_MARKER) == null) {
                    //????????????
                    addMarker(LOC_MARKER, markerOption
                            .icon(BitmapDescriptorFactory.fromResource(mLocationRes))
                            .anchor(0.5f, 0.5f));
                    //????????????,????????????????????????????????????????????????20???
                    mAMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()), 19, 0, 0)));
                } else {

                    //???????????????????????????sdk??????????????????????????????????????????????????????????????????????????????????????????????????????
                    startChangeLocation(phoneLatlng);

                }
                if (isFindMode) {
                    if (planeMarker != null) {
                        Marker locMark = markerMap.get(LOC_MARKER);
                        if (locMark != null) {
                            locMark.setTitle(ResourceUtils.getString(R.string.distance) + ":" + String.format("%.1f m", AMapUtils.calculateLineDistance(locMark.getPosition(), planeMarker.getPosition())));
                            locMark.setFlat(false);
                            locMark.showInfoWindow();
                        }
                        //??????????????????
                        drawFixedLine(isFindMode, mDottedWidth, mDottedColor, FIND_LINE,
                                phoneLatlng, planeMarker.getPosition());
                    }
                }
            } else {
                if (isFirstLocation || isLocationSuccess) {
                    isLocationSuccess = false;
                    isFirstLocation = false;
                    if (mapChangeListener != null)
                        mapChangeListener.onLocationResult(isLocationSuccess);
                }
//                if (isFirstLocError) {
//                    showPanorDialog(ABFlyingFactory.PHONE_LOCATION_ERROR);
//                    isFirstLoc = false;
//                }
//                String errText = "????????????," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
//                MLog.log("??????AmapErr" + errText);
            }
        }
    }

    /**
     * ???????????????????????????????????????
     *
     * @param latLng
     */
    private void startChangeLocation(LatLng latLng) {
        Marker locMarker = markerMap.get(LOC_MARKER);
        if (locMarker != null) {
            LatLng curLatlng = locMarker.getPosition();
            if (curLatlng == null || !curLatlng.equals(latLng)) {
                locMarker.setPosition(latLng);
            }
        }
    }

    /**
     * ??????????????????
     */
    public void openOffLineMap(boolean isOpen) {
        mAMap.setLoadOfflineData(isOpen);
    }


    /**
     * ??????????????????
     */
    private void drawCircle(LatLng centerLatLng, LatLng planeLatLng, double aroundRadius) {
        Marker aroundMarker = markerMap.get(AROUND_CENTER_MARKER);
        if (aroundMarker != null) {
            if (aroundRadius == 0) {
                aroundRadius = (float) MavMessageHelp.getDistance(centerLatLng.latitude, centerLatLng.longitude,
                        planeLatLng.latitude, planeLatLng.longitude);
            }
            if (circle == null) {
                circle = mAMap.addCircle(new CircleOptions().
                        center(centerLatLng).
                        radius(aroundRadius).
                        fillColor(ResourceUtils.getColor(R.color.color_transparent)).
                        strokeColor(ResourceUtils.getColor(R.color.color_00cf00)).
                        strokeWidth(5));

            } else {
                circle.setRadius(aroundRadius);
            }
            drawFixedLine(false, 4, mDottedColor,
                    AROUND_LIDE, aroundMarker.getPosition(), planeMarker.getPosition());
            if (mapChangeListener != null) mapChangeListener.onAroundChange(aroundRadius);

        }
    }


    /**
     * ?????????????????????
     * ??????????????????
     *
     * @param isDottedLine ??????true or ??????false
     * @param lineWidth    ??????
     * @param lineColor    ???????????????
     * @param lineTag      ?????????????????????
     * @param oldData      ???????????????
     * @param newData      ???????????????
     */
    private void drawFixedLine(boolean isDottedLine, int lineWidth, int lineColor, String lineTag, LatLng oldData, LatLng newData) {
        List<LatLng> latLngs = new ArrayList<>();
        if (oldData != null) {
            latLngs.add(oldData);
        }
        if (newData != null) {
            latLngs.add(newData);
        }
        // ??????????????????
        if (latLngs.size() == 2) {
            Polyline polyline = linesMap.get(lineTag);
            if (polyline == null) {
                polyline = mAMap.addPolyline(new PolylineOptions()
                        .addAll(latLngs)
                        .setDottedLine(isDottedLine)
                        .width(lineWidth)
                        .zIndex(1)
                        .color(lineColor));
                linesMap.put(lineTag, polyline);
            } else {
                polyline.setOptions(new PolylineOptions()
                        .addAll(latLngs)
                        .setDottedLine(isDottedLine)
                        .width(lineWidth)
                        .zIndex(1)
                        .color(lineColor));
            }
        }
    }

    /**
     * ??????marker
     */
    public void addMarker(String tag, MarkerOptions options) {
        if (!markerMap.containsKey(tag)) {
            Marker marker = mAMap.addMarker(options);
            markerMap.put(tag, marker);
        }
    }

    /**
     * ??????marker
     */
    public void removeMarker(String tag) {
        if (markerMap.containsKey(tag)) {
            markerMap.get(tag).remove();
            markerMap.remove(tag);
        }
    }

    public void removeLine(String tag) {
        if (linesMap.containsKey(tag)) {
            linesMap.get(tag).remove();
            linesMap.remove(tag);
        }
    }

    public WaypointModel addWaypointMarker(Activity activity, LatLng latlng, float fanceRadius) {
        if (mWaypointModel == null)
            mWaypointModel = new WaypointModel();
        String toastStr = "";
        if (mWaypointModel.getWaypointMarkers().size() > 9) {
            ToastUtils.showLongToast(ResourceUtils.getString(R.string.the_number_of_waypoint_cannot_be_more_than_10));
            return mWaypointModel;
        }
        Marker takeOffPointMarker = markerMap.get(TAKE_OFF_MARKER);
        if (takeOffPointMarker != null && takeOffPointMarker.getPosition() != null) {
            MLog.log("?????????????????????????????????---->" + fanceRadius);
            if (fanceRadius <= 300) {
                toastStr = ResourceUtils.getString(R.string.waypoint_max_limit);
                circle = mAMap.addCircle(new CircleOptions().
                        center(takeOffPointMarker.getPosition()).
                        radius(fanceRadius).
                        fillColor(ResourceUtils.getColor(R.color.color_transparent)).
                        strokeColor(ResourceUtils.getColor(R.color.color_00cf00)).
                        strokeWidth(0));
            } else {
                toastStr = ResourceUtils.getString(R.string.waypoint_max_limit_300);
                circle = mAMap.addCircle(new CircleOptions().
                        center(takeOffPointMarker.getPosition()).
                        radius(300).
                        fillColor(ResourceUtils.getColor(R.color.color_transparent)).
                        strokeColor(ResourceUtils.getColor(R.color.color_00cf00)).
                        strokeWidth(0));
            }
        }
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(latlng);
//        if (wayPointView == null) {
        wayPointView = activity.getLayoutInflater().inflate(R.layout.waypoint_layout,
                null);
        AutoUtils.auto(wayPointView);
//        }
        markerOption.icon(BitmapDescriptorFactory.fromView(wayPointView));
        markerOption.title(ResourceUtils.getString(R.string.waypoint) + ":" + (mWaypointModel.getWaypointMarkers().size() + 1));
        markerOption.setFlat(true);
        Marker marker = mAMap.addMarker(markerOption);
        if (circle != null && !circle.contains(marker.getPosition())) {
            marker.remove();
            ToastUtils.showLongToast(toastStr);
            return mWaypointModel;
        }
        List<LatLng> latLngs = new ArrayList<>();
        if (mWaypointModel.getWaypointMarkers().size() >= 1) {
            latLngs.add(mWaypointModel.getWaypointMarkers().get(mWaypointModel.getWaypointMarkers().size() - 1).getPosition());
            latLngs.add(marker.getPosition());
            if (mWaypointModel.getTotalMileages() + AMapUtils.calculateLineDistance(latLngs.get(0), latLngs.get(1)) <= 2000) {
                mWaypointModel.setMileages(AMapUtils.calculateLineDistance(latLngs.get(0), latLngs.get(1)));
                mWaypointModel.addWaypointMarker(marker);
                // ??????????????????
                mWaypointModel.addLines(mAMap.addPolyline(new PolylineOptions().
                        addAll(latLngs).width(9).color(ResourceUtils.getColor(R.color.color_4097e1))));
            } else {
                marker.remove();
                ToastUtils.showLongToast(ResourceUtils.getString(R.string.total_mileage_shall_not_exceed_2km));
            }
        } else {
            mWaypointModel.addWaypointMarker(marker);
        }
        if (circle != null)
            circle.remove();
        return mWaypointModel;
    }


    /**
     * ??????????????????
     */
    public WaypointModel notifyWayPointData() {
        if (mWaypointModel != null && mWaypointModel.getWaypointMarkers().size() > 0) {
            mWaypointModel.setTotalMileages(0);
            for (int i = 0; i < mWaypointModel.getWaypointMarkers().size(); i++) {
                mWaypointModel.getWaypointMarkers().get(i).setTitle(ResourceUtils.getString(R.string.waypoint) + (i + 1));
                List<LatLng> latLngs = new ArrayList<>();
                latLngs.add(mWaypointModel.getWaypointMarkers().get(i).getPosition());
                if ((i + 1) < mWaypointModel.getWaypointMarkers().size()) {
                    latLngs.add(mWaypointModel.getWaypointMarkers().get(i + 1).getPosition());
                    mWaypointModel.setMileages(AMapUtils.calculateLineDistance(latLngs.get(0), latLngs.get(1)));
                    mWaypointModel.addLines(mAMap.addPolyline(new PolylineOptions().
                            addAll(latLngs).width(9).color(ResourceUtils.getColor(R.color.color_f34235))));
                }
            }
            return mWaypointModel;
        }
        return null;
    }

    /**
     * ??????????????????
     */
    public void clearWaypoint() {
        if (mWaypointModel != null) {
            if (mWaypointModel.getWaypointPolylines().size() > 0) {
                for (Polyline line : mWaypointModel.getWaypointPolylines()) {
                    line.remove();
                }
                mWaypointModel.getWaypointPolylines().clear();
            }
            if (mWaypointModel.getWaypointMarkers().size() > 0) {
                for (Marker marker : mWaypointModel.getWaypointMarkers()) {
                    marker.remove();
                }
                mWaypointModel.getWaypointMarkers().clear();
            }
            mWaypointModel.setTotalMileages(0);
            mWaypointModel = null;
        }
    }

    /**
     * ????????????????????????
     *
     * @param planeYaw ????????????
     * @param newLat   ?????????????????????
     * @param aroundR  ????????????????????????????????????????????????
     */
    public void smoothMoveMarker(float planeYaw, LatLng newLat, float aroundR) {

        planeNewLatlng = newLat;
        if (CoordinateConverter.isAMapDataAvailable(newLat.latitude, newLat.longitude))
            newLat = GPSUtils.wgs2GCJ(newLat.latitude, newLat.longitude);
        List<LatLng> latLngs = new ArrayList<>();
        Marker takeOffPointMarker = markerMap.get(TAKE_OFF_MARKER);
        if (planeOldLatlng == null && takeOffPointMarker != null) {
            planeOldLatlng = takeOffPointMarker.getPosition();
        }
        if (planeOldLatlng == null) {
            planeOldLatlng = planeNewLatlng;
        }
        latLngs.add(planeOldLatlng);
        latLngs.add(newLat);
        if (planeMarker == null) {
            planeMarker = new SmoothMoveMarker(mAMap);
            if (planeView == null) {
                planeView = LayoutInflater.from(mContext).inflate(R.layout.plane_layout, null);
//                        activity.getLayoutInflater().inflate(R.layout.plane_layout,
//                        null);
                AutoUtils.auto(planeView);
            }
            planeMarker.setDescriptor(BitmapDescriptorFactory.fromView(planeView));
        }
//        LogUtils.d("??????????????????====>" + planeMoveDistance + "||" + AMapUtils.calculateLineDistance(newLat, planeOldLatlng));
        planeMoveDistance += AMapUtils.calculateLineDistance(newLat, planeOldLatlng);
        if (planeMoveDistance > 2) {
            //?????????????????????????????????????????????5?????????????????????5???????????????????????????????????????????????????????????????
            //??????????????????????????????????????????????????????
            planeMoveDistance = 0;
            mAMap.animateCamera(CameraUpdateFactory.changeLatLng(newLat));
        }
        planeMarker.setPoints(latLngs);//???????????????????????????list
        // ?????????????????????
        if (planeMarker.getMarker() != null) {
            planeMarker.setRotate(planeYaw);
        }
        planeMarker.setTotalDuration(planeOldLatlng == planeNewLatlng ? 1 : 10);//??????????????????????????????
        planeMarker.startSmoothMove();
        if (takeOffPointMarker != null && planeMarker != null) {
            //??????????????????
            drawFixedLine(isFindMode, isFindMode ? mDottedWidth : mSolidWidth,
                    isFindMode ? mDottedColor : mSolidColor,
                    TAKE_OFF_LINE, takeOffPointMarker.getPosition(), planeMarker.getPosition());
        }
        planeOldLatlng = newLat;
        //????????????????????????
        UserBean bean = DBClient.findObjById(UserBean.class, 0);
        if (bean != null) {
            bean.setPlaneLat(planeOldLatlng.latitude);
            bean.setPlaneLng(planeOldLatlng.longitude);
            DBClient.updateObject(bean);
        } else {
            bean = new UserBean();
            bean.setPlaneLat(planeOldLatlng.latitude);
            bean.setPlaneLng(planeOldLatlng.longitude);
            bean.setUserId(0);
            DBClient.addObject(bean);
        }
        Marker aroundCenterMarker = markerMap.get(AROUND_CENTER_MARKER);
        if (aroundCenterMarker != null) {
            drawCircle(aroundCenterMarker.getPosition(), planeOldLatlng, aroundR);
        }
    }

    /**
     * ?????????marker
     */
    public Marker moveMarker(int markerbitmap, String markerTag, LatLng latLng) {
        Marker marker = markerMap.get(markerTag);
        MarkerOptions markerOption;
        if (CoordinateConverter.isAMapDataAvailable(latLng.latitude, latLng.longitude))
            latLng = GPSUtils.wgs2GCJ(latLng.latitude, latLng.longitude);
        if (marker == null) {
            markerOption = new MarkerOptions();//??????marker????????????
            /**
             * ?????????????????????
             */
            markerOption.draggable(false);
            // ???Marker???????????????????????????????????????????????????
            markerOption.setFlat(false);
            if (latLng != null) {
                markerOption.position(latLng);//??????????????????
            }
            if (markerbitmap != 0)
                markerOption.icon(BitmapDescriptorFactory.fromResource(markerbitmap));
            else
                markerOption.icon(BitmapDescriptorFactory.defaultMarker());
            marker = mAMap.addMarker(markerOption);
            markerMap.put(markerTag, marker);
        } else {
            /**???????????????????????????????????????????????????????????????**/
            markerOption = marker.getOptions();
            markerOption.position(latLng);
            marker.setMarkerOptions(markerOption);
        }
        if (markerTag.equals(TAKE_OFF_MARKER)) {
            //?????????????????????
            UserBean bean = DBClient.findObjById(UserBean.class, 0);
            if (bean != null) {
                bean.setHomeLat(marker.getPosition().latitude);
                bean.setHomeLng(marker.getPosition().longitude);
                bean.setUserId(0);
                DBClient.updateObject(bean);
//                LogUtils.d("??????????????????===>" + bean.toString());
            } else {
                bean = new UserBean();
                bean.setHomeLat(marker.getPosition().latitude);
                bean.setHomeLng(marker.getPosition().longitude);
                bean.setUserId(0);
                DBClient.addObject(bean);
                LogUtils.d("??????????????????===>" + bean.toString());
            }
        }
        return marker;//????????????
    }

    /**
     * ?????????????????????
     */
    public void addTakeOffMarker(LatLng latLng) {
        moveMarker(R.mipmap.start_fly_point_img, TAKE_OFF_MARKER, latLng);
//                new LatLng(Double.parseDouble(loacationMode.getLat()), loacationMode.getLng()));
    }

    /**
     * ??????????????????????????????
     */
    public void changeToFindPlane(boolean isFindMode) {
        this.isFindMode = isFindMode;
        if (!isFindMode) {
            removeLine(FIND_LINE);
            removeLine(TAKE_OFF_LINE);
            if (markerMap.get(LOC_MARKER) != null) markerMap.get(LOC_MARKER).hideInfoWindow();
        } else {
            if (planeMarker != null) {
                if (markerMap.get(TAKE_OFF_MARKER) != null) {
                    drawFixedLine(true, mDottedWidth, mSolidColor,
                            TAKE_OFF_LINE, markerMap.get(TAKE_OFF_MARKER).getPosition(), planeMarker.getPosition());
                }
                if (markerMap.get(LOC_MARKER) != null) {
                    drawFixedLine(true, mDottedWidth, mDottedColor, FIND_LINE,
                            markerMap.get(LOC_MARKER).getPosition(), planeMarker.getPosition());
                }
            }

        }
    }

    public LatLng getPlaneMarkerLatLng() {
        return planeMarker == null ? null : planeMarker.getPosition();
    }

    public LatLng getMobileLatLng() {
        return phoneLatlng;
    }

    /**
     * ????????????
     **/

    public void writeWaypoints() {
    }

    /**
     * ?????????????????????
     */
    public void addAroundCenterMarker() {
        if (planeMarker != null) {
            moveMarker(R.mipmap.around_center_icon, AROUND_CENTER_MARKER,
                    GPSUtils.gcj2WGSExactly(planeMarker.getPosition().latitude, planeMarker.getPosition().longitude));
        }
    }

    /**
     * ????????????
     */
    public void clearAround() {
        removeLine(AROUND_LIDE);
        removeMarker(AROUND_CENTER_MARKER);
        if (circle != null) {
            circle.remove();
            circle = null;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        mMapView.onSaveInstanceState(outState);
    }

    public void onDestroy() {
        mMapView.onDestroy();
        if (planeMarker != null)
            planeMarker.destroy();
    }

    public void onResume() {
        mMapView.onResume();
    }

    public void onPause() {
        mMapView.onPause();
        if (planeMarker != null) planeMarker.startSmoothMove();
    }

    public void onLowMemory() {
        mMapView.onLowMemory();
    }

    /**
     * ????????????????????????
     */
    public void changePhoneHeader(float angle) {
        if (mAMap != null && markerMap.get(LOC_MARKER) != null) {
            float bearing = mAMap.getCameraPosition().bearing;
            MLog.log("?????????????????????????????????=" + bearing + "||" + angle);
            markerMap.get(LOC_MARKER).setRotateAngle(bearing - angle);//
        }
    }

    public WaypointModel removeSigleWaypointFormUser(Marker marker) {
        mWaypointModel.removeWaypointMarker(marker);
        mWaypointModel.removeAllLine();
        return notifyWayPointData();
    }

}
