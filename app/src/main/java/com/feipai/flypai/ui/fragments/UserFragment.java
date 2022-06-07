package com.feipai.flypai.ui.fragments;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.feipai.flypai.R;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.base.BaseMvpFragment;
import com.feipai.flypai.beans.FunctionBean;
import com.feipai.flypai.beans.FunctionChildBean;
import com.feipai.flypai.beans.PlaneVersionBean;
import com.feipai.flypai.beans.ProductModel;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.connect.ConnectManager;
import com.feipai.flypai.mvp.contract.fragcontract.UserFragContract;
import com.feipai.flypai.mvp.presenters.fragmentpresenters.UserFragPresenter;
import com.feipai.flypai.ui.activity.MapboxOfflineActivity;
import com.feipai.flypai.ui.view.ActionDialog;
import com.feipai.flypai.ui.view.FunctionChildItemView;
import com.feipai.flypai.ui.view.FunctionItemView;
import com.feipai.flypai.ui.view.UpgradeDialog;
import com.feipai.flypai.utils.CameraCommand;
import com.feipai.flypai.utils.global.BaseExspandableDecoration;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.RxBusUtils;
import com.feipai.flypai.utils.global.ToastUtils;
import com.feipai.flypai.utils.global.Utils;
import com.feipai.flypai.utils.imageloader.GlideApp;
import com.zaihuishou.expandablerecycleradapter.adapter.BaseExpandableAdapter;
import com.zaihuishou.expandablerecycleradapter.model.ExpandableListItem;
import com.zaihuishou.expandablerecycleradapter.viewholder.AbstractAdapterItem;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;


public class UserFragment extends BaseMvpFragment<UserFragPresenter> implements UserFragContract.View {
    private ImageView mHeadImg;
    private RecyclerView rcy;
    private TextView mAppVersionTv;
    private TextView mPlaneAckTv;
    private ImageView mPeaceImg;
    private TextView mPeaceTimeTv;
    private BaseExpandableAdapter mBaseExpandableAdapter;
    public String[] nameStr = ResourceUtils.getStringArray(R.array._FUNCTION_ITEM);
    //    public int[] imgStr = new int[]{R.mipmap.function_college_img, R.mipmap.function_upgrade, R.mipmap.function_shop_img, R.mipmap.function_map_img, R.mipmap.function_setting};
    public int[] imgStr = new int[]{R.mipmap.function_college_img, R.mipmap.function_map_img};
    private String[] flypaiChild = ResourceUtils.getStringArray(R.array._FUNCTION_FLYPIE_COLLEGE_ITEM);
    private String[] planeChild = null;
    //= ResourceUtils.getStringArray(R.array._FUNCTION_PLANE_FW_ITEM);
    private String[] debugChild = ResourceUtils.getStringArray(R.array.JSCH_SETTING_DEBUG);
    private List<FunctionBean> functionBeans = new ArrayList<>();
    private final int ITEM_TYPE_PRENT = 1;
    private final int ITEM_TYPE_CHILD = 2;

    PlaneVersionBean planeVersionBean;

    private ActionDialog mActionDialog;
    private UpgradeDialog updateDialog;


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
    public void onSupportVisible() {
        super.onSupportVisible();
//        if (planeVersionBean != null)
//            RxBusUtils.getDefault().post(new RxbusBean(UPDATE_VERSION_VALUE, planeVersionBean));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected boolean isUseRxBus() {
        return true;
    }


    @Override
    protected void initView() {
        super.initView();
//        imgStr = new int[]{R.mipmap.function_college_img, R.mipmap.function_upgrade, R.mipmap.function_shop_img, R.mipmap.function_map_img, R.mipmap.function_setting};
        imgStr = new int[]{R.mipmap.function_college_img, R.mipmap.function_map_img};
        flypaiChild = ResourceUtils.getStringArray(R.array._FUNCTION_FLYPIE_COLLEGE_ITEM);
//        planeChild = ResourceUtils.getStringArray(R.array._FUNCTION_PLANE_FW_ITEM);
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
//        if (mAppVersionTv != null) {
//            planeVersionBean.isAppNeedUpgrade();
//            mAppVersionTv.setText(planeVersionBean.getLoacalAppVersion());
//        }
//        refreshAppAck(planeVersionBean.getAck());
    }

    @Override
    public void startToActivity(int requestCode, int requestType) {
        Intent intent = new Intent();
        if (requestCode == ConstantFields.INTENT_PARAM.OUTLINE_MAP) {
            intent.setClass(getPageActivity(), MapboxOfflineActivity.class);
        } else {
//            intent.setClass(getPageActivity(), FlypaiCollegeActivity.class);
//            intent.putExtra(ConstantFields.INTENT_PARAM.PAGE_CODE, requestCode);
//            intent.putExtra(ConstantFields.INTENT_PARAM.PAGE_TYPE, requestType);
        }
        startActivity(intent);
    }


