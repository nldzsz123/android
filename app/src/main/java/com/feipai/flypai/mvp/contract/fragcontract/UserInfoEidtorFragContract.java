package com.feipai.flypai.mvp.contract.fragcontract;

import android.app.Activity;

import com.feipai.flypai.base.BaseSimpleFragment;
import com.feipai.flypai.mvp.BaseView;

import java.util.List;


public interface UserInfoEidtorFragContract {


    interface View extends BaseView {
        void showButtomDialog(List<String> list);
    }
    interface Presenter {

        /**
         * 选择头像
         */
        void selectHead();
        /**
         * 相册中选取
         */
        void getIconFromPhotoAlbum(Activity activity);
        /**
         * 拍照获取
         */
        void getIconFromCamera();

        /**
         * 选择国家
         */
        void seleCountry();

        /**
         * 重新定位
         */
        void reposition();

        /**
         * 返回
         */
        void back();

        /**
         * 跳过
         */
        void skipToNext();

        /**
         * 保存用户数据
         */
        void saveInfo();


    }
}
