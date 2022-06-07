package com.feipai.flypai.utils.global;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subscribers.SerializedSubscriber;

public class RxBusUtils {
    private final FlowableProcessor<Object> mBus;
    private static volatile RxBusUtils sRxBusUtils = null;

    public RxBusUtils() {
        //调用toSerialized()方法，保证线程安全
        mBus = PublishProcessor.create().toSerialized();
    }

    public static synchronized RxBusUtils getDefault() {
        if (sRxBusUtils == null) {
            synchronized (RxBusUtils.class) {
                if (sRxBusUtils == null) {
                    sRxBusUtils = new RxBusUtils();
                }
            }
        }
        return sRxBusUtils;
    }


    /**
     * 发送消息
     *
     * @param o
     */
    public void post(Object o) {
        new SerializedSubscriber<>(mBus).onNext(o);
    }

    /**
     * 确定接收消息的类型
     *
     * @param aClass
     * @param <T>
     * @return
     */
    public <T> Flowable<T> toFlowable(Class<T> aClass) {
        return mBus.ofType(aClass);
    }

    /**
     * 判断是否有订阅者
     *
     * @return
     */
    public boolean hasSubscribers() {
        return mBus.hasSubscribers();
    }

}