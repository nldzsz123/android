package com.feipai.flypai.api;

import android.util.Log;

import com.feipai.flypai.beans.LoginCodeMsg;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.utils.global.NetworkUtils;
import com.feipai.flypai.utils.global.RxUtils;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class RxLoopSchedulers {

    /**
     * 循环
     */
    public static Observable composeLoop(BaseView mv, long initialDelay, long period, Function function) {
        return Observable.interval(initialDelay, period, TimeUnit.MILLISECONDS)
                .map(function)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(RxUtils.bindUntilEventActivity(mv, ActivityEvent.DESTROY));
    }

    /**
     * 耗时操作，返回一个值
     */
    public static Observable composeIO(BaseView mv, Function function) {
        return Observable.just("")
                .map(function)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(RxUtils.bindUntilEventActivity(mv, ActivityEvent.DESTROY));
    }

    /**
     * 提交一个任务到主线程
     */
    public static Observable composeMain(BaseView mv, Object object) {
        return Observable.just(object)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(RxUtils.bindUntilEventActivity(mv, ActivityEvent.DESTROY));
    }
}
