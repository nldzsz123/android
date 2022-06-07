package com.feipai.flypai.ui.fragments;

import android.content.Intent;
import android.net.wifi.ScanResult;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.RelativeLayout;

import com.feipai.flypai.R;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.base.BaseMvpFragment;
import com.feipai.flypai.base.basedialog.BaseDialog;
import com.feipai.flypai.beans.PlaneInfo;
import com.feipai.flypai.beans.ProductModel;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.connect.ConnectManager;
import com.feipai.flypai.mvp.contract.fragcontract.MainFragContract;
import com.feipai.flypai.mvp.presenters.fragmentpresenters.MainFragPresenter;
import com.feipai.flypai.utils.MLog;
import com.feipai.flypai.utils.cache.CacheManager;
import com.feipai.flypai.utils.daoutils.DBClient;
import com.feipai.flypai.utils.global.HandlerUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.NetworkUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.Utils;
import com.feipai.flypai.utils.imageloader.IImageLoader;
import com.feipai.flypai.utils.imageloader.ImageLoaderFactory;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.List;

import static com.feipai.flypai.app.ConstantFields.PREF.PLANE_TYPE;
import static com.feipai.flypai.app.ConstantFields.ProductType_4k;
import static com.feipai.flypai.app.ConstantFields.ProductType_4kAir;
import static com.feipai.flypai.app.ConstantFields.ProductType_6kAir;


public class MainFragment extends BaseMvpFragment<MainFragPresenter> implements MainFragContract.View {
    private Button mSearchButton;
    private LinearLayout mConnectHintContentLy;
    private Button mConnectButton;
    private ImageView mConnectLogo;
    private BaseDialog mBaseDialog;
    private RelativeLayout mParentBgLy;
    private RelativeLayout mBgLy;
    private int planeBgRes;
    private int planeIconRes;
    private ImageView mPlane4KIcon;
    private ImageView mPlaneAirIcon;
    private RelativeLayout mAdvertisingSpaceLy;

    @Override
    protected int initLayout() {
        return R.layout.fragment_main_layout;
    }

    @Override
    protected void initView() {
        super.initView();
        mParentBgLy = findViewById(R.id.main_fragment_parent_bg_ly);
        ImageLoaderFactory.getImageLoader().loadResourceForBackground(mParentBgLy,
                R.mipmap.main_plane_bg,
                new IImageLoader.Options(R.mipmap.main_plane_bg));
        mBgLy = findViewById(R.id.main_frag_bg_ly);
        mSearchButton = findViewById(R.id.search_button);
        mSearchButton.setOnClickListener(this);
        mSearchButton.setVisibility(Utils.isDebug ? View.VISIBLE : View.GONE);
        mConnectButton = findViewById(R.id.connect_plane_button);
        mConnectButton.setOnClickListener(this);
        mConnectHintContentLy = findViewById(R.id.connect_plane_hint_ly);
        mConnectLogo = findViewById(R.id.connect_plane_logo);
        mPlane4KIcon = findViewById(R.id.plane_icon_4k);
        mPlaneAirIcon = findViewById(R.id.plane_icon_air);
        bindPlaneType(CacheManager.getSharedPrefUtils().getInt(PLANE_TYPE, ProductType_4k));
        mAdvertisingSpaceLy = findViewById(R.id.advertising_space_ly);
        mAdvertisingSpaceLy.setOnClickListener(this);
    }

    @Override
    protected void initInject() {
        mPresenter = new MainFragPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected boolean isUseRxBus() {
        return true;
    }

    @Override
    protected void initListener() {
        super.initListener();
    }

    @Override
    protected void initRxbusListener(RxbusBean msg) {
        super.initRxbusListener(msg);
        mPresenter.handleEvent(msg);
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        // 开启连接监听
        if (!ConnectManager.getInstance().isConneted()) {
            if (NetworkUtils.getCurConnetWifiName() == null) {
                //沒有连上的WIFI
                refreshConnectButtonStatus(7, ResourceUtils.getString(R.string.plane_unconnect), R.drawable.connect_plane_button_bg_def, R.color.color_4097e1);
            } else {
                refreshConnectButtonStatus(8, ResourceUtils.getString(NetworkUtils.getCurConnetWifiName().startsWith(ConstantFields.DATA_CONFIG.FLYPAI_NAME_START) ?
                        R.string.wifi_connectting : R.string.plane_unconnect), R.drawable.connect_plane_button_bg_def, R.color.color_4097e1);
            }
        } else {
            intoCamera();
        }
    }

    @Override
    public void onSupportInvisible() {
        super.onSupportInvisible();
//        LogUtils.d("主页面隐藏===>");
        if (mBaseDialog != null && mBaseDialog.isShowing()) {
            mBaseDialog.dismiss();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
//        LogUtils.d("主页面隐藏===>" + hidden);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_button:
                mPresenter.searchWifi();
                break;
            case R.id.connect_plane_button:
                mPresenter.connectButtonToCamera();
                break;
        }
    }


    @Override
    public String getConnectButtonText() {
        return mConnectButton.getText().toString().trim();
    }

    @Override
    public void showGotoSettingDialog(String title, String content) {
        if (mBaseDialog != null) {
            if (mBaseDialog.isShowing()) {
                mBaseDialog.dismiss();
            }
            mBaseDialog = null;
        }
        mBaseDialog = new BaseDialog.Builder(getPageActivity())
                .setWidthAndHeight(AutoUtils.getPercentHeightSize(672), AutoUtils.getPercentHeightSize(408))
                .setContentView(R.layout.base_dialog)
                .setText(R.id.base_dialog_title, title)
                .setText(R.id.base_dialog_content, content)
                .setText(R.id.confirm_tv, ResourceUtils.getString(R.string.go_to_settings))
                .setText(R.id.cancel_tv, ResourceUtils.getString(R.string.end))
                .setOnClickListener(R.id.confirm_tv, v -> {
                    //TODO 确认
                    mBaseDialog.dismiss();
                    //跳转GPS设置界面
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, 1000);
                }).setOnClickListener(R.id.cancel_tv, v -> {
                    //TODO 取消
                    mBaseDialog.dismiss();
                }).create();
        if (!mBaseDialog.isShowing()) {
            mBaseDialog.show();
        }
    }

