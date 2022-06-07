package com.feipai.flypai.ui.view;


import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.feipai.flypai.utils.global.LogUtils;

public class ViewSizeChangeAnimation extends Animation {
    int initialHeight;
    int targetHeight = 0;
    int initialWidth;
    int targetWidth = 0;
    View mView;

    public ViewSizeChangeAnimation(View view, int targetHeight, int targetWidth) {
        this.mView = view;
        this.targetHeight = targetHeight;
        this.targetWidth = targetWidth;
    }

    public void setTagetHeightAndWidth(int targetHeight, int targetWidth) {

        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
    }


    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        if (targetHeight >0) {
            mView.getLayoutParams().height = initialHeight + (int) ((targetHeight - initialHeight) * interpolatedTime);
        }
        if (targetWidth > 0)
            mView.getLayoutParams().width = initialWidth + (int) ((targetWidth - initialWidth) * interpolatedTime);
        mView.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        this.initialHeight = height;
        this.initialWidth = width;
        super.initialize(width, height, parentWidth, parentHeight);
    }

    public void startAnimation(int duration) {
        this.setDuration(duration);
        setFillAfter(true);
        mView.startAnimation(this);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
