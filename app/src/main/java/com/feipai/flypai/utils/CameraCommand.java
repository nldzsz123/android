package com.feipai.flypai.utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;

import com.feipai.flypai.R;
import com.feipai.flypai.api.CameraCommandCallback;
import com.feipai.flypai.api.CameraCommandHandlerCallback;
import com.feipai.flypai.api.DataSocketReadCallback;
import com.feipai.flypai.api.ResultCallback;
import com.feipai.flypai.api.RxLoopObserver;
import com.feipai.flypai.api.RxLoopSchedulers;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.base.BaseCameraMsg;
import com.feipai.flypai.base.BaseEntity;
import com.feipai.flypai.beans.ABCmdValue;
import com.feipai.flypai.beans.FileBean;
import com.feipai.flypai.beans.SettingBean;
import com.feipai.flypai.beans.SettingBean.ParamBean;
import com.feipai.flypai.connect.ConnectManager;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.ui.view.Camera.CameraSetConstaint;
import com.feipai.flypai.utils.cache.CacheManager;
import com.feipai.flypai.utils.global.FileUtils;
import com.feipai.flypai.utils.global.JsonUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.NetworkUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.StringUtils;
import com.feipai.flypai.utils.gsonlib.MGson;
import com.feipai.flypai.utils.socket.CameraSocket;
import com.feipai.flypai.utils.socket.SocketBaseMsg;
import com.feipai.flypai.utils.socket.SocketManager;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import io.reactivex.functions.Function;

import static com.feipai.flypai.app.ConstantFields.CAMERA_CONFIG.*;
import static com.feipai.flypai.app.ConstantFields.ProductType_4kAir;
import static com.feipai.flypai.app.ConstantFields.ProductType_6kAir;

public class CameraCommand {

    private CameraSocket cameraSocket;
    private int token = -1;
    public ParamBean bean;
    private int flyType;
    private String[] videoResStr = ResourceUtils.getStringArray(R.array.AB_VIDEO_RESOLUTION_4K);
    private String[] videoResDisplay = ResourceUtils.getStringArray(R.array.AB_VIDEO_RESOLUTION_DISPLAY_4K);

    private String sendFileMd5 = null;


    // ??????
    private static CameraCommand mCmd;

    public static CameraCommand getCmdInstance() {
        if (mCmd == null) {
            synchronized (CameraCommand.class) {
                if (mCmd == null) {
                    mCmd = new CameraCommand();
                }
            }
        }
        return mCmd;
    }

    private CameraCommand() {
        mCommands = new ArrayList<>();
    }

    public void connTcp(CameraSocket cameraSocket) {
        this.cameraSocket = cameraSocket;
    }

    public void setToken(int token) {
        LogUtils.d("??????token" + token);
        this.token = token;
    }

    public int getToken() {
        return token;
    }

    public int getFlyType() {
        return flyType;
    }

    public String getSendFileMd5() {
        return sendFileMd5;
    }

    private ArrayList<SocketBaseMsg> mCommands;

    /**
     * ????????????????????????2K???4K???6K
     */
    public void setFlyType(String ssid) {
        this.flyType = flyType;
        CacheManager.getSharedPrefUtils().putInt(ConstantFields.PREF.FLY_PIE_TYPE, flyType);
        switch (flyType) {
            case 0://2K
                setVideoResStr(ResourceUtils.getStringArray(R.array.AB_VIDEO_RESOLUTION_2K));
                setVideoResDisplay(ResourceUtils.getStringArray(R.array.AB_VIDEO_RESOLUTION_DISPLAY_2K));
                break;
            default://4K//6K
                setVideoResStr(ResourceUtils.getStringArray(R.array.AB_VIDEO_RESOLUTION_4K));
                setVideoResDisplay(ResourceUtils.getStringArray(R.array.AB_VIDEO_RESOLUTION_DISPLAY_4K));
                break;
        }
    }

    public String[] getVideoResStr() {
        return videoResStr;
    }

    public void setVideoResStr(String[] videoResStr) {
        this.videoResStr = videoResStr;
    }

    public String[] getVideoResDisplay() {
        return videoResDisplay;
    }

    public void setVideoResDisplay(String[] videoResDisplay) {
        this.videoResDisplay = videoResDisplay;
    }

