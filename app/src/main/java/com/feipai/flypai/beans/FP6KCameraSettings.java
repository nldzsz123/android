package com.feipai.flypai.beans;

import android.util.SparseArray;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.feipai.flypai.R;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.ui.view.Camera.CameraSetConstaint;
import com.feipai.flypai.ui.view.Camera.CameraSetItem;
import com.feipai.flypai.ui.view.Camera.CameraSetItem.*;
import com.feipai.flypai.ui.view.Camera.CameraSetItem.FPTwoButtonCellItem;
import com.feipai.flypai.ui.view.Camera.CameraSetItem.FPSwitchCellItem;
import com.feipai.flypai.ui.view.Camera.CameraSetItem.FPSettingBaseCellItem;
import com.feipai.flypai.utils.cache.CacheManager;
import com.feipai.flypai.utils.cache.SharedPrefUtils;
import com.feipai.flypai.utils.global.ConvertUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.socket.TcpReadListener;

import java.util.ArrayList;
import java.util.List;

public class FP6KCameraSettings extends FPCameraSettingBase<MultiItemEntity> {
    private String updatevideoDimenssion; // 视频分辨率

    public FP6KCameraSettings() {
        this.lastUseMode = CameraSetConstaint.CameraModePhoto;
        updatevideoDimenssion = "5472x3072 30P 16:9 AH";    // 默认6K
        int type = CacheManager.getSharedPrefUtils().getInt(ConstantFields.PREF.Grid_type);
        gridMode =  type== -1 ? 0:type;

        photoOptionItems();
        photoEffectItems();
        videoOptionItems();
        videoEffectItems();
        generaSettingItems();
    }

    @Override
    public void updateCurDimension(String dimension) {
        updatevideoDimenssion = dimension;
    }

    @Override
    public ArrayList<MultiItemEntity> photoOptionItems() {
        if (_photoOptionItems == null) {
            // 拍照模式
            CameraSetItem.FPTwoButtonCellItem photoModeItem = new CameraSetItem.FPTwoButtonCellItem();
            photoModeItem.keyId = CameraSetConstaint.cell_photo_mode_key;
            photoModeItem.leftTitle = ResourceUtils.getString(R.string.camra_set_photo_take_method);
            photoModeItem.titles = ResourceUtils.getStringArray(R.array.photo_take_method);
            photoModeItem.normalMipmap = R.mipmap.btn_normal;
            photoModeItem.selectMipmap = R.mipmap.btn_select;

            // 照片尺寸
            CameraSetItem.FPTwoButtonCellItem photoAtioItem = new CameraSetItem.FPTwoButtonCellItem();
            photoAtioItem.keyId = CameraSetConstaint.cell_photo_Atio_key;
            photoAtioItem.leftTitle = ResourceUtils.getString(R.string.camra_set_photo_atio);
            photoAtioItem.titles = ResourceUtils.getStringArray(R.array.photo_take_atio_6k);
            photoAtioItem.normalMipmap = R.mipmap.btn_normal;
            photoAtioItem.selectMipmap = R.mipmap.btn_select;
            photoAtioItem.commands = ResourceUtils.getStringArray(R.array.photoAitios_6k);

            // 锐度
            CameraSetItem.FPTwoButtonCellItem ruiduItem = new CameraSetItem.FPTwoButtonCellItem();
            ruiduItem.keyId = CameraSetConstaint.cell_photo_ruidu_key;
            ruiduItem.leftTitle = ResourceUtils.getString(R.string.camra_set_photo_ruidu);
            ruiduItem.titles = ResourceUtils.getStringArray(R.array.ruidu);
            ruiduItem.normalMipmap = R.mipmap.btn_normal;
            ruiduItem.selectMipmap = R.mipmap.btn_select;
            ruiduItem.commands =  ResourceUtils.getStringArray(R.array.photo_ruidu);

            // 效果增强
            FPAccessryBaseCellItem<FPScrollItem> photoEnhItem = new FPAccessryBaseCellItem();
            photoEnhItem.leftTitle = ResourceUtils.getString(R.string.enhancement_effect);
            List<FPScrollItem> enhChildItems = new ArrayList<>();
            FPScrollItem enhChild1 = new FPScrollItem();
            enhChild1.keyId = CameraSetConstaint.cell_photo_baohedu_key;
            enhChild1.itemTitle = ResourceUtils.getString(R.string.saturation);
            enhChild1.values = ResourceUtils.getStringArray(R.array.saturation);
            enhChild1.commands = ResourceUtils.getStringArray(R.array.saturation);
            enhChild1.visibleSize = 3;
            enhChildItems.add(enhChild1);
            FPScrollItem enhChild2 = new FPScrollItem();
            enhChild2.keyId = CameraSetConstaint.cell_photo_duibidu_key;
            enhChild2.itemTitle = ResourceUtils.getString(R.string.contrast);
            enhChild2.values = ResourceUtils.getStringArray(R.array.contrast);
            enhChild2.commands = ResourceUtils.getStringArray(R.array.contrast);
            enhChild2.visibleSize = 3;
            enhChildItems.add(enhChild2);
            photoEnhItem.setSubItems(enhChildItems);

            // raw格式
            CameraSetItem.FPSwitchCellItem rawItem = new CameraSetItem.FPSwitchCellItem();
            rawItem.keyId = CameraSetConstaint.cell_photo_raw_key;
            rawItem.leftTitle = ResourceUtils.getString(R.string.camra_set_photo_raw);

            _photoOptionItems = new SparseArray<>();
            _photoOptionItems.put(CameraSetConstaint.cell_photo_mode_key, photoModeItem);
            _photoOptionItems.put(CameraSetConstaint.cell_photo_Atio_key, photoAtioItem);
            _photoOptionItems.put(CameraSetConstaint.cell_photo_ruidu_key, ruiduItem);
            _photoOptionItems.put(CameraSetConstaint.cell_photo_advanced_key, photoEnhItem);
            _photoOptionItems.put(CameraSetConstaint.cell_photo_baohedu_key, enhChild1);
            _photoOptionItems.put(CameraSetConstaint.cell_photo_duibidu_key, enhChild2);
            _photoOptionItems.put(CameraSetConstaint.cell_photo_raw_key, rawItem);

        }

        // 拍照模式
        CameraSetItem.FPTwoButtonCellItem photoModeItem = (CameraSetItem.FPTwoButtonCellItem) _photoOptionItems.get(CameraSetConstaint.cell_photo_mode_key);
        photoModeItem.selectButtonIndex = photoMethod;

        // 照片尺寸
        CameraSetItem.FPTwoButtonCellItem photoAtioItem = (CameraSetItem.FPTwoButtonCellItem) _photoOptionItems.get(CameraSetConstaint.cell_photo_Atio_key);
        photoAtioItem.selectButtonIndex = photoAtio;
        photoAtioItem.curCommand = photoAtioItem.commands[photoAtio];

        // 锐度
        CameraSetItem.FPTwoButtonCellItem ruiduItem = (CameraSetItem.FPTwoButtonCellItem) _photoOptionItems.get(CameraSetConstaint.cell_photo_ruidu_key);
        ruiduItem.selectButtonIndex = photoRuidu;
        ruiduItem.curCommand = photoAtioItem.commands[photoRuidu];

        // 效果增强
        FPAccessryBaseCellItem photoEhnEffectItem = (FPAccessryBaseCellItem) _photoOptionItems.get(CameraSetConstaint.cell_photo_advanced_key);
        FPScrollItem enhChild1 = (FPScrollItem)photoEhnEffectItem.getSubItem(0);
        enhChild1.curIndex = photosaturation;   // 饱和度
        FPScrollItem enhChild2 = (FPScrollItem)photoEhnEffectItem.getSubItem(1);
        enhChild2.curIndex = photocontrast;     // 对比度
        photoEhnEffectItem.setExpanded(false);

        // Raw格式
        CameraSetItem.FPSwitchCellItem rawItem = (CameraSetItem.FPSwitchCellItem) _photoOptionItems.get(CameraSetConstaint.cell_photo_raw_key);
        rawItem.switchOn = photoRaw;

        ArrayList<MultiItemEntity> returnItems = new ArrayList<>();
        returnItems.add(photoModeItem);
        returnItems.add(photoAtioItem);
        returnItems.add(ruiduItem);
        returnItems.add(photoEhnEffectItem);
        returnItems.add(rawItem);

        return returnItems;
    }

