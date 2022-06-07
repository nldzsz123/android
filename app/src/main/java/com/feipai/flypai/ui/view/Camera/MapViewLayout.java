package com.feipai.flypai.ui.view.Camera;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.feipai.flypai.R;
import com.feipai.flypai.api.MapChangeListener;
import com.feipai.flypai.beans.WaypointModel;
import com.feipai.flypai.beans.mavlinkbeans.LocationBean;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.ui.activity.CameraActivity;
import com.feipai.flypai.ui.view.FPImageButton;
import com.feipai.flypai.ui.view.mapbox.MapboxLatLng;
import com.feipai.flypai.ui.view.mapbox.MapboxWayPointModel;
import com.feipai.flypai.utils.GPSUtils;
import com.feipai.flypai.utils.PlaneCommand;
import com.feipai.flypai.utils.SmallerGestures;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.SensorUtil;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 与地图的mapview不能同名
 */
public class MapViewLayout extends RelativeLayout implements MapChangeListener {
    // 是否小窗口
    private boolean isSmall;
    @BindView(R.id.map_view)
    GaoDeMapView mMapView;
    private MapViewHelper mapViewHelper;
    GestureDetector tapDetector;
    public float distanceBetweenMobileWithPlane = 0;

    @BindView(R.id.mapbox_view)
    MyMapBoxMap mMapBoxView;
    private MapBoxViewHelper mapBoxViewHelper;

    @BindView(R.id.camera_change_ly_id)
    LinearLayout change_ly;

    @BindView(R.id.map_type_change)
    FPImageButton change_type_button;
    private int type = 0;   //0 正常 1卫星地图
    private boolean isZh;//中国地图用高德
    private Activity activity;
    private Bundle mSavedInstanceState;

    /**
     * 方向传感器
     */
    private SensorUtil mSensorUtil;
    private OnMapChangedCallback mapCallback;

    public MapViewLayout(Context context) {
        this(context, null);
    }

