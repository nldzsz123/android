package com.feipai.flypai.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.feipai.flypai.ui.view.Camera.DrawGridView;
import com.feipai.flypai.utils.MLog;
import com.zhy.autolayout.utils.AutoUtils;

/**
 * Created by xiongli on 2016-08-30.
 */
@SuppressLint("AppCompatCustomView")
public class MoveImageView extends ImageView {
    private int imgHight = 0;
    private int imgWidth = 0;
    private int lastX = 0;
    private int lastY = 0;
    private final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private int touchX, touchY;

    public MoveImageView(Context context) {
        super(context);
    }

    public MoveImageView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public MoveImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        imgHight = MeasureSpec.getSize(heightMeasureSpec);
        imgWidth = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(AutoUtils.getPercentWidthSize(177), AutoUtils.getPercentHeightSize(180));  //这里面是原始的大小，需要重新计算可以修改本行
    }

    public void onMoveChange(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //将点下的点的坐标保存

                lastX = (int) event.getX();
                lastY = (int) event.getY();
                touchX = lastX;
                touchY = lastY;
                break;
//            case MotionEvent.ACTION_UP:
//                //计算出需要移动的距离
//                int dx = (int) this.getX() - lastX;
//                int dy = (int) this.getY() - lastY;
//                //将移动距离加上，现在本身距离边框的位置
//                int left = this.getLeft() + dx;
//                int top = this.getTop() + dy;
//                //获取到layoutParams然后改变属性，在设置回去
//                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.getLayoutParams();
//                layoutParams.height = imgHight;
//                layoutParams.width = imgWidth;
//                layoutParams.leftMargin = left;
//                layoutParams.topMargin = top;
//                this.setLayoutParams(layoutParams);
//                //记录最后一次移动的位置
//                lastX = (int) event.getRawX();
//                lastY = (int) event.getRawY();
//                break;
        }
        //刷新界面
        postInvalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Drawable drawable = getDrawable();

        //空值判断，必要步骤，避免由于没有设置src导致的异常错误
        if (drawable == null) {
            return;
        }


        //必要步骤，避免由于初始化之前导致的异常错误
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        if (!(drawable instanceof BitmapDrawable)) {
            return;
        }
        Bitmap b = ((BitmapDrawable) drawable).getBitmap();

        if (null == b) {
            return;
        }

        Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);


        canvas.drawBitmap(bitmap, touchX - bitmap.getWidth() / 2, touchY - bitmap.getHeight() / 2, mPaint);
    }


}