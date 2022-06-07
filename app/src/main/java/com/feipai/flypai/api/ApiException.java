package com.feipai.flypai.api;

import android.text.TextUtils;

import com.feipai.flypai.utils.global.LogUtils;


/**
 * Api异常
 *
 * @author yanglin
 */
public class ApiException {

    // 日志TAG
    private final static String TAG = ApiException.class.getSimpleName();

    // 异常信息
    public Throwable throwable;
    // 错误码
    public String errorCode;
    // 错误信息
    public String errorMsg;

    /**
     * 打印异常信息
     */
    public void printExceptionMsg() {
        LogUtils.d(TAG, getPrintExceptionMsg());
    }

    /**
     * 拼接异常信息
     */
    public String getPrintExceptionMsg() {
        if (throwable != null) {
            throwable.printStackTrace();
            return throwable.toString();
        } else {
            return "errorCode：" + errorCode + "," + "errorMsg：" + errorMsg;
        }
    }

    /**
     * 拼接Toast异常信息
     */
    public String getToastExceptionMsg() {
        if (throwable != null) {
            throwable.printStackTrace();
            return throwable.toString();
        } else {
            return TextUtils.isEmpty(errorMsg) ? "未知API异常" : errorMsg;
        }
    }

    /**
     * 是否是服务器api异常
     */
    public boolean isApiException() {
        return throwable == null;
    }

}