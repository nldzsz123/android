package com.feipai.flypai.api;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class RxLoopObserver<T> implements Observer<T>, IHttpResponse<T> {
    private Disposable disposables;

    @Override
    public void onSubscribe(Disposable d) {
        this.disposables = d;
    }


    @Override
    public void onNext(T t) {
        // 回调数据对象
        onResponseSuccess(t);

    }

    @Override
    public void onError(Throwable e) {
        // 异常信息处理
        ApiException apiException = new ApiException();
        apiException.throwable = e;
        onResponseError(apiException);
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onResponseSuccess(T t) {

    }

    @Override
    public void onResponseError(ApiException apiException) {
        if (apiException != null) {
            apiException.printExceptionMsg();
        }
    }

    public void disposeDisposables() {
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
    }
}
