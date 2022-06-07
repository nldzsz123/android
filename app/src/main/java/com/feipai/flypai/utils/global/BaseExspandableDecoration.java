package com.feipai.flypai.utils.global;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.feipai.flypai.ui.view.FunctionChildItemView;
import com.feipai.flypai.ui.view.FunctionItemView;
import com.zaihuishou.expandablerecycleradapter.viewholder.AbstractExpandableAdapterItem;
import com.zaihuishou.expandablerecycleradapter.viewholder.BaseAdapterItem;
import com.zhy.autolayout.utils.AutoUtils;

public class BaseExspandableDecoration extends RecyclerView.ItemDecoration {

    private int mSpace;
    /**
     * 横向布局还是垂直布局，默认横向布局
     */
    private boolean isVertical = false;

    public BaseExspandableDecoration(int pxValue, boolean isVertical) {
        this.isVertical = isVertical;
        mSpace = pxValue;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (isVertical && parent.getChildAdapterPosition(view) > 0) {
            RecyclerView.ViewHolder childViewHolder = parent.getChildViewHolder(view);
            if (childViewHolder instanceof BaseAdapterItem) {
                if ((((BaseAdapterItem) childViewHolder).getItem()) instanceof FunctionChildItemView) {
                    outRect.top = AutoUtils.getPercentHeightSize(0);
                } else {
                    outRect.top = AutoUtils.getPercentHeightSize(mSpace);
                }
            }
        }
    }

}