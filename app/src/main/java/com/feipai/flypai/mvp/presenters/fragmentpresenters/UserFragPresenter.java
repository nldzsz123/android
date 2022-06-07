package com.feipai.flypai.mvp.presenters.fragmentpresenters;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.feipai.flypai.api.CameraCommandCallback;
import com.feipai.flypai.api.DataSocketReadCallback;
import com.feipai.flypai.api.RxLoopObserver;
import com.feipai.flypai.api.RxLoopSchedulers;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.app.FlyPieApplication;
import com.feipai.flypai.beans.ABCmdValue;
import com.feipai.flypai.beans.FunctionBean;
import com.feipai.flypai.beans.FunctionChildBean;
import com.feipai.flypai.beans.PlaneInfo;
import com.feipai.flypai.beans.PlaneVersionBean;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.beans.UserBean;
import com.feipai.flypai.connect.ConnectManager;
import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.mvp.contract.fragcontract.UserFragContract;
import com.feipai.flypai.ui.activity.CameraActivity;
import com.feipai.flypai.updatafirmware.UpdateFirmware;
import com.feipai.flypai.utils.CameraCommand;
import com.feipai.flypai.utils.cache.CacheManager;
import com.feipai.flypai.utils.daoutils.DBClient;
import com.feipai.flypai.utils.global.IntentUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.RegexUtils;
import com.feipai.flypai.utils.global.RemoteJschUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.RxBusUtils;
import com.feipai.flypai.utils.global.StringUtils;
import com.feipai.flypai.utils.global.TimeUtils;
import com.feipai.flypai.utils.global.ToastUtils;
import com.feipai.flypai.utils.global.Utils;
import com.zaihuishou.expandablerecycleradapter.model.ExpandableListItem;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.functions.Function;

import static com.feipai.flypai.app.ConstantFields.BusEventType.*;
import static com.feipai.flypai.app.ConstantFields.CAMERA_CONFIG.SD_INTERNAL;
import static com.feipai.flypai.app.ConstantFields.CAMERA_CONFIG.START_SEND_FILE;
import static com.feipai.flypai.app.ConstantFields.INTENT_PARAM.*;
import static com.feipai.flypai.app.ConstantFields.ProductType_4k;
import static com.feipai.flypai.app.ConstantFields.ProductType_4kAir;
import static com.feipai.flypai.app.ConstantFields.ProductType_6kAir;
import static com.feipai.flypai.app.ConstantFields.UPGRADE_FW.*;

public class UserFragPresenter implements UserFragContract.Presenter, BasePresenter<UserFragContract.View> {

    private UserFragContract.View mView;


    @Inject
    public UserFragPresenter() {

    }


    @Override
    public void attachView(UserFragContract.View view) {
        this.mView = view;
    }

    @Override
    public void detachView() {

    }


    @Override
    public void refreshVersion(PlaneVersionBean planeVersionBean) {//, List<FunctionBean> functionBeans
        mView.initVersionBean(planeVersionBean);
        if (mView.getFunctionBeans().size() < 1 || !(mView.getFunctionBeans().get(1) instanceof FunctionBean))
            return;
        FunctionBean functionBean = mView.getFunctionBeans().get(1);
        List<FunctionChildBean> versionChilds = functionBean.mChild;
        if (planeVersionBean.isAppNeedUpgrade() || (ConnectManager.getInstance().isConneted() && (planeVersionBean.isCamereNeedUpgrade() || planeVersionBean.isPlaneNeedUpgrade() || planeVersionBean.isYuntaiNeedUpgrade()))) {
            functionBean.isHintImgShow = true;
        } else {
            functionBean.isHintImgShow = false;
        }
        if (versionChilds != null) {
            versionChilds.get(0).isNeedUpgrade = planeVersionBean.isAppNeedUpgrade();
            versionChilds.get(1).isNeedUpgrade = planeVersionBean.isPlaneNeedUpgrade();
            versionChilds.get(1).setVersion(planeVersionBean.getLocalPlaneVersion());
            versionChilds.get(2).isNeedUpgrade = planeVersionBean.isCamereNeedUpgrade();
            versionChilds.get(2).setVersion(planeVersionBean.getLocalCameraVersion());
            versionChilds.get(3).isNeedUpgrade = planeVersionBean.isYuntaiNeedUpgrade();
            versionChilds.get(3).setVersion(planeVersionBean.getLocalYuntaiVersion());
        }
        mView.notifyAdapter();
        if (!planeVersionBean.getLocalPlaneVersion().equals("1") && planeVersionBean.isPlaneVersionIsForceUpgrade()) {
            //飞控强制升级,先注释掉
            LogUtils.d("开始上传飞控固件");
//            mView.showUpgradeProgress(ConstantFields.UPGRADE_FW.START_UPGRADE_PLANE_FW, -1);
        }

    }

