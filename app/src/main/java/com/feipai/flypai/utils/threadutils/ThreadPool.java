package com.feipai.flypai.utils.threadutils;


public class ThreadPool {
    private static ThreadPoolProxy threadP;
    private static ThreadPoolProxy threadPd;

    /**
     * 普通线程池
     *
     * @return
     */
    public static ThreadPoolProxy getThreadP() {
        if (threadP == null) {
            synchronized (ThreadPool.class) {
                if (threadP == null) {
                    threadP = new ThreadPoolProxy(8, 8, 5);
                }
            }
        }

        return threadP;
    }

    /**
     * 下载线程池
     *
     * @return
     */
    public static ThreadPoolProxy getThreadPd() {
        if (threadPd == null) {
            synchronized (ThreadPool.class) {
                if (threadPd == null) {
                    threadPd = new ThreadPoolProxy(3, 3, 5000);
                }
            }
        }

        return threadPd;
    }

}
