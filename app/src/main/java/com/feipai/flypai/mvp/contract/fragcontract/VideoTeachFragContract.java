package com.feipai.flypai.mvp.contract.fragcontract;

import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.mvp.BaseView;

public class VideoTeachFragContract {
    // 日志Tag
    public String TAG = getClass().getSimpleName();

    public interface View extends BaseView {


    }

    public interface Presenter extends BasePresenter<View> {


    }
}
