package com.feipai.flypai.mvp;

import android.app.Activity;

public class ILoginView implements BaseView {
    String userPhone;
    int verificationCode;

    @Override
    public Activity getPageActivity() {
        return null;
    }

//    @Override
//    public void showDialog(String text) {
//
//    }
//
//    @Override
//    public void dismissDialog() {

//    }
}
