package com.feipai.flypai.utils.imageloader;

/**
 * 图片加载工厂，默认使用 Glide 加载图片
 *
 * @author chenshige
 */
public class ImageLoaderFactory {

    private static IImageLoader mImageLoader;

    /**
     * 安装自定义的ImageLoader,在Application OnCreate 中配置，否则可能配置不成功
     */
    public synchronized static void configureCustomImageLoader(IImageLoader loader) {
        if (mImageLoader == null) {
            mImageLoader = loader;
        }
    }

    public static IImageLoader getImageLoader() {
        if (mImageLoader == null) {
            synchronized (ImageLoaderFactory.class) {
                if (mImageLoader == null) {
                    mImageLoader = new GlideLoader();
                }
            }
        }
        return mImageLoader;
    }

}