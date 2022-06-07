package com.feipai.flypai.utils.cache;

import android.text.TextUtils;
import android.util.LruCache;

/**
 * 内存缓存
 *
 * @author chenshige
 */
public class MemoryCache {

    // 缓存容器
    private LruCache<String, Object> mCache;
    // 单例实体
    private static MemoryCache mInstance;

    private MemoryCache() {
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        mCache = new LruCache<String, Object>(cacheSize);
    }

    public static MemoryCache getInstance() {
        if (mInstance == null) {
            synchronized (MemoryCache.class) {
                if (mInstance == null) {
                    mInstance = new MemoryCache();
                }
            }
        }
        return mInstance;
    }

    /**
     * 缓存数据
     *
     * @param key   查找的Key
     * @param value 数据实体
     */
    public synchronized void put(String key, Object value) {
        if (TextUtils.isEmpty(key) || value == null) {
            return;
        }

        if (mCache.get(key) != null) {
            mCache.remove(key);
        }
        mCache.put(key, value);
    }

    /**
     * 获取缓存的数据
     *
     * @param key 查找的Key
     * @return 数据实体
     */
    public Object get(String key) {
        return mCache.get(key);
    }

    /**
     * 获取缓存的数据
     *
     * @param key   查找的Key
     * @param clazz 转换成的实体字节码
     * @return 数据实体
     */
    public synchronized <T> T get(String key, Class<T> clazz) {
        try {
            return (T) mCache.get(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 移除缓存
     *
     * @param key 查找的Key
     */
    public void remove(String key) {
        if (mCache.get(key) != null) {
            mCache.remove(key);
        }
    }

    /**
     * 检查是否有对应Key的缓存数据
     */
    public boolean contains(String key) {
        return mCache.get(key) != null;
    }

    /**
     * 清空缓存
     */
    public void clear() {
        mCache.evictAll();
    }

}