    @Override
    public void showWifiListDialog(List<ScanResult> wifiList) {

    }

    @Override
    public void refreshWifiLv() {

    }


    @Override
    public void onDisConnected() {
        super.onDisConnected();
        HandlerUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LogUtils.d("主页面断开连接..." + ConnectManager.getInstance().isConneted());
                if (NetworkUtils.getCurConnetWifiName() == null) {
                    //沒有连上的WIFI
                    refreshConnectButtonStatus(0, ResourceUtils.getString(R.string.plane_unconnect), R.drawable.connect_plane_button_bg_def, R.color.color_4097e1);
                } else {
                    refreshConnectButtonStatus(1, ResourceUtils.getString(NetworkUtils.getCurConnetWifiName().startsWith(ConstantFields.DATA_CONFIG.FLYPAI_NAME_START) ?
                            R.string.wifi_connectting : R.string.plane_unconnect), R.drawable.connect_plane_button_bg_def, R.color.color_4097e1);
                }
            }
        });


    }

    @Override
    public void onConnected(ProductModel productModel) {
        super.onConnected(productModel);
        bindPlaneType(productModel.productType);
        intoCamera();
    }

    @Override
    public void intoCamera() {
        PlaneInfo planeInfo = DBClient.findObjByColumn(PlaneInfo.class, PlaneInfo.COLUMNNAME_ID, NetworkUtils.getCurConnetWifiName());
        LogUtils.d("这个数据====>" + planeInfo);
        if (planeInfo != null && planeInfo.getAcked() == 0) {
            refreshConnectButtonStatus(3, ResourceUtils.getString(R.string.plane_need_ack), R.drawable.search_button_bg_def, R.color.color_ffffff);
        } else {
            refreshConnectButtonStatus(2, ResourceUtils.getString(R.string.into_camera), R.drawable.connect_plane_button_connected_bg, R.color.color_ffffff);
        }
    }

    @Override
    public void refreshConnectButtonStatus(int where, String text, int bgRes, int textColor) {
        LogUtils.d("刷新主页面按钮状态---->" + where + "||||" + text);
        if (mConnectLogo == null) return;
        if (text.equals(ResourceUtils.getString(R.string.into_camera)) || text.equals(ResourceUtils.getString(R.string.plane_need_ack))) {
            mConnectHintContentLy.setVisibility(View.INVISIBLE);
            mConnectLogo.setImageResource(R.mipmap.plane_connect_img);
        } else {
            mConnectHintContentLy.setVisibility(View.VISIBLE);
            mConnectLogo.setImageResource(R.mipmap.plane_img_def);
        }
        mConnectButton.setText(text);
        mConnectButton.setBackground(ResourceUtils.getDrawabe(bgRes));
        mConnectButton.setTextColor(ResourceUtils.getColor(textColor));
    }

    /**
     * 绑定飞机的型号
     */
    private void bindPlaneType(int type) {
        if (mPlane4KIcon == null) return;
        if (type == ProductType_4kAir) {
            planeBgRes = R.mipmap.splash_activity;
            planeIconRes = R.mipmap.air4k_icon;
            mPlane4KIcon.setVisibility(View.GONE);
            mPlaneAirIcon.setVisibility(View.VISIBLE);
            mPlaneAirIcon.setImageResource(planeIconRes);
            MLog.log("连接上4kAIR");
        } else if (type == ProductType_6kAir) {
            planeBgRes = R.mipmap.splash_activity;
            planeIconRes = R.mipmap.air6k_icon;
            mPlane4KIcon.setVisibility(View.GONE);
            mPlaneAirIcon.setVisibility(View.VISIBLE);
            mPlaneAirIcon.setImageResource(planeIconRes);
            MLog.log("连接上6kAIR");
        } else {
            MLog.log("连接上4k");
            planeBgRes = R.mipmap.splash_activity;
            planeIconRes = R.mipmap.flypie_type_4k;
            mPlane4KIcon.setVisibility(View.VISIBLE);
            mPlaneAirIcon.setVisibility(View.GONE);
            mPlaneAirIcon.setImageResource(planeIconRes);
        }
    }

}