    @Override
    public ArrayList<MultiItemEntity> photoEffectItems() {
        if (_photoEffectItems == null) {
            //ISO和快门 曝光
            CameraSetItem.FPMiddleTwoButtonCellItem isoShutterItem = new CameraSetItem.FPMiddleTwoButtonCellItem();
            isoShutterItem.keyId = CameraSetConstaint.cell_photo_isoShutterAuto_key;
            isoShutterItem.titles = ResourceUtils.getStringArray(R.array.iosshutter_auto_manual);
            isoShutterItem.normalMipmap = R.mipmap.btn_normal;
            isoShutterItem.selectMipmap = R.mipmap.btn_select;
            isoShutterItem.commands = ResourceUtils.getStringArray(R.array.iosshutter_auto_manual_method);

            // 曝光
            FPScrollItem baoguangItem = new FPScrollItem();
            baoguangItem.keyId = CameraSetConstaint.cell_photo_baoguang_key;
            baoguangItem.itemTitle = ResourceUtils.getString(R.string.exposure);
            baoguangItem.values = ResourceUtils.getStringArray(R.array.baoguang_values);
            baoguangItem.visibleSize = 3;
            baoguangItem.commands = ResourceUtils.getStringArray(R.array.baoguang_values);

            // iso
            FPScrollItem isoItem = new FPScrollItem();
            isoItem.keyId = CameraSetConstaint.cell_photo_iso_key;
            isoItem.itemTitle = "ISO";
            isoItem.values = ResourceUtils.getStringArray(R.array.PHOTO_ISO_VALUES);
            isoItem.visibleSize = 3;
            isoItem.commands = ResourceUtils.getStringArray(R.array.PHOTO_ISO_VALUES);

            // shutter
            FPScrollItem shutterItem = new FPScrollItem();
            shutterItem.keyId = CameraSetConstaint.cell_photo_shutter_key;
            shutterItem.itemTitle = ResourceUtils.getString(R.string.the_shutter);
            shutterItem.values = ResourceUtils.getStringArray(R.array._PHOTO_SHUTTER_6K);
            shutterItem.visibleSize = 3;
            shutterItem.curCommand = "1/40s";
            shutterItem.commands = ResourceUtils.getStringArray(R.array._PHOTO_SHUTTER_6K);

            _photoEffectItems = new SparseArray<>();
            _photoEffectItems.put(CameraSetConstaint.cell_photo_isoShutterAuto_key, isoShutterItem);
            _photoEffectItems.put(CameraSetConstaint.cell_photo_baoguang_key, baoguangItem);
            _photoEffectItems.put(CameraSetConstaint.cell_photo_iso_key, isoItem);
            _photoEffectItems.put(CameraSetConstaint.cell_photo_shutter_key, shutterItem);
        }

        ArrayList<MultiItemEntity> returnItems = new ArrayList<>();

        // iso自动手动
        CameraSetItem.FPMiddleTwoButtonCellItem isoShutterItem = (CameraSetItem.FPMiddleTwoButtonCellItem) _photoEffectItems.get(CameraSetConstaint.cell_photo_isoShutterAuto_key);
        isoShutterItem.selectButtonIndex = photoISOAuto?0:1;
        returnItems.add(isoShutterItem);
        CameraSetItem.FPScrollItem baogItem = (CameraSetItem.FPScrollItem) _photoEffectItems.get(CameraSetConstaint.cell_photo_baoguang_key);
        baogItem.curIndex = photoEv;
        CameraSetItem.FPScrollItem isoItem = (CameraSetItem.FPScrollItem) _photoEffectItems.get(CameraSetConstaint.cell_photo_iso_key);
        isoItem.curIndex = photoISO;
        CameraSetItem.FPScrollItem shutItem = (CameraSetItem.FPScrollItem) _photoEffectItems.get(CameraSetConstaint.cell_photo_shutter_key);
        shutItem.curIndex = photoShutter;
        if (photoISOAuto) {
            returnItems.add(baogItem);
        } else {
            returnItems.add(isoItem);
            returnItems.add(shutItem);
        }

        return returnItems;
    }

