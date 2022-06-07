package com.feipai.flypai.mvp;

import android.app.Activity;

/**
 * View的基类
 *
 * @author yanglin
 */
public interface BaseView {

    /**
     * 获取页面的Activity
     */
    Activity getPageActivity();

//    void showDialog(String text);
//
//    void dismissDialog();
}