    // ???????????????????????????????????????????????????
    public void getLastFileName(BaseView baseView, boolean isPhoto, CameraCommandCallback<ABCmdValue<String>> callback) {
        BaseCameraMsg msg = new BaseCameraMsg(callback);
        msg.setToken(token);
        msg.setMsgId(LAST_FILE);
        msg.setType(isPhoto ? "panorama" : "video");
        sendSocketMessage(msg, msg.getMsgId(), DataType_string, new CameraCommandHandlerCallback<ABCmdValue<String>>() {
            @Override
            public void onDigestResults(ABCmdValue<String> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data);
                }
            }
        });
    }

    /**
     * ????????????????????????????????????
     */
    public void getCameraStatus(BaseView baseView, CameraCommandCallback<Integer> callback) {
        BaseCameraMsg msg = new BaseCameraMsg();
        msg.setToken(token);
        msg.setMsgId(APP_STATUS);
        msg.setType("app_status");
        msg.setSize(0);
        msg.setRval(0);

        sendSocketMessage(msg, msg.getMsgId(), DataType_string, new CameraCommandHandlerCallback<ABCmdValue<String>>() {
            @Override
            public void onDigestResults(ABCmdValue<String> cb) {
                if (cb == null) {
                    if (callback != null) {
                        callback.onCompleteInMain(baseView, CameraSetConstaint.CameraValue_unknown);
                    }
                    return;
                }
                int status = CameraSetConstaint.CameraValue_unknown;
                String param = cb.getParam();
                if (param.equals("vf")) {
                    status = CameraSetConstaint.CameraValue_vf;
                } else if (param.equals("photo_vf")) {
                    status = CameraSetConstaint.CameraValue_photo_vf;
                } else if (param.equals("photo_idle")) {
                    status = CameraSetConstaint.CameraValue_photo_idle;
                } else if (param.equals("idle")) {
                    status = CameraSetConstaint.CameraValue_idle;
                } else if (param.equals("record")) {
                    status = CameraSetConstaint.CameraValue_record;
                } else if (param.equals("photo_mode")) {
                    status = CameraSetConstaint.CameraValue_photo_mode;
                }

                if (callback != null) {
                    callback.onCompleteInMain(baseView, status);
                }
            }
        });
    }

    public void photoZoomClose() {
        BaseCameraMsg msg3 = new BaseCameraMsg();
        msg3.setToken(token);
        msg3.setMsgId(2);
        msg3.setType(ConstantFields.CAMERA_CMD.SET_PHOTO_ZOOM);
        msg3.setParam("off");
        sendSocketMessage(msg3, msg3.getMsgId(), DataType_ABCMDValue, null);
    }

    public void changeToNormalPhotoModeAndTakePhoto(BaseView baseView, CameraCommandCallback<Integer> callback) {
        BaseCameraMsg msg1 = new BaseCameraMsg();
        msg1.setToken(token);
        msg1.setMsgId(260);
        sendSocketMessage(msg1, msg1.getMsgId(), DataType_integer, null);

//        BaseCameraMsg msg2 = new BaseCameraMsg();
//        msg2.setToken(token);
//        msg2.setMsgId(ConstantFields.CAMERA_CONFIG.CHANGE_MODE);
//        msg2.setParam(1);
//        sendSocketMessage(msg2, msg2.getMsgId(), DataType_ABCMDValue, null);

        BaseCameraMsg msg3 = new BaseCameraMsg();
        msg3.setToken(token);
        msg3.setMsgId(2);
        msg3.setType("photo_time_lapse_mode");
        msg3.setParam("off");
        sendSocketMessage(msg3, msg3.getMsgId(), DataType_ABCMDValue, null);

        BaseCameraMsg msg4 = new BaseCameraMsg();
        msg4.setToken(token);
        msg4.setMsgId(2);
        msg4.setType("photo_vr_mode");
        msg4.setParam("off");
        sendSocketMessage(msg4, msg4.getMsgId(), DataType_ABCMDValue, null);

        BaseCameraMsg msg5 = new BaseCameraMsg();
        msg5.setToken(token);
        msg5.setMsgId(259);
        sendSocketMessage(msg5, msg5.getMsgId(), DataType_ABCMDValue, null);

        BaseCameraMsg msg6 = new BaseCameraMsg();
        msg6.setToken(token);
        msg6.setMsgId(TAKE_PHOTO);
        sendSocketMessage(msg6, msg6.getMsgId(), DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval());
                }
            }
        });
    }

    // ???????????????????????????
    public void takePhotoComplete(BaseView baseView, CameraCommandCallback<Integer> callback) {
        BaseCameraMsg msg6 = new BaseCameraMsg();
        msg6.setToken(token);
        msg6.setMsgId(TAKE_PHOTO);
        sendSocketMessage(msg6, msg6.getMsgId(), DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval());
                }
            }
        });
    }

    // ?????????????????????????????????????????????
    public void takePhotoInDelayModeComplete(BaseView baseView, boolean isVideo, int flyType, boolean isOpenDelayTimeMode, CameraCommandCallback<Boolean> callback) {

        BaseCameraMsg msg1 = new BaseCameraMsg();
        msg1.setToken(token);
        msg1.setMsgId(ConstantFields.CAMERA_CONFIG.STOP_VF);
        sendSocketMessage(msg1, msg1.getMsgId(), DataType_integer, null);

        if (isVideo) {
            BaseCameraMsg msg6 = new BaseCameraMsg();
            msg6.setToken(token);
            msg6.setMsgId(ConstantFields.CAMERA_CONFIG.CHANGE_MODE);
            msg6.setParam(1);
            sendSocketMessage(msg6, msg6.getMsgId(), DataType_ABCMDValue, null);
        }

        //??????????????????
        BaseCameraMsg msg2 = new BaseCameraMsg();
        msg2.setToken(token);
        msg2.setMsgId(ConstantFields.CAMERA_CONFIG.CAMERA_SET);
        msg2.setType(ConstantFields.CAMERA_CMD.SET_PHOTO_VR_MODE);
        msg2.setParam(ResourceUtils.getStringArray(R.array.SWITHC)[0]);
        sendSocketMessage(msg2, msg2.getMsgId(), DataType_ABCMDValue, null);


        //??????????????????
        BaseCameraMsg msg3 = new BaseCameraMsg();
        msg3.setToken(token);
        msg3.setMsgId(ConstantFields.CAMERA_CONFIG.CAMERA_SET);
        msg3.setType(ConstantFields.CAMERA_CMD.SET_TIME_LAPSE_MODE);
        msg3.setParam(ResourceUtils.getStringArray(R.array.SWITHC)[isOpenDelayTimeMode ? 1 : 0]);
        sendSocketMessage(msg3, msg3.getMsgId(), DataType_ABCMDValue, null);

        //???????????????0
        BaseCameraMsg msg7 = new BaseCameraMsg();
        msg7.setToken(token);
        msg7.setMsgId(2058);
        msg7.setParam(0);
        sendSocketMessage(msg7, msg7.getMsgId(), DataType_integer, null);

        BaseCameraMsg msg5 = new BaseCameraMsg();
        msg5.setToken(token);
        msg5.setMsgId(ConstantFields.CAMERA_CONFIG.RESET_VF);
        sendSocketMessage(msg5, msg5.getMsgId(), DataType_ABCMDValue, null);

        BaseCameraMsg msg11 = new BaseCameraMsg();
        msg11.setToken(token);
        msg11.setMsgId(ConstantFields.CAMERA_CONFIG.STOP_VF);
        sendSocketMessage(msg11, msg11.getMsgId(), DataType_integer, null);

        BaseCameraMsg msg4 = new BaseCameraMsg();
        msg4.setToken(token);
        msg4.setMsgId(ConstantFields.CAMERA_CONFIG.CAMERA_SET);
        msg4.setType(ConstantFields.CAMERA_CMD.SET_PHOTO_SIZE);
        String param = ResourceUtils.getStringArray(R.array.PHOTO_SIZE_4K)[1];
        if (flyType == ProductType_4kAir) {
            param = ResourceUtils.getStringArray(R.array.PHOTO_SIZE_4KAir)[1];
        } else if (flyType == ProductType_6kAir) {
            param = ResourceUtils.getStringArray(R.array.photoAitios_6k)[1];
        }
        msg4.setParam(param);
        sendSocketMessage(msg4, msg4.getMsgId(), DataType_ABCMDValue, null);

        BaseCameraMsg msg55 = new BaseCameraMsg();
        msg55.setToken(token);
        msg55.setMsgId(ConstantFields.CAMERA_CONFIG.RESET_VF);
        sendSocketMessage(msg55, msg55.getMsgId(), DataType_ABCMDValue, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (data.getRval() == 0) {
                    if (callback != null) callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    // ???????????????????????????
    public void takePhotoInDelayComplete(BaseView baseView, boolean ismakeDir, CameraCommandCallback<Boolean> callback) {

        BaseCameraMsg msg2 = new BaseCameraMsg();
        msg2.setToken(token);
        msg2.setMsgId(ConstantFields.CAMERA_CONFIG.TAKE_TIME_LAPSE_MSG);
        msg2.setParam(ismakeDir ? 1 : 0);
        sendSocketMessage(msg2, msg2.getMsgId(), DataType_string, new CameraCommandHandlerCallback<ABCmdValue<String>>() {
            @Override
            public void onDigestResults(ABCmdValue<String> data) {
                if (callback != null) callback.onCompleteInMain(baseView, data.getRval() == 0);
            }
        });

    }

    public void changeToPanorMode(BaseView baseView, int flyType, boolean isVideo, boolean isOpenPanorMode, CameraCommandCallback<Boolean> callback) {
        BaseCameraMsg msg1 = new BaseCameraMsg();
        msg1.setToken(token);
        msg1.setMsgId(ConstantFields.CAMERA_CONFIG.STOP_VF);
        sendSocketMessage(msg1, msg1.getMsgId(), DataType_integer, null);

        if (isVideo) {
            BaseCameraMsg msg6 = new BaseCameraMsg();
            msg6.setToken(token);
            msg6.setMsgId(ConstantFields.CAMERA_CONFIG.CHANGE_MODE);
            msg6.setParam(1);
            sendSocketMessage(msg6, msg6.getMsgId(), DataType_ABCMDValue, null);
        }
        //??????????????????
        BaseCameraMsg msg2 = new BaseCameraMsg();
        msg2.setToken(token);
        msg2.setMsgId(ConstantFields.CAMERA_CONFIG.CAMERA_SET);
        msg2.setType(ConstantFields.CAMERA_CMD.SET_TIME_LAPSE_MODE);
        msg2.setParam(ResourceUtils.getStringArray(R.array.SWITHC)[0]);
        sendSocketMessage(msg2, msg2.getMsgId(), DataType_ABCMDValue, null);


        //???????????????????????????
        BaseCameraMsg msg3 = new BaseCameraMsg();
        msg3.setToken(token);
        msg3.setMsgId(ConstantFields.CAMERA_CONFIG.CAMERA_SET);
        msg3.setType(ConstantFields.CAMERA_CMD.SET_PHOTO_VR_MODE);
        msg3.setParam(ResourceUtils.getStringArray(R.array.SWITHC)[isOpenPanorMode ? 1 : 0]);
        sendSocketMessage(msg3, msg3.getMsgId(), DataType_ABCMDValue, null);

        //???????????????0
        BaseCameraMsg msg7 = new BaseCameraMsg();
        msg7.setToken(token);
        msg7.setMsgId(2058);
        msg7.setParam(0);
        sendSocketMessage(msg7, msg7.getMsgId(), DataType_integer, null);

        BaseCameraMsg msg5 = new BaseCameraMsg();
        msg5.setToken(token);
        msg5.setMsgId(ConstantFields.CAMERA_CONFIG.RESET_VF);
        sendSocketMessage(msg5, msg5.getMsgId(), DataType_ABCMDValue, null);

        BaseCameraMsg msg11 = new BaseCameraMsg();
        msg11.setToken(token);
        msg11.setMsgId(ConstantFields.CAMERA_CONFIG.STOP_VF);
        sendSocketMessage(msg11, msg11.getMsgId(), DataType_integer, null);

        BaseCameraMsg msg4 = new BaseCameraMsg();
        msg4.setToken(token);
        msg4.setMsgId(ConstantFields.CAMERA_CONFIG.CAMERA_SET);
        msg4.setType(ConstantFields.CAMERA_CMD.SET_PHOTO_SIZE);
        String param = ResourceUtils.getStringArray(R.array.PHOTO_SIZE_4K)[0];
        if (flyType == ProductType_4kAir) {
            param = ResourceUtils.getStringArray(R.array.PHOTO_SIZE_4KAir)[0];
        } else if (flyType == ProductType_6kAir) {
            param = ResourceUtils.getStringArray(R.array.photoAitios_6k)[0];
        }
        msg4.setParam(param);
        sendSocketMessage(msg4, msg4.getMsgId(), DataType_ABCMDValue, null);

        BaseCameraMsg msg55 = new BaseCameraMsg();
        msg55.setToken(token);
        msg55.setMsgId(ConstantFields.CAMERA_CONFIG.RESET_VF);
        sendSocketMessage(msg55, msg55.getMsgId(), DataType_ABCMDValue, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (data.getRval() == 0) {
                    if (callback != null) callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    // ???????????????????????????
    public void takePhotoInPanoComplete(BaseView baseView, boolean ismakeDir, CameraCommandCallback<String> callback) {

        BaseCameraMsg msg2 = new BaseCameraMsg();
        msg2.setToken(token);
        msg2.setMsgId(772);
        msg2.setParam(ismakeDir ? 1 : 0);
        sendSocketMessage(msg2, msg2.getMsgId(), DataType_string, new CameraCommandHandlerCallback<ABCmdValue<String>>() {
            @Override
            public void onDigestResults(ABCmdValue<String> data) {
                if (callback != null)
                    callback.onCompleteInMain(baseView, data.getRval() == 0 ? data.getParam() : "");
            }
        });

    }


    public void startRecordComplete(BaseView baseView, CameraCommandCallback<Integer> callback) {
        BaseCameraMsg msg6 = new BaseCameraMsg();
        msg6.setToken(token);
        msg6.setMsgId(START_RECORD);
        sendSocketMessage(msg6, msg6.getMsgId(), DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval());
                }
            }
        });
    }

    public void stopRecordComplete(BaseView baseView, CameraCommandCallback<String> callback) {
        BaseCameraMsg msg6 = new BaseCameraMsg();
        msg6.setToken(token);
        msg6.setMsgId(STOP_RECORD);
        sendSocketMessage(msg6, msg6.getMsgId(), DataType_string, new CameraCommandHandlerCallback<ABCmdValue<String>>() {
            @Override
            public void onDigestResults(ABCmdValue<String> data) {
                if (callback != null) {
                    if (data.getRval() == 0) {
                        callback.onCompleteInMain(baseView, data.getParam());
                    } else {
                        callback.onErrorCodeInMain(baseView, data.getMsg_id(), data.getRval());
                    }
                }
            }
        });
    }

    // ?????????????????????
    public void changeToVideoModeComplete(BaseView baseView, CameraCommandCallback<Boolean> callback) {
        BaseCameraMsg msg1 = new BaseCameraMsg();
        msg1.setToken(token);
        msg1.setMsgId(260);
        sendSocketMessage(msg1, msg1.getMsgId(), DataType_integer, null);

        BaseCameraMsg msg6 = new BaseCameraMsg();
        msg6.setToken(token);
        msg6.setMsgId(ConstantFields.CAMERA_CONFIG.CHANGE_MODE);
        msg6.setParam(0);
        sendSocketMessage(msg6, msg6.getMsgId(), DataType_integer, null);

        BaseCameraMsg msg5 = new BaseCameraMsg();
        msg5.setToken(token);
        msg5.setMsgId(259);
        sendSocketMessage(msg5, msg5.getMsgId(), DataType_ABCMDValue, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    // ?????????????????????
    public void changeToPhotoMode(BaseView baseView, CameraCommandCallback<Boolean> callback) {
        BaseCameraMsg msg1 = new BaseCameraMsg();
        msg1.setToken(token);
        msg1.setMsgId(260);
        sendSocketMessage(msg1, msg1.getMsgId(), DataType_integer, null);

        BaseCameraMsg msg6 = new BaseCameraMsg();
        msg6.setToken(token);
        msg6.setMsgId(ConstantFields.CAMERA_CONFIG.CHANGE_MODE);
        msg6.setParam(1);
        sendSocketMessage(msg6, msg6.getMsgId(), DataType_integer, null);

        BaseCameraMsg msg5 = new BaseCameraMsg();
        msg5.setToken(token);
        msg5.setMsgId(259);
        sendSocketMessage(msg5, msg5.getMsgId(), DataType_ABCMDValue, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    // ??????????????????
    public void setPhotoAtio(BaseView baseView, String val, CameraCommandCallback<Boolean> callback) {
        BaseCameraMsg msg1 = new BaseCameraMsg();
        msg1.setToken(token);
        msg1.setMsgId(260);
        sendSocketMessage(msg1, msg1.getMsgId(), DataType_integer, null);

        BaseCameraMsg msg4 = new BaseCameraMsg();
        msg4.setToken(token);
        msg4.setMsgId(2);
        msg4.setType("photo_size");
        msg4.setParam(val);
        sendSocketMessage(msg4, msg4.getMsgId(), DataType_ABCMDValue, null);

        BaseCameraMsg msg5 = new BaseCameraMsg();
        msg5.setToken(token);
        msg5.setMsgId(259);
        sendSocketMessage(msg5, msg5.getMsgId(), DataType_ABCMDValue, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    // ?????? 10 bit for 6k
    public void swith10bit(BaseView baseView, boolean openOrClose, CameraCommandCallback<Boolean> callback) {
        BaseCameraMsg msg4 = new BaseCameraMsg();
        msg4.setToken(token);
        msg4.setMsgId(2);
        msg4.setType("video_10bit_mode");
        msg4.setParam(openOrClose ? "on" : "off");

        sendSocketMessage(msg4, msg4.getMsgId(), DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    // ???????????? Raw??????
    public void swithRaw(BaseView baseView, boolean openOrClose, CameraCommandCallback<Boolean> callback) {
        BaseCameraMsg msg4 = new BaseCameraMsg();
        msg4.setToken(token);
        msg4.setMsgId(2);
        msg4.setType("photo_raw");
        msg4.setParam(openOrClose ? "on" : "off");

        sendSocketMessage(msg4, msg4.getMsgId(), DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    // ??????????????? ???????????????????????????AE???
    public void swithAELock(BaseView baseView, boolean openOrClose, boolean photoMode, CameraCommandCallback<Boolean> callback) {
        BaseCameraMsg msg4 = new BaseCameraMsg();
        msg4.setToken(token);
        msg4.setMsgId(2);
        msg4.setType(photoMode ? "photo_3a_lock" : "video_3a_lock");
        msg4.setParam(openOrClose ? "on" : "off");

        sendSocketMessage(msg4, msg4.getMsgId(), DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    // ??????????????????????????????????????????ISO ?????????????????????
    public void swithIQAuto(BaseView baseView, boolean openOrClose, boolean photoMode, CameraCommandCallback<Boolean> callback) {
        BaseCameraMsg msg1 = new BaseCameraMsg();
        msg1.setToken(token);
        msg1.setMsgId(260);
        sendSocketMessage(msg1, msg1.getMsgId(), DataType_integer, null);

        BaseCameraMsg msg4 = new BaseCameraMsg();
        msg4.setToken(token);
        msg4.setMsgId(2);
        msg4.setType(photoMode ? "photo_image_mode" : "video_image_mode");
        msg4.setParam(openOrClose ? "auto" : "manual");
        sendSocketMessage(msg4, msg4.getMsgId(), DataType_ABCMDValue, null);

        BaseCameraMsg msg5 = new BaseCameraMsg();
        msg5.setToken(token);
        msg5.setMsgId(259);
        sendSocketMessage(msg5, msg5.getMsgId(), DataType_ABCMDValue, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    // ??????????????????
    public void setZoomEnd(BaseView baseView, CameraCommandCallback<Boolean> callback) {
        BaseCameraMsg msg4 = new BaseCameraMsg();
        msg4.setToken(token);
        msg4.setMsgId(2060);
        msg4.setParam(0);

        sendSocketMessage(msg4, msg4.getMsgId(), DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    // ??????????????????
    public void setZoomBegin(BaseView baseView, int toZoom, int speed, int step, CameraCommandCallback<Boolean> callback) {
        BaseCameraMsg msg4 = new BaseCameraMsg();
        msg4.setToken(token);
        msg4.setMsgId(2059);
        msg4.setParam(toZoom);
        msg4.setSpeed(speed);
        msg4.setStep(step);
        sendSocketMessage(msg4, msg4.getMsgId(), DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    // ?????????????????????
    public void setZoomValue(BaseView baseView, int toZoom, CameraCommandCallback<Boolean> callback) {
        BaseCameraMsg msg4 = new BaseCameraMsg();
        msg4.setToken(token);
        msg4.setMsgId(2058);
        msg4.setParam(toZoom);
        sendSocketMessage(msg4, msg4.getMsgId(), DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    // ?????????????????????????????????????????????
    public void setShutter(BaseView baseView, String shutter, boolean photoMode, CameraCommandCallback<Boolean> callback) {
        BaseCameraMsg msg4 = new BaseCameraMsg();
        msg4.setToken(token);
        msg4.setMsgId(2);
        msg4.setType(photoMode ? "photo_shutter" : "video_shutter");
        msg4.setParam(shutter);

        sendSocketMessage(msg4, msg4.getMsgId(), DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    // ???????????????????????????????????????ISO
    public void setIso(BaseView baseView, String iso, boolean photoMode, CameraCommandCallback<Boolean> callback) {
        BaseCameraMsg msg4 = new BaseCameraMsg();
        msg4.setToken(token);
        msg4.setMsgId(2);
        msg4.setType(photoMode ? "photo_iso" : "video_iso");
        msg4.setParam(iso);

        sendSocketMessage(msg4, msg4.getMsgId(), DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    // ???????????????????????????????????????EV
    public void setEV(BaseView baseView, String ev, boolean photoMode, CameraCommandCallback<Boolean> callback) {
        BaseCameraMsg msg4 = new BaseCameraMsg();
        msg4.setToken(token);
        msg4.setMsgId(2);
        msg4.setType(photoMode ? "photo_ev" : "video_ev");
        msg4.setParam(ev);

        sendSocketMessage(msg4, msg4.getMsgId(), DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    // ???????????? ??????
    public void setPhotoSharpness(BaseView baseView, String sharpe, CameraCommandCallback<Boolean> callback) {
        BaseCameraMsg msg4 = new BaseCameraMsg();
        msg4.setToken(token);
        msg4.setMsgId(2);
        msg4.setType("photo_sharpness");
        msg4.setParam(sharpe);

        sendSocketMessage(msg4, msg4.getMsgId(), DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    // ?????????????????????
    public void setDemession(BaseView baseView, String demension, CameraCommandCallback<Boolean> callback) {
        BaseCameraMsg msg1 = new BaseCameraMsg();
        msg1.setToken(token);
        msg1.setMsgId(260);
        sendSocketMessage(msg1, msg1.getMsgId(), DataType_integer, null);

        BaseCameraMsg msg4 = new BaseCameraMsg();
        msg4.setToken(token);
        msg4.setMsgId(2);
        msg4.setType("video_resolution");
        msg4.setParam(demension);
        sendSocketMessage(msg4, msg4.getMsgId(), DataType_ABCMDValue, null);

        BaseCameraMsg msg5 = new BaseCameraMsg();
        msg5.setToken(token);
        msg5.setMsgId(259);
        sendSocketMessage(msg5, msg5.getMsgId(), DataType_ABCMDValue, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    // ????????????????????????
    public void setVideoEncode(BaseView baseView, String encodetype, CameraCommandCallback<Boolean> callback) {
        BaseCameraMsg msg1 = new BaseCameraMsg();
        msg1.setToken(token);
        msg1.setMsgId(260);
        sendSocketMessage(msg1, msg1.getMsgId(), DataType_integer, null);

        BaseCameraMsg msg4 = new BaseCameraMsg();
        msg4.setToken(token);
        msg4.setMsgId(2);
        msg4.setType("sec_stream");
        msg4.setParam(encodetype);
        sendSocketMessage(msg4, msg4.getMsgId(), DataType_ABCMDValue, null);

        BaseCameraMsg msg5 = new BaseCameraMsg();
        msg5.setToken(token);
        msg5.setMsgId(259);
        sendSocketMessage(msg5, msg5.getMsgId(), DataType_ABCMDValue, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    // ?????????????????? 6k????????????
    public void setFengshan(BaseView baseView, boolean openOrClose, CameraCommandCallback<Boolean> callback) {
        BaseCameraMsg msg4 = new BaseCameraMsg();
        msg4.setToken(token);
        msg4.setMsgId(2);
        msg4.setType("fan_mode");
        msg4.setParam(openOrClose ? "on" : "off");

        sendSocketMessage(msg4, msg4.getMsgId(), DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    // ???????????????
    public void setAntifier(BaseView baseView, String antifier, CameraCommandCallback<Boolean> callback) {
        BaseCameraMsg msg1 = new BaseCameraMsg();
        msg1.setToken(token);
        msg1.setMsgId(260);
        sendSocketMessage(msg1, msg1.getMsgId(), DataType_integer, null);

        BaseCameraMsg msg4 = new BaseCameraMsg();
        msg4.setToken(token);
        msg4.setMsgId(2);
        msg4.setType("frequency");
        msg4.setParam(antifier);
        sendSocketMessage(msg4, msg4.getMsgId(), DataType_integer, null);

        BaseCameraMsg msg5 = new BaseCameraMsg();
        msg5.setToken(token);
        msg5.setMsgId(259);
        sendSocketMessage(msg5, msg5.getMsgId(), DataType_ABCMDValue, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    // ?????????SD???
    public void formatSDcard(BaseView baseView, CameraCommandCallback<Boolean> callback) {
        BaseCameraMsg msg4 = new BaseCameraMsg();
        msg4.setToken(token);
        msg4.setMsgId(4);
        msg4.setParam("C");

        sendSocketMessage(msg4, msg4.getMsgId(), DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    // ????????????
    public void rebootCamera() {
        clearCommands();
        BaseCameraMsg msg1 = new BaseCameraMsg();
        msg1.setToken(token);
        msg1.setMsgId(2052);
        sendSocketMessage(msg1, msg1.getMsgId(), DataType_integer, null);
    }

    // ??????????????????
    public void resetCameraParams(BaseView baseView, boolean needReset, CameraCommandCallback<Boolean> callback) {
        if (needReset) {
            BaseCameraMsg msg1 = new BaseCameraMsg();
            msg1.setToken(token);
            msg1.setMsgId(260);
            sendSocketMessage(msg1, msg1.getMsgId(), DataType_integer, null);

            BaseCameraMsg msg4 = new BaseCameraMsg();
            msg4.setToken(token);
            msg4.setMsgId(2);
            msg4.setType("default_setting");
            msg4.setParam("on");
            sendSocketMessage(msg4, msg4.getMsgId(), DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
                @Override
                public void onDigestResults(ABCmdValue<Integer> data) {
                    if (callback != null) {
                        callback.onCompleteInMain(baseView, data.getRval() == 0);
                    }
                }
            });

            BaseCameraMsg msg5 = new BaseCameraMsg();
            msg5.setToken(token);
            msg5.setMsgId(259);
            sendSocketMessage(msg5, msg5.getMsgId(), DataType_ABCMDValue, null);
        } else {
            BaseCameraMsg msg4 = new BaseCameraMsg();
            msg4.setToken(token);
            msg4.setMsgId(2);
            msg4.setType("default_setting");
            msg4.setParam("on");
            sendSocketMessage(msg4, msg4.getMsgId(), DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
                @Override
                public void onDigestResults(ABCmdValue<Integer> data) {
                    if (callback != null) {
                        callback.onCompleteInMain(baseView, data.getRval() == 0);
                    }
                }
            });
        }

    }

    /**
     * ?????????????????????????????????
     */
    public void startRobLine(CameraCommandCallback callback) {
        BaseCameraMsg msg = new BaseCameraMsg(callback);
        msg.setToken(token);
        msg.setMsgId(ROB_LINE);
        msg.setRval(0);
        if (cameraSocket != null)
            cameraSocket.send(msg);
    }

    /**
     * ??????????????????
     */
    public void getRecordTime(BaseView baseView, CameraCommandCallback<Integer> callback) {
        BaseCameraMsg msg = new BaseCameraMsg();
        msg.setToken(token);
        msg.setMsgId(RECORED_TIME);
        sendSocketMessage(msg, msg.getMsgId(), DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval() == 0 ? data.getParam() : -1);
                }
            }
        });
    }

    public void readTakePhotoResultComplete(BaseView baseView, CameraCommandCallback<Integer> callback) {
        BaseCameraMsg msg = new BaseCameraMsg();
        msg.setToken(token);
        msg.setMsgId(769);

        SocketBaseMsg ssmsg = new SocketBaseMsg();
        ssmsg.msgTag = msg.getMsgId();
        ssmsg.params = msg.getJsonMsg().getBytes();
        ssmsg.callback = new SocketBaseMsg.Callback() {
            @Override
            public void onComplete(int result_code, byte[] responseData) {
                if (responseData != null && responseData.length > 0) {
                    String buffStr = new String(responseData, 0, responseData.length);
                    parsingPackage(buffStr);
                }
            }
        };
        ssmsg.returnCallback = new SocketBaseMsg.ReturnCallback() {
            @Override
            public void onComplete(String data) {
                if (callback != null) {
                    Type type = new TypeToken<ABCmdValue<Integer>>() {
                    }.getType();
                    ABCmdValue<Integer> value = MGson.newGson().fromJson(data, type);
                    callback.onCompleteInMain(baseView, value.getRval() == 0 ? value.getParam() : 0);
                }
            }
        };
        mLock.lock();
        mCommands.add(ssmsg);
        mLock.unlock();
    }

    public void toggleDataSocket(CameraCommandCallback callback) {
        BaseCameraMsg msg = new BaseCameraMsg(callback);
        msg.setToken(token);
        msg.setMsgId(DATA_SOCKET);
        msg.setType("TCP");
        msg.setParam(ConnectManager.getInstance().mProductModel.dataSocketIp);

        sendSocketMessage(msg, msg.getMsgId(), DataType_ABCMDValue, new CameraCommandHandlerCallback<ABCmdValue>() {
            @Override
            public void onDigestResults(ABCmdValue mv) {
                if (mv.getRval() == 0) {
                    if (callback != null)
                        callback.onComplete(mv);
                }
            }
        });
    }


    /**
     * ??????VF
     */
    public void resetVf(ResultCallback.BoolCallback callback) {
        BaseCameraMsg msg = new BaseCameraMsg();
        msg.setToken(token);
        msg.setMsgId(RESET_VF);

        sendSocketMessage(msg, msg.msgId, DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onResult(data != null && data.getRval() == 0);
                }
            }
        });
    }

    /**
     * ??????SD?????????
     */
    public void getFreeSDCard(CameraCommandCallback callback) {
        BaseCameraMsg msg = new BaseCameraMsg(callback);
        msg.setToken(token);
        msg.setMsgId(SDCARD_FREE);
        msg.setType("free");

        sendSocketMessage(msg, msg.msgId, DataType_Long, new CameraCommandHandlerCallback<ABCmdValue<Long>>() {
            @Override
            public void onDigestResults(ABCmdValue<Long> mv) {
                if (callback != null)
                    callback.onComplete(mv);
            }
        });
    }

    public void getFreeSDCardInmain(BaseView baseView, CameraCommandCallback<Long> callback) {
        BaseCameraMsg msg = new BaseCameraMsg(callback);
        msg.setToken(token);
        msg.setMsgId(SDCARD_FREE);
        msg.setType("free");

        sendSocketMessage(msg, msg.msgId, DataType_Long, new CameraCommandHandlerCallback<ABCmdValue<Long>>() {
            @Override
            public void onDigestResults(ABCmdValue<Long> mv) {
                if (callback != null)
                    callback.onCompleteInMain(baseView, mv.getRval() == 0 ? mv.getParam() : -1l);
            }
        });
    }

    public void getTotalSDCard(BaseView baseView, CameraCommandCallback<Integer> callback) {
        BaseCameraMsg msg = new BaseCameraMsg();
        msg.setToken(token);
        msg.setMsgId(SDCARD_FREE);
        msg.setType("total");

        sendSocketMessage(msg, msg.getMsgId(), DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> mv) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, mv.getRval() == 0 ? mv.getParam() : -1);
                }
            }
        });
    }

    /**
     * ??????????????????????????????
     * 6k ????????????,4k???????????????????????????
     * yesOrNot:true ????????????false ?????????
     */
    public void getSDCardStateComplete(BaseView baseview, CameraCommandCallback<Integer> callback) {
        BaseCameraMsg msg = new BaseCameraMsg();
        msg.setToken(token);
        msg.setMsgId(SD_INTERNAL);

        sendSocketMessage(msg, msg.msgId, DataType_integer, new CameraCommandHandlerCallback<ABCmdValue>() {
            @Override
            public void onDigestResults(ABCmdValue mv) {
                if (callback != null) callback.onCompleteInMain(baseview, mv.getRval());
            }
        });
    }

    // ???????????????????????? 6k????????????
    public void getZoomSliderValueComplete(BaseView baseview, CameraCommandCallback<Integer> callback) {
        BaseCameraMsg msg = new BaseCameraMsg();
        msg.setToken(token);
        msg.setMsgId(2057);

        sendSocketMessage(msg, msg.msgId, DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> mv) {
                if (callback != null)
                    callback.onCompleteInMain(baseview, mv.getRval() == 0 ? mv.getParam() : -1);
            }
        });
    }

    /**
     * ??????????????????
     *
     * @param ???String???
     */
    public void setCameraParam(BaseView baseView, String type, Object param, CameraCommandCallback<Boolean> callback) {

        BaseCameraMsg msg4 = new BaseCameraMsg();
        msg4.setToken(token);
        msg4.setMsgId(2);
        msg4.setType(type);
        msg4.setParam(param);

        sendSocketMessage(msg4, msg4.getMsgId(), DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> data) {
                if (callback != null) {
                    callback.onCompleteInMain(baseView, data.getRval() == 0);
                }
            }
        });
    }

    public void setSpotMeter(boolean isPhoto, boolean isSpotOpen, int x, int y) {
        String[] meters = ResourceUtils.getStringArray(R.array.METER_STATUS);
        BaseCameraMsg msg1 = new BaseCameraMsg();
        msg1.setToken(token);
        msg1.setMsgId(2);
        msg1.setType(isPhoto ? ConstantFields.CAMERA_CMD.SET_PHOTO_METER : ConstantFields.CAMERA_CMD.SET_VIDOE_METER);
        msg1.setParam(!isSpotOpen ? meters[0] : meters[2]);
        sendSocketMessage(msg1, msg1.msgId, DataType_integer, null);

        if (isSpotOpen) {
            BaseCameraMsg msg2 = new BaseCameraMsg();
            msg2.setToken(token);
            msg2.setMsgId(SPOT_METER);
            msg2.setXposition(x);
            msg2.setYposition(y);
            sendSocketMessage(msg2, msg2.getMsgId(), DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
                @Override
                public void onDigestResults(ABCmdValue<Integer> data) {
                }
            });
        }
    }

    /**
     * ??????????????????
     * 1.??????assets???bin???????????????
     * 2.??????SD?????????
     * 3.
     */
    public void startSendFile(BaseView baseView, DataSocketReadCallback callback) {
        RxLoopSchedulers.composeIO(baseView, new Function() {
            @Override
            public String apply(Object object) throws Exception {
                String filePath = FileUtils.getSdPaths(ConstantFields.SD_DIR.FP_BIN);
                if (FileUtils.isFileExists(filePath)) {
                    LogUtils.d("??????????????????", "?????????????????????" + filePath);
                    FileUtils.deleteDir(new File(filePath));
                }
                String name = FileUtils.unZipAssetsFolder(ConnectManager.getInstance().mProductModel.cameraFirmareName, filePath);
                filePath = filePath + File.separator;
                FileUtils.rename(new File(filePath + name), ConstantFields.SD_FILE_NAME.FP_CAMERA_BIN);
                return filePath + ConstantFields.SD_FILE_NAME.FP_CAMERA_BIN;
            }
        }).subscribe(new RxLoopObserver<String>() {
            @Override
            public void onNext(String outFilePath) {
                super.onNext(outFilePath);
                this.disposeDisposables();
                File file = new File(outFilePath);
                if (FileUtils.isFileExists(file)) {
                    //????????????SD?????????????????????
                    if (callback != null) callback.onUnpackZipResult(true);
                    if (ConnectManager.getInstance().mProductModel.productType == ConstantFields.ProductType_6kAir) {
                        getSDCardStateComplete(baseView, new CameraCommandCallback<Integer>() {
                            @Override
                            public void onComplete(Integer internalCardSate) {
                                if (internalCardSate == 1) {
                                    // ???????????????????????????????????????
                                    LogUtils.d("????????????====>???????????????????????????????????????");
                                    if (callback != null) callback.onErrorCallback(SD_INTERNAL, 1);
                                } else {
                                    getSdFreeForSendFile(file, callback);
                                }
                            }
                        });
                    } else {
                        getSdFreeForSendFile(file, callback);
                    }


//                    if (cameraSocket != null)
//                        cameraSocket.send(msg);

                } else {
                    if (callback != null) callback.onUnpackZipResult(false);
                    LogUtils.d("???????????????" + outFilePath);
                }
            }
        });
    }

    private void getSdFreeForSendFile(File file, DataSocketReadCallback callback) {
        getFreeSDCard(new CameraCommandCallback<ABCmdValue<Long>>() {
            @Override
            public void onComplete(ABCmdValue<Long> cb) {
                if (cb.getRval() == 0) {
                    //??????
                    if (FileUtils.getFileLength(file) <= cb.getParam() * 1024) {
                        //????????????
                        LogUtils.d("??????????????????", "SD?????????");
                        startSendFile(file, callback);
                    } else {
                        //????????????
                        if (callback != null)
                            callback.onErrorCallback(SDCARD_FREE, -100);
                    }
                } else {
                    //??????
                    if (callback != null)
                        callback.onErrorCallback(SDCARD_FREE, cb.getRval());
                }
            }
        });
    }


    public void startSendFile(File file, DataSocketReadCallback callback) {
        String tagPath = ConstantFields.UPGRADE_FW.OLD_FW_PATH;
        sendFileMd5 = FileUtils.getFileMD5ToString(file);
        BaseCameraMsg msg = new BaseCameraMsg(null);
        msg.setToken(token);
        msg.setMsgId(START_SEND_FILE);
        msg.setParam(tagPath);
        msg.setSize(FileUtils.getFileLength(file));
        msg.setMd5sum(sendFileMd5);
        msg.setOffset(0);

        sendSocketMessage(msg, msg.getMsgId(), DataType_ABCMDValue, new CameraCommandHandlerCallback<ABCmdValue>() {
            @Override
            public void onDigestResults(ABCmdValue cb) {
                if (cb.getRval() == 0) {
                    sendFileUse8787(file.getAbsolutePath(), callback);
                } else {
                    if (callback != null)
                        callback.onErrorCallback(msg.getMsgId(), cb.getRval());
                }
            }
        });
    }

    private void sendFileUse8787(String filepath, DataSocketReadCallback callback) {
        LogUtils.d("?????????????????????===???" + filepath);
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(filepath));
            long count = inputStream.available();
            while (count == 0) {
                count = inputStream.available();
            }
            int packetSize = 1024 * 1024;//?????????????????????1M
            byte[] buf = new byte[packetSize];
            int packet = 0;
            int packetCount = (int) Math.ceil((double) count / ((double) packetSize));//?????????
//                        lastDataPacket = (int) (count - ((long) (packetSize * packetCount)));//??????????????????????????????0
            int len;
            //??????????????????????????????
            while (((len = inputStream.read(buf)) != -1)) {
                Thread.sleep(2000);//??????1s????????????
                packet++;
                LogUtils.d("??????????????????", "???????????????--->" + packet + "||" + packetCount + "||" + len);
                if (callback != null)
                    callback.onReadUpdataProgress((int) ((float) packet / (float) packetCount * 100f));
                sendMessageUseDataSocket(buf, len);
            }
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void renameFile(BaseView baseView, String oldName, String newName, CameraCommandCallback callback) {
        BaseCameraMsg msg = new BaseCameraMsg(callback);
        msg.setToken(token);
        msg.setMsgId(RENAME_FILE);
        msg.setOldname(oldName);
        msg.setNewname(newName);

        sendSocketMessage(msg, msg.msgId, DataType_ABCMDValue, new CameraCommandHandlerCallback<ABCmdValue>() {
            @Override
            public void onDigestResults(ABCmdValue mv) {
                if (callback != null)
                    callback.onCompleteInMain(baseView, mv);
            }
        });
    }

    public void renameFileInMain(BaseView baseView, String oldName, String newName, CameraCommandCallback callback) {
        BaseCameraMsg msg = new BaseCameraMsg(callback);
        msg.setToken(token);
        msg.setMsgId(RENAME_FILE);
        msg.setOldname(oldName);
        msg.setNewname(newName);

        sendSocketMessage(msg, msg.msgId, DataType_ABCMDValue, new CameraCommandHandlerCallback<ABCmdValue>() {
            @Override
            public void onDigestResults(ABCmdValue mv) {
                if (callback != null)
                    callback.onCompleteInMain(baseView, mv);
            }
        });
    }

    public void removeFileInMain(BaseView baseView, String srcname, String dstname, CameraCommandCallback callback) {
        BaseCameraMsg msg = new BaseCameraMsg(callback);
        msg.setToken(token);
        msg.setMsgId(1292);
        msg.setSrcname(srcname);
        msg.setDstname(dstname);

        sendSocketMessage(msg, msg.msgId, DataType_ABCMDValue, new CameraCommandHandlerCallback<ABCmdValue>() {
            @Override
            public void onDigestResults(ABCmdValue mv) {
                if (callback != null)
                    callback.onCompleteInMain(baseView, mv);
            }
        });
    }


    /**
     * ?????????SD????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    public void detectiondFw(BaseView bv, CameraCommandCallback callback) {
        BaseCameraMsg msg = new BaseCameraMsg();
        msg.setToken(token);
        msg.setMsgId(DETECTIOND_FW);

        sendSocketMessage(msg, msg.getMsgId(), DataType_ABCMDValue, new CameraCommandHandlerCallback<ABCmdValue>() {
            @Override
            public void onDigestResults(ABCmdValue mv) {
                if (callback != null) callback.onCompleteInMain(bv, mv);
            }
        });
    }

    int thumbLenth = 0;
    byte[] thumb = new byte[0];

    public void getFileThumb(boolean isSmall,
                             int tpye,
                             String path,
                             FileBean bean,
                             DataSocketReadCallback callback) {
        boolean old = ConnectManager.getInstance().mProductModel.productType == ConstantFields.ProductType_4k;
        if (!StringUtils.isEmpty(path)) {
            path = tpye == ConstantFields.FILE_TYPE.TYPE_VIDEO ?
                    path.replace(old ? ConstantFields.SD_DIR.VR_PRO_MOVIE : ConstantFields.SD_DIR.VR_PRO_MOVIE_NEW,
                            old ? ConstantFields.SD_DIR.VR_PRO_MOVIE_CACHE : ConstantFields.SD_DIR.VR_PRO_MOVIE_CACHE_NEW)
                    : path;
            BaseCameraMsg msg = new BaseCameraMsg();
            msg.setToken(token);
            msg.setMsgId(FILE_THUMB);
            msg.setType(tpye == ConstantFields.FILE_TYPE.TYPE_VIDEO ? "idr" : (isSmall ? "smallthumb" : "thumb"));
            msg.setParam(ConstantFields.SD_DIR.MEDIA_START + path);

            thumb = new byte[0];
            sendSocketMessage(msg, msg.getMsgId(), DataType_ABCMDValue, new CameraCommandHandlerCallback<ABCmdValue>() {
                @Override
                public void onDigestResults(ABCmdValue cb) {
                    if (cb.getRval() == 0) {
                        if (StringUtils.isEmpty(cb.getSize())) {
                            callback.onErrorCallback(msg.getMsgId(), cb.getRval());
                        } else {
                            thumbLenth = Integer.parseInt(cb.getSize());
                            if (callback != null)
                                callback.onReadThumb(thumbLenth, thumb);
//                        bean.setReadBitmapLength(Integer.parseInt(cb.getSize()));
                        }
                    } else {
                        callback.onErrorCallback(msg.getMsgId(), cb.getRval());
                    }
                }
            });

            SocketManager.getCmdInstance().setDataSocketReadListener(new DataSocketReadCallback<FileBean>() {
                @Override
                public void onReadData(byte[] msg) {
                    super.onReadData(msg);
//                LogUtils.d("??????datasocket??????===>msg=" + msg.length + "||" + thumb.length);
                    if (thumb.length > 0) {
                        byte[] bytes = new byte[thumb.length + msg.length];
                        System.arraycopy(thumb, 0, bytes, 0, thumb.length);
                        System.arraycopy(msg, 0, bytes, thumb.length, msg.length);
                        thumb = new byte[bytes.length];
                        System.arraycopy(bytes, 0, thumb, 0, bytes.length);
                    } else {
                        thumb = new byte[msg.length];
                        System.arraycopy(msg, 0, thumb, 0, msg.length);
                    }
                    if (callback != null)
                        callback.onReadThumb(thumbLenth, thumb);
//                while ((bean.getReadBitmapLength() == 0 || thumb.length < bean.getReadBitmapLength()) && msg == null) {
//                    LogUtils.d("?????????===>thumb=" + thumb.length + "||" + bean.getReadBitmapLength() + "|||" + (msg == null));
//                }
//                LogUtils.d("??????datasocket??????===>thumb=" + thumb.length + "||" + bean.getReadBitmapLength());
//                if (bean.getReadBitmapLength() != 0 && thumb.length == bean.getReadBitmapLength()) {

//                }
                }
            });
//        if (cameraSocket != null)
//            cameraSocket.send(msg);
        }
    }

    public void getVideoParameters(String videoPath, CameraCommandCallback callback) {
        BaseCameraMsg msg = new BaseCameraMsg(callback);
        msg.setToken(token);
        msg.setMsgId(VIDEO_PARAMETERS);
        msg.setParam(ConstantFields.SD_DIR.MEDIA_START + videoPath);

        sendSocketMessage(msg, msg.getMsgId(), DataType_ABCMDValue, new CameraCommandHandlerCallback<ABCmdValue>() {
            @Override
            public void onDigestResults(ABCmdValue mv) {
                //????????????????????????????????????????????????????????????????????????????????????????????????
                if (callback != null) callback.onComplete(mv);
            }
        });

//        if (cameraSocket != null)
//            cameraSocket.send(msg);
    }

    /**
     * ?????????????????????
     */
    public void getAllSetting(BaseView baseView, CameraCommandCallback callback) {
//        LogUtils.d("?????????????????????");
        BaseCameraMsg msg = new BaseCameraMsg(callback);
        msg.setToken(token);
        msg.setMsgId(ALL_SETTING);
        sendSocketMessage(msg, msg.getMsgId(), DataType_SettingBean, new CameraCommandHandlerCallback<SettingBean>() {
            @Override
            public void onDigestResults(SettingBean settingBean) {
                //?????????????????????
                if (callback != null) callback.onCompleteInMain(baseView, settingBean);
            }
        });
    }

    public void setCareraTime() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = sDateFormat.format(new Date());
        sDateFormat = new SimpleDateFormat("HH:mm:ss");
        String time = sDateFormat.format(new Date());

        BaseCameraMsg msg = new BaseCameraMsg();
        msg.setToken(token);
        msg.setMsgId(2);
        msg.setType(ConstantFields.CAMERA_CMD.SET_CAMERA_TIME);
        msg.setParam(date + " " + time);

        sendSocketMessage(msg, msg.msgId, DataType_integer, null);
    }


    /**
     * ???????????????????????????
     * ??????????????????????????????
     */
    public String getAllMediaFile(String string, CameraCommandCallback callback) {
        // ?????????????????????
        String path = ConstantFields.SD_DIR.MEDIA_START + string;
        BaseCameraMsg msg = new BaseCameraMsg(callback);
        msg.setToken(token);
        msg.setMsgId(MEDIA_DATA);
        msg.setParam(path + ConstantFields.SD_DIR.MEDIA_END);

        sendSocketMessage(msg, msg.getMsgId(), DataType_ABCMDValue, new CameraCommandHandlerCallback<ABCmdValue>() {

            @Override
            public void onDigestResults(ABCmdValue data) {
                if (callback != null)
                    callback.onComplete(data);
            }
        });

        return string;
    }

    // ?????????????????????
    public void getInternalSDCardFileListComplete(BaseView baseView, CameraCommandCallback<ArrayList<Map<String, String>>> callback) {

        ArrayList<Map<String, String>> renturnItems = new ArrayList<>();
        // 1????????????????????????
        String frompath = "/tmp/FL0/";
        String topath = "/tmp/SD0/";
//        String frompath = "/tmp/SD0/";
//        String topath = "/tmp/FL0/";
        for (int list = 0; list < 3; list++) {
            String path = "Photo/";
            if (list == 1) {
                path = "Movie/";
            }
            if (list == 2) {
                path = "Cache/";
            }
            final int finallist = list;
            final String finalPath = path;
            String para = String.format("%sFlyPie/%s -D -S", frompath, path);
            BaseCameraMsg msg = new BaseCameraMsg(callback);
            msg.setToken(token);
            msg.setMsgId(MEDIA_DATA);
            msg.setParam(path);
            getAllMediaFileWithPath(para, new CameraCommandCallback<ABCmdValue>() {
                @Override
                public void onComplete(ABCmdValue data) {
                    List<Map<String, String>> lists = data.getList();
                    // ??????????????????
                    int toal = lists.size();
                    for (int i = 0; i < toal; i++) {
                        Map<String, String> file = lists.get(i);
                        String name = (String) file.keySet().toArray()[0];
                        String fullPath = String.format("%sFlyPie/%s%s", frompath, finalPath, name);
                        String newPath = String.format("%sFlyPie/%s%s", topath, finalPath, name);
                        Map<String, String> item = new HashMap<>();
                        item.put(fullPath, newPath);
                        if ((name.endsWith(".DNG") || name.endsWith(".JPG")) && finallist == 0) {
                            renturnItems.add(item);
                        } else if (name.endsWith(".MP4") && finallist == 1) {
                            renturnItems.add(item);
                        } else if ((name.endsWith(".MP4") || name.endsWith(".jpg")) && finallist == 2) {
                            renturnItems.add(item);
                        }
                    }
                }
            });
        }
        // ===== ?????????????????????????????? ====///

        // 2????????????SD????????????
        String paraSD = String.format("%sFP_AI/ -D -S", topath);
        getAllMediaFileWithPath(paraSD, new CameraCommandCallback<ABCmdValue>() {
            @Override
            public void onComplete(ABCmdValue data) {
                List<Map<String, String>> paths2 = data.getList();

                int maxV = 0;
                int maxT = 0;
                for (int pIndex2 = 0; pIndex2 < paths2.size(); pIndex2++) {
                    Map<String, String> file = paths2.get(pIndex2);
                    String path2 = (String) file.keySet().toArray()[0];
                    path2 = path2.replace("/", "");
                    if (path2.startsWith("V")) {
                        path2 = path2.replace("V", "");
                        if (maxV < Integer.valueOf(path2)) {
                            maxV = Integer.valueOf(path2);
                        }
                    } else {
                        path2 = path2.replace("T", "");
                        if (maxT < Integer.valueOf(path2)) {
                            maxT = Integer.valueOf(path2);
                        }
                    }
                }
                final int finalmaxV = maxV;
                final int finalmaxT = maxT;

                // 3???????????????????????????????????????
                String para = String.format("%sFP_AI/ -D -S", frompath);
                getAllMediaFileWithPath(para, new CameraCommandCallback<ABCmdValue>() {
                    @Override
                    public void onComplete(ABCmdValue data) {
                        List<Map<String, String>> paths2 = data.getList();

                        if (paths2.size() == 0) {
                            if (callback != null) {
                                callback.onCompleteInMain(baseView, renturnItems);
                            }
                            return;
                        }

                        for (int pIndex2 = 0; pIndex2 < paths2.size(); pIndex2++) {
                            final int finalpIndex2 = pIndex2;
                            Map<String, String> file = paths2.get(pIndex2);
                            String path2 = (String) file.keySet().toArray()[0];
                            String path222 = path2.replace("/", "");
                            path222 = path222.replace("V", "");
                            path222 = path222.replace("T", "");
                            final String finaPath222 = path222;

                            String folderPath2 = String.format("%sFP_AI/%s -D -S", frompath, path2);
                            String smartFromPath = frompath + "FP_AI/";
                            String smartToPath = topath + "FP_AI/";
                            if (!path2.startsWith(".")) {
                                getAllMediaFileWithPath(folderPath2, new CameraCommandCallback<ABCmdValue>() {
                                    @Override
                                    public void onComplete(ABCmdValue data) {
                                        List<Map<String, String>> paths1 = data.getList();

                                        for (int pIndex1 = 0; pIndex1 < paths1.size(); pIndex1++) {
                                            Map<String, String> file1 = paths1.get(pIndex1);
                                            String path1 = (String) file1.keySet().toArray()[0];

                                            // 4????????????????????????????????????
                                            if (!path2.startsWith(".")) {
                                                String srcpathParam = String.format("%s%s%s", smartFromPath, path2, path1);
                                                // ??????????????? "V"????????????????????????????????? "T"???????????????????????????????????????
                                                if (path2.startsWith("V")) {                // "V"?????????????????????????????????
                                                    String dstpathParam = String.format("%sV%04d/%s", smartToPath, finalmaxV + Integer.valueOf(finaPath222), path1);
                                                    Map<String, String> item = new HashMap<>();
                                                    item.put(srcpathParam, dstpathParam);
                                                    renturnItems.add(item);
                                                } else {
                                                    String dstpathParam = String.format("%sT%04d/%s", smartToPath, finalmaxT + Integer.valueOf(finaPath222), path1);
                                                    Map<String, String> item = new HashMap<>();
                                                    item.put(srcpathParam, dstpathParam);
                                                    renturnItems.add(item);
                                                }
                                            }
                                        }

                                        if (finalpIndex2 + 1 == paths2.size()) {
                                            if (callback != null) {
                                                callback.onCompleteInMain(baseView, renturnItems);
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });

    }

    public void getAllMediaFileWithPath(String path, CameraCommandCallback callback) {

        BaseCameraMsg msg = new BaseCameraMsg(callback);
        msg.setToken(token);
        msg.setMsgId(MEDIA_DATA);
        msg.setParam(path);

        sendSocketMessage(msg, msg.getMsgId(), DataType_ABCMDValue, new CameraCommandHandlerCallback<ABCmdValue>() {

            @Override
            public void onDigestResults(ABCmdValue data) {
                if (callback != null)
                    callback.onComplete(data);
            }
        });
    }

    public void deleteRemotFile(boolean isDir, String path, CameraCommandCallback callback) {
        BaseCameraMsg msg = new BaseCameraMsg(callback);
        msg.setToken(token);
        msg.setMsgId(isDir ? DELECT_PANORAMA_FOLDER : DELETE_FILE);
        msg.setParam(ConstantFields.SD_DIR.MEDIA_START + path);
        sendSocketMessage(msg, msg.getMsgId(), DataType_ABCMDValue, new CameraCommandHandlerCallback<ABCmdValue>() {

            @Override
            public void onDigestResults(ABCmdValue data) {
                if (callback != null)
                    callback.onComplete(data);
            }
        });
    }

    public void deleteRemotPanoramaFolder(String path) {
        BaseCameraMsg msg = new BaseCameraMsg();
        msg.setToken(token);
        msg.setMsgId(DELECT_PANORAMA_FOLDER);
        msg.setParam(ConstantFields.SD_DIR.MEDIA_START + path);
        if (cameraSocket != null)
            cameraSocket.send(msg);
    }

    public void putSettingStatus(ParamBean bean) {
        this.bean = bean;
    }

    public ParamBean getSettingStatus() {
        return bean;
    }

    /**
     * ???????????????
     */
    private String datas = "";

    private void parsingPackage(String data) {
        datas = datas + data;
        if (!datas.contains("}{")) {
            if ((datas.startsWith("{\"rval\"") && datas.endsWith("}]}") //?????????????????????????????????
                    || datas.endsWith("[]}"))//??????????????????????????????
                    || ((!datas.contains("listing") && !datas.contains("MP4") && !datas.contains("JPG")) && datas.endsWith("}"))//??????????????????????????????
                    || (datas.startsWith("{\"rval\"") && !datas.contains("},{"))) {//?????????????????????????????????
                readSocket(datas);
                datas = "";
            } else {
                LogUtils.d("????????????" + datas);
            }
        } else {
            LogUtils.d("??????????????????");
            String b = datas.substring(0, datas.indexOf("}{") + 1);
            String c = datas.substring(datas.indexOf("}{") + 1);
            datas = "";
            LogUtils.d("??????????????????" + b + "||" + c);
            parsingPackage(b);
            if (c != null && !c.equals("") && c.length() > 0) {
                LogUtils.d("???????????????1---" + c);
                c.substring(2);
                LogUtils.d("???????????????2---" + c);
                parsingPackage(c);
            }
        }
    }

    private ReentrantLock mLock = new ReentrantLock();

    // SD????????????????????????
    public interface CameraSdcardStatusCallback {
        void sdcardStatus(int value);
    }

    private CameraSdcardStatusCallback mStatusCallback;

    // ??????????????????????????????
    public interface CameraFirmwareupdateCompleteCallback {
        void updateComplete(ABCmdValue cb);
    }

    public void setmStatusCallback(CameraSdcardStatusCallback mStatusCallback) {
        this.mStatusCallback = mStatusCallback;
    }

    public void setMupdateComplete(CameraFirmwareupdateCompleteCallback mupdateComplete) {
        this.mupdateComplete = mupdateComplete;
    }

    private CameraFirmwareupdateCompleteCallback mupdateComplete;
    private Handler mMainHander = new Handler(Looper.getMainLooper());

    private void readSocket(String data) {
        ABCmdValue cb = MGson.newGson().fromJson(data, ABCmdValue.class);
        LogUtils.d("???????????????==> " + cb.toString());

        if (cb.getMsg_id() == SD_INSERT || cb.getMsg_id() == SD_PULL_OUT
                || cb.getMsg_id() == SD_FULL || cb.getMsg_id() == SD_NOT_INIT
                || cb.getMsg_id() == SD_SPEED_SLOW || cb.getMsg_id() == SD_FRAGMENTATION
                || cb.getMsg_id() == SD_LOW) {           //sd?????????
            mMainHander.post(() -> {
                if (mStatusCallback != null) {
                    mStatusCallback.sdcardStatus(cb.getMsg_id());
                }
            });
            return;
        } else if (cb.getMsg_id() == 1793) {
            BaseCameraMsg msg1793 = new BaseCameraMsg();
            msg1793.setToken(token);
            msg1793.setMsgId(1793);
            msg1793.setRval(0);
            SocketBaseMsg ssmsg = new SocketBaseMsg();
            ssmsg.msgTag = msg1793.getMsgId();
            ssmsg.params = msg1793.getJsonMsg().getBytes();
            realysendData(ssmsg);
            return;
        } else if (cb.getMsg_id() == 7) {   // ???????????????????????? ????????????????????????
            if (mupdateComplete != null) {
                mMainHander.post(() -> {
                    mupdateComplete.updateComplete(cb);
                });
            }
            return;
        }

        try {
            if (mLock.tryLock() || mLock.tryLock(1, TimeUnit.MICROSECONDS)) {
                if (mCommands.size() > 0) {
                    SocketBaseMsg msg = mCommands.get(0);
                    if (msg != null && msg.returnCallback != null) {
//                        LogUtils.d("????????????====>?????????" + mCommands.size() + "||||" + msg.msgTag);
                        mLock.unlock();
                        if (msg.msgTag == cb.getMsg_id()) {
                            msg.returnCallback.onComplete(data);
                            mLock.lock();
                            mCommands.remove(msg);
                            mLock.unlock();
//                            LogUtils.d("????????????====>?????????" + mCommands.size() + "||||" + msg.msgTag);
                            sendDataToCommandPort();
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (mLock.isHeldByCurrentThread()) {
                mLock.unlock();
            }
        }
    }

    private final static int DataType_integer = 0;
    private final static int DataType_boolean = 1;
    private final static int DataType_string = 2;
    private final static int DataType_ABCMDValue = 3;
    private final static int DataType_Long = 4;
    private final static int DataType_SettingBean = 5;

    @IntDef({DataType_integer, DataType_boolean, DataType_string, DataType_ABCMDValue, DataType_Long, DataType_SettingBean})
    @Retention(RetentionPolicy.SOURCE)
    @interface DataType {
    }

    private void sendSocketMessage(BaseCameraMsg msg, int responseTag, @DataType int dataType, CameraCommandHandlerCallback callback) {
        if (msg == null || msg.getJsonMsg() == null)
            return;
        Type type = new TypeToken<ABCmdValue<Integer>>() {
        }.getType();
        if (dataType == DataType_string) {
            type = new TypeToken<ABCmdValue<String>>() {
            }.getType();
        } else if (dataType == DataType_ABCMDValue) {
            type = new TypeToken<ABCmdValue>() {
            }.getType();
        } else if (dataType == DataType_Long) {
            type = new TypeToken<ABCmdValue<Long>>() {
            }.getType();
        } else if (dataType == DataType_SettingBean) {
            type = new TypeToken<SettingBean>() {
            }.getType();
        }
        final Type finalType = type;

        SocketBaseMsg ssmsg = new SocketBaseMsg();
        ssmsg.msgTag = responseTag;
        ssmsg.params = msg.getJsonMsg().getBytes();
        ssmsg.returnCallback = new SocketBaseMsg.ReturnCallback() {
            @Override
            public void onComplete(String data) {
                if (callback != null) {
                    callback.onResultData(finalType, data);
                }
            }
        };
//        LogUtils.d("????????????====>??????" + mCommands.size() + "||||" + ssmsg.msgTag);
        mLock.lock();
        mCommands.add(ssmsg);
        if (mCommands.size() == 1) {
            sendDataToCommandPort();
        }
        mLock.unlock();
    }


    private void sendMessageUseDataSocket(byte[] bytes, int length) {
        SocketManager.getCmdInstance().sendDataCommandTo8787(bytes, length);
    }


    /**
     * ??????Session
     */
    public void startSession(CameraCommandCallback callback) {
        synchronized (mCommands) {
            if (mCommands != null) mCommands.clear();
        }
        BaseCameraMsg msg = new BaseCameraMsg(callback);
        msg.token = 0;
        msg.msgId = START_SESSION;

        sendSocketMessage(msg, msg.msgId, DataType_integer, new CameraCommandHandlerCallback<ABCmdValue<Integer>>() {
            @Override
            public void onDigestResults(ABCmdValue<Integer> mv) {
                if (mv.getRval() == 0) {
                    LogUtils.d("START_SESSION??????===>????????????TCP");
                    setToken(mv.getParam());
                    toggleDataSocket(callback);
                } else {
                    if (callback != null) callback.onErrorCode(mv.getMsg_id(), mv.getRval());
                }
            }
        });
    }


    private void sendDataToCommandPort() {
        if (mCommands.size() == 0) {
            LogUtils.d("??????????????????????????????");
            return;
        }

        SocketBaseMsg msg = mCommands.get(0);
//        LogUtils.d("??????????????????????????? " + mCommands.size());
        String buffStr = new String(msg.params, 0, msg.params.length);
        LogUtils.d("???????????????==> " + JsonUtils.toJson(buffStr) + " ??????????????????????????? ==> " + (mCommands.size() - 1));

        realysendData(msg);
    }

    private void realysendData(SocketBaseMsg ssmsg) {
//        LogUtils.d("????????????????????? ==> " + JsonUtils.toJson(new String(ssmsg.params, 0, ssmsg.params.length)));
        ssmsg.callback = new SocketBaseMsg.Callback() {
            @Override
            public void onComplete(int result_code, byte[] responseData) {
                if (responseData != null && responseData.length > 0) {
                    String buffStr = new String(responseData, 0, responseData.length);
                    parsingPackage(buffStr);
                }
            }
        };
        SocketManager manager = SocketManager.getCmdInstance();
        manager.sendCameraCommandData(ssmsg);
    }

    public void clearCommands() {
        mLock.lock();
        mCommands.clear();
        mLock.unlock();
    }
}
