package com.feipai.flypai.mvp.contract.activitycontract;

import com.feipai.flypai.base.BaseMvpFragment;
import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.mvp.BaseView;

public class FlypaiCollegeActivityContract {
    // 日志Tag
    public String TAG = getClass().getSimpleName();

    public interface View extends BaseView {


    }

    public interface Presenter extends BasePresenter<FlypaiCollegeActivityContract.View> {


    }

}