    @Override
    public void refreshAppAck(int ack) {
//        planeVersionBean.setPlaneNeedAck(ack);
//        if (mPlaneAckTv != null) {
//            if (planeVersionBean.getAck() == -1) {
//                mPlaneAckTv.setText(ResourceUtils.getString(R.string.app_name));
//            } else if (planeVersionBean.getAck() == 0) {
//                mPlaneAckTv.setText(ResourceUtils.getString(R.string.app_name) + " " + ResourceUtils.getString(R.string.not_ack));
//            } else {
//                mPlaneAckTv.setText(ResourceUtils.getString(R.string.app_name) + " " + ResourceUtils.getString(R.string.acked));
//
//            }
//        }
    }

    @Override
    protected void initData() {
        super.initData();
        if (Utils.isDebug) {
            nameStr = ResourceUtils.getStringArray(R.array._FUNCTION_ITEM_DEBUG);
//            imgStr = new int[]{
//                    R.mipmap.function_college_img,
//                    R.mipmap.function_upgrade,
//                    R.mipmap.function_shop_img,
//                    R.mipmap.function_map_img,
//                    R.mipmap.function_setting,
//                    R.mipmap.function_setting,
//                    R.mipmap.function_setting};
        }
        initItemData();
        mBaseExpandableAdapter = new BaseExpandableAdapter(functionBeans) {

            @NonNull
            @Override
            public AbstractAdapterItem<Object> getItemView(Object type) {
                int itemType = (int) type;
                switch (itemType) {
                    case ITEM_TYPE_PRENT:
                        return new FunctionItemView();
                    case ITEM_TYPE_CHILD:
                        return new FunctionChildItemView();
                }
                return null;
            }

            @Override
            public Object getItemViewType(Object t) {
                if (t instanceof FunctionBean) {
                    return ITEM_TYPE_PRENT;
                } else if (t instanceof FunctionChildBean) {
                    return ITEM_TYPE_CHILD;
                }
                return super.getItemViewType(t);
            }
        };
        rcy.addItemDecoration(new BaseExspandableDecoration(24, true));
        rcy.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        rcy.setAdapter(mBaseExpandableAdapter);
        mBaseExpandableAdapter.setExpandCollapseListener(new BaseExpandableAdapter.ExpandCollapseListener() {
            @Override
            public void onListItemExpanded(int position) {
            }

            @Override
            public void onListItemCollapsed(int position) {
            }

            @Override
            public void onChildItemClick(ExpandableListItem item) {
                mPresenter.functionItemOnClick(item, nameStr, flypaiChild, planeChild, debugChild);
            }

        });

//        if (planeVersionBean != null && planeVersionBean.isPlaneVersionIsForceUpgrade()) {
//            LogUtils.d("开始上传飞控固件");
////            showUpgradeProgress(ConstantFields.UPGRADE_FW.START_UPGRADE_PLANE_FW, -1);
//        }

    }

