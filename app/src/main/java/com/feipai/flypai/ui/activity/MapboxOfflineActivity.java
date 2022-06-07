package com.feipai.flypai.ui.activity;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.feipai.flypai.base.BaseMvpActivity;
import com.feipai.flypai.mvp.contract.activitycontract.MapboxOfflineContract;
import com.feipai.flypai.mvp.presenters.activitypresenters.MapboxOfflineActivityPresenter;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.ToastUtils;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;

import timber.log.Timber;
import static com.feipai.flypai.ui.view.Camera.MapBoxViewHelper.DEFAULT_DISPLACEMENT;
import static com.feipai.flypai.ui.view.Camera.MapBoxViewHelper.DEFAULT_INTERVAL;
import static com.feipai.flypai.ui.view.Camera.MapBoxViewHelper.DEFAULT_MAX_WAIT_TIME;
import static com.feipai.flypai.ui.view.Camera.MapBoxViewHelper.DEFAULT_FASTEST_INTERVAL;

public class MapboxOfflineActivity extends BaseMvpActivity<MapboxOfflineActivityPresenter> implements
        MapboxOfflineContract.View,
        View.OnClickListener,
        LocationEngineCallback<LocationEngineResult> {

    // UI elements
    private MapView mMapView;
    private MapboxMap mMap;
    private ProgressBar progressBar;
    private Button downloadButton;
    private Button listButton;
    private TextView mTitleTv;
    private ImageView mBackImg;


    private boolean isEndNotified;
    private int regionSelected;

    // Offline objects
    private OfflineManager mOfflineManager;
    private OfflineRegion mOfflineRegion;

    private LocationEngineCallback<LocationEngineResult> locationEngineCallback;
    private LocationEngine locationEngine;

    @Override
    protected int initLayout() {
        return R.layout.mapbox_offline_activity_layout;
    }

    @Override
    protected void initWindow() {
        super.initWindow();
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_SHOW_BAR)
                .navigationBarColor(R.color.color_ffffff) //导航栏颜色，不写默认黑色
                .navigationBarDarkIcon(true) //导航栏图标是深色，不写默认为亮色
                .flymeOSStatusBarFontColor(R.color.color_000000)//修改flyme OS状态栏字体颜色
                .statusBarDarkFont(true)//状态栏字体是深色，不写默认为亮色
                .init();


    }

    @Override
    protected boolean isUseRxBus() {
        return super.isUseRxBus();
    }

    @Override
    protected void initListener() {
        super.initListener();
    }


    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        // Set up the MapView
        mBackImg = findViewById(R.id.title_back);
        mBackImg.setOnClickListener(this);
        mTitleTv = findViewById(R.id.title_tv);
        mTitleTv.setText(ResourceUtils.getString(R.string.offline_map));
        mMapView = findViewById(R.id.offline_mapview);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mMap = mapboxMap;
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        initLocationComponent(style, MapboxOfflineActivity.this);
                        // Assign progressBar for later use
                        progressBar = findViewById(R.id.offline_progress_bar);
                        // Set up the offlineManager
                        mOfflineManager = OfflineManager.getInstance(MapboxOfflineActivity.this);
                        // Bottom navigation bar button clicks are handled here.
                        // Download offline button
                        downloadButton = findViewById(R.id.offline_download_button);
                        downloadButton.setOnClickListener(MapboxOfflineActivity.this);
                        // List offline regions
                        listButton = findViewById(R.id.offline_list_button);
                        listButton.setOnClickListener(MapboxOfflineActivity.this);
                    }
                });
            }
        });