    @Override
    public ArrayList<MultiItemEntity> videoOptionItems() {
        if (_videoOptionItems == null) {
            // 高清录制
            CameraSetItem.FPTwoButtonCellItem hdItem = new CameraSetItem.FPTwoButtonCellItem();
            hdItem.keyId = CameraSetConstaint.cell_video_record_hd_key;
            hdItem.leftTitle = ResourceUtils.getString(R.string.camra_set_record_hd);
            hdItem.titles = ResourceUtils.getStringArray(R.array.video_record_method);
            hdItem.normalMipmap = R.mipmap.btn_normal;
            hdItem.selectMipmap = R.mipmap.btn_select;
            hdItem.commands = ResourceUtils.getStringArray(R.array.record_encode_type);

            // 变焦控制条
            CameraSetItem.FPTwoButtonCellItem zoomItem = new CameraSetItem.FPTwoButtonCellItem();
            zoomItem.keyId = CameraSetConstaint.cell_zoom_switch_key;
            zoomItem.leftTitle = ResourceUtils.getString(R.string.camra_set_zoomslider);
            zoomItem.titles = ResourceUtils.getStringArray(R.array.zoom_speed);
            zoomItem.normalMipmap = R.mipmap.btn_normal;
            zoomItem.selectMipmap = R.mipmap.btn_select;

            // 视频尺寸
            FPAccessryBaseCellItem<FPDimenssionItem> dimensionItem = new FPAccessryBaseCellItem();
            dimensionItem.leftTitle = ResourceUtils.getString(R.string.camra_set_video_atio);

            FPDimenssionItem item6k = new FPDimenssionItem();
            item6k.keyId = CameraSetConstaint.cell_video_Dimesion_6k_key;
            item6k.leftTitle = "6K";
            item6k.valueTitle = "(5472x3072)";
            item6k.dimension = "5472x3072";
            item6k.checked = true;
            item6k.defaultfps = CameraSetConstaint.Video_fps_30;
            item6k.currentfps = CameraSetConstaint.Video_fps_30;
            item6k.curCommand = "5472x3072 30P 16:9 AH";
            item6k.setExpanded(true);

            List<FPButtonsItem> fpsItemsFor6K = new ArrayList<>();
            FPButtonsItem fps6k = new FPButtonsItem();
            fps6k.keyId = CameraSetConstaint.cell_video_Dimesion_fps_key;
            String fpsFor6k[] = {CameraSetConstaint.Video_fps_24,CameraSetConstaint.Video_fps_30};
            String cmdFor6k[] = {"5472x3072 24P 16:9 AH","5472x3072 30P 16:9 AH"};
            fps6k.buttonTitles = fpsFor6k;
            fps6k.commands = cmdFor6k;
            fps6k.normalColor = R.color.color_ffffff;
            fps6k.selectColor = R.color.color_dimension_checked;
            fpsItemsFor6K.add(fps6k);
            item6k.setSubItems(fpsItemsFor6K);

            FPDimenssionItem itemC4k = new FPDimenssionItem();
            itemC4k.keyId = CameraSetConstaint.cell_video_Dimesion_C4k_key;
            itemC4k.leftTitle = "4K";
            itemC4k.valueTitle = "(4096x2160)";
            itemC4k.dimension = "4096x2160";
            itemC4k.checked = false;
            itemC4k.defaultfps = CameraSetConstaint.Video_fps_30;
            itemC4k.currentfps = CameraSetConstaint.Video_fps_30;
            itemC4k.curCommand = "4096x2160 30P 17:9 AH";
            List<FPButtonsItem> fpsItemsForC4k = new ArrayList<>();
            FPButtonsItem fpsC4k = new FPButtonsItem();
            fpsC4k.keyId = CameraSetConstaint.cell_video_Dimesion_fps_key;
            String fpsForfpsC4k[] = {CameraSetConstaint.Video_fps_24,CameraSetConstaint.Video_fps_30,CameraSetConstaint.Video_fps_60};
            String cmdForC4k[] = {"4096x2160 24P 17:9 AH","4096x2160 30P 17:9 AH","4096x2160 60P 17:9 AH"};
            fpsC4k.buttonTitles = fpsForfpsC4k;
            fpsC4k.commands = cmdForC4k;
            fpsC4k.normalColor = R.color.color_ffffff;
            fpsC4k.selectColor = R.color.color_dimension_checked;
            fpsItemsForC4k.add(fpsC4k);
            itemC4k.setSubItems(fpsItemsForC4k);

            FPDimenssionItem item2dot7k = new FPDimenssionItem();
            item2dot7k.keyId = CameraSetConstaint.cell_video_Dimesion_2dot7k_key;
            item2dot7k.leftTitle = "2.7K";
            item2dot7k.valueTitle = "(2720x1530)";
            item2dot7k.dimension = "2720x1530";
            item2dot7k.checked = false;
            item2dot7k.defaultfps = CameraSetConstaint.Video_fps_30;
            item2dot7k.currentfps = CameraSetConstaint.Video_fps_30;
            item2dot7k.curCommand = "2720x1530 30P 16:9 AH";
            List<FPButtonsItem> fpsItemsFor2dot7k = new ArrayList<>();
            FPButtonsItem fps2dot7k = new FPButtonsItem();
            fps2dot7k.keyId = CameraSetConstaint.cell_video_Dimesion_fps_key;
            String fpsFor2dot7k[] = {CameraSetConstaint.Video_fps_24,CameraSetConstaint.Video_fps_30,CameraSetConstaint.Video_fps_60};
            String cmdFor2dot7k[] = {"2720x1530 24P 16:9 AH","2720x1530 30P 16:9 AH","2720x1530 60P 16:9 AH"};
            fps2dot7k.buttonTitles = fpsFor2dot7k;
            fps2dot7k.commands = cmdFor2dot7k;
            fps2dot7k.normalColor = R.color.color_ffffff;
            fps2dot7k.selectColor = R.color.color_dimension_checked;
            fpsItemsFor2dot7k.add(fps2dot7k);
            item2dot7k.setSubItems(fpsItemsFor2dot7k);

            FPDimenssionItem item1080P = new FPDimenssionItem();
            item1080P.keyId = CameraSetConstaint.cell_video_Dimesion_1080P_key;
            item1080P.leftTitle = "1080P";
            item1080P.valueTitle = "(1920x1080)";
            item1080P.dimension = "1920x1080";
            item1080P.checked = false;
            item1080P.defaultfps = CameraSetConstaint.Video_fps_30;
            item1080P.currentfps = CameraSetConstaint.Video_fps_30;
            item1080P.curCommand = "1920x1080 30P 16:9 AH";
            List<FPButtonsItem> fpsItemsFor1080P = new ArrayList<>();
            FPButtonsItem fps1080P = new FPButtonsItem();
            fps1080P.keyId = CameraSetConstaint.cell_video_Dimesion_fps_key;
            String fpsFor1080P[] = {CameraSetConstaint.Video_fps_24,CameraSetConstaint.Video_fps_30,CameraSetConstaint.Video_fps_60,CameraSetConstaint.Video_fps_120};
            String cmdFor1080P[] = {"1920x1080 24P 16:9 AH","1920x1080 30P 16:9 AH","1920x1080 60P 16:9 AH","1920x1080 120P 16:9 AH"};
            fps1080P.buttonTitles = fpsFor1080P;
            fps1080P.commands = cmdFor1080P;
            fps1080P.normalColor = R.color.color_ffffff;
            fps1080P.selectColor = R.color.color_dimension_checked;
            fpsItemsFor1080P.add(fps1080P);
            item1080P.setSubItems(fpsItemsFor1080P);

            FPDimenssionItem item4k = new FPDimenssionItem();
            item4k.keyId = CameraSetConstaint.cell_video_Dimesion_1080P_key;
            item4k.leftTitle = "4K HQ";
            item4k.valueTitle = "(3840x2160)";
            item4k.dimension = "3840x2160";
            item4k.checked = false;
            item4k.defaultfps = CameraSetConstaint.Video_fps_30;
            item4k.currentfps = CameraSetConstaint.Video_fps_30;
            item4k.curCommand = "3840x2160 30P 16:9 AH";
            List<FPButtonsItem> fpsItemsFor4k = new ArrayList<>();
            FPButtonsItem fps4k = new FPButtonsItem();
            fps4k.keyId = CameraSetConstaint.cell_video_Dimesion_fps_key;
            String fpsFor4k[] = {CameraSetConstaint.Video_fps_24,CameraSetConstaint.Video_fps_30,CameraSetConstaint.Video_fps_60};
            String cmdFor4k[] = {"3840x2160 24P 16:9 AH","3840x2160 30P 16:9 AH","3840x2160 60P 16:9 AH"};
            fps4k.buttonTitles = fpsFor4k;
            fps4k.commands = cmdFor4k;
            fps4k.normalColor = R.color.color_ffffff;
            fps4k.selectColor = R.color.color_dimension_checked;
            fpsItemsFor4k.add(fps4k);
            item4k.setSubItems(fpsItemsFor4k);

            FPDimenssionItem item1080P_22 = new FPDimenssionItem();
            item1080P_22.keyId = CameraSetConstaint.cell_video_Dimesion_1080x1920_key;
            item1080P_22.leftTitle = "vertical";
            item1080P_22.valueTitle = "(1080x1920)";
            item1080P_22.dimension = "1080x1920";
            item1080P_22.checked = false;
            item1080P_22.defaultfps = CameraSetConstaint.Video_fps_30;
            item1080P_22.currentfps = CameraSetConstaint.Video_fps_30;
            item1080P_22.curCommand = "1080x1920 30P 9:16 AH";
            List<FPButtonsItem> fpsItemsFor1080P_22 = new ArrayList<>();
            FPButtonsItem fps1080P_22 = new FPButtonsItem();
            fps1080P_22.keyId = CameraSetConstaint.cell_video_Dimesion_fps_key;
            String fpsFor1080P_22[] = {CameraSetConstaint.Video_fps_30};
            String cmdFor1080P_22[] = {"1080x1920 30P 9:16 HH"};
            fps1080P_22.buttonTitles = fpsFor1080P_22;
            fps1080P_22.commands = cmdFor1080P_22;
            fps1080P_22.normalColor = R.color.color_ffffff;
            fps1080P_22.selectColor = R.color.color_dimension_checked;
            fpsItemsFor1080P_22.add(fps1080P_22);
            item1080P_22.setSubItems(fpsItemsFor1080P_22);

            // 效果增强
            FPAccessryBaseCellItem<FPScrollItem> videoEnhItem = new FPAccessryBaseCellItem();
            videoEnhItem.leftTitle = ResourceUtils.getString(R.string.enhancement_effect);
            List<FPScrollItem> enhChildItems = new ArrayList<>();
            FPScrollItem enhChild1 = new FPScrollItem();
            enhChild1.keyId = CameraSetConstaint.cell_video_baohedu_key;
            enhChild1.itemTitle = ResourceUtils.getString(R.string.saturation);
            enhChild1.values = ResourceUtils.getStringArray(R.array.saturation);
            enhChild1.commands = ResourceUtils.getStringArray(R.array.saturation);
            enhChild1.visibleSize = 3;
            enhChildItems.add(enhChild1);
            FPScrollItem enhChild2 = new FPScrollItem();
            enhChild2.keyId = CameraSetConstaint.cell_video_duibidu_key;
            enhChild2.itemTitle = ResourceUtils.getString(R.string.contrast);
            enhChild2.values = ResourceUtils.getStringArray(R.array.contrast);
            enhChild2.commands = ResourceUtils.getStringArray(R.array.contrast);
            enhChild2.visibleSize = 3;
            enhChildItems.add(enhChild2);
            videoEnhItem.setSubItems(enhChildItems);

            _videoOptionItems = new SparseArray<>();
            _videoOptionItems.put(CameraSetConstaint.cell_video_record_hd_key, hdItem);
            _videoOptionItems.put(CameraSetConstaint.cell_zoom_switch_key, zoomItem);
            _videoOptionItems.put(CameraSetConstaint.cell_video_Dimesion_key, dimensionItem);
            _videoOptionItems.put(CameraSetConstaint.cell_video_Dimesion_6k_key, item6k);
            _videoOptionItems.put(CameraSetConstaint.cell_video_Dimesion_C4k_key, itemC4k);
            _videoOptionItems.put(CameraSetConstaint.cell_video_Dimesion_2dot7k_key, item2dot7k);
            _videoOptionItems.put(CameraSetConstaint.cell_video_Dimesion_1080P_key, item1080P);
            _videoOptionItems.put(CameraSetConstaint.cell_video_Dimesion_4k_key, item4k);
            _videoOptionItems.put(CameraSetConstaint.cell_video_Dimesion_1080x1920_key, item1080P_22);
            _videoOptionItems.put(CameraSetConstaint.cell_video_advanced_key, videoEnhItem);
            _videoOptionItems.put(CameraSetConstaint.cell_video_baohedu_key, enhChild1);
            _videoOptionItems.put(CameraSetConstaint.cell_video_duibidu_key, enhChild2);
        }

        // 高清录制
        CameraSetItem.FPTwoButtonCellItem hdItem = (CameraSetItem.FPTwoButtonCellItem) _videoOptionItems.get(CameraSetConstaint.cell_video_record_hd_key);
        hdItem.selectButtonIndex = recordType;

        // 变焦控制条
        FPTwoButtonCellItem zoomItem = (CameraSetItem.FPTwoButtonCellItem) _videoOptionItems.get(CameraSetConstaint.cell_zoom_switch_key);
        zoomItem.selectButtonIndex = zoomIndex;

        // 视频尺寸
        FPAccessryBaseCellItem videoDimension = (FPAccessryBaseCellItem) _videoOptionItems.get(CameraSetConstaint.cell_video_Dimesion_key);
        List<FPDimenssionItem> dimensionChildItems = new ArrayList<>();
        if (recordType == CameraSetConstaint.FPEncodeTypeH264) {
            dimensionChildItems.add((FPDimenssionItem)_videoOptionItems.get(CameraSetConstaint.cell_video_Dimesion_6k_key));
            dimensionChildItems.add((FPDimenssionItem)_videoOptionItems.get(CameraSetConstaint.cell_video_Dimesion_C4k_key));
            dimensionChildItems.add((FPDimenssionItem)_videoOptionItems.get(CameraSetConstaint.cell_video_Dimesion_4k_key));
            dimensionChildItems.add((FPDimenssionItem)_videoOptionItems.get(CameraSetConstaint.cell_video_Dimesion_2dot7k_key));
            dimensionChildItems.add((FPDimenssionItem)_videoOptionItems.get(CameraSetConstaint.cell_video_Dimesion_1080P_key));
            dimensionChildItems.add((FPDimenssionItem)_videoOptionItems.get(CameraSetConstaint.cell_video_Dimesion_1080x1920_key));
        } else {
            dimensionChildItems.add((FPDimenssionItem)_videoOptionItems.get(CameraSetConstaint.cell_video_Dimesion_4k_key));
            dimensionChildItems.add((FPDimenssionItem)_videoOptionItems.get(CameraSetConstaint.cell_video_Dimesion_2dot7k_key));
            dimensionChildItems.add((FPDimenssionItem)_videoOptionItems.get(CameraSetConstaint.cell_video_Dimesion_1080P_key));
            dimensionChildItems.add((FPDimenssionItem)_videoOptionItems.get(CameraSetConstaint.cell_video_Dimesion_1080x1920_key));
        }
        videoDimension.setSubItems(dimensionChildItems);
        videoDimension.setExpanded(false);

        List<FPDimenssionItem> items = videoDimension.getSubItems();
        for (int i = 0; i < items.size(); i++) {
            FPDimenssionItem item = (FPDimenssionItem)videoDimension.getSubItem(i);
            if (updatevideoDimenssion.contains(item.dimension)) {
                item.setExpanded(true);
                item.checked = true;
                videoDimension.rightTitle = item.leftTitle+" "+item.currentfps;
            } else {
                item.setExpanded(false);
                item.checked = false;
            }

            item.defaultfps = CameraSetConstaint.Video_fps_30;
            if (recordType == CameraSetConstaint.FPEncodeTypeH264) {
                item.curCommand = item.curCommand.replace("HH","AH");
            } else {
                item.curCommand = item.curCommand.replace("AH","HH");
            }

            FPButtonsItem fpsItem = item.getSubItem(0);
            if (fpsItem.keyId == CameraSetConstaint.cell_video_Dimesion_C4k_key) {  // C4k h265和h264 fps不一样
                if (recordType == CameraSetConstaint.FPEncodeTypeH264) {
                    String fpsForfpsC4kH264[] = {CameraSetConstaint.Video_fps_24,CameraSetConstaint.Video_fps_30,CameraSetConstaint.Video_fps_60};
                    String cmdForfpsC4kH264[] = {"4096x2160 24P 17:9 AH","4096x2160 30P 17:9 AH","4096x2160 60P 17:9 AH"};
                    fpsItem.buttonTitles = fpsForfpsC4kH264;
                    fpsItem.commands = cmdForfpsC4kH264;
                } else {
                    String fpsForfpsC4kH265[] = {CameraSetConstaint.Video_fps_24,CameraSetConstaint.Video_fps_30};
                    String cmdForfpsC4kH265[] = {"4096x2160 24P 17:9 HH","4096x2160 30P 17:9 HH"};
                    fpsItem.buttonTitles = fpsForfpsC4kH265;
                    fpsItem.commands = cmdForfpsC4kH265;
                }
            }

            if (fpsItem.keyId == CameraSetConstaint.cell_video_Dimesion_1080P_key) {  // C4k h265和h264 fps不一样
                if (recordType == CameraSetConstaint.FPEncodeTypeH264) {
                    String fpsFor1080PH264[] = {CameraSetConstaint.Video_fps_24,CameraSetConstaint.Video_fps_30,CameraSetConstaint.Video_fps_60,CameraSetConstaint.Video_fps_120};
                    String cmdForfpsC4kH264[] = {"1920x1080 24P 16:9 AH","1920x1080 30P 16:9 AH","1920x1080 60P 16:9 AH","1920x1080 120P 16:9 AH"};
                    fpsItem.buttonTitles = fpsFor1080PH264;
                    fpsItem.commands = cmdForfpsC4kH264;
                } else {
                    String fpsFor1080PH265[] = {CameraSetConstaint.Video_fps_24,CameraSetConstaint.Video_fps_30,CameraSetConstaint.Video_fps_60};
                    String cmdForfpsC4kH265[] = {"1920x1080 24P 16:9 HH","1920x1080 30P 16:9 HH","1920x1080 60P 16:9 HH"};
                    fpsItem.buttonTitles = fpsFor1080PH265;
                    fpsItem.commands = cmdForfpsC4kH265;
                }
            }

            // 进行AH和HH的替换
            for (int j = 0; j < fpsItem.commands.length; j++) {
                String comd = fpsItem.commands[j];
                if (recordType == CameraSetConstaint.FPEncodeTypeH264) {
                    comd.replace("HH","AH");
                } else {
                    comd.replace("AH","HH");
                }
            }

            // 当前选中项目
            for (int j = 0; j < fpsItem.buttonTitles.length; j++) {
                if (fpsItem.buttonTitles[j].equals(item.currentfps)) {
                    fpsItem.selectIndex = j;
                }
            }
        }

        // 视频 效果增强
        FPAccessryBaseCellItem videoEnhItem = (FPAccessryBaseCellItem) _videoOptionItems.get(CameraSetConstaint.cell_video_advanced_key);
        FPScrollItem enhChild1 = (FPScrollItem)videoEnhItem.getSubItem(0);
        enhChild1.curIndex = videosaturation; // 饱和度
        FPScrollItem enhChild2 = (FPScrollItem)videoEnhItem.getSubItem(1);
        enhChild2.curIndex = videocontrast; // 对比度
        videoEnhItem.setExpanded(false);

        ArrayList<MultiItemEntity> returnItems = new ArrayList<>();
        returnItems.add(hdItem);
        returnItems.add(zoomItem);
        returnItems.add(videoDimension);
        returnItems.add(videoEnhItem);

        return returnItems;
    }

