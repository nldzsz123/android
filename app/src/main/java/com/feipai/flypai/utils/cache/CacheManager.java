package com.feipai.flypai.utils.cache;


import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.utils.global.FileUtils;

import java.io.File;

/**
 * 缓存管理
 *
 * @author chenshige
 */
public class CacheManager {

    private static MemoryCache mMemoryCache;
    private static SharedPrefUtils mSharedPrefUtils;
    private static ACache mACache;

    private CacheManager() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 磁盘缓存
     */
    public static ACache getACache() {
        if (mACache == null) {
            synchronized (CacheManager.class) {
                if (mACache == null) {
                    mACache = ACache.get(new File(FileUtils.getExternalAppCacheDirByName(ConstantFields.APP_CONFIG.ACACHE_DIR_NAME)));
                }
            }
        }
        return mACache;
    }

    /**
     * 内存缓存
     */
    public static MemoryCache getMemoryCache() {
        if (mMemoryCache == null) {
            synchronized (CacheManager.class) {
                if (mMemoryCache == null) {
                    mMemoryCache = MemoryCache.getInstance();
                }
            }
        }
        return mMemoryCache;
    }

    /**
     * SharedPref 工具
     */
    public static SharedPrefUtils getSharedPrefUtils() {
        if (mSharedPrefUtils == null) {
            synchronized (CacheManager.class) {
                if (mSharedPrefUtils == null) {
                    mSharedPrefUtils = new SharedPrefUtils(ConstantFields.APP_CONFIG.SHARED_PREF_NAME);
                }
            }
        }
        return mSharedPrefUtils;
    }

}
