package com.feipai.flypai.mvp.presenters.activitypresenters;

import com.feipai.flypai.mvp.BasePresenter;
import com.feipai.flypai.mvp.contract.activitycontract.LoginActivityContract;

public class LoginActivityPresenter implements LoginActivityContract.Presenter, BasePresenter<LoginActivityContract.View> {
    protected LoginActivityContract.View mView;

    @Override
    public void attachView(LoginActivityContract.View view) {
        this.mView = view;
    }

    @Override
    public void detachView() {

    }}