//        BarUtils.setTransparentStatusBar(this);
    }


    /**
     * 激活定位
     */
    @SuppressWarnings({"MissingPermission"})
    private void initLocationComponent(Style loadedMapStyle, LocationEngineCallback<LocationEngineResult> locationEngineCallback) {
        if (PermissionsManager.areLocationPermissionsGranted(getPageActivity())) {
            LocationComponent locationComponent = mMap.getLocationComponent();
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(getPageActivity(), loadedMapStyle).build());
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationChangeListener(locationEngineCallback);
        }

    }

    /**
     * 初始化定位监听
     */
    @SuppressWarnings({"MissingPermission"})
    private void initLocationChangeListener(LocationEngineCallback<LocationEngineResult> locationEngineCallback) {
        this.locationEngineCallback = locationEngineCallback;
        locationEngine = LocationEngineProvider.getBestLocationEngine(getPageActivity());

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
                .setDisplacement(DEFAULT_DISPLACEMENT)
                //设置位置更新的最大等待时间（以毫秒为单位）。
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                //设置位置更新的最快间隔（以毫秒为单位）
                .setFastestInterval(DEFAULT_FASTEST_INTERVAL)
                .build();

        locationEngine.requestLocationUpdates(request, locationEngineCallback, getMainLooper());
        locationEngine.getLastLocation(locationEngineCallback);
    }

    @Override
    protected void initInject() {
        mPresenter = new MapboxOfflineActivityPresenter();
        mPresenter.attachView(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_back:
                MapboxOfflineActivity.this.finish();
                break;
            case R.id.offline_download_button:
                showDownloadOfflineMapDialog();
                break;
            case R.id.offline_list_button:
                mPresenter.downloadedMapList(mMap, mOfflineManager);
                break;
        }
    }


    private void showDownloadOfflineMapDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapboxOfflineActivity.this);
        final EditText regionNameEdit = new EditText(MapboxOfflineActivity.this);
        regionNameEdit.setHint(getString(R.string.set_region_name_hint));

        // Build the dialog box
        builder.setTitle(getString(R.string.map_offline_dialog_title))
                .setView(regionNameEdit)
                .setMessage(getString(R.string.map_offline_dialog_message))
                .setPositiveButton(getString(R.string.map_offline_download), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String regionName = regionNameEdit.getText().toString();
                        // Require a region name to begin the download.
                        // If the user-provided string is empty, display
                        // a toast message and do not begin download.
                        if (regionName.length() == 0) {
                            showToast(ResourceUtils.getString(R.string.map_offline_dialog_toast));
                        } else {
                            // Begin download process
                            mPresenter.downloadOfflineMap(mMap, mOfflineManager, regionName);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.map_offline_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        // Display the dialog
        builder.show();


    }

    @Override
    public OfflineRegion getOfflineRegion() {
        return mOfflineRegion;

    }

    @Override
    public void setOfflineRegion(OfflineRegion region) {
        this.mOfflineRegion = region;
    }

    @Override
    public void showToast(String text) {
        ToastUtils.showShortToast(text);
    }

    @Override
    public void updateProgress(int percentage) {
        progressBar.setIndeterminate(false);
        progressBar.setProgress(percentage);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void downloadSuccess() {
        if (isEndNotified) {
            return;
        }
        // Enable buttons
        downloadButton.setEnabled(true);
        listButton.setEnabled(true);

        // Stop and hide the progress bar
        isEndNotified = true;
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);
        showToast(ResourceUtils.getString(R.string.map_offline_download_success));
    }

    @Override
    public void setRegionSelected(int regionSelectedId) {
        this.regionSelected = regionSelectedId;
    }

    @Override
    public int getRegionSelected() {
        return regionSelected;
    }

    @Override
    public void showProgressBar(boolean isShow) {
        progressBar.setIndeterminate(isShow);
        progressBar.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onRegionDeleteSuccess() {
        showProgressBar(false);
        showToast(ResourceUtils.getString(R.string.toast_region_deleted));
    }

    @Override
    public void onRegionDeletedError(String error) {
        showProgressBar(false);
        Timber.e("Error: %s", error);
    }

    //================================
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if (mOfflineRegion != null)
            mOfflineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE);
    }

    @Override
    public void onSuccess(LocationEngineResult result) {
        //定位成功
    }

    @Override
    public void onFailure(@NonNull Exception exception) {
        //定位失败
    }
}
