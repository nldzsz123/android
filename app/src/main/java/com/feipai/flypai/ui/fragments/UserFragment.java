package com.feipai.flypai.ui.fragments;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.feipai.flypai.R;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.base.BaseMvpFragment;
import com.feipai.flypai.beans.PlaneVersionBean;
import com.feipai.flypai.beans.ProductModel;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.mvp.contract.fragcontract.UserFragContract;
import com.feipai.flypai.mvp.presenters.fragmentpresenters.UserFragPresenter;
import com.feipai.flypai.ui.activity.MapboxOfflineActivity;
import com.feipai.flypai.utils.CameraCommand;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.RxBusUtils;
import com.feipai.flypai.utils.global.ToastUtils;
import com.feipai.flypai.utils.global.Utils;
import com.feipai.flypai.utils.imageloader.GlideApp;

import io.reactivex.android.schedulers.AndroidSchedulers;


public class UserFragment extends BaseMvpFragment<UserFragPresenter> implements UserFragContract.View {
    private ImageView mHeadImg;
    private RecyclerView rcy;
    private TextView mAppVersionTv;
    private TextView mPlaneAckTv;
    private ImageView mPeaceImg;
    private TextView mPeaceTimeTv;
    public String[] nameStr = ResourceUtils.getStringArray(R.array._FUNCTION_ITEM);
    public int[] imgStr = new int[]{R.mipmap.function_college_img, R.mipmap.function_map_img};
    private String[] flypaiChild = ResourceUtils.getStringArray(R.array._FUNCTION_FLYPIE_COLLEGE_ITEM);
    private String[] planeChild = null;
    private String[] debugChild = ResourceUtils.getStringArray(R.array.JSCH_SETTING_DEBUG);

    PlaneVersionBean planeVersionBean;

    @Override
    protected void initInject() {
        mPresenter = new UserFragPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_user_layout;
    }

    @Override
    protected boolean isUseRxBus() {
        return true;
    }


    @Override
    protected void initView() {
        super.initView();
        imgStr = new int[]{R.mipmap.function_college_img, R.mipmap.function_map_img};
        flypaiChild = ResourceUtils.getStringArray(R.array._FUNCTION_FLYPIE_COLLEGE_ITEM);
        mHeadImg = findViewById(R.id.user_info_head_img);
        GlideApp.with(getActivity())
                .load(R.mipmap.app_icon_round)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(mHeadImg);
        rcy = findViewById(R.id.user_info_rcy);
        mPlaneAckTv = findViewById(R.id.user_info_plane_ack);
        mAppVersionTv = findViewById(R.id.user_info_app_version_tv);
        mPeaceImg = findViewById(R.id.user_info_peace_img);
        mPeaceTimeTv = findViewById(R.id.user_info_peace_time_tv);
        mPresenter.refreshPeaceStatus();
    }


    @Override
    public void initVersionBean(PlaneVersionBean bean) {
        initItemData();
        this.planeVersionBean = bean;
    }

    @Override
    public void startToActivity(int requestCode, int requestType) {
        Intent intent = new Intent();
        if (requestCode == ConstantFields.INTENT_PARAM.OUTLINE_MAP) {
            intent.setClass(getPageActivity(), MapboxOfflineActivity.class);
        } else {
        }
        startActivity(intent);
    }


    @Override
    public void refreshAppAck(int ack) {
    }

    @Override
    protected void initData() {
        super.initData();
        if (Utils.isDebug) {
            nameStr = ResourceUtils.getStringArray(R.array._FUNCTION_ITEM_DEBUG);
        }
        initItemData();
    }

    private void initItemData() {
    }

    @Override
    protected void initListener() {
        super.initListener();
        RxBusUtils.getDefault().toFlowable(RxbusBean.class)
                .observeOn(AndroidSchedulers.mainThread())
                .filter(event -> isVisible())
                .doOnNext(event -> {

                }).subscribe();
    }

    @Override
    protected void initRxbusListener(RxbusBean msg) {
        super.initRxbusListener(msg);
        mPresenter.handleEvent(msg);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void notifyAdapter() {
    }

    @Override
    public void upgradeSuccess() {
    }

    @Override
    public void upgradeSDCardError() {
    }

    @Override
    public void upgradeFailed(int errorCode) {
    }

    /**
     * 显示确认对话框
     */
    @Override
    public void showActionDialog(int action) {
    }

    @Override
    public void dismissActionDialog() {
    }

    @Override
    public void dismissUpgradeDialog() {
    }

    @Override
    public void showUpgradeProgress(String type, int progress) {
    }


    @Override
    public void refreshPeaceTime(String time) {
    }

    @Override
    public void showToast(String text) {
        ToastUtils.showLongToast(text);
    }


    @Override
    public CameraCommand getCmd() {
        return cmd;
    }

    @Override
    public void onConnected(ProductModel productModel) {
        super.onConnected(productModel);
    }

    @Override
    public void onDisConnected() {
        super.onDisConnected();
    }

    @Override
    public void showDefDialog(String text) {
        showKpDialog(text);
    }

    @Override
    public void dismissDefDialog() {
        dismissKpDialog();
    }
}
