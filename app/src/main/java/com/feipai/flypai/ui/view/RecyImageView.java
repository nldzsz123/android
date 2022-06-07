package com.feipai.flypai.ui.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.feipai.flypai.R;
import com.feipai.flypai.utils.global.LogUtils;

import java.util.Random;

public class RecyImageView extends View {
    Bitmap bitmap, bitmapTem;
    int bw, bh;

    private int mDrawH, mDrawW;

    public RecyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RecyImageView(Context context) {
        super(context);
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
        bw = this.bitmap.getWidth();
        bh = this.bitmap.getHeight();
        bitmapTem = bitmap.createBitmap(bw, bh, Bitmap.Config.RGB_565);
        invalidate();
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setPixels(int[] pixels) {
        bitmapTem.setPixels(pixels, 0, bw, 0, 0, bw, bh);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        if (bitmapTem != null && !bitmapTem.isRecycled()) {
            LogUtils.d("放图片的view尺寸====" + this.getHeight() + this.getWidth());
            mDrawW = Math.max(bw, this.getWidth());
            mDrawH = mDrawW / this.getWidth() * this.getHeight();
            RectF rectF = new RectF(0, 0, mDrawW, mDrawH);   //w和h分别是屏幕的宽和高，也就是你想让图片显示的宽和高
            canvas.drawBitmap(bitmapTem, null, rectF, null);
//            canvas.drawBitmap(bitmapTem, 0, 0, null);

        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (bitmapTem != null && !bitmapTem.isRecycled())
            bitmapTem.recycle();
        if (bitmap != null && !bitmap.isRecycled())
            bitmap.recycle();

    }
}