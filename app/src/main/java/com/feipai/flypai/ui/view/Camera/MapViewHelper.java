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

    private int mSolidWidth;//实线宽
    private int mSolidColor;//实线颜色
    private int mDottedWidth;//虛线宽
    private int mDottedColor;//虚线颜色
    private int mFindPlaneTakeOffLineColor;
    private int mLocationRes;//定位图标(手机位置)

    GaoDeMapView mMapView;
    private AMap mAMap;
    private MapInfoWindowAdapter mapInfoWinAdapter;
    private UiSettings mapSettings;
    private LatLng phoneLatlng;
    private Map<String, Marker> markerMap = new HashMap<>();//存储绘制的marker
    private Map<String, Polyline> linesMap = new HashMap<>();//存储地图上所有线段
    private WaypointModel mWaypointModel;
    private MapboxWayPointModel mMapboxWayPointModel;
    //    private List<Polyline> waypointPolylines = new ArrayList<>();//航点线段绘制
    private SmoothMoveMarker planeMarker = null;//飞机作为一个marker而显示
    private float planeMoveDistance = 0;
    //用于环绕圆，航点范围限定圆圈
    private Circle circle;
    private LatLng planeNewLatlng;//飞机新的经纬度坐标
    private LatLng planeOldLatlng;//飞机原始的经纬度坐标
    private View planeView;
    private View wayPointView;

    private AMapLocationClient mLocationClient = null;//定位发起端
    private AMapLocationClientOption mLocationOption = null;//定位参数
    private LocationSource.OnLocationChangedListener mListener = null;//定位监听器

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
            //随着手势旋转
            mapSettings.setRotateGesturesEnabled(true);
//            setMapCustomStyleFile(this);
//            mAMap.setLoadOfflineData(false);
            mAMap.setLoadOfflineData(true);
            mAMap.setMapLanguage(LanguageUtil.isEnglish() ? AMap.ENGLISH : AMap.CHINESE);
//            mAMap.reloadMap();
            mapSettings = mAMap.getUiSettings();
            mapSettings.setRotateGesturesEnabled(true);///随着手势旋转
            mapInfoWinAdapter = new MapInfoWindowAdapter(mContext, new MapInfoWindowAdapter.DelectWaypointListener() {
                @Override
                public void delectWaypoinit(Marker marker) {
                    MLog.log("删除当前航点..." + marker.getTitle());
                    if (mWaypointModel != null) {
                        if (mapChangeListener != null)
                            mapChangeListener.onRemoveMakrerFromUser(marker);
                    }
                }
            });
            mAMap.setInfoWindowAdapter(mapInfoWinAdapter);