    private void initItemData() {
        if (ConnectManager.getInstance().isConneted() && ConnectManager.getInstance().mProductModel.productType == ConstantFields.ProductType_6kAir) {
            if (functionBeans.size() > 0 && planeChild != null && planeChild.length == ResourceUtils.getStringArray(R.array._FUNCTION_PLANE_FW_ITEM_FOR6KA).length)
                return;
            planeChild = ResourceUtils.getStringArray(R.array._FUNCTION_PLANE_FW_ITEM_FOR6KA);
        } else {
            if (functionBeans.size() > 0 && planeChild != null && planeChild.length == ResourceUtils.getStringArray(R.array._FUNCTION_PLANE_FW_ITEM).length)
                return;
            planeChild = ResourceUtils.getStringArray(R.array._FUNCTION_PLANE_FW_ITEM);
        }
//        if (functionBeans != null) functionBeans.clear();
        for (int i = 0; i < imgStr.length; i++) {
            FunctionBean bean = new FunctionBean();
            List<FunctionChildBean> childBeans = new ArrayList<>();
            bean.name = nameStr[i];
            bean.img = imgStr[i];
            if (planeVersionBean != null) {
                if (i == 1) {
//                    bean.isHintImgShow = planeVersionBean.isAppNeedUpgrade() || (ConnectManager.getInstance().isConneted() && planeVersionBean != null && (planeVersionBean.isCamereNeedUpgrade() || planeVersionBean.isPlaneNeedUpgrade() || planeVersionBean.isYuntaiNeedUpgrade()));
//                    for (int j = 0; j < planeChild.length; j++) {
//                        FunctionChildBean childBean = new FunctionChildBean();
//                        childBean.index = j;
//                        childBean.name = planeChild[j];
//
//                        if (planeVersionBean != null) {
//                            if (j == 0) {
//                                childBean.isNeedUpgrade = planeVersionBean.isAppNeedUpgrade();
//                                childBean.setVersion(planeVersionBean.getLoacalAppVersion());
//                            }
//                            if (j == 1) {
//                                childBean.isNeedUpgrade = planeVersionBean.isPlaneNeedUpgrade();
//                                childBean.setVersion(planeVersionBean.getLocalPlaneVersion());
//                            }
//                            if (j == 2) {
//                                childBean.isNeedUpgrade = planeVersionBean.isCamereNeedUpgrade();
//                                childBean.setVersion(planeVersionBean.getLocalCameraVersion());
//                            }
//                            if (j == 3) {
//                                childBean.isNeedUpgrade = planeVersionBean.isYuntaiNeedUpgrade();
//                                childBean.setVersion(planeVersionBean.getLocalYuntaiVersion());
//                            }
//                        } else {
//                            childBean.isNeedUpgrade = false;
//                        }
//                        if (childBean != null)
//                            childBeans.add(childBean);
//                    }
//                    bean.mChild = childBeans;
                } else if (i == 0) {
                    bean.isHintImgShow = false;
                    for (int j = 0; j < flypaiChild.length; j++) {
                        FunctionChildBean childBean = new FunctionChildBean();
                        childBean.name = flypaiChild[j];
                        childBean.index = j;
                        childBeans.add(childBean);
                    }
                    bean.mChild = childBeans;
                } else if (i == 5 || i == 6) {
                    bean.isHintImgShow = false;
                    for (int j = 0; j < debugChild.length; j++) {
                        FunctionChildBean childBean = new FunctionChildBean();
                        childBean.name = nameStr[i] + debugChild[j];
                        childBean.index = j;
                        childBeans.add(childBean);
                    }
                    bean.mChild = childBeans;
                } else {
                    bean.isHintImgShow = false;
                }
                if (!functionBeans.contains(bean)) {
                    functionBeans.add(bean);
                } else {
                    //包含
                    int childIndex = functionBeans.indexOf(bean);
                    LogUtils.d("替换下标===>" + childIndex);
                    FunctionBean chB = functionBeans.get(childIndex);
                    LogUtils.d("替换名称===>" + chB.getItemName());
                    if (chB != null && chB.getChildItemList() != null &&
                            chB.getChildItemList().size() != bean.getChildItemList().size()) {
                        LogUtils.d("替换一次子View");
                        functionBeans.set(childIndex, bean);
                    }
                }
            }
        }
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
        if (mBaseExpandableAdapter != null)
            mBaseExpandableAdapter.notifyDataSetChanged();
    }

    @Override
    public void upgradeSuccess() {
//        ToastUtils.showShortToast(R.string.fw_uploaded_successfully);
//        if (updateDialog != null) updateDialog.dialogDismiss();
//        if (mActionDialog.getAction() == ConstantFields.ACTION_PARAM.UPGRADE_COMPLETE_RESTART) {
//            return;
//        }
//        showActionDialog(ConstantFields.ACTION_PARAM.UPGRADE_COMPLETE_RESTART);
    }

    @Override
    public void upgradeSDCardError() {
//        ToastUtils.showShortToast(R.string.fw_uploaded_error_sd_error);
//        if (updateDialog != null) updateDialog.dialogDismiss();
//        showActionDialog(ConstantFields.ACTION_PARAM.UPGRADE_ERROR);
//        if (updateDialog != null && updateDialog.isShowing()) {
//            updateDialog.dismiss();
//            updateDialog = null;
//        }
    }

    @Override
    public void upgradeFailed(int errorCode) {
//        if (updateDialog != null && updateDialog.isShowing()) {
//            updateDialog.dialogDismiss();
//        }
//        if (errorCode == 3) {
//            showActionDialog(ConstantFields.ACTION_PARAM.UPGRADE_IN_FLYING_ERROR);
//        } else {
//            showActionDialog(ConstantFields.ACTION_PARAM.UPGRADE_NET_WORK_ERROR);
//        }
//        switch (errorCode) {
//            case UpdateFirmware.UpdateFirmwareErrorCannotConnectToHost:
//                LogUtils.d("UserFrag固件升级--->连接超时...");
//                break;
//            case UpdateFirmware.UpdateFirmwareReadError:
//                LogUtils.d("UserFrag固件升级---飞控固件：上传读取超时");
//                break;
//            case UpdateFirmware.UpdateFirmwareErrorInvalidFirmware:
//                LogUtils.d("UserFrag固件升级---飞控固件：上传固件错误");
//                break;
//        }
    }

