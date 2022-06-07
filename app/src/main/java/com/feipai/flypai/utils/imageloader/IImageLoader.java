package com.feipai.flypai.utils.imageloader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

/**
 * 图片加载接口
 *
 * @author yanglin
 */
public interface IImageLoader {

    void init(Context context);

    void loadNet(ImageView target, String url, Options options);

    void loadResource(ImageView target, int resId, Options options);

    void loadResourceForBackground(View target, int resId, Options options);


    void loadAssets(ImageView target, String assetName, Options options);

    void loadFile(ImageView target, File file, Options options);

    void loadUri(ImageView target, Uri uri, Options options);

    void clearMemoryCache(Context context);

    void clearDiskCache(Context context);

    void resume(Context context);

    void pause(Context context);

    /**
     * 一些图片加载配置
     */
    class Options {

        public static final int RES_NONE = -1;

        // 加载中的资源id
        public int mLoadingResId = RES_NONE;
        // 加载失败的资源id
        public int mLoadErrorResId = RES_NONE;
        //  加载原来的drawable
        public boolean mIsLoadDrawable;
        /**
         * 显示高
         */
        public int mWide = 0;
        /**
         * 指定高
         */
        public int mHeight = 0;

        public Options() {

        }

        public Options(int resId) {
            this.mLoadErrorResId = resId;
            this.mLoadingResId = resId;
        }

        public Options(int resId, int wide, int height) {
            this.mLoadErrorResId = resId;
            this.mLoadingResId = resId;
            this.mWide = wide;
            this.mHeight = height;
        }


        public Options(boolean isLoadDrawable, int wide, int height) {
            this.mIsLoadDrawable = isLoadDrawable;
            this.mWide = wide;
            this.mHeight = height;
        }

        public Options(int size, boolean isLoadDrawable) {
            this.mWide = size;
            this.mHeight = size;
            this.mIsLoadDrawable = isLoadDrawable;
        }


        public Options(int loadingResId, int loadErrorResId, int wide, int height) {
            this.mLoadingResId = loadingResId;
            this.mLoadErrorResId = loadErrorResId;
            this.mWide = wide;
            this.mHeight = height;
        }
    }

}
