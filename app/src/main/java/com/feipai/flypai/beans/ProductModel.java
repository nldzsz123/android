package com.feipai.flypai.beans;

import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.utils.cache.CacheManager;
import com.feipai.flypai.utils.cache.SharedPrefUtils;
import com.feipai.flypai.utils.global.LogUtils;

import static com.feipai.flypai.app.ConstantFields.PREF.PLANE_TYPE;

public class ProductModel {

    // 产品型号
    public @ConstantFields.ProductType
    int productType;

    // 访问图传的地址 格式 rtsp://192.168.42.1/live
    public String videoUrl;
    // 与飞机通信的Ip地址 格式 192.168.42.1
    public String remoteIpAddress;
    // 兼容新老图传，datasocket连接
    public String dataSocketIp;
    // 相机固件名称
    public String cameraFirmareName;
    // 云台固件名称
    public String yuntaiFirmareName;
    // 飞控固件名称
    public String feikongFirmareName;

    //http://192.168.42.1/DCIM/VR6PRO/Cache/20190805_190821.MP4
    public String videoUrlHeader;

    public ProductModel(@ConstantFields.ProductType int type, String vIp, String rIp,String dataSocketIp, String cfw, String yfw, String fkfw) {
        this.productType = type;
        this.videoUrl = vIp;
        this.remoteIpAddress = rIp;
        this.dataSocketIp = dataSocketIp;
        this.cameraFirmareName = cfw;
        this.yuntaiFirmareName = yfw;
        this.feikongFirmareName = fkfw;
        this.videoUrlHeader = "http://" + remoteIpAddress + "/DCIM/";
        CacheManager.getSharedPrefUtils().putInt(PLANE_TYPE,type);
        LogUtils.d("当前机型===>" + toString());
    }

    // 内置卡路径名；如果没有，为"";6k系列飞机才有
    public static String getInternalCardName(@ConstantFields.ProductType int type) {
        if (type == ConstantFields.ProductType_6kAir) {
            return "FL0";
        }
        return "";
    }

    // 外置SD卡路径
    public static String getExtenalCardName() {
        return "SD0";
    }

    // 访问拍照和录像的根目录名
    public static String getPhotoAndMovePathName() {
        return "";
    }

    // 访问智能拍摄(延时，全景等的)根目录名
    public static String getSmartPathName(@ConstantFields.ProductType int type) {
        if (type == ConstantFields.ProductType_6kAir) {
            return "Flypie_AI";
        }
        return "FP_Panorama";
    }

    @Override
    public String toString() {
        return "ProductModel{" +
                "productType=" + productType +
                ", videoUrl='" + videoUrl + '\'' +
                ", remoteIpAddress='" + remoteIpAddress + '\'' +
                ", dataSocketIp='" + dataSocketIp + '\'' +
                ", cameraFirmareName='" + cameraFirmareName + '\'' +
                ", yuntaiFirmareName='" + yuntaiFirmareName + '\'' +
                ", feikongFirmareName='" + feikongFirmareName + '\'' +
                ", videoUrlHeader='" + videoUrlHeader + '\'' +
                '}';
    }
}
