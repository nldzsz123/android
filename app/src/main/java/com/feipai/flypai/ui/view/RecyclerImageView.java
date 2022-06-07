package com.feipai.flypai.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.feipai.flypai.utils.global.LogUtils;

public class RecyclerImageView extends AppCompatImageView {

    private Bitmap mOriginalBmp;
    private int[] mOriginalPxs;

    private int mBmpH;
    private int mBmpW;

    private int mDrawH;
    private int mDrawW;

    public RecyclerImageView(Context context) {
        super(context);
    }

    public RecyclerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setImageDrawable(null);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        if (mOriginalBmp == null || mOriginalBmp.isRecycled()) {
            this.mOriginalBmp = bm;
            mBmpW = mOriginalBmp.getWidth();
            mBmpH = mOriginalBmp.getHeight();
            if (mOriginalPxs == null) {
                mOriginalPxs = new int[mBmpW * mBmpH];
            }
            mOriginalBmp.getPixels(mOriginalPxs, 0, mBmpW, 0, 0, mBmpW, mBmpH);
        }
//        LogUtils.d("设置了setImageBitmap====>" + mBmpW + "||" + mBmpH);
    }

    public Bitmap getBitmap() {
        mOriginalBmp.setPixels(mOriginalPxs, 0, mBmpW, 0, 0, mBmpW, mBmpH);
        return mOriginalBmp;
    }

    public void setPixels(int[] pixels) {
        if (mOriginalBmp != null && !mOriginalBmp.isRecycled()) {
//            LogUtils.d("设置了px====>" + mBmpW + "||" + mBmpH);
            mOriginalBmp.setPixels(pixels, 0, mBmpW, 0, 0, mBmpW, mBmpH);
            invalidate();
        }
    }

    public int[] getOriginalPxs() {
        return mOriginalPxs;
    }

    public int getmBmpH() {
        return mBmpH;
    }

    public int getmBmpW() {
        return mBmpW;
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        if ((mOriginalBmp == null || mOriginalBmp.isRecycled()) && drawable != null) {
            this.mOriginalBmp = ((BitmapDrawable) drawable).getBitmap();
            mBmpW = mOriginalBmp.getWidth();
            mBmpH = mOriginalBmp.getHeight();
            if (mOriginalPxs == null) {
                mOriginalPxs = new int[mBmpW * mBmpH];
            }
            mOriginalBmp.getPixels(mOriginalPxs, 0, mBmpW, 0, 0, mBmpW, mBmpH);
        }
        LogUtils.d("设置了setImageDrawable====>" + mBmpW + "||" + mBmpH);
    }


    /**
     * 正确的设置imageDrawable的方法
     */
    public void resetImageDrawable(@Nullable Drawable drawable) {
        if (mOriginalBmp != null && !mOriginalBmp.isRecycled()) {
            mOriginalBmp.recycle();
            mOriginalBmp = null;
        }
        mOriginalPxs = null;
        setImageDrawable(drawable);
    }

    /**
     * 正确的设置imageDrawable的方法
     */
    public void resetImageBitmap(Bitmap bm) {
        if (mOriginalBmp != null && !mOriginalBmp.isRecycled()) {
            mOriginalBmp.recycle();
            mOriginalBmp = null;
        }
        setImageBitmap(bm);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mOriginalBmp != null && !mOriginalBmp.isRecycled()) {
            mDrawW = Math.max(mBmpW, this.getWidth());
            mDrawH = mDrawW / this.getWidth() * this.getHeight();
            RectF rectF = new RectF(0, 0, mDrawW, mDrawH);   //w和h分别是屏幕的宽和高，也就是你想让图片显示的宽和高
            canvas.drawBitmap(mOriginalBmp, null, rectF, null);
        } else {
            super.onDraw(canvas);
        }
    }


}
