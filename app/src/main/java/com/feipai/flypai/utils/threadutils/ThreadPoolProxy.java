package com.feipai.flypai.utils.threadutils;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolProxy {
    private int mCorePoolSize;
    private int mMaximumPoolSize;
    private long mKeepAliveTime;
    private ThreadPoolExecutor mThreadPool;

    public ThreadPoolProxy(int corePoolSize, int maximumPoolSize,
                           long keepAliveTime) {
        mCorePoolSize = corePoolSize;
        mMaximumPoolSize = maximumPoolSize;
        mKeepAliveTime = keepAliveTime;

    }

    private void initThreadPool() {
        if (mThreadPool == null || mThreadPool.isShutdown()) {
            synchronized (ThreadPoolProxy.class) {
                if (mThreadPool == null || mThreadPool.isShutdown()) {
                    mThreadPool = new ThreadPoolExecutor(mCorePoolSize,
                            mMaximumPoolSize, mKeepAliveTime,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<Runnable>(),
                            Executors.defaultThreadFactory(),
                            new ThreadPoolExecutor.DiscardPolicy());
                }
                mThreadPool.allowCoreThreadTimeOut(true);

            }

        }
    }

    public void excute(Runnable task) {
        initThreadPool();
        mThreadPool.execute(task);
    }

    public Future<?> submit(Runnable task) {
        initThreadPool();
        Future<?> submit = mThreadPool.submit(task);

        return submit;

    }

    public void remove(Runnable task) {
        if (mThreadPool == null)
            return;
        mThreadPool.remove(task);
    }

}
