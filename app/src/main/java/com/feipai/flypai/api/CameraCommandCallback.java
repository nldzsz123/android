package com.feipai.flypai.api;


import com.feipai.flypai.mvp.BaseView;

/**
 * 相机消息回调
 */
public abstract class CameraCommandCallback<V> {

    /**
     * 只接受成功的回调
     */
    abstract public void onComplete(V cb);

    public void onCompleteInMain(BaseView bv, V v) {
        if (v != null) {
            //如若为null 则不会处理回调，避免空指针
            RxLoopSchedulers.composeMain(bv, v)
                    .subscribe(new RxLoopObserver<V>() {
                        @Override
                        public void onNext(V v) {
                            super.onNext(v);
                            this.disposeDisposables();
                            CameraCommandCallback.this.onComplete(v);
                        }
                    });
        }
    }

    public void onErrorCodeInMain(BaseView bv, int msgId, int code) {
        RxLoopSchedulers.composeMain(bv, code)
                .subscribe(new RxLoopObserver<Integer>() {
                    @Override
                    public void onNext(Integer errorCode) {
                        super.onNext(errorCode);
                        this.disposeDisposables();
                        CameraCommandCallback.this.onErrorCode(msgId, errorCode);
                    }
                });
    }

    /**
     * 错误码回调,如果需要则重写此方法
     * 需要判断msgid（针对多指令融合）,单指令无需判断
     */
    public void onErrorCode(int msgId, int code) {

    }


}
