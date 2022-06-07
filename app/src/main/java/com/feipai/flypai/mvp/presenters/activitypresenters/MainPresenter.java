package com.feipai.flypai.mvp.presenters.activitypresenters;


import com.feipai.flypai.R;
import com.feipai.flypai.api.DataSocketReadCallback;
import com.feipai.flypai.api.RetrofitHelper;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.beans.PlaneInfo;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.beans.UserBean;
import com.feipai.flypai.connect.ConnectManager;
import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.mvp.contract.activitycontract.MainContract;
import com.feipai.flypai.mvp.contract.activitycontract.MainContract.View;
import com.feipai.flypai.ui.activity.MainActivity;
import com.feipai.flypai.updatafirmware.UpdateFirmware;
import com.feipai.flypai.utils.daoutils.DBClient;
import com.feipai.flypai.utils.global.JsonUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.NetworkUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.RxBusUtils;
import com.feipai.flypai.utils.global.ToastUtils;
import static com.feipai.flypai.app.ConstantFields.UPGRADE_FW.UPDATE_PLANE_FW;
import static com.feipai.flypai.app.ConstantFields.UPGRADE_FW.UPDATE_YUNTAI_FW;

/**
 *
 */
public class MainPresenter implements MainContract.Presenter, BasePresenter<MainContract.View> {
    protected View mView;

    private UpdateFirmware updateFirmware;

    private RetrofitHelper mRetrofitHelper;

    @Override
    public void attachView(MainContract.View view) {
        this.mView = view;
    }

    @Override
    public void detachView() {

    }

    public MainPresenter(RetrofitHelper mRetrofitHelper) {
        this.mRetrofitHelper = mRetrofitHelper;
    }

    @Override
    public boolean getVersionFromServier() {
        return false;
    }

    @Override
    public void handlerEvent(RxbusBean event) {
        switch (event.TAG) {
            case ConstantFields.UPGRADE_FW.START_UPGRADE_PLANE_FW:
                LogUtils.d("MainActivity接收由UserFragment发来的消息");
                updateFwToPlane((Integer) event.object);
                break;
            case ConstantFields.UPGRADE_FW.START_UPGRADE_CAMERA_FW:
                sendFilePrepare();
//                readCameraMainSocket((ABCmdValue) event.object);
                break;
        }
    }


    @Override
    public void sendFilePrepare() {
        mView.getCmd().startSendFile(mView, new DataSocketReadCallback() {
            @Override
            public void onReadUpdataProgress(int progress) {
                super.onReadUpdataProgress(progress);
                // TODO: 2019/7/3 更新进度
                LogUtils.d("上传camera bin进度---》" + progress);
                RxBusUtils.getDefault().post(new RxbusBean(ConstantFields.UPGRADE_FW.UPDATE_CAMERA_PROGRESS, progress));
            }

            @Override
            public void onErrorCallback(int msgId, int errorCode) {
                super.onErrorCallback(msgId, errorCode);
                //可能无卡，容量不足，或者START_SEND_FILE=1286 出错
            }
        });
    }

    @Override
    public void readCameraDataSocket(byte[] buffer) {

    }

    @Override
    public void checkPlaneFwVersion(int type) {
        initUpdateFirm();
        updateFirmware.update(type, false);
    }

    @Override
    public void resetUpdateFirm() {
        if (updateFirmware != null)
            updateFirmware.reset();
    }

