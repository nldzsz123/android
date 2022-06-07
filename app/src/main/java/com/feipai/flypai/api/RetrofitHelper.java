package com.feipai.flypai.api;

import com.feipai.flypai.app.FlyPieApplication;
import com.feipai.flypai.utils.global.NetworkUtils;
import com.feipai.flypai.utils.global.SSLSocketClient;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit 辅助类
 *
 * @author yanglin
 */
public class RetrofitHelper {

    /**
     * 默认超时时间
     */
    int DEFAULT_TIMEOUT_TIME = 60;//10

    /**
     * 文件上传默认超时时间
     */
    int DEFAULT_UPLOAD_TIMEOUT_TIME = 20;

    String URL_HEADER = "http://";

    public RetrofitHelper() {
        initOkHttp();
    }

    private static void initOkHttp() {



    }


}