//            mapSettings.setAllGesturesEnabled(true);///所有手势可用
            initAmap(mapClickListener, locationSource);
        }

    }

    /**
     * 初始化地图
     */
    private void initAmap(AMap.OnMapClickListener mapClickListener, LocationSource locationSource) {
        mAMap.setOnMapClickListener(mapClickListener);
        mAMap.setLocationSource(locationSource);// 设置定位监听
        // 自定义系统定位蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // 自定义定位蓝点图标
//        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.phone_ponit_img));
        // 自定义精度范围的圆形边框颜色
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
        // 自定义精度范围的圆形边框宽度
        myLocationStyle.strokeWidth(0);
        // 设置圆形的填充颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
        //设置是否显示定位小蓝点，用于满足只想使用定位，不想使用定位小蓝点的场景，设置false以后图面上不再有定位蓝点的概念，但是会持续回调位置信息。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);
        myLocationStyle.showMyLocation(false);
        // 将自定义的 myLocationStyle 对象添加到地图上
        mAMap.setMyLocationStyle(myLocationStyle);
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        mAMap.setMyLocationEnabled(true);
        //使用 aMap.setMapTextZIndex(2) 可以将地图底图文字设置在添加的覆盖物之上
        mAMap.setMapTextZIndex(2);
        UserBean userBean = DBClient.findObjById(UserBean.class, 0);
        if (userBean != null) {
            LogUtils.d("用户信息====>" + userBean.toString());
            if (userBean.getPlaneLat() != 0 && userBean.getPlaneLng() != 0) {
                smoothMoveMarker(0, GPSUtils.gcj2WGSExactly(userBean.getPlaneLat(), userBean.getPlaneLng()), 0);
            }
//            if (waypointDbDao.getPlanePointById(0) != null)
//                movePlaneMarker(waypointDbDao.getPlanePointById(0));
            if (userBean.getHomeLat() != 0 && userBean.getHomeLng() != 0) {
                LatLng latLng = GPSUtils.gcj2WGSExactly(userBean.getHomeLat(), userBean.getHomeLng());
                LogUtils.d("显示家的位置经纬度====>" + latLng.latitude + "||||" + latLng.longitude);
                addTakeOffMarker(latLng);
            }
        }
    }

    /**
     * 初始化定位
     */
    private void initLoc(AMapLocationListener aMapLocationListener) throws Exception {
        if (mLocationClient == null) {
            //初始化定位
            mLocationClient = new AMapLocationClient(Utils.context);
            //设置定位回调监听
            mLocationClient.setLocationListener(aMapLocationListener);
            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式（無網絡時使用）Device_Sensors
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置是否返回地址信息（默认返回地址信息）
            mLocationOption.setNeedAddress(true);
            //设置是否只定位一次,默认为false
            mLocationOption.setOnceLocation(false);
            //设置是否强制刷新WIFI，默认为强制刷新
//            mLocationOption.setWifiActiveScan(true);
            //设置是否允许模拟位置,默认为false，不允许模拟位置
            mLocationOption.setMockEnable(true);
            //设置定位间隔,单位毫秒,默认为2000ms
            mLocationOption.setInterval(2000);
            //设置是否开启定位缓存机制
            mLocationOption.setLocationCacheEnable(true);
            //给定位客户端对象设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            if (mLocationClient.getLastKnownLocation() != null) {
                mAMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
                        new LatLng(mLocationClient.getLastKnownLocation().getLatitude(),
                                mLocationClient.getLastKnownLocation().getLongitude()), 19, 0, 0)));
            }
            //启动定位
            mLocationClient.startLocation();
        }
    }

    /**
     * 地图开启定位监听
     */
    public void startLoc(OnLocationChangedListener onLocationChangedListener, AMapLocationListener aMapLocationListener) throws Exception {
        mListener = onLocationChangedListener;
        initLoc(aMapLocationListener);
    }

    /**
     * 停止定位
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
     * 定位回调处理
     */
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                if (!isLocationSuccess) {
                    isLocationSuccess = true;
                    if (mapChangeListener != null)
                        mapChangeListener.onLocationResult(isLocationSuccess);
                }
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点,为手机所在位置
                double latitudeFirst = aMapLocation.getLatitude();//获取纬度
                double longitudeFirst = aMapLocation.getLongitude();//获取经度
                phoneLatlng = new LatLng(latitudeFirst, longitudeFirst);
                MarkerOptions markerOption = new MarkerOptions();
                markerOption.position(phoneLatlng);

                if (markerMap.get(LOC_MARKER) == null) {
                    //首次定位
                    addMarker(LOC_MARKER, markerOption
                            .icon(BitmapDescriptorFactory.fromResource(mLocationRes))
                            .anchor(0.5f, 0.5f));
                    //首次定位,选择移动到地图中心点并修改级别到20级
                    mAMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()), 19, 0, 0)));
                } else {

                    //二次以后定位，使用sdk中没有的模式，让地图和小蓝点一起移动到中心点（类似导航锁车时的效果）
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
                        //绘制飞行直线
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
//                String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
//                MLog.log("定位AmapErr" + errText);
            }
        }
    }

    /**
     * 修改自定义定位小蓝点的位置
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
     * 开启离线地图
     */
    public void openOffLineMap(boolean isOpen) {
        mAMap.setLoadOfflineData(isOpen);
    }


    /**
     * 地图绘制圆圈
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
     * 绘制两点间距离
     * 保存固定直线
     *
     * @param isDottedLine 虚线true or 实线false
     * @param lineWidth    线宽
     * @param lineColor    线段绘制色
     * @param lineTag      绘制的线段标识
     * @param oldData      起始经纬度
     * @param newData      结束经纬度
     */
    private void drawFixedLine(boolean isDottedLine, int lineWidth, int lineColor, String lineTag, LatLng oldData, LatLng newData) {
        List<LatLng> latLngs = new ArrayList<>();
        if (oldData != null) {
            latLngs.add(oldData);
        }
        if (newData != null) {
            latLngs.add(newData);
        }
        // 绘制飞行轨迹
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
     * 添加marker
     */
    public void addMarker(String tag, MarkerOptions options) {
        if (!markerMap.containsKey(tag)) {
            Marker marker = mAMap.addMarker(options);
            markerMap.put(tag, marker);
        }
    }

    /**
     * 移除marker
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
            MLog.log("航点飞行时当前围栏半径---->" + fanceRadius);
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
                // 绘制飞行轨迹
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
     * 刷新航点线路
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
     * 清除所有航点
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
     * 平滑移动飞机图标
     *
     * @param planeYaw 飞机角度
     * @param newLat   新的移动坐标值
     * @param aroundR  环绕半径，在环绕中心点存在的时候
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
//        LogUtils.d("飞机移动距离====>" + planeMoveDistance + "||" + AMapUtils.calculateLineDistance(newLat, planeOldLatlng));
        planeMoveDistance += AMapUtils.calculateLineDistance(newLat, planeOldLatlng);
        if (planeMoveDistance > 2) {
            //原始位置与最初位置相差距离大于5米，即飞机发送5米的位移，则移动地图到飞机坐标为地图中心点
            //此处依然可能有手指触碰到地图时的冲突
            planeMoveDistance = 0;
            mAMap.animateCamera(CameraUpdateFactory.changeLatLng(newLat));
        }
        planeMarker.setPoints(latLngs);//设置平滑移动的轨迹list
        // 设置滑动的图标
        if (planeMarker.getMarker() != null) {
            planeMarker.setRotate(planeYaw);
        }
        planeMarker.setTotalDuration(planeOldLatlng == planeNewLatlng ? 1 : 10);//设置平滑移动的总时间
        planeMarker.startSmoothMove();
        if (takeOffPointMarker != null && planeMarker != null) {
            //绘制飞行直线
            drawFixedLine(isFindMode, isFindMode ? mDottedWidth : mSolidWidth,
                    isFindMode ? mDottedColor : mSolidColor,
                    TAKE_OFF_LINE, takeOffPointMarker.getPosition(), planeMarker.getPosition());
        }
        planeOldLatlng = newLat;
        //存储飞机的新位置
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
     * 移动的marker
     */
    public Marker moveMarker(int markerbitmap, String markerTag, LatLng latLng) {
        Marker marker = markerMap.get(markerTag);
        MarkerOptions markerOption;
        if (CoordinateConverter.isAMapDataAvailable(latLng.latitude, latLng.longitude))
            latLng = GPSUtils.wgs2GCJ(latLng.latitude, latLng.longitude);
        if (marker == null) {
            markerOption = new MarkerOptions();//创建marker设置对象
            /**
             * 图标是否可拖拽
             */
            markerOption.draggable(false);
            // 将Marker设置为贴地显示，可以双指下拉看效果
            markerOption.setFlat(false);
            if (latLng != null) {
                markerOption.position(latLng);//飞机的经纬度
            }
            if (markerbitmap != 0)
                markerOption.icon(BitmapDescriptorFactory.fromResource(markerbitmap));
            else
                markerOption.icon(BitmapDescriptorFactory.defaultMarker());
            marker = mAMap.addMarker(markerOption);
            markerMap.put(markerTag, marker);
        } else {
            /**如果已经存在，只是移动图标，可以只更新坐标**/
            markerOption = marker.getOptions();
            markerOption.position(latLng);
            marker.setMarkerOptions(markerOption);
        }
        if (markerTag.equals(TAKE_OFF_MARKER)) {
            //存储起飞点位置
            UserBean bean = DBClient.findObjById(UserBean.class, 0);
            if (bean != null) {
                bean.setHomeLat(marker.getPosition().latitude);
                bean.setHomeLng(marker.getPosition().longitude);
                bean.setUserId(0);
                DBClient.updateObject(bean);
//                LogUtils.d("存储家的位置===>" + bean.toString());
            } else {
                bean = new UserBean();
                bean.setHomeLat(marker.getPosition().latitude);
                bean.setHomeLng(marker.getPosition().longitude);
                bean.setUserId(0);
                DBClient.addObject(bean);
                LogUtils.d("添加家的位置===>" + bean.toString());
            }
        }
        return marker;//添加图标
    }

    /**
     * 添加起飞点位置
     */
    public void addTakeOffMarker(LatLng latLng) {
        moveMarker(R.mipmap.start_fly_point_img, TAKE_OFF_MARKER, latLng);
//                new LatLng(Double.parseDouble(loacationMode.getLat()), loacationMode.getLng()));
    }

    /**
     * 切换成寻找飞机的模式
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
     * 航点飞行
     **/

    public void writeWaypoints() {
    }

    /**
     * 添加环绕中心点
     */
    public void addAroundCenterMarker() {
        if (planeMarker != null) {
            moveMarker(R.mipmap.around_center_icon, AROUND_CENTER_MARKER,
                    GPSUtils.gcj2WGSExactly(planeMarker.getPosition().latitude, planeMarker.getPosition().longitude));
        }
    }

    /**
     * 清除环绕
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
     * 改变手机头部朝向
     */
    public void changePhoneHeader(float angle) {
        if (mAMap != null && markerMap.get(LOC_MARKER) != null) {
            float bearing = mAMap.getCameraPosition().bearing;
            MLog.log("手机当前位置与地图角度=" + bearing + "||" + angle);
            markerMap.get(LOC_MARKER).setRotateAngle(bearing - angle);//
        }
    }

    public WaypointModel removeSigleWaypointFormUser(Marker marker) {
        mWaypointModel.removeWaypointMarker(marker);
        mWaypointModel.removeAllLine();
        return notifyWayPointData();
    }

}