    @Override
    public void initUpdateFirm() {
        if (updateFirmware == null) {
            updateFirmware = new UpdateFirmware(ConnectManager.getInstance().mProductModel.remoteIpAddress, ConstantFields.FLY_PORT.PLANE_PORT, new UpdateFirmware.UpdateCallback() {
                @Override
                public void update(int code, int progress) {
                    switch (code) {
                        case UpdateFirmware.UpdateFirmwareErrorCannotConnectToHost:
                            LogUtils.d("固件升级--->连接超时...");
                            mView.getVersionBean().initVer();
                            mView.sendHandlerMessage(ConstantFields.MESSAGE_WHAT.UPDATE_VERSION, null);
                            if (!mView.getVersionBean().getLocalPlaneVersion().equals("1")) {
                                /**上传过程中出错*/
                                RxBusUtils.getDefault().post(new RxbusBean(ConstantFields.UPGRADE_FW.UPDATE_PLANE_FW_ERROR, code));
                            }
                            break;
                        case UpdateFirmware.UpdateFirmwareReadError:
                            LogUtils.d("固件升级---飞控固件：上传读取超时");
//                            mView.sendHandlerMessage(ConstantFields.MESSAGE_WHAT.UPDATE_VERSION, null);
//                            if (!mView.getVersionBean().getLocalPlaneVersion().equals("1")) {
                            /**上传过程中出错*/
                            RxBusUtils.getDefault().post(new RxbusBean(ConstantFields.UPGRADE_FW.UPDATE_PLANE_FW_ERROR, code));
//                            }
                            break;
                        case UpdateFirmware.UpdateFirmwareErrorInvalidFirmware:
                            LogUtils.d("固件升级---飞控固件：上传固件错误");
                            mView.sendHandlerMessage(ConstantFields.MESSAGE_WHAT.UPDATE_VERSION, null);
                            RxBusUtils.getDefault().post(new RxbusBean(ConstantFields.UPGRADE_FW.UPDATE_PLANE_FW_ERROR, code));
                            break;
                        case UpdateFirmware.UpdateFirmwareErrorOK:
                            // TODO: 2019/4/19 正常 传输 progress
                            mView.uploadPlaneFw(progress);
                            if (progress >= 100) {
                                mView.getVersionBean().setLocalPlaneVersion(mView.getVersionBean().getServerPlaneVersion());
                                mView.sendHandlerMessage(ConstantFields.MESSAGE_WHAT.UPDATE_VERSION, null);
                            }
                            break;
                    }
                }

                @Override
                public void upDateYuntai(int code, int progress) {
                    if (code == UpdateFirmware.UpdateFirmwareErrorOK) {
                        mView.uploadYuntaiFw(progress);
                        if (progress >= 100) {
                            mView.getVersionBean().setLocalPlaneVersion(mView.getVersionBean().getServerYuntaiVersion());
                            mView.sendHandlerMessage(ConstantFields.MESSAGE_WHAT.UPDATE_VERSION, null);
                        }
                    } else if (code == UpdateFirmware.UpdateFirmwareReadError) {
                        LogUtils.d("固件升级---云台固件：上传网络出错了");
                        RxBusUtils.getDefault().post(new RxbusBean(ConstantFields.UPGRADE_FW.UPDATE_PLANE_FW_ERROR, code));
//                        mView.sendHandlerMessage(ConstantFields.MESSAGE_WHAT.UPDATE_VERSION, null);
                    }
                }

                @Override
                public void requestPlaneVer(boolean succed, boolean unLock, String planVer) {
                    // TODO: 2019/4/19 飞控固件检测结果
                    LogUtils.d("飞控版本号获取回调--->" + succed + "||" + unLock + "||" + planVer);
                    if (succed) {
                        mView.getVersionBean().setLocalPlaneVersion(planVer);
                        mView.getVersionBean().setUnLock(unLock);
                        mView.sendHandlerMessage(ConstantFields.MESSAGE_WHAT.UPDATE_VERSION, null);
//                        LogUtils.d("飞控版本号获取回调--->" + mView.getVersionBean().isPlaneVersionIsForceUpgrade() + "||" + mView.getBottomBarIndex());
                        if (mView.getVersionBean().isPlaneVersionIsForceUpgrade() && !unLock && mView.getBottomBarIndex() != 0) {
//                            mView.sendHandlerMessage(ConstantFields.MESSAGE_WHAT.SWITCH_MAIN_TAB, MainActivity.USER);
                        }
                    } else {
                        // TODO: 2019/4/19 飞控固件已经出错
                        mView.getVersionBean().loaclPlaneFwError();
                        mView.sendHandlerMessage(ConstantFields.MESSAGE_WHAT.UPDATE_VERSION, null);
                        if (mView.getBottomBarIndex() != 0)
                            mView.sendHandlerMessage(ConstantFields.MESSAGE_WHAT.SWITCH_MAIN_TAB, MainActivity.USER);
                    }
                }

                @Override
                public void requestYuntaiVer(boolean succed, boolean unLock, String yuntaiVer) {
                    // TODO: 2019/4/19 云台固件检测结果
                    if (succed) {
                        mView.getVersionBean().setLocalYuntaiVersion(yuntaiVer);
                        mView.getVersionBean().setUnLock(unLock);
                        mView.sendHandlerMessage(ConstantFields.MESSAGE_WHAT.UPDATE_VERSION, null);
                    } else {
                        // TODO: 2019/4/19 飞控固件已经出错
                    }
                }

                @Override
                public void requestSerial(boolean succed, String serialNo) {
                    // TODO: 2019/4/19 序列号
                    LogUtils.d("读取到飞控序列表" + serialNo + "|||" + NetworkUtils.getCurConnetWifiName());
                    PlaneInfo info = DBClient.findObjByColumn(PlaneInfo.class, PlaneInfo.COLUMNNAME_ID, NetworkUtils.getCurConnetWifiName());
                    if (info != null) {
                        info.setFlyControlSerialNumber(serialNo);
                        DBClient.updateObject(info);
                    } else {
                        info = new PlaneInfo();
                        info.setWifiName(NetworkUtils.getCurConnetWifiName());
                        info.setFlyControlSerialNumber(serialNo);
                        info.setUser_id(DBClient.findObjById(UserBean.class, 0));
                        DBClient.addObject(info);
                    }
                }

                @Override
                public void requestAct(boolean succed, int result) {
                    // TODO: 2019/4/19 是否激活
                    LogUtils.d("是否已激活--->" + succed + "||" + result);
                    if (succed) {
                        PlaneInfo info = DBClient.findObjByColumn(PlaneInfo.class, PlaneInfo.COLUMNNAME_ID, NetworkUtils.getCurConnetWifiName());
                        if (info != null) {
                            info.setAcked(result);
                            DBClient.updateObject(info);
                        } else {
                            info = new PlaneInfo();
                            info.setWifiName(NetworkUtils.getCurConnetWifiName());
                            info.setAcked(result);
                            info.setUser_id(DBClient.findObjById(UserBean.class, 0));
                            DBClient.addObject(info);
                        }
                        mView.getVersionBean().setPlaneNeedAck(result);
                        RxBusUtils.getDefault().post(new RxbusBean(ConstantFields.BusEventType.ACT_RESPONSE, result));
                    }

                }
            });
        }

    }

