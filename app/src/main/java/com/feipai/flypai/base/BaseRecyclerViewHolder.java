package com.feipai.flypai.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.feipai.flypai.utils.imageloader.IImageLoader;
import com.feipai.flypai.utils.imageloader.ImageLoaderFactory;
import com.zhy.autolayout.utils.AutoUtils;

import java.io.File;

/**
 * RecyclerView Adapter ViewHolder
 *
 * @author chenshige
 */
public class BaseRecyclerViewHolder extends BaseViewHolder {
    private View mConvertView;
    private final SparseArray<View> mViews;

    public BaseRecyclerViewHolder(View view) {
        super(view);
        AutoUtils.auto(view);
        this.mConvertView = itemView;
        this.mViews = new SparseArray<View>(8);
    }

    public BaseRecyclerViewHolder createViewHolder(View itemView) {
        BaseRecyclerViewHolder holder = new BaseRecyclerViewHolder(itemView);
        return holder;
    }

    public static BaseRecyclerViewHolder createViewHolder(Context context,
                                                          ViewGroup parent, int layoutId) {
        View itemView = LayoutInflater.from(context).inflate(layoutId, parent,
                false);
        BaseRecyclerViewHolder holder = new BaseRecyclerViewHolder(itemView);
        return holder;
    }

    public View getItemConvertView() {
        return mConvertView;
    }

    /**
     * 通过控件的Id获取对于的控件，如果没有则加入views
     *
     * @param viewId
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = mConvertView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * 设置图片资源
     */
    public BaseRecyclerViewHolder setImageResource(int viewId, int imageResId, IImageLoader.Options options) {
        ImageView view = getView(viewId);
        ImageLoaderFactory.getImageLoader().loadResource(view, imageResId, options);
        return this;
    }

    /**
     * 设置图片资源
     */
    public BaseRecyclerViewHolder setImageResource(int viewId, File file, IImageLoader.Options options) {
        ImageView view = getView(viewId);
        ImageLoaderFactory.getImageLoader().loadFile(view, file, options);
        return this;
    }

    /**
     * 设置video的ID，加载video缩略图
     */
    public BaseRecyclerViewHolder setImageWithVideo(int viewId, String filePath, IImageLoader.Options options) {
        ImageView view = getView(viewId);
        ImageLoaderFactory.getImageLoader().loadFile(view, new File(filePath), options);
        return this;
    }

    /**
     * set image from a URL
     */
    public BaseRecyclerViewHolder setImageUrl(int viewId, String imageUrl, IImageLoader.Options options) {
        ImageView view = getView(viewId);
        ImageLoaderFactory.getImageLoader().loadNet(view, imageUrl, options);
        return this;
    }

    /**
     * set image level
     */
    public BaseRecyclerViewHolder setImageLevel(int viewId, int level) {
        ImageView view = getView(viewId);
        view.setImageLevel(level);
        return this;
    }

    /**
     * set image level
     */
    public BaseRecyclerViewHolder setEnabled(int viewId, boolean enabled) {
        View view = getView(viewId);
        view.setEnabled(enabled);
        return this;
    }

    /**
     * 设置透明度
     *
     * @param viewId
     * @param value
     * @return
     */
    @SuppressLint("NewApi")
    public BaseRecyclerViewHolder setAlpha(int viewId, float value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getView(viewId).setAlpha(value);
        } else {
            AlphaAnimation alpha = new AlphaAnimation(value, value);
            alpha.setDuration(0);
            alpha.setFillAfter(true);
            getView(viewId).startAnimation(alpha);
        }
        return this;
    }


    public BaseRecyclerViewHolder setVisible(int viewId, boolean visible) {
        View view = getView(viewId);
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
        return this;
    }


    /**
     * 点击事件监听
     *
     * @param viewId
     * @param listener
     * @return
     */
    public BaseRecyclerViewHolder setItemOnClickListener(int viewId,
                                                         View.OnClickListener listener) {
        View view = getView(viewId);
        view.setOnClickListener(listener);
        return this;
    }

    /**
     * 触摸事件
     *
     * @param viewId
     * @param listener
     * @return
     */
    public BaseRecyclerViewHolder setItemOnTouchListener(int viewId,
                                                         View.OnTouchListener listener) {
        View view = getView(viewId);
        view.setOnTouchListener(listener);
        return this;
    }

    /**
     * 长按事件
     *
     * @param viewId
     * @param listener
     * @return
     */
    public BaseRecyclerViewHolder setItemOnLongClickListener(int viewId,
                                                             View.OnLongClickListener listener) {
        View view = getView(viewId);
        view.setOnLongClickListener(listener);
        return this;
    }

}