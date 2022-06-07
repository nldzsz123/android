package com.feipai.flypai.beans;

import android.util.SparseArray;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.feipai.flypai.ui.view.Camera.CameraSetConstaint;
import com.feipai.flypai.ui.view.Camera.CameraSetItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***所有设置项Item数据以MultiItemEntity为基类,可用instanceof进行强转*/
public abstract class FPCameraSettingBase<T extends MultiItemEntity> {

    public @CameraSetConstaint.CameraMode
    int lastUseMode;    // 相机模式
    public @CameraSetConstaint.PhotoMode
    int photoSubMode;   //拍照模式的子模式
    public @CameraSetConstaint.FPEncodeType
    int recordType;     //录制视频编码方式 6k 才有
    public boolean isRecording;


    public int sdcardFree;              //sd卡剩余容量
    public int sdcardTotal;             //sd卡总容量
    public int yanchiSecond;           //如果是是由app间隔拍照 间隔的秒钟数

    public boolean autoTakephoto;       // 是否由app间隔拍照
    public int photoEv;             // 照片EV的索引
    public boolean photoISOAuto;         //目前的照片是否ISO自动
    public boolean photoISOAutoValue;    //目前的照片手动模式中的ISO值是否为auto
    public int photoISO;                // 照片ISO的索引
    public boolean photoShutterAutoValue;//目前的照片手动模式中的shutter值是否auto
    public int photoShutter;             // 照片Shutter的索引
    public int photocontrast;           // 照片对比度
    public int photosaturation;        // 照片饱和度
    public int photosharpness;        // 照片锐度
    public boolean photoAELock;          //照片AE锁定
    public int photoZoom;              //照片缩放

    public int videoEv;             // 视频EV的索引
    public boolean videoISOAuto;        //目前的录像是否ISO自动
    public boolean videoISOAutoValue;    //目前的视频手动模式中的ISO值是否为auto
    public int videoISO;                // 视频ISO的索引
    public boolean videoShutterAutoValue;//目前的视频手动模式中的shutter值是否auto
    public int videoShutter;             // 视频Shutter的索引
    public int videocontrast;       // 视频对比度
    public int videosaturation;        // 视频饱和度
    public int videosharpness;        // 视频锐度
    public boolean videoAELock;         //视频AE锁定
    public int videoZoom;          //视频缩放
    public boolean video10bitModeOn;         //6k 才有 视频10bit模式


    // 照片设置
    public int photoMethod;              //拍照模式
    public int photoAtio;               //拍照尺寸
    public int photoRuidu;               //照片锐度
    public boolean photoRaw;              //Raw 格式

    // 视频设置

    // 一般设置
    public boolean qianbideng;     // 前臂灯
    public boolean houbideng;      // 后臂灯
    public boolean ytHoriSet;      // 云台水平微调
    public int antikeyIndex;   // 抗闪烁
    public int gridMode;       // 网格线


    // for 部分机型 6k 系列
    // 云台控制条
    public boolean ytSwitchOn;
    // 变焦控制条 速度
    public int zoomIndex;
    public boolean fenOn;         //6k 才有 风扇是否打开

    // 一般设置项目
    protected SparseArray<T> _generaSettingItems;
    // 照片设置
    protected SparseArray<T> _photoOptionItems;
    // 照片效果
    protected SparseArray<T> _photoEffectItems;
    // 视频设置
    protected SparseArray<T> _videoOptionItems;
    // 视频效果
    protected SparseArray<T> _videoEffectItems;

    // 照片设置
    abstract public ArrayList<T> photoOptionItems();

    // 照片效果
    abstract public ArrayList<T> photoEffectItems();

    // 视频设置
    abstract public ArrayList<T> videoOptionItems();

    // 视频效果
    abstract public ArrayList<T> videoEffectItems();

    // 一般设置项目
    abstract public ArrayList<T> generaSettingItems();

    abstract public void updateCameraSettings(SettingBean.ParamBean params);

    // 目前的视频设置项
    public CameraSetItem.FPDimenssionItem currentVideoDimenSion() {

        CameraSetItem.FPAccessryBaseCellItem accessryBaseCellItem = (CameraSetItem.FPAccessryBaseCellItem) _videoOptionItems.get(CameraSetConstaint.cell_video_Dimesion_key);
        List<CameraSetItem.FPDimenssionItem> items = (List<CameraSetItem.FPDimenssionItem>) accessryBaseCellItem.getSubItems();
        CameraSetItem.FPDimenssionItem item = items.get(0);

        for (int i = 0; i < items.size(); i++) {
            CameraSetItem.FPDimenssionItem item22 = items.get(i);
            if (item22.checked) {
                item = item22;
                break;
            }
        }

        return item;
    }

    // 更新目前的视频分辨率
    abstract public void updateCurDimension(String dimension);

    // 获取当前设置项
    public CameraSetItem.FPSettingBaseCellItem itemForKeyId(int keyId) {
        CameraSetItem.FPSettingBaseCellItem item;

        item = (CameraSetItem.FPSettingBaseCellItem) _photoOptionItems.get(keyId);
        if (item != null) {
            return item;
        }

        item = (CameraSetItem.FPSettingBaseCellItem) _photoEffectItems.get(keyId);
        if (item != null) {
            return item;
        }

        item = (CameraSetItem.FPSettingBaseCellItem) _videoOptionItems.get(keyId);
        if (item != null) {
            return item;
        }

        item = (CameraSetItem.FPSettingBaseCellItem) _videoEffectItems.get(keyId);
        if (item != null) {
            return item;
        }

        item = (CameraSetItem.FPSettingBaseCellItem) _generaSettingItems.get(keyId);
        if (item != null) {
            return item;
        }

        return item;
    }


}