    @Override
    public void updateFwToPlane(int type) {
        switch (type) {
            case UPDATE_PLANE_FW:
                if (!mView.getVersionBean().isUnLock()) {
                    String firmContent = ResourceUtils.geFileFromAssets(mView.getPageActivity(), ConnectManager.getInstance().mProductModel.feikongFirmareName
                            /*"appfws/planebin.px4"*/);
                    LogUtils.d("固件升级---飞控固件：开始上传" + firmContent);
                    if (JsonUtils.isGoodJson(firmContent)) {
                        RxBusUtils.getDefault().post(new RxbusBean(ConstantFields.UPGRADE_FW.UPDATE_PLANE_PROGRESS, 0));
                        if (updateFirmware == null) {
                            initUpdateFirm();
                        }
                        updateFirmware.setFirmwareContent(firmContent);
                        updateFirmware.update(2, false);
                    } else {
                        ToastUtils.showShortToast(R.string.fw_is_damaged);
                    }
                } else {
                    RxBusUtils.getDefault().post(new RxbusBean(ConstantFields.UPGRADE_FW.UAV_UNLOCK, 3));
                }
                break;
            case UPDATE_YUNTAI_FW:
//                String path = GeneralFactory.getLocalYuntaiBinPath();
                LogUtils.d("固件升级-->云台固件开始上传");
//            if (FileUtils.fileExists(path)) {
                if (!mView.getVersionBean().isUnLock()) {
                    if (updateFirmware == null) initUpdateFirm();
                    updateFirmware.setYunFwPath(ConnectManager.getInstance().mProductModel.yuntaiFirmareName);
                    updateFirmware.update(3, false);
                } else {
                    RxBusUtils.getDefault().post(new RxbusBean(ConstantFields.UPGRADE_FW.UAV_UNLOCK, 3));
                }

                break;
        }

    }


    @Override
    public void requestAllVersionFromServer() {

    }

    @Override
    public void requestAckForServer() {

    }
}