    @Override
    public void functionItemOnClick(ExpandableListItem item, String[] nameStr, String[] flypaiChild, String[] planeChild, String[] debugChild) {
        String itemName = item.getItemName();
        LogUtils.d("当前选择的item，无子View的item单击,onChildItemClick==》itemName=" + itemName);
        if (itemName.equals(flypaiChild[0])) {
            // TODO: 2020/7/18 视频教程
//            mView.startToActivity(VIDEO_TEACH, 0);
//            LogUtils.d("选择第一项" + flypaiChild[0]);
        } else if (itemName.equals(flypaiChild[1])) {
            // TODO: 2020/7/18 用户手册
//            LogUtils.d("选择第二项" + flypaiChild[1]);
        } else if (itemName.equals(nameStr[1])) {
//            LogUtils.d("选择第二项" + nameStr[1]);
            mView.startToActivity(OUTLINE_MAP, 0);
        }
//        if (itemName.equals(nameStr[2])) {
//            // TODO: 2019/4/13 商城
////            IntentUtils.openTaobaoShopping(mView.getPageActivity());
//        } else if (itemName.equals(nameStr[3])) {
//            // TODO: 2019/7/24 离线地图
////            mView.startToActivity(OUTLINE_MAP, 0);
//        } else if (itemName.equals(nameStr[4])) {
//            // TODO: 2019/4/13 设置
//            mView.startToActivity(APP_SETTING, 0);
//        } else if (itemName.equals(flypaiChild[0])) {
//            // TODO: 2019/4/13 视频教程
////            mView.startToActivity(VIDEO_TEACH, 0);
//        } else if (itemName.equals(flypaiChild[1])) {
//            // TODO: 2019/4/13 4K说明书
//            mView.startToActivity(SPECIFICATION, ProductType_4k);
//        } else if (itemName.equals(flypaiChild[2])) {
//            // TODO: 2019/8/2 4kair 说明书
//            mView.startToActivity(SPECIFICATION, ProductType_4kAir);
//        } else if (itemName.equals(flypaiChild[3])) {
//            // TODO: 2019/8/2 6k 说明书
//            mView.startToActivity(SPECIFICATION, ProductType_6kAir);
//        } else if (itemName.equals(flypaiChild[4])) {
//            // TODO: 2019/4/13 实名登记教程
//            LogUtils.d("点击实名登记");
////            mView.startToActivity(REAL_NAME_REGISTRATION, 0);
//        } else if (itemName.equals(planeChild[0])) {
//            // TODO: 2019/4/13 APP更新
//            if (item instanceof FunctionChildBean) {
//                FunctionChildBean bean = (FunctionChildBean) item;
//                if (bean.isNeedUpgrade) {
//                    if (!IntentUtils.openYingYongBao(mView.getPageActivity())) {
//                        mView.showToast(ResourceUtils.getString(R.string.install_yinyongbao_client_first));
//                    }
////                    LogUtils.d("开始上传飞控固件");
////                    mView.showUpgradeProgress(ConstantFields.UPGRADE_FW.START_UPGRADE_PLANE_FW, -1);
//                } else {
//                    if (!StringUtils.isEmpty(bean.getVersion()))
//                        mView.showToast(ResourceUtils.getString(R.string.do_not_need_to_upgrade_app));
//                }
//            }
//        } else if (itemName.equals(planeChild[1])) {
//            // TODO: 2019/4/13 飞控固件更新
//            if (item instanceof FunctionChildBean) {
//                FunctionChildBean bean = (FunctionChildBean) item;
//                if (ConnectManager.getInstance().isConneted()) {
//                    if (bean.isNeedUpgrade) {
//                        LogUtils.d("开始上传飞控固件");
//                        mView.showActionDialog(ConstantFields.ACTION_PARAM.START_UPGRADE_PLANE_FW);
//                    } else {
//                        if (!StringUtils.isEmpty(bean.getVersion()))
//                            mView.showToast(ResourceUtils.getString(R.string.do_not_need_to_upgrade_fw));
//                        else {
//                            mView.showToast(ResourceUtils.getString(R.string.connect_flypie));
//                        }
//                    }
//                } else {
//                    mView.showToast(ResourceUtils.getString(R.string.connect_flypie));
//                }
//            }
//        } else if (itemName.equals(planeChild[2])) {
//            // TODO: 2019/4/13 相机固件更新
//            if (item instanceof FunctionChildBean) {
//                FunctionChildBean bean = (FunctionChildBean) item;
//                if (ConnectManager.getInstance().isConneted() && !StringUtils.isEmpty(bean.getVersion()) && bean.getVersion().length() > 1) {
//                    if (bean.isNeedUpgrade) {
//                        mView.showActionDialog(ConstantFields.ACTION_PARAM.START_UPGRADE_CAMERA_FW);
//                    } else {
//                        if (!StringUtils.isEmpty(bean.getVersion()))
//                            mView.showToast(ResourceUtils.getString(R.string.do_not_need_to_upgrade_fw));
//                        else {
//                            mView.showToast(ResourceUtils.getString(R.string.connect_flypie));
//                        }
//                    }
//                } else {
//                    mView.showToast(ResourceUtils.getString(R.string.connect_flypie));
//                }
//            }
//
//        } else if (itemName.equals(planeChild[3])) {
//            // TODO: 2019/4/13 云台固件升级
////            if (Utils.isDebug) {
//            if (item instanceof FunctionChildBean) {
//                FunctionChildBean bean = (FunctionChildBean) item;
//                if (ConnectManager.getInstance().isConneted()) {
//                    if (bean.isNeedUpgrade) {
//                        mView.showActionDialog(ConstantFields.ACTION_PARAM.START_UPGRADE_YUNTAI_FW);
//                    } else {
//                        mView.showToast(ResourceUtils.getString(R.string.do_not_need_to_upgrade_fw));
//                    }
//                } else {
//                    mView.showToast(ResourceUtils.getString(R.string.connect_flypie));
//                }
//            }
////            }
//        } else if (itemName.equals(planeChild[4])) {
//            // TODO: 2019/11/30 图传升级
//            startCheckFigureUpgrade();
////            startFigureUpgrade();
//        } else if (itemName.endsWith(debugChild[0])) {
//            // TODO: 2019/11/1 参数设置
//            LogUtils.d("参数设置");
//            RxLoopSchedulers.composeIO(mView, new Function() {
//                @Override
//                public String apply(Object object) throws Exception {
//                    String result = "";
//                    if (itemName.startsWith(ResourceUtils.getStringArray(R.array._FUNCTION_ITEM_DEBUG)[5])) {
//                        //天空端
//                        result = RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP424, ConstantFields.SHELL_CMD.UIC_SET_CMD);
//                    } else {
//                        //地面端
//                        result = RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP421, ConstantFields.SHELL_CMD.UIC_SET_CMD);
//                    }
//                    return result;
//                }
//            }).subscribe(new RxLoopObserver<String>() {
//                @Override
//                public void onNext(String value) {
//                    super.onNext(value);
//                    this.disposeDisposables();
//                    ToastUtils.showLongToast("设置参数成功");
//                    LogUtils.d("设置参数成功=" + value);
//                }
//            });
//        } else if (itemName.endsWith(debugChild[1])) {
//            // TODO: 2019/11/1 提交设置
//            LogUtils.d("参数提交");
//            RxLoopSchedulers.composeIO(mView, new Function() {
//                @Override
//                public String apply(Object object) throws Exception {
//                    String result = "";
//                    if (itemName.startsWith(ResourceUtils.getStringArray(R.array._FUNCTION_ITEM_DEBUG)[5])) {
//                        //天空端
//                        result = RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP424, ConstantFields.SHELL_CMD.UIC_COMMIT_CMD);
//                    } else {
//                        //地面端
//                        result = RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP421, ConstantFields.SHELL_CMD.UIC_COMMIT_CMD);
//                    }
//                    return result;
//                }
//            }).subscribe(new RxLoopObserver<String>() {
//                @Override
//                public void onNext(String value) {
//                    super.onNext(value);
//                    this.disposeDisposables();
//                    ToastUtils.showLongToast("提交参数成功");
//                    LogUtils.d("提交参数成功=" + value);
//                }
//            });
//        } else if (itemName.endsWith(debugChild[2])) {
//            // TODO: 2019/11/1 读取设置
//            LogUtils.d("参数读取");
//            RxLoopSchedulers.composeIO(mView, new Function() {
//                @Override
//                public String apply(Object object) throws Exception {
//                    String result = "";
//                    if (itemName.startsWith(ResourceUtils.getStringArray(R.array._FUNCTION_ITEM_DEBUG)[5])) {
//                        //天空端
//                        result = RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP424, ConstantFields.SHELL_CMD.UIC_GET_CMD);
//                    } else {
//                        //地面端
//                        result = RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP421, ConstantFields.SHELL_CMD.UIC_GET_CMD);
//                    }
//                    return result;
//                }
//            }).subscribe(new RxLoopObserver<String>() {
//                @Override
//                public void onNext(String value) {
//                    super.onNext(value);
//                    this.disposeDisposables();
//                    ToastUtils.showLongToast("读取参数成功" + value);
//                    LogUtils.d("读取参数成功=" + value);
//                }
//            });
//        } else if (itemName.endsWith(debugChild[3])) {
//            // TODO: 2019/11/1 保存设置
//            LogUtils.d("参数保存");
//            RxLoopSchedulers.composeIO(mView, new Function() {
//                @Override
//                public String apply(Object object) throws Exception {
//                    String result = "";
//                    if (itemName.startsWith(ResourceUtils.getStringArray(R.array._FUNCTION_ITEM_DEBUG)[5])) {
//                        //天空端
//                        result = RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP424, ConstantFields.SHELL_CMD.RELOAD_CONFIG_CMD);
//                    } else {
//                        //地面端
//                        result = RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP421, ConstantFields.SHELL_CMD.RELOAD_CONFIG_CMD);
//                    }
//                    return result;
//                }
//            }).subscribe(new RxLoopObserver<String>() {
//                @Override
//                public void onNext(String value) {
//                    super.onNext(value);
//                    this.disposeDisposables();
//                    ToastUtils.showLongToast("保存参数成功");
//                    LogUtils.d("保存参数成功=" + value);
//                }
//            });
//        }
    }

    /**
     * 检测图传是否已是最新
     */
    private void startCheckFigureUpgrade() {
        mView.showDefDialog(ResourceUtils.getString(R.string.detecting));
        RxLoopSchedulers.composeIO(mView, new Function() {
            @Override
            public Boolean apply(Object object) throws Exception {
                //天空端读取参数
                String result1 = RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP424, ConstantFields.SHELL_CMD.UIC_GET_CMD);
                if (RegexUtils.canParseInt(result1.trim()) && Integer.parseInt(result1.trim()) == 30) {
                    //如果设置成了30，则继续
                    //地面端读取参数
                    String result2 = RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP421, ConstantFields.SHELL_CMD.UIC_GET_CMD);
                    if (RegexUtils.canParseInt(result2.trim()) && Integer.parseInt(result2.trim()) == 30) {
                        //地面端保存参数
                        LogUtils.d("更新图传====》地面是30" + result2);
                        return true;
                    }
                }
                return false;
            }
        }).subscribe(new RxLoopObserver<Boolean>() {
            @Override
            public void onNext(Boolean value) {
                super.onNext(value);
                this.disposeDisposables();
                if (value) {
                    mView.dismissDefDialog();
                    ToastUtils.showShortToast(ResourceUtils.getString(R.string.transmission_is_newest));
                    LogUtils.d("更新图传已是最新");
                } else {
                    LogUtils.d("更新图传不是最新开始升级");
                    startFigureUpgrade();
                }
            }
        });
    }


    @Override
    public void updateCameraFw(Context context, String path) {

    }

    @Override
    public void updatePlaneFw(int type) {
        RxBusUtils.getDefault().post(new RxbusBean(ConstantFields.UPGRADE_FW.START_UPGRADE_PLANE_FW, type));
    }

    @Override
    public void refreshPeaceStatus() {
        UserBean bean = DBClient.findObjById(UserBean.class, 0);
        if (bean != null) {
            List<PlaneInfo> infos = DBClient.findObjByColumns(PlaneInfo.class, PlaneInfo.COLUMNNAME_USER_ID, bean.getPhoneNumb());
            if (infos != null && infos.size() > 0) {
                PlaneInfo info = infos.get(0);
                String endTime = info.getEnd_time();
                if (!StringUtils.isEmpty(endTime) && !endTime.equals("0")) {
                    mView.refreshPeaceTime(endTime);
                } else {
                    mView.refreshPeaceTime(null);
                }
            }
        }
    }

    @Override
    public void handleEvent(RxbusBean event) {
        switch (event.TAG) {
            case UPDATE_VERSION_VALUE:
                refreshVersion((PlaneVersionBean) event.object);
                break;
            case UPDATE_CAMERA_PROGRESS:
                LogUtils.d("UserFragment更新相机上传进度条-->" + event.object);
                mView.showUpgradeProgress(UPDATE_CAMERA_PROGRESS, (int) event.object);
                break;
            case UPDATE_CAMERA_SUCCESS:
                mView.upgradeSuccess();
                break;
            case ConstantFields.UPGRADE_FW.UPDATE_PLANE_PROGRESS:
                int progress = (int) event.object;
                LogUtils.d("UserFragment更新飞控上传进度条-->" + progress);
                mView.showUpgradeProgress(ConstantFields.UPGRADE_FW.UPDATE_PLANE_PROGRESS, progress);
                if (progress >= 100) {
                    mView.upgradeSuccess();
                }
                break;
            case ConstantFields.UPGRADE_FW.UPDATE_YUNTAI_PROGRESS:
                int ytPro = (int) event.object;
                LogUtils.d("UserFragment更新云台上传进度条-->" + ytPro);
                if (ytPro >= 100) {
                    mView.upgradeSuccess();
                } else {
                    mView.showUpgradeProgress(ConstantFields.UPGRADE_FW.UPDATE_YUNTAI_PROGRESS, ytPro);
                }

                break;
            case ConstantFields.UPGRADE_FW.UPDATE_PLANE_FW_ERROR:
                //上传固件出错
                mView.upgradeFailed((int) event.object);
                break;
            case ConstantFields.UPGRADE_FW.UAV_UNLOCK:
                //上传固件出错
                mView.upgradeFailed((int) event.object);
                break;
            case WIFI_CONNTENTED:
                LogUtils.d("UserFragment连上飞机--》wifiConntentedObserveOn");
                break;
            case WIFI_BREAK_IN_BACKGRAOUND:
                LogUtils.d("UserFragment与飞机断开--》wifiBreakInBackgroundObserveOn");
//                mView.upgradeFailed();
                break;
            case WIFI_RSSI_CHANGE:
                LogUtils.d("UserFragment---》wifi信号强弱" + event.object);
                break;
            case WIFI_OTHER_CONNECTED:
                LogUtils.d("UserFragment与其他网络连接上--》otherWifiConnectedObserveOn");
                break;
            case WIFI_DISCONNECT:
                LogUtils.d("UserFragment无wifi网络连接--》wifiDisconnectObserveOn");
//                mView.upgradeFailed(UpdateFirmware.UpdateFirmwareReadError);
                break;
            case ACT_RESPONSE:
                mView.refreshAppAck((Integer) event.object);
                break;
        }
    }

    public void startUpdateCameraBin() {
        mView.showActionDialog(ConstantFields.ACTION_PARAM.UNPACK_CAMERA_BIN_ZIP);
        mView.getCmd().startSendFile(mView, new DataSocketReadCallback() {

            @Override
            public void onUnpackZipResult(boolean isSuccess) {
                super.onUnpackZipResult(isSuccess);
                mView.dismissActionDialog();
                if (!isSuccess) {
                    ToastUtils.showLongToast(ResourceUtils.getString(R.string.unpack_zip_failed));
                } else {
                    mView.showUpgradeProgress(UPDATE_CAMERA_PROGRESS, 0);
                }
            }

            @Override
            public void onReadUpdataProgress(int progress) {
                super.onReadUpdataProgress(progress);
                // TODO: 2019/7/3 更新进度
                LogUtils.d("上传camera bin进度---》" + progress);
                RxLoopSchedulers.composeMain(mView, progress).subscribe(new RxLoopObserver<Integer>() {
                    @Override
                    public void onNext(Integer pr) {
                        super.onNext(pr);
                        this.disposeDisposables();
                        mView.showUpgradeProgress(UPDATE_CAMERA_PROGRESS, pr);
                    }
                });

//                RxBusUtils.getDefault().post(new RxbusBean(ConstantFields.UPGRADE_FW.UPDATE_CAMERA_PROGRESS, progress));
            }

            @Override
            public void onErrorCallback(int msgId, int errorCode) {
                super.onErrorCallback(msgId, errorCode);
                //可能无卡，容量不足，或者START_SEND_FILE=1286 出错
                RxLoopSchedulers.composeMain(mView, msgId)
                        .subscribe(new RxLoopObserver<Integer>() {
                            @Override
                            public void onNext(Integer id) {
                                super.onNext(id);
                                this.disposeDisposables();
                                mView.dismissUpgradeDialog();
                                if (msgId == SD_INTERNAL) {
                                    mView.showActionDialog(ConstantFields.ACTION_PARAM.UPGRADE_SD_ERROR_FOR_6KA);
                                } else {
                                    mView.showActionDialog(ConstantFields.ACTION_PARAM.UPGRADE_ERROR);
                                }
                            }
                        });

            }
        });
    }

    public void sendCameraBinComplete(ABCmdValue cb) {
        /**文件上传完成主动返回*/
        String md5 = cb.getMd5((List) cb.getParam());
        if (cb.getType().equals("put_file_complete") && mView.getCmd().getSendFileMd5() != null
                && md5 != null && mView.getCmd().getSendFileMd5().equals(md5)) {
            LogUtils.d("相机固件上传成功");
            mView.getCmd().renameFileInMain(mView, OLD_FW_PATH, ConnectManager.getInstance().getNewTagCameraBinName(), new CameraCommandCallback<ABCmdValue>() {
                @Override
                public void onComplete(ABCmdValue data) {
                    LogUtils.d("重命名成功");
                    if (ConnectManager.getInstance().mProductModel.productType != ProductType_4k) {
                        // TODO: 2019/8/21 6k和4ka相机发送重启相机无效，只能通知手动重启
                        mView.upgradeSuccess();
                    } else {
                        mView.getCmd().detectiondFw(mView, new CameraCommandCallback<ABCmdValue>() {
                            @Override
                            public void onComplete(ABCmdValue data) {
                                LogUtils.d("相机升级===>通知SD卡更新");
                                mView.dismissUpgradeDialog();
                                mView.showActionDialog(ConstantFields.ACTION_PARAM.UPGRADE_COMPLETE);
                            }
                        });
                    }

                }
            });
        } else {
            LogUtils.d("相机固件上传失败");
            mView.dismissUpgradeDialog();
            mView.showActionDialog(ConstantFields.ACTION_PARAM.UPGRADE_ERROR);
        }
    }


    /**
     * 开始升级图传
     */
    private void startFigureUpgrade() {
        mView.showDefDialog(ResourceUtils.getString(R.string.updating_image_transfer));
        RxLoopSchedulers.composeIO(mView, new Function() {
            @Override
            public Boolean apply(Object object) throws Exception {
                //天空端设置参数
                RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP424, ConstantFields.SHELL_CMD.UIC_SET_CMD);
                //天空端提交30参数
                RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP424, ConstantFields.SHELL_CMD.UIC_COMMIT_CMD);
                //天空端读取参数
                String result1 = RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP424, ConstantFields.SHELL_CMD.UIC_GET_CMD);
                if (RegexUtils.canParseInt(result1.trim()) && Integer.parseInt(result1.trim()) == 30) {
                    //如果设置成了30，则继续
                    //天空端保存参数
                    LogUtils.d("更新图传====》天空是30" + result1);
                    RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP421, ConstantFields.SHELL_CMD.RELOAD_CONFIG_CMD);
                    //地面端设置参数
                    RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP421, ConstantFields.SHELL_CMD.UIC_SET_CMD);
                    //地面端提交30参数
                    RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP421, ConstantFields.SHELL_CMD.UIC_COMMIT_CMD);
                    //地面端读取参数
                    String result2 = RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP421, ConstantFields.SHELL_CMD.UIC_GET_CMD);
                    if (RegexUtils.canParseInt(result2.trim()) && Integer.parseInt(result2.trim()) == 30) {
                        //地面端保存参数
                        LogUtils.d("更新图传====》地面是30" + result2);
                        RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP421, ConstantFields.SHELL_CMD.RELOAD_CONFIG_CMD);
                        return true;
                    }
                }
                return false;
            }
        }).subscribe(new RxLoopObserver<Boolean>() {
            @Override
            public void onNext(Boolean value) {
                super.onNext(value);
                this.disposeDisposables();
                mView.dismissDefDialog();
                ToastUtils.showShortToast(ResourceUtils.getString(value ? R.string.updating_success : R.string.updating_failed));
                if (value) {
                    LogUtils.d("更新图传成功");
                } else {
                    LogUtils.d("更新图传失败");
                }
            }
        });
    }

}
