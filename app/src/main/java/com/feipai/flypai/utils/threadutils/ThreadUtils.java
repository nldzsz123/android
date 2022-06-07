package com.feipai.flypai.utils.threadutils;

/**
 * Created by YangLin on 2016-11-09.
 */

public class ThreadUtils {
    public static void run(Runnable r) {
        ThreadPool.getThreadP().excute(r);
    }

    public static void remove(Runnable r) {
        ThreadPool.getThreadP().remove(r);
    }

}