    @Override
    public ArrayList<MultiItemEntity> videoEffectItems() {
        if (_videoEffectItems == null) {
            //ISO和快门 曝光
            CameraSetItem.FPMiddleTwoButtonCellItem isoShutterItem = new CameraSetItem.FPMiddleTwoButtonCellItem();
            isoShutterItem.keyId = CameraSetConstaint.cell_video_isoShutterAuto_key;
            isoShutterItem.titles = ResourceUtils.getStringArray(R.array.iosshutter_auto_manual);
            isoShutterItem.normalMipmap = R.mipmap.btn_normal;
            isoShutterItem.selectMipmap = R.mipmap.btn_select;
            isoShutterItem.commands = ResourceUtils.getStringArray(R.array.iosshutter_auto_manual_method);

            // 曝光
            FPScrollItem baoguangItem = new FPScrollItem();
            baoguangItem.keyId = CameraSetConstaint.cell_video_baoguang_key;
            baoguangItem.itemTitle = ResourceUtils.getString(R.string.exposure);
            baoguangItem.values = ResourceUtils.getStringArray(R.array.baoguang_values);
            baoguangItem.visibleSize = 3;
            baoguangItem.commands = ResourceUtils.getStringArray(R.array.baoguang_values);

            // iso
            FPScrollItem isoItem = new FPScrollItem();
            isoItem.keyId = CameraSetConstaint.cell_video_iso_key;
            isoItem.itemTitle = "ISO";
            isoItem.values = ResourceUtils.getStringArray(R.array.VIDEO_ISO_VALUES);
            isoItem.visibleSize = 3;
            isoItem.commands = ResourceUtils.getStringArray(R.array.VIDEO_ISO_VALUES);

            // shutter
            FPScrollItem shutterItem = new FPScrollItem();
            shutterItem.keyId = CameraSetConstaint.cell_video_shutter_key;
            shutterItem.itemTitle = ResourceUtils.getString(R.string.the_shutter);
            shutterItem.values = ResourceUtils.getStringArray(R.array._30VIDEO_SHUTTER);
            shutterItem.commands = ResourceUtils.getStringArray(R.array._30VIDEO_SHUTTER);
            shutterItem.visibleSize = 3;
            shutterItem.curCommand = "1/40s";

            _videoEffectItems = new SparseArray<>();
            _videoEffectItems.put(CameraSetConstaint.cell_video_isoShutterAuto_key, isoShutterItem);
            _videoEffectItems.put(CameraSetConstaint.cell_video_baoguang_key, baoguangItem);
            _videoEffectItems.put(CameraSetConstaint.cell_video_iso_key, isoItem);
            _videoEffectItems.put(CameraSetConstaint.cell_video_shutter_key, shutterItem);
        }

        ArrayList<MultiItemEntity> returnItems = new ArrayList<>();

        // iso 自动手动
        CameraSetItem.FPMiddleTwoButtonCellItem isoShutterItem = (CameraSetItem.FPMiddleTwoButtonCellItem) _videoEffectItems.get(CameraSetConstaint.cell_video_isoShutterAuto_key);
        isoShutterItem.selectButtonIndex = videoISOAuto?0:1;
        returnItems.add(isoShutterItem);
        CameraSetItem.FPScrollItem baogItem = (CameraSetItem.FPScrollItem) _videoEffectItems.get(CameraSetConstaint.cell_video_baoguang_key);
        baogItem.curIndex = videoEv;
        CameraSetItem.FPScrollItem isoItem = (CameraSetItem.FPScrollItem) _videoEffectItems.get(CameraSetConstaint.cell_video_iso_key);
        isoItem.curIndex = videoISO;
        CameraSetItem.FPScrollItem shutItem = (CameraSetItem.FPScrollItem) _videoEffectItems.get(CameraSetConstaint.cell_video_shutter_key);
        CameraSetItem.FPDimenssionItem ditem = currentVideoDimenSion();
        if (ditem.currentfps.equals(CameraSetConstaint.Video_fps_30)) {   //30fps
            shutItem.values = ResourceUtils.getStringArray(R.array._30VIDEO_SHUTTER);
            shutItem.commands = ResourceUtils.getStringArray(R.array._30VIDEO_SHUTTER);
        } else if (ditem.currentfps.equals(CameraSetConstaint.Video_fps_60)) {
            shutItem.values = ResourceUtils.getStringArray(R.array._60VIDEO_SHUTTER);
            shutItem.commands = ResourceUtils.getStringArray(R.array._60VIDEO_SHUTTER);
        } else if (ditem.currentfps.equals(CameraSetConstaint.Video_fps_120)) {
            shutItem.values = ResourceUtils.getStringArray(R.array._120VIDEO_SHUTTER);
            shutItem.commands = ResourceUtils.getStringArray(R.array._120VIDEO_SHUTTER);
        } else if (ditem.currentfps.equals(CameraSetConstaint.Video_fps_24)) {
            shutItem.values = ResourceUtils.getStringArray(R.array._24VIDEO_SHUTTER);
            shutItem.commands = ResourceUtils.getStringArray(R.array._24VIDEO_SHUTTER);
        }
        if (videoShutter > shutItem.commands.length) {
            videoShutter = shutItem.commands.length - 1;
        }
        shutItem.curIndex = videoShutter;
        if (videoISOAuto) {
            returnItems.add(baogItem);
        } else {
            returnItems.add(isoItem);
            returnItems.add(shutItem);
        }

        return returnItems;
    }

