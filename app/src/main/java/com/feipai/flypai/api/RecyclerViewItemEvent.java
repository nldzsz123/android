package com.feipai.flypai.api;

import android.support.v7.widget.RecyclerView;

import com.chad.library.adapter.base.entity.MultiItemEntity;

public interface RecyclerViewItemEvent<V extends RecyclerView.ViewHolder, T extends MultiItemEntity> {

    void onItemClickListener(V v, T t);

    void onItemLongClickListener(V v, T t);

    void onItemDownloadImgClickListener(V v, T t);
}
