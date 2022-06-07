package com.feipai.flypai.utils.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;


import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.feipai.flypai.utils.global.LogUtils;

import java.io.File;

/**
 * Glide 图片加载
 *
 * @author chenshige
 */
public class GlideLoader implements IImageLoader {

    @Override
    public void init(Context context) {

    }

    @Override
    public void loadNet(ImageView target, String url, Options options) {
        load(getRequestManager(target.getContext()).load(url), target, options);
    }

    @Override
    public void loadResource(ImageView target, int resId, Options options) {
        load(getRequestManager(target.getContext()).load(resId), target, options);
    }

    @Override
    public void loadResourceForBackground(View target, int resId, Options options) {
        loadForBackground(getRequestManager(target.getContext()).load(resId), target, options);
    }

    @Override
    public void loadAssets(ImageView target, String assetName, Options options) {
        load(getRequestManager(target.getContext()).load("file:///android_asset/" + assetName), target, options);
    }

    @Override
    public void loadFile(ImageView target, File file, Options options) {
        load(getRequestManager(target.getContext()).load(file), target, options);
    }

    @Override
    public void loadUri(ImageView target, Uri uri, Options options) {
        load(getRequestManager(target.getContext()).load(uri), target, options);
    }

    @Override
    public void clearMemoryCache(Context context) {
        GlideApp.get(context).clearMemory();
    }

    @Override
    public void clearDiskCache(Context context) {
        GlideApp.get(context).clearDiskCache();
    }

    @Override
    public void resume(Context context) {
        getRequestManager(context).resumeRequests();
    }

    @Override
    public void pause(Context context) {
        getRequestManager(context).pauseRequests();
    }

    private GlideRequests getRequestManager(Context context) {
        return GlideApp.with(context);
    }

    private void load(GlideRequest request, ImageView target, Options options) {
        if (options != null) {
            if (options.mLoadingResId != Options.RES_NONE) {
                request.placeholder(options.mLoadingResId);
            }
            if (options.mLoadErrorResId != Options.RES_NONE) {
                request.error(options.mLoadErrorResId);
            }
//            if (options.mIsLoadDrawable) {
//                request.placeholder(target.getDrawable());
//            }
            if (options.mWide > 0) {
                request.override(options.mWide, options.mHeight);
            }

        }
        request.transition(DrawableTransitionOptions.withCrossFade()).dontAnimate().into(target);
    }

    private void loadForBackground(GlideRequest request, View target, Options options) {
        if (options != null) {
            if (options.mLoadingResId != Options.RES_NONE) {
                request.placeholder(options.mLoadingResId);
            }
            if (options.mLoadErrorResId != Options.RES_NONE) {
                request.error(options.mLoadErrorResId);
            }
            if (options.mIsLoadDrawable) {
                request.placeholder(target.getBackground());
            }
            if (options.mWide > 0) {
                request.override(options.mWide, options.mHeight);
            }

        }
        CustomTarget<Drawable> listenerTarget = new CustomTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                target.setBackground(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        };
        request.transition(DrawableTransitionOptions.withCrossFade()).dontAnimate().into(listenerTarget);
    }

}