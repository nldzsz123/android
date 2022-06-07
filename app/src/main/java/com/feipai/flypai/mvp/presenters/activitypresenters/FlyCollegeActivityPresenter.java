package com.feipai.flypai.mvp.presenters.activitypresenters;


import com.MavLinkMsgHandler;
import com.Parser;
import com.feipai.flypai.api.RetrofitHelper;
import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.mvp.contract.activitycontract.FlypaiCollegeActivityContract;
import com.feipai.flypai.utils.socket.MavLinkSocket;

import javax.inject.Inject;

public class FlyCollegeActivityPresenter implements FlypaiCollegeActivityContract.Presenter, BasePresenter<FlypaiCollegeActivityContract.View> {
    protected FlypaiCollegeActivityContract.View mView;

    private MavLinkSocket mavLinkSocket = MavLinkSocket.getInstance();
    private Parser parser = new Parser();

    private MavLinkMsgHandler mMavMsgHandler;


    private RetrofitHelper mRetrofitHelper;

    @Inject
    public FlyCollegeActivityPresenter(RetrofitHelper mRetrofitHelper) {
        this.mRetrofitHelper = mRetrofitHelper;
    }

    @Override
    public void attachView(FlypaiCollegeActivityContract.View view) {
        this.mView = view;
    }

    @Override
    public void detachView() {

    }


}
