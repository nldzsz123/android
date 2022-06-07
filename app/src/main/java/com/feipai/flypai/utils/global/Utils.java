package com.feipai.flypai.utils.global;

import android.content.Context;

import static android.os.Debug.isDebuggerConnected;

public class Utils {

    public static Context context;
    public static boolean isDebug;

    private Utils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 初始化工具类
     *
     * @param context 上下文
     */
    public static void init(Context context, boolean isDebug) {
        Utils.context = context.getApplicationContext();
        Utils.isDebug = isDebug;
        checkDebuggerConnected(isDebug);
    }

    /**
     * 检查调试器,防止被动态调试
     */
    private static void checkDebuggerConnected(boolean isDebug) {
        if (isDebuggerConnected() && !isDebug) {
            // 被动态调试，退出程序
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

}