    /**
     * 显示确认对话框
     */
    @Override
    public void showActionDialog(int action) {
//        if (mActionDialog == null) {
//            mActionDialog = new ActionDialog(this, new ActionDialog.ActionDialogListener() {
//                @Override
//                public void onConfirmCallback(int action) {
//                    if (action == ConstantFields.ACTION_PARAM.UPGRADE_COMPLETE) {
//
//                    } else {
//                        if (ConnectManager.getInstance().isConneted()) {
//                            if (action == ConstantFields.ACTION_PARAM.START_UPGRADE_CAMERA_FW) {
//                                //确认升级相机
//                                mPresenter.startUpdateCameraBin();
////                            RxBusUtils.getDefault().post(new RxbusBean(ConstantFields.UPGRADE_FW.START_UPGRADE_CAMERA_FW, 1));
//                            } else if (action == ConstantFields.ACTION_PARAM.START_UPGRADE_PLANE_FW) {
//                                //确认升级飞控
//                                mPresenter.updatePlaneFw(ConstantFields.UPGRADE_FW.UPDATE_PLANE_FW);
//                            } else if (action == ConstantFields.ACTION_PARAM.START_UPGRADE_YUNTAI_FW) {
//                                //确认升级云台
//                                mPresenter.updatePlaneFw(ConstantFields.UPGRADE_FW.UPDATE_YUNTAI_FW);
//                            }
//                        } else {
//                            // TODO: 2019/4/18 网络出现问题
////                            showActionDialog(ConstantFields.ACTION_PARAM.START_UPGRADE_NET_ERROR);
//                        }
//
//                    }
//                }
//            });
//        }
//        mActionDialog.showDialog(action);
    }

    @Override
    public void dismissActionDialog() {
//        if (mActionDialog != null)
//            mActionDialog.actionDialogDismiss();
    }

    @Override
    public void dismissUpgradeDialog() {
//        if (updateDialog != null) {
//            updateDialog.dialogDismiss();
//        }
    }