    public MapViewLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapViewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        mapViewHelper = new MapViewHelper(context, attrs, defStyleAttr, mMapView, this);
        mapBoxViewHelper = new MapBoxViewHelper(context, attrs, defStyleAttr, mMapBoxView, this);
    }

    private void initView(Context context) {
        mSensorUtil = new SensorUtil(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.camera_map_view, this, true);
        ButterKnife.bind(this);

        setSmall(true);
    }

    public boolean isSmall() {
        return isSmall;
    }

    public void setSmall(boolean small) {
        isSmall = small;
        if (isZh) {
            mMapView.setDispatchTouch(!isSmall);
        } else {
            mMapBoxView.setDispatchTouch(!isSmall);
            change_ly.setVisibility(isSmall ? GONE : VISIBLE);
        }
    }

    public void setMapChangeListener(OnMapChangedCallback mapCallback) {
        this.mapCallback = mapCallback;
    }


    public void toFullScreen() {
        //模拟触屏点击屏幕事件
        int x = 0;
        int y = 0;
        final long downTime = SystemClock.uptimeMillis();
        final MotionEvent downEvent = MotionEvent.obtain(
                downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0);
        final MotionEvent upEvent = MotionEvent.obtain(
                downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 0);
        //添加到webview_loading_round_iv上
        tapDetector.onTouchEvent(downEvent);
        tapDetector.onTouchEvent(upEvent);
        downEvent.recycle();
        upEvent.recycle();
    }

    public void toSmallCreen() {

    }

    // 添加单击手势
    public void addTapGesture(Context context, SmallerGestures onTapSmallGesture) {
        if (context == null || onTapSmallGesture == null) {
            return;
        }

        // 添加地图窗口的手势
        tapDetector = new GestureDetector(context, onTapSmallGesture);
        setClickable(true);
        setFocusable(true);
        if (isZh) {
            mapViewHelper.setTouch(new AMap.OnMapTouchListener() {
                @Override
                public void onTouch(MotionEvent motionEvent) {
                    tapDetector.onTouchEvent(motionEvent);
                }
            });
        } else {
            mapBoxViewHelper.setTouch(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return tapDetector.onTouchEvent(event);
                }
            });
        }
    }

    @OnClick(R.id.map_type_change)
    void onTapChangeMapTypeButton() {
        if (isZh) {

        } else {
            mapBoxViewHelper.onMapChange();
            if (type == 0) {
                type = 1;
                change_type_button.setImageResource(R.mipmap.map_regular);
                if (mapCallback != null)
                    mapCallback.onChangeMapType(type);

            } else {
                type = 0;
                change_type_button.setImageResource(R.mipmap.map_satellite);
                if (mapCallback != null)
                    mapCallback.onChangeMapType(type);
            }
        }
    }

    // 移除前面的单击手势
    public void removeTapGesture() {
        if (isZh) {
            mapViewHelper.removeTouch();
        } else {
            mapBoxViewHelper.removeTouch();
        }
    }

    /*****地图相关******************************************************/

    /**
     * 语言地区设定
     */
    public void initMapHelper(boolean isZh, Activity activity) {
        this.isZh = isZh;
        this.activity = activity;
    }

    public void initGaoDeMap(Bundle savedInstanceState, AMap.OnMapClickListener mapClickListener, LocationSource locationSource) {
        mapViewHelper.initMapView(savedInstanceState, mapClickListener, locationSource);
        mMapView.setVisibility(VISIBLE);
        mMapBoxView.setVisibility(GONE);
    }

    public void initMapboxMap(Bundle savedInstanceState, MapboxMap.OnMapClickListener mapClickListener, LocationEngineCallback<LocationEngineResult> locationEngineCallback, int t) {
        mapBoxViewHelper.initMapBoxMap(savedInstanceState, mapClickListener, locationEngineCallback, t);
        mMapView.setVisibility(GONE);
        mMapBoxView.setVisibility(VISIBLE);
        LogUtils.d("初始化mapbox");
        type = t;
    }

    public void upMapboxStyle(Bundle savedInstanceState, MapboxMap.OnMapClickListener mapClickListener, LocationEngineCallback<LocationEngineResult> locationEngineCallback, int t) {
        if (mapBoxViewHelper.getBoxMapStyle() == null)
            initMapboxMap(savedInstanceState, mapClickListener, locationEngineCallback, t);
        else {
            mapBoxViewHelper.updateBoxMapStyle(locationEngineCallback, t);
        }
    }


    public void onMapLocationChange(AMapLocation aMapLocation) {
        if (mapCallback != null) {
            LatLng mobileLatlng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
            LatLng planeLatLng = mapViewHelper.getPlaneMarkerLatLng();
            if (planeLatLng != null) {
                distanceBetweenMobileWithPlane = AMapUtils.calculateLineDistance(mobileLatlng, planeLatLng);
            }

            mapCallback.onGPGChange(aMapLocation.getAccuracy(), mobileLatlng, distanceBetweenMobileWithPlane);

        }
        mapViewHelper.onLocationChanged(aMapLocation);
    }

    /**
     * mapbox定位监听
     * 用于跟随功能距离限制
     */
    public void onMapboxLocationChange(Location location) {
        if (!isMapLoaded()) {
            return;
        }

        if (mapCallback != null) {
            MapboxLatLng mobileLatLng = new MapboxLatLng(location);
            MapboxLatLng planeLatLng = mapBoxViewHelper.getPlaneMarkerLatLng();
            if (planeLatLng != null) {
                distanceBetweenMobileWithPlane = (float) mobileLatLng.distanceTo(planeLatLng);
                //回调统一使用高德坐标系，此时应该将mapbox坐标系转换成高德坐标系
                mapCallback.onGPGChange(location.getAccuracy(), GPSUtils.wgs2GCJ(mobileLatLng.getLatitude(), mobileLatLng.getLongitude())
                        , distanceBetweenMobileWithPlane);
            }

            mapBoxViewHelper.onLocationSuccess(location);

        }
    }

    public void onMapboxLocationError(Exception exception) {
        mapBoxViewHelper.onLocationError(exception);
    }

    public void mapViewActivate(LocationSource.OnLocationChangedListener onLocationChangedListener, AMapLocationListener aMapLocationListener) throws Exception {
        mapViewHelper.startLoc(onLocationChangedListener, aMapLocationListener);
    }

    public void mapViewDeactivate() {
        mapViewHelper.stopLoc();
    }


    public void movePlaneMarker(LocationBean lBean) {
        if (lBean != null) {
            if (isZh) {
                if (lBean.getLocaLtlg() != null)
                    mapViewHelper.smoothMoveMarker(lBean.getPlaneYaw(), lBean.getLocaLtlg(), lBean.getcRadius() / 100);
            } else {
                if (lBean.getMapboxLocaLtlg() != null)
                    mapBoxViewHelper.smoothMoveMarker(lBean.getPlaneYaw(), lBean.getMapboxLocaLtlg(), lBean.getcRadius() / 100);
            }
        }
    }

    public boolean isMapLoaded() {
        if (isZh) {
            return true;
        }
        return mapBoxViewHelper.isMaploaded();
    }

    public void addHomePosition(LocationBean lBean) {
        if (isZh) {
            mapViewHelper.addTakeOffMarker(new LatLng(lBean.getHomeLt(), lBean.getHomeLg()));
        } else {
            mapBoxViewHelper.addTakeOffMarker(new MapboxLatLng(lBean.getHomeLt(), lBean.getHomeLg()));
        }
    }

    /**
     * 重置起飞点，暂时未使用，先不管
     */
    public void resetHomePosition() {
        LatLng planeLatLng = mapViewHelper.getPlaneMarkerLatLng();
        if (planeLatLng != null) {
            LatLng latLng = GPSUtils.gcj2WGSExactly(planeLatLng.latitude, planeLatLng.longitude);
            PlaneCommand.getInstance().resetHomePoint((float) latLng.latitude, (float) latLng.longitude);

        }
    }


    /**
     * 添加航点
     */
    public WaypointModel addWaypointMarker(Activity ac, LatLng latLng, int fcaneR) {
        return mapViewHelper.addWaypointMarker(ac, latLng, fcaneR);
    }

    public MapboxWayPointModel addWayPointMarkerOnBoxMap(Activity ac, com.mapbox.mapboxsdk.geometry.LatLng latLng, int fcaneR) {

        return mapBoxViewHelper.addWaypointMarker(ac, latLng, fcaneR);
    }

    /**
     * 向飞行器写入航点
     */
    public void writeWaypoints() {
        mapViewHelper.writeWaypoints();
    }

    /**
     * 清除航点飞行
     */
    public void removeWaypoint() {
        if (isZh()) {
            mapViewHelper.clearWaypoint();
        } else {
            mapBoxViewHelper.clearWaypoint();
        }
    }

    /**
     * 添加环绕中心点
     */
    public void addAroundCenterMarker() {
        if (isZh) {
            mapViewHelper.addAroundCenterMarker();
        } else {
            mapBoxViewHelper.addAroundCenterMarker();
        }
    }

    /**
     * 清除环绕飞行
     */
    public void removeAroundCenter() {
        if (isZh) {
            mapViewHelper.clearAround();
        } else {
            mapBoxViewHelper.clearAround();
        }

    }

    public boolean isMapPositioningSuccess() {
        if (isZh) {
            return mapViewHelper.getMobileLatLng() != null;
        } else {
            return mapBoxViewHelper.getPlaneMarkerLatLng() != null;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        if (isZh)
            mapViewHelper.onSaveInstanceState(outState);
        else
            mapBoxViewHelper.onSaveInstanceState(outState);
    }

    public void onDestroy() {
        if (isZh)
            mapViewHelper.onDestroy();
        else
            mapBoxViewHelper.onDestroy();
    }

    public void onResume() {
        if (isZh)
            mapViewHelper.onResume();
        else
            mapBoxViewHelper.onResume();
        if (mSensorUtil != null) {
            mSensorUtil.registerSensorListener();
            mSensorUtil.setListener(a -> {
                if (isZh)
                    mapViewHelper.changePhoneHeader(a);
                else
                    mapBoxViewHelper.changePhoneHeader(a);
            });
        }
    }

    public void onStart() {
        if (!isZh) {
            mapBoxViewHelper.onStart();
        }
    }


    public void onPause() {
        if (isZh)
            mapViewHelper.onPause();
        else
            mapBoxViewHelper.onPause();
        if (mSensorUtil != null) {
            mSensorUtil.unRegisterSensorListener();
        }
    }

    public void onStop() {
        if (!isZh)
            mapBoxViewHelper.onStop();
    }

    public void onLowMemory() {
        if (isZh)
            mapViewHelper.onLowMemory();
        else
            mapBoxViewHelper.onLowMemory();
    }

    public boolean isZh() {
        return isZh;
    }

    public void changeTofindPlaneMode(boolean isFinding) {
        if (isZh) {
            mapViewHelper.changeToFindPlane(isFinding);
        } else {
            mapBoxViewHelper.changeToFindPlane(isFinding);
        }
    }

    @Override
    public void onRemoveMakrerFromUser(Marker marker) {
        if (mapCallback != null)
            mapCallback.onWaypointRemoved(marker);
    }

    public WaypointModel removeWaypointFromUser(Marker marker) {
        return mapViewHelper.removeSigleWaypointFormUser(marker);
    }

    @Override
    public void onAroundChange(double aroundRadius) {
        if (mapCallback != null)
            mapCallback.onAroundChanged(aroundRadius);
    }

    @Override
    public void onLocationResult(boolean isSuccess) {
        if (mapCallback != null)
            mapCallback.onLocationResult(isSuccess);
    }

    /**
     *  mapstyle change button 是否可点击
     */
    public void showMapChangeEnabled(boolean enabled) {
        change_type_button.setEnabled(enabled);
        change_type_button.setAlpha(enabled ? 1f : 0.5f);
    }

    public interface OnMapChangedCallback {
        void onWaypointRemoved(Marker marker);

        void onAroundChanged(double aroundRadius);

        void onGPGChange(float mobileGPS, LatLng mobileLatlng, float distance);

        void onLocationResult(boolean isSuccess);

        void onChangeMapType(int type);
    }
}