    @Override
    public ArrayList<MultiItemEntity> generaSettingItems() {

        if (_generaSettingItems == null) {
            // 前臂灯
            CameraSetItem.FPSwitchCellItem qbdSwitchItem = new CameraSetItem.FPSwitchCellItem();
            qbdSwitchItem.keyId = CameraSetConstaint.cell_qianbideng_key;
            qbdSwitchItem.cellType = CameraSetItem.CameraSetItemType_Switch;
            qbdSwitchItem.leftTitle = ResourceUtils.getString(R.string.camra_set_qianbideng);
            qbdSwitchItem.switchOn = false;

            // 后壁灯
            CameraSetItem.FPSwitchCellItem hbdSwitchItem = new CameraSetItem.FPSwitchCellItem();
            hbdSwitchItem.keyId = CameraSetConstaint.cell_houbideng_key;
            hbdSwitchItem.cellType = CameraSetItem.CameraSetItemType_Switch;
            hbdSwitchItem.leftTitle = ResourceUtils.getString(R.string.camra_set_houbideng);
            hbdSwitchItem.switchOn = false;

            // 云台水平微调
            CameraSetItem.FPSwitchCellItem ythoriSwitchItem = new CameraSetItem.FPSwitchCellItem();
            ythoriSwitchItem.keyId = CameraSetConstaint.cell_yt_hori_set_key;
            ythoriSwitchItem.cellType = CameraSetItem.CameraSetItemType_Switch;
            ythoriSwitchItem.leftTitle = ResourceUtils.getString(R.string.camra_set_yt_hori_set);
            ythoriSwitchItem.switchOn = false;

            // 抗闪烁
            int normalMipmaps[] = {R.mipmap.btn_normal,R.mipmap.btn_normal,R.mipmap.btn_normal};
            int selectMipmaps[] = {R.mipmap.btn_select,R.mipmap.btn_select,R.mipmap.btn_select};
            CameraSetItem.FPThreeButtonItem antiflickerItem = new CameraSetItem.FPThreeButtonItem();
            antiflickerItem.keyId = CameraSetConstaint.cell_genera_antiflicker_key;
            antiflickerItem.cellType = CameraSetItem.CameraSetItemType_ThreeButton;
            antiflickerItem.leftTitle = ResourceUtils.getString(R.string.camra_set_antikey);
            antiflickerItem.buttontitles = ResourceUtils.getStringArray(R.array.camera_set_anti_values);
            antiflickerItem.normalMipmaps = normalMipmaps;
            antiflickerItem.selectMipmaps = selectMipmaps;
            antiflickerItem.commands = ResourceUtils.getStringArray(R.array.camera_set_anti_comds);

            // 网格线
            int gridnormalMipmaps[] = {R.mipmap.btn_normal,R.mipmap.camera_set_grid_jg_nor,R.mipmap.camera_set_grid_dj_nor};
            int gridselectMipmaps[] = {R.mipmap.btn_select,R.mipmap.camera_set_grid_jg_sel,R.mipmap.camera_set_grid_dj_sel};
            CameraSetItem.FPThreeButtonItem gridItem = new CameraSetItem.FPThreeButtonItem();
            gridItem.keyId = CameraSetConstaint.cell_genera_grid_key;
            gridItem.cellType = CameraSetItem.CameraSetItemType_ThreeButton;
            gridItem.leftTitle = ResourceUtils.getString(R.string.camra_set_grid);
            gridItem.buttontitles = ResourceUtils.getStringArray(R.array.camera_set_grid_values);
            gridItem.normalMipmaps = gridnormalMipmaps;
            gridItem.selectMipmaps = gridselectMipmaps;

            // sd卡容量
            CameraSetItem.FPTitleValueItem sdItem = new CameraSetItem.FPTitleValueItem();
            sdItem.keyId = CameraSetConstaint.cell_genera_sdcapcity_key;
            sdItem.cellType = CameraSetItem.CameraSetItemType_Title_value;
            sdItem.leftTitle = ResourceUtils.getString(R.string.camra_set_sd_cap);
            sdItem.rightTitle = "--/--";


            // 重置相机参数
            CameraSetItem.FPTitleValueItem resetItem = new CameraSetItem.FPTitleValueItem();
            resetItem.keyId = CameraSetConstaint.cell_genera_reset_key;
            resetItem.cellType = CameraSetItem.CameraSetItemType_Title_value;
            resetItem.leftTitle = ResourceUtils.getString(R.string.camra_set_sd_reset);
            resetItem.rightTitle = "";

            // 格式化sd卡
            CameraSetItem.FPTitleValueItem formatItem = new CameraSetItem.FPTitleValueItem();
            formatItem.keyId = CameraSetConstaint.cell_genera_format_key;
            formatItem.cellType = CameraSetItem.CameraSetItemType_Title_value;
            formatItem.leftTitle = ResourceUtils.getString(R.string.camra_set_sd_format);
            formatItem.rightTitle = "";

            // 文件转移
            CameraSetItem.FPTitleValueItem moveItem = new CameraSetItem.FPTitleValueItem();
            moveItem.keyId = CameraSetConstaint.cell_genera_move_key;
            moveItem.cellType = CameraSetItem.CameraSetItemType_Title_value;
            moveItem.leftTitle = ResourceUtils.getString(R.string.camra_set_sd_move);
            moveItem.rightTitle = "";

            _generaSettingItems = new SparseArray<>();
            _generaSettingItems.put(CameraSetConstaint.cell_qianbideng_key, qbdSwitchItem);
            _generaSettingItems.put(CameraSetConstaint.cell_houbideng_key, hbdSwitchItem);
            _generaSettingItems.put(CameraSetConstaint.cell_yt_hori_set_key, ythoriSwitchItem);
            _generaSettingItems.put(CameraSetConstaint.cell_genera_antiflicker_key, antiflickerItem);
            _generaSettingItems.put(CameraSetConstaint.cell_genera_grid_key, gridItem);
            _generaSettingItems.put(CameraSetConstaint.cell_genera_sdcapcity_key, sdItem);
            _generaSettingItems.put(CameraSetConstaint.cell_genera_reset_key, resetItem);
            _generaSettingItems.put(CameraSetConstaint.cell_genera_format_key, formatItem);
            _generaSettingItems.put(CameraSetConstaint.cell_genera_move_key, moveItem);
        }

        // 前臂灯
        CameraSetItem.FPSwitchCellItem qbdSwitchItem = (FPSwitchCellItem) _generaSettingItems.get(CameraSetConstaint.cell_qianbideng_key);
        qbdSwitchItem.switchOn = qianbideng;

        // 后壁灯
        CameraSetItem.FPSwitchCellItem hbdSwitchItem = (FPSwitchCellItem) _generaSettingItems.get(CameraSetConstaint.cell_houbideng_key);
        hbdSwitchItem.switchOn = houbideng;

        // 云台水平微调
        CameraSetItem.FPSwitchCellItem ythoriSwitchItem = (FPSwitchCellItem) _generaSettingItems.get(CameraSetConstaint.cell_yt_hori_set_key);
        ythoriSwitchItem.switchOn = ytHoriSet;

        // 抗闪烁
        CameraSetItem.FPThreeButtonItem antiflickerItem = (FPThreeButtonItem) _generaSettingItems.get(CameraSetConstaint.cell_genera_antiflicker_key);
        antiflickerItem.selectButtonIndex = antikeyIndex;
        antiflickerItem.commands = ResourceUtils.getStringArray(R.array.camera_set_anti_comds);

        // 网格线
        CameraSetItem.FPThreeButtonItem gridItem = (FPThreeButtonItem) _generaSettingItems.get(CameraSetConstaint.cell_genera_grid_key);

        gridItem.selectButtonIndex = gridMode;

        // SD卡容量
        CameraSetItem.FPTitleValueItem sdItem = (FPTitleValueItem) _generaSettingItems.get(CameraSetConstaint.cell_genera_sdcapcity_key);
        if (sdcardTotal <= 0) {
            sdItem.rightTitle = "--/--";
        } else {
            sdItem.rightTitle = String.format("%.2fG/%.2fG",sdcardFree/(1024.0f*1024.0f),sdcardTotal/(1024.0f*1024.0f));
        }

        // 重置相机参数
        CameraSetItem.FPTitleValueItem resetItem = (FPTitleValueItem) _generaSettingItems.get(CameraSetConstaint.cell_genera_reset_key);

        // 格式化SD卡
        CameraSetItem.FPTitleValueItem formatItem = (FPTitleValueItem) _generaSettingItems.get(CameraSetConstaint.cell_genera_format_key);

        // 文件转移
        CameraSetItem.FPTitleValueItem moveItem = (FPTitleValueItem) _generaSettingItems.get(CameraSetConstaint.cell_genera_move_key);

        ArrayList<MultiItemEntity> returnItems = new ArrayList<>();
        returnItems.add(qbdSwitchItem);
        returnItems.add(hbdSwitchItem);
        returnItems.add(ythoriSwitchItem);
        returnItems.add(antiflickerItem);
        returnItems.add(gridItem);
        returnItems.add(sdItem);
        returnItems.add(moveItem);
        returnItems.add(formatItem);
        returnItems.add(resetItem);

        return returnItems;
    }

