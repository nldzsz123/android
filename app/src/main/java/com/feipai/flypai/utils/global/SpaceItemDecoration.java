package com.feipai.flypai.utils.global;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zhy.autolayout.utils.AutoUtils;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int mSpace;
    /**
     * 横向布局还是垂直布局，默认横向布局
     */
    private boolean isVertical;

    public SpaceItemDecoration(int pxValue, boolean isVertical) {
        this.isVertical = isVertical;
        mSpace = pxValue;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (isVertical) {
            //从第二个条目开始，距离上方Item的距离
            if (parent.getChildAdapterPosition(view) > 0)
                outRect.top = AutoUtils.getPercentHeightSize(mSpace);
        } else {
            if (parent.getChildAdapterPosition(view) > 0) {
                outRect.left = mSpace;
            } else {
                outRect.left = 0;
            }
        }
    }

}