    @Override
    public void showUpgradeProgress(String type, int progress) {
//        LogUtils.d("显示升级对话框--->" + type + "||" + progress);
//        if (updateDialog == null) {
//            updateDialog = new UpgradeDialog(getPageActivity(), new UpgradeDialog.CancelListener() {
//                @Override
//                public void cancelCallback(String action) {
//
//                }
//            });
//        }
//        if (progress >= 0) {
//            updateDialog.updateProgress(progress);
//        }
//        updateDialog.showDialog(type);

//        if (updateDialog == null)
//            updateDialog = new BaseDialog.Builder(getPageActivity())
//                    .setContentView(R.layout.upgrade_dialog)
//                    .setWidthAndHeight(672, 408)
//                    .create();
//        updateDialog.setCancelable(false);
//        switch (type) {
//            case ConstantFields.UPGRADE_FW.UPDATE_CAMERA_PROGRESS:
//            case ConstantFields.UPGRADE_FW.START_UPGRADE_CAMERA_FW:
//                updateDialog.setText(R.id.base_dialog_title,
//                        getResources().getString(progress >= 0 && FlyPieApplication.getInstance().isTcpSuccess() ? R.string.updating_camera_fw : R.string.confirm_updating_camera_fw));
//                break;
//            case ConstantFields.UPGRADE_FW.UPDATE_PLANE_PROGRESS:
//            case ConstantFields.UPGRADE_FW.START_UPGRADE_PLANE_FW:
//                updateDialog.setText(R.id.base_dialog_title,
//                        getResources().getString(progress >= 0 && FlyPieApplication.getInstance().isTcpSuccess() ? R.string.updating_plane_fw : R.string.confirm_updating_plane_fw));
//                break;
//            case ConstantFields.UPGRADE_FW.UPDATE_YUNTAI_PROGRESS:
//            case ConstantFields.UPGRADE_FW.START_UPGRADE_YUNTAI_FW:
//                updateDialog.setText(R.id.base_dialog_title,
//                        getResources().getString(progress >= 0 && FlyPieApplication.getInstance().isTcpSuccess() ? R.string.updating_yuntai_fw : R.string.confirm_updating_yuntai_fw));
//                break;
//        }
//        updateDialog.setText(R.id.base_dialog_content, getResources().getString(R.string.dont_close_or_disconnect));
//        updateDialog.setViewVisibility(R.id.circle_progress, progress >= 0 && FlyPieApplication.getInstance().isTcpSuccess() ? View.VISIBLE : View.GONE);
//        updateDialog.setViewVisibility(R.id.confirm_upgrade_ly, progress >= 0 && FlyPieApplication.getInstance().isTcpSuccess() ? View.GONE : View.VISIBLE);
//        updateDialog.setOnclickListener(R.id.confirm_upgrade_tv, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //TODO 开始上传文件
//                if (ConnectManager.getInstance().isConneted()) {
//                    switch (type) {
//                        case ConstantFields.UPGRADE_FW.START_UPGRADE_CAMERA_FW:
//                            //开始上传相机固件
//                            if (FlyPieApplication.getInstance().isDataTcpSuccess()) {
//                                if (cmd != null)
//                                    cmd.getFreeSDCard(new CameraCommandCallback<ABCmdValue>() {
//                                        @Override
//                                        public void onComplete(ABCmdValue data) {
//                                            RxBusUtils.getDefault().post(new RxbusBean(ConstantFields.UPGRADE_FW.START_UPGRADE_CAMERA_FW, data));
//                                        }
//                                    });
//                            }
//                            break;
//                        case ConstantFields.UPGRADE_FW.START_UPGRADE_PLANE_FW:
//                            mPresenter.updatePlaneFw(ConstantFields.UPGRADE_FW.UPDATE_PLANE_FW);
//                            break;
//                        case ConstantFields.UPGRADE_FW.START_UPGRADE_YUNTAI_FW:
//                            mPresenter.updatePlaneFw(ConstantFields.UPGRADE_FW.UPDATE_YUNTAI_FW);
//                            break;
//                        default:
//                            updateDialog.dismiss();
//                            break;
//                    }
//
//                } else {
//                    if (updateDialog.getViewVisibility(R.id.confirm_upgrade_ly) == View.VISIBLE) {
//                        // TODO: 2019/4/18 点击直接退出升级
//                        updateDialog.dismiss();
//                    } else {
//                        updateDialog.setViewVisibility(R.id.circle_progress, View.GONE);
//                        updateDialog.setViewVisibility(R.id.confirm_upgrade_ly, View.VISIBLE);
//                        updateDialog.setText(R.id.base_dialog_title, getResources().getString(R.string.cont_updating));
//                        updateDialog.setText(R.id.base_dialog_content, getResources().getString(R.string.connect_plane_retry));
//                    }
//                }
//            }
//        });
//        if (progress >= 0) {
//            updateDialog.setCircleProgress(R.id.circle_progress, progress);
//        }
//        if (!updateDialog.isShowing())
//            updateDialog.show();
    }


    @Override
    public void refreshPeaceTime(String time) {
//        mPeaceImg.setVisibility(View.VISIBLE);
//        mPeaceTimeTv.setVisibility(View.VISIBLE);
//        if (time != null) {
//            mPeaceImg.setImageResource(R.mipmap.peace_img);
//            mPeaceTimeTv.setText(ResourceUtils.getString(R.string.peace_time_end) + time);
//            mPeaceTimeTv.setTextColor(ResourceUtils.getColor(R.color.color_4097e1));
//        } else {
//            mPeaceImg.setImageResource(R.mipmap.unpeace_img);
//            mPeaceTimeTv.setText(ResourceUtils.getString(R.string.peace_time_not_to_buy));
//            mPeaceTimeTv.setTextColor(ResourceUtils.getColor(R.color.color_b3b3b3));
//        }
    }

    @Override
    public void showToast(String text) {
        ToastUtils.showLongToast(text);
    }

    @Override
    public List<FunctionBean> getFunctionBeans() {
        return functionBeans;
    }


    @Override
    public CameraCommand getCmd() {
        return cmd;
    }

    @Override
    public void onConnected(ProductModel productModel) {
        super.onConnected(productModel);
//        cmd.setMupdateComplete(new CameraCommand.CameraFirmwareupdateCompleteCallback() {
//
//            @Override
//            public void updateComplete(ABCmdValue cb) {
//                mPresenter.sendCameraBinComplete(cb);
//            }
//        });
    }

    @Override
    public void onDisConnected() {
        super.onDisConnected();
//        if (updateDialog != null && updateDialog.isShowing()) {
//            updateDialog.dialogDismiss();
//            LogUtils.d("断开网络，显示升级网络出错");
//            if (mActionDialog != null && !mActionDialog.isShowing())
//                showActionDialog(ConstantFields.ACTION_PARAM.UPGRADE_NET_WORK_ERROR);
//        }
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