    @Override
    public void updateCameraSettings(SettingBean.ParamBean params) {
        if (params.getVideo_resolution() != null) {
            String value = params.getVideo_resolution();
            if (value.contains("HH")) { // h265
                recordType = CameraSetConstaint.FPEncodeTypeH265;
            } else {
                recordType = CameraSetConstaint.FPEncodeTypeH264;
            }
            updatevideoDimenssion = value;
        }
        if (params.getPhoto_size() != null) {
            String value = params.getPhoto_size();
            String[] values = ResourceUtils.getStringArray(R.array.photoAitios_6k);
            int index = 0;
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(value)) {
                    index = i;
                    break;
                }
            }
            photoAtio = index;
        }
        if (params.getPhoto_raw() != null) {
            String value = params.getPhoto_raw();
            photoRaw = value.equals("on");
        }
        if (params.getPhoto_iso() != null) {
            String value = params.getPhoto_iso();
            if (value.equals("auto")) {
                value = "400";
                photoISOAutoValue = true;
            } else {
                photoISOAutoValue = false;
            }
            String[] values = ResourceUtils.getStringArray(R.array.PHOTO_ISO_VALUES);
            int index = 0;
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(value)) {
                    index = i;
                    break;
                }
            }
            photoISO = index;
        }
        if (params.getVideo_iso() != null) {
            String value = params.getVideo_iso();
            if (value.equals("auto")) {
                value = "400";
                videoISOAutoValue = true;
            } else {
                videoISOAutoValue = false;
            }
            String[] values = ResourceUtils.getStringArray(R.array.VIDEO_ISO_VALUES);
            int index = 0;
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(value)) {
                    index = i;
                    break;
                }
            }
            videoISO = index;
        }
        if (params.getPhoto_ev() != null) {
            String value = params.getPhoto_ev();
            String[] values = ResourceUtils.getStringArray(R.array.baoguang_values);
            int index = 0;
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(value)) {
                    index = i;
                    break;
                }
            }
            photoEv = index;
        }
        if (params.getVideo_ev() != null) {
            String value = params.getVideo_ev();
            String[] values = ResourceUtils.getStringArray(R.array.baoguang_values);
            int index = 0;
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(value)) {
                    index = i;
                    break;
                }
            }
            videoEv = index;
        }
        if (params.getPhoto_shutter() != null) {
            String value = params.getPhoto_shutter();
            if (value.equals("auto")) {
                value = "1/40s";
                photoShutterAutoValue = true;
            } else {
                photoShutterAutoValue = false;
            }
            String[] values = ResourceUtils.getStringArray(R.array._PHOTO_SHUTTER);
            int index = 0;
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(value)) {
                    index = i;
                    break;
                }
            }
            photoShutter = index;
        }
        if (params.getVideo_shutter() != null) {
            String value = params.getVideo_shutter();
            if (value.equals("auto")) {
                value = "1/40s";
                videoShutterAutoValue = true;
            } else {
                videoShutterAutoValue = false;
            }
            // 先用照片的曝光 获取索引
            String[] values = ResourceUtils.getStringArray(R.array._PHOTO_SHUTTER);
            int index = 0;
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(value)) {
                    index = i;
                    break;
                }
            }
            videoShutter = index;
        }
        if (params.getFrequency() != null) {
            String value = params.getFrequency();
            // 先用照片的曝光 获取索引
            String[] values = ResourceUtils.getStringArray(R.array.camera_set_anti_comds);
            int index = 0;
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(value)) {
                    index = i;
                    break;
                }
            }
            antikeyIndex = index;
        }
        if (params.getPhoto_contrast() != null) {
            String value = params.getPhoto_contrast();
            // 先用照片的曝光 获取索引
            String[] values = ResourceUtils.getStringArray(R.array.contrast);
            int index = 0;
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(value)) {
                    index = i;
                    break;
                }
            }
            photocontrast = index;
        }
        if (params.getVideo_contrast() != null) {
            String value = params.getVideo_contrast();
            // 先用照片的曝光 获取索引
            String[] values = ResourceUtils.getStringArray(R.array.contrast);
            int index = 0;
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(value)) {
                    index = i;
                    break;
                }
            }
            videocontrast = index;
        }
        if (params.getPhoto_saturation() != null) {
            String value = params.getPhoto_saturation();
            // 先用照片的曝光 获取索引
            String[] values = ResourceUtils.getStringArray(R.array.saturation);
            int index = 0;
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(value)) {
                    index = i;
                    break;
                }
            }
            photosaturation = index;
        }
        if (params.getVideo_saturation() != null) {
            String value = params.getVideo_saturation();
            // 先用照片的曝光 获取索引
            String[] values = ResourceUtils.getStringArray(R.array.saturation);
            int index = 0;
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(value)) {
                    index = i;
                    break;
                }
            }
            videosaturation = index;
        }
        if (params.getPhoto_sharpness() != null) {
            String value = params.getPhoto_sharpness();
            // 先用照片的曝光 获取索引
            String[] values = ResourceUtils.getStringArray(R.array.photo_ruidu);
            int index = 0;
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(value)) {
                    index = i;
                    break;
                }
            }
            photosharpness = index;
        }
        if (params.getVideo_sharpness() != null) {
            String value = params.getVideo_sharpness();
            // 先用照片的曝光 获取索引
            String[] values = ResourceUtils.getStringArray(R.array.photo_ruidu);
            int index = 0;
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(value)) {
                    index = i;
                    break;
                }
            }
            videosharpness = index;
        }
        if (params.getPhoto_image_mode() != null) {
            String value = params.getPhoto_image_mode();
            if (value.equals("auto")) {
                photoISOAuto = true;
            } else {
                photoISOAuto = false;
            }
        }
        if (params.getVideo_image_mode() != null) {
            String value = params.getVideo_image_mode();
            if (value.equals("auto")) {
                videoISOAuto = true;
            } else {
                videoISOAuto = false;
            }
        }
        if (params.getPhoto_3a_lock() != null) {
            String value = params.getPhoto_3a_lock();
            if (value.equals("on")) {
                photoAELock = true;
            } else {
                photoAELock = false;
            }
        }
        if (params.getVideo_3a_lock() != null) {
            String value = params.getVideo_3a_lock();
            if (value.equals("on")) {
                videoAELock = true;
            } else {
                videoAELock = false;
            }
        }
        if (params.getPhoto_vr_mode() != null) {
            String value = params.getPhoto_vr_mode();
            if (value.equals("on")) {
                photoSubMode = CameraSetConstaint.PhotoModePano;
            }
        }
        if (params.getPhoto_time_lapse_mode() != null) {
            String value = params.getPhoto_time_lapse_mode();
            if (value.equals("on")) {
                photoSubMode = CameraSetConstaint.PhotoModeDelay;
            }
        }
        if (params.getVideo_10bit_mode() != null) {
            String value = params.getVideo_10bit_mode();
            if (value.equals("on")) {
                video10bitModeOn = true;
            } else {
                video10bitModeOn = false;
            }
        }
        if (params.getFen_mode() != null) {
            String value = params.getFen_mode();
            if (value.equals("on")) {
                fenOn = true;
            } else {
                fenOn = false;
            }
        }

        // 刷新一下数据
        photoOptionItems();
        photoEffectItems();
        videoOptionItems();
        videoEffectItems();
        generaSettingItems();
    }
}
