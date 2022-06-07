package com.feipai.flypai.utils.global;

import android.support.annotation.Nullable;
import android.util.Log;

import com.feipai.flypai.app.ConstantFields;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

/**
 * 日志相关工具类
 */
public class LogUtils {

    private LogUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    private static boolean isDebug;
    private static LogManager mManger;

    /**
     * 在application调用初始化
     */
    public static void init(boolean debug) {
        isDebug = debug;
        mManger = LogManager.getInstance();
        FormatStrategy formatStrategy = null;
        if (isDebug) {
            formatStrategy = PrettyFormatStrategy.newBuilder()
                    .showThreadInfo(true)
                    .methodCount(2)
                    .methodOffset(0)
                    .logStrategy(new LogCatStrategy())
                    .tag(ConstantFields.APP_CONFIG.DEBUG_TAG)
                    .build();
        } else {
            formatStrategy = PrettyFormatStrategy.newBuilder()
                    .showThreadInfo(true)
                    .methodCount(2)
                    .methodOffset(0)
                    .tag(ConstantFields.APP_CONFIG.DEBUG_TAG)
                    .build();
        }
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy) {
            @Override
            public boolean isLoggable(int priority, @Nullable String tag) {
                return isDebug;
            }
        });
    }

    public static void d(String tag, String message) {
        if (isDebug) {
            Logger.t(tag).d(message);
        }else {
//            if (mManger != null)
//                mManger.print(message);
        }
    }

    public static void d(String message) {
        if (isDebug) {
            Logger.d(message);
        }else {
//            if (mManger != null)
//                mManger.print(message);
        }
    }

    public static void e(Throwable throwable, String message, Object... args) {
        if (isDebug) {
            Logger.e(throwable, message, args);
        }else {
//            if (mManger != null)
//                mManger.print(message,throwable);
        }
    }

    public static void e(String message, Object... args) {
        if (isDebug) {
            Logger.e(message, args);
        }else {
//            mManger.print(message);
        }
    }

    public static void i(String message, Object... args) {
        if (isDebug) {
            Logger.i(message, args);
        }
    }

    public static void v(String message, Object... args) {
        if (isDebug) {
            Logger.v(message, args);
        }
    }

    public static void w(String message, Object... args) {
        if (isDebug) {
            Logger.v(message, args);
        }
    }

    public static void wtf(String message, Object... args) {
        if (isDebug) {
            Logger.wtf(message, args);
        }
    }

    public static void json(String message) {
        if (isDebug) {
            Logger.json(message);
        }
    }

    public static void xml(String message) {
        if (isDebug) {
            Logger.xml(message);
        }
    }

    /**
     * 解决 Android 3.0排序乱问题
     */
    public static class LogCatStrategy implements LogStrategy {

        @Override
        public void log(int priority, String tag, String message) {
            Log.println(priority, randomKey() + tag, message);
        }

        private int last;

        private String randomKey() {
            int random = (int) (10 * Math.random());
            if (random == last) {
                random = (random + 1) % 10;
            }
            last = random;
            return String.valueOf(random);
        }
    }

}
