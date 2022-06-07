package com.feipai.flypai.mvp.contract.fragcontract;

import com.feipai.flypai.base.BaseSimpleFragment;
import com.feipai.flypai.beans.FileBean;
import com.feipai.flypai.beans.MediaTypeBean;
import com.feipai.flypai.beans.RxbusBean;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.utils.CameraCommand;

import java.util.List;


public interface MediaFragContract {


    interface View extends BaseView {
        CameraCommand getCmd();

        List<MediaTypeBean> getMediaType();

        void notifyAdapter();

        void showLoading(String text);

        void dismissLoading();

    }

    interface Presenter {

        /**
         * 从本地获取媒体库所有文件
         */
        void getAllFileFromLocal();

        /**
         * 读取宿主发来的消息
         */
        void handlerEvent(RxbusBean msg);


        /**
         * item点击事件
         */
        void mediaItemOnClick(MediaTypeBean bean);

    }
}
