package com.feipai.flypai.mvp.presenters.fragmentpresenters;

import android.content.Context;

import com.feipai.flypai.R;
import com.feipai.flypai.api.CameraCommandCallback;
import com.feipai.flypai.api.DataSocketReadCallback;
import com.feipai.flypai.api.RxLoopObserver;
import com.feipai.flypai.api.RxLoopSchedulers;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.beans.ABCmdValue;
import com.feipai.flypai.beans.PlaneInfo;
import com.feipai.flypai.beans.PlaneVersionBean;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.beans.UserBean;
import com.feipai.flypai.connect.ConnectManager;
import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.mvp.contract.fragcontract.UserFragContract;
import com.feipai.flypai.utils.daoutils.DBClient;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.RegexUtils;
import com.feipai.flypai.utils.global.RemoteJschUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.RxBusUtils;
import com.feipai.flypai.utils.global.StringUtils;
import com.feipai.flypai.utils.global.ToastUtils;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.functions.Function;

import static com.feipai.flypai.app.ConstantFields.BusEventType.*;
import static com.feipai.flypai.app.ConstantFields.CAMERA_CONFIG.SD_INTERNAL;
import static com.feipai.flypai.app.ConstantFields.ProductType_4k;
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

        mView.notifyAdapter();
        if (!planeVersionBean.getLocalPlaneVersion().equals("1") && planeVersionBean.isPlaneVersionIsForceUpgrade()) {
            //??????????????????,????????????
            LogUtils.d("????????????????????????");
//            mView.showUpgradeProgress(ConstantFields.UPGRADE_FW.START_UPGRADE_PLANE_FW, -1);
        }

    }

    /**
     * ??????????????????????????????
     */
    private void startCheckFigureUpgrade() {
        mView.showDefDialog(ResourceUtils.getString(R.string.detecting));
        RxLoopSchedulers.composeIO(mView, new Function() {
            @Override
            public Boolean apply(Object object) throws Exception {
                //?????????????????????
                String result1 = RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP424, ConstantFields.SHELL_CMD.UIC_GET_CMD);
                if (RegexUtils.canParseInt(result1.trim()) && Integer.parseInt(result1.trim()) == 30) {
                    //??????????????????30????????????
                    //?????????????????????
                    String result2 = RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP421, ConstantFields.SHELL_CMD.UIC_GET_CMD);
                    if (RegexUtils.canParseInt(result2.trim()) && Integer.parseInt(result2.trim()) == 30) {
                        //?????????????????????
                        LogUtils.d("????????????====????????????30" + result2);
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
                    LogUtils.d("????????????????????????");
                } else {
                    LogUtils.d("????????????????????????????????????");
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
                LogUtils.d("UserFragment???????????????????????????-->" + event.object);
                mView.showUpgradeProgress(UPDATE_CAMERA_PROGRESS, (int) event.object);
                break;
            case UPDATE_CAMERA_SUCCESS:
                mView.upgradeSuccess();
                break;
            case ConstantFields.UPGRADE_FW.UPDATE_PLANE_PROGRESS:
                int progress = (int) event.object;
                LogUtils.d("UserFragment???????????????????????????-->" + progress);
                mView.showUpgradeProgress(ConstantFields.UPGRADE_FW.UPDATE_PLANE_PROGRESS, progress);
                if (progress >= 100) {
                    mView.upgradeSuccess();
                }
                break;
            case ConstantFields.UPGRADE_FW.UPDATE_YUNTAI_PROGRESS:
                int ytPro = (int) event.object;
                LogUtils.d("UserFragment???????????????????????????-->" + ytPro);
                if (ytPro >= 100) {
                    mView.upgradeSuccess();
                } else {
                    mView.showUpgradeProgress(ConstantFields.UPGRADE_FW.UPDATE_YUNTAI_PROGRESS, ytPro);
                }

                break;
            case ConstantFields.UPGRADE_FW.UPDATE_PLANE_FW_ERROR:
                //??????????????????
                mView.upgradeFailed((int) event.object);
                break;
            case ConstantFields.UPGRADE_FW.UAV_UNLOCK:
                //??????????????????
                mView.upgradeFailed((int) event.object);
                break;
            case WIFI_CONNTENTED:
                LogUtils.d("UserFragment????????????--???wifiConntentedObserveOn");
                break;
            case WIFI_BREAK_IN_BACKGRAOUND:
                LogUtils.d("UserFragment???????????????--???wifiBreakInBackgroundObserveOn");
//                mView.upgradeFailed();
                break;
            case WIFI_RSSI_CHANGE:
                LogUtils.d("UserFragment---???wifi????????????" + event.object);
                break;
            case WIFI_OTHER_CONNECTED:
                LogUtils.d("UserFragment????????????????????????--???otherWifiConnectedObserveOn");
                break;
            case WIFI_DISCONNECT:
                LogUtils.d("UserFragment???wifi????????????--???wifiDisconnectObserveOn");
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
                // TODO: 2019/7/3 ????????????
                LogUtils.d("??????camera bin??????---???" + progress);
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
                //????????????????????????????????????START_SEND_FILE=1286 ??????
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
        /**??????????????????????????????*/
        String md5 = cb.getMd5((List) cb.getParam());
        if (cb.getType().equals("put_file_complete") && mView.getCmd().getSendFileMd5() != null
                && md5 != null && mView.getCmd().getSendFileMd5().equals(md5)) {
            LogUtils.d("????????????????????????");
            mView.getCmd().renameFileInMain(mView, OLD_FW_PATH, ConnectManager.getInstance().getNewTagCameraBinName(), new CameraCommandCallback<ABCmdValue>() {
                @Override
                public void onComplete(ABCmdValue data) {
                    LogUtils.d("???????????????");
                    if (ConnectManager.getInstance().mProductModel.productType != ProductType_4k) {
                        // TODO: 2019/8/21 6k???4ka?????????????????????????????????????????????????????????
                        mView.upgradeSuccess();
                    } else {
                        mView.getCmd().detectiondFw(mView, new CameraCommandCallback<ABCmdValue>() {
                            @Override
                            public void onComplete(ABCmdValue data) {
                                LogUtils.d("????????????===>??????SD?????????");
                                mView.dismissUpgradeDialog();
                                mView.showActionDialog(ConstantFields.ACTION_PARAM.UPGRADE_COMPLETE);
                            }
                        });
                    }

                }
            });
        } else {
            LogUtils.d("????????????????????????");
            mView.dismissUpgradeDialog();
            mView.showActionDialog(ConstantFields.ACTION_PARAM.UPGRADE_ERROR);
        }
    }


    /**
     * ??????????????????
     */
    private void startFigureUpgrade() {
        mView.showDefDialog(ResourceUtils.getString(R.string.updating_image_transfer));
        RxLoopSchedulers.composeIO(mView, new Function() {
            @Override
            public Boolean apply(Object object) throws Exception {
                //?????????????????????
                RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP424, ConstantFields.SHELL_CMD.UIC_SET_CMD);
                //???????????????30??????
                RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP424, ConstantFields.SHELL_CMD.UIC_COMMIT_CMD);
                //?????????????????????
                String result1 = RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP424, ConstantFields.SHELL_CMD.UIC_GET_CMD);
                if (RegexUtils.canParseInt(result1.trim()) && Integer.parseInt(result1.trim()) == 30) {
                    //??????????????????30????????????
                    //?????????????????????
                    LogUtils.d("????????????====????????????30" + result1);
                    RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP421, ConstantFields.SHELL_CMD.RELOAD_CONFIG_CMD);
                    //?????????????????????
                    RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP421, ConstantFields.SHELL_CMD.UIC_SET_CMD);
                    //???????????????30??????
                    RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP421, ConstantFields.SHELL_CMD.UIC_COMMIT_CMD);
                    //?????????????????????
                    String result2 = RemoteJschUtils.execRemoteCmd(ConstantFields.SHELL_IP.IP421, ConstantFields.SHELL_CMD.UIC_GET_CMD);
                    if (RegexUtils.canParseInt(result2.trim()) && Integer.parseInt(result2.trim()) == 30) {
                        //?????????????????????
                        LogUtils.d("????????????====????????????30" + result2);
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
                    LogUtils.d("??????????????????");
                } else {
                    LogUtils.d("??????????????????");
                }
            }
        });
    }

}
