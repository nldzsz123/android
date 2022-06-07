package com.feipai.flypai.ui.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.feipai.flypai.R;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.StringUtils;


/**
 * 自定义SeekBar
 * <attr name="seek_progress" format="integer" /><!--滑块的位置（默认区间0-100）-->
 * <attr name="seek_max_count" format="integer" /><!--最小值（默认0）-->
 * <attr name="seek_min_count" format="integer" /><!--最大值（默认100）-->
 * <attr name="seek_bar_progress" format="integer" /><!--滑块的位置（默认0-100）-->
 * <attr name="progress_color" format="color" /><!--滑竿的颜色-->
 * <attr name="progress_past_color" format="color" /><!--滑竿已滑过的颜色-->
 * <attr name="progress_future_color" format="color" /><!--滑竿未滑过的颜色-->
 * <attr name="progress_size" format="dimension" /><!--滑竿的大小（宽度或者高度）-->
 * <attr name="thumb_color" format="color" /><!--滑块的颜色-->
 * <attr name="thumb_image" format="reference" /><!--滑块的图片-->
 * <attr name="drag_able" format="boolean" /><!--可拖动的-->
 * <attr name="seek_orientation" format="enum"> <!--方向 默认横屏-->
 * ---<enum name="horizontal" value="0" />
 * ---<enum name="vertical" value="1" />
 * </attr>
 * Created by Administrator on 2018/4/4 0004.
 */

public class VerticalSeekBar extends View {
    private static final String TAG = VerticalSeekBar.class.getSimpleName();
    private static final int DEFAULT_CIRCLE_COLOR = Color.WHITE;
    private static final int DEFAULT_THUMB_TEXT_COLOR = Color.BLACK;

    private int seekBarOrientation;//方向 0=horizontal，1=vertical；
    private int progressColor, progressPastColor, progressFutureColor;
    private float progressSize;
    private float thumbTextSize;
    private int startColor = DEFAULT_CIRCLE_COLOR;
    private int middleColor = DEFAULT_CIRCLE_COLOR;
    private int endColor = DEFAULT_CIRCLE_COLOR;
    private int thumbColor;
    private int thumbTextColor;
    private int pastColorArray[] = {startColor, middleColor, endColor};
    private int futureColorArray[] = {startColor, middleColor, endColor};
    private float x, y;
    private float mRadius;
    private int progress;
    private String progressText;
    private int maxCount = 100, minCount = 0;
    private float sLeft, sTop, sRight, sBottom;
    private float sWidth, sHeight;
    private LinearGradient linearGradient;
    private Paint paint = new Paint();
    protected OnSlideChangeListener onSlideChangeListener;

    private int thumbImage;
    private boolean dragAble;

    public VerticalSeekBar(Context context) {
        this(context, null);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VerticalSeekBar, defStyle, 0);

        seekBarOrientation = a.getInteger(R.styleable.VerticalSeekBar_seek_orientation, 0);
        setProgress(a.getInteger(R.styleable.VerticalSeekBar_seek_progress, 0));
        setMax(a.getInteger(R.styleable.VerticalSeekBar_seek_max_count, 100));
        setMin(a.getInteger(R.styleable.VerticalSeekBar_seek_min_count, 0));
        thumbColor = a.getColor(R.styleable.VerticalSeekBar_thumb_color, DEFAULT_CIRCLE_COLOR);
        thumbTextColor = a.getColor(R.styleable.VerticalSeekBar_thumb_text_color, DEFAULT_THUMB_TEXT_COLOR);
        thumbTextSize = a.getDimension(R.styleable.VerticalSeekBar_thumb_text_size, 0);
        dragAble = a.getBoolean(R.styleable.VerticalSeekBar_drag_able, true);
        progressColor = a.getColor(R.styleable.VerticalSeekBar_progress_color, DEFAULT_CIRCLE_COLOR);
        progressPastColor = a.getColor(R.styleable.VerticalSeekBar_progress_past_color, progressColor);
        progressFutureColor = a.getColor(R.styleable.VerticalSeekBar_progress_future_color, progressColor);
        progressSize = a.getDimension(R.styleable.VerticalSeekBar_progress_size, 0);
        thumbImage = a.getResourceId(R.styleable.VerticalSeekBar_thumb_image, 0);
        a.recycle();
        setCircle_color(thumbColor);
        setProgressColor(progressPastColor, progressFutureColor);
    }

    /**
     * @param circle_color 滑块的颜色，在没有设置背景图情况下生效
     */
    public void setCircle_color(int circle_color) {
        this.thumbColor = circle_color;
        invalidate();
    }

    /**
     * @param dragAble 设置是否可以拖动
     */
    public void setDragAble(boolean dragAble) {
        this.dragAble = dragAble;
        invalidate();
    }

    /**
     * @param progress_past_color   滑竿已滑过的颜色
     * @param progress_future_color 滑竿未滑过的颜色
     */
    public void setProgressColor(int progress_past_color, int progress_future_color) {
        pastColorArray[0] = progress_past_color;
        pastColorArray[1] = progress_past_color;
        pastColorArray[2] = progress_past_color;
        futureColorArray[0] = progress_future_color;
        futureColorArray[1] = progress_future_color;
        futureColorArray[2] = progress_future_color;
        invalidate();
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int h = getMeasuredHeight();
        int w = getMeasuredWidth();

        if (seekBarOrientation == 0) {
            if (thumbImage == 0) {
                mRadius = (float) h / 2;
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.outWidth = getMeasuredWidth();
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), thumbImage, options);
                int outW = (int) (getMeasuredHeight() * bitmap.getWidth() / bitmap.getHeight());
                mRadius = outW / 2;
            }
            if (progressSize == 0 || progressSize > h) {
                sTop = h * 0.3f;
                sBottom = h * 0.7f;
            } else {
                sTop = (float) ((double) (h - progressSize) / (double) 2);
                sBottom = (float) ((double) (h - progressSize) / (double) 2) + progressSize;
            }
            sLeft = 0;
            sRight = w;
            sWidth = sRight - sLeft;
            sHeight = sBottom - sTop;
            x = (progress / (float) maxCount) * sWidth;
            y = (float) h / 2;
        } else {
            if (thumbImage == 0) {
                mRadius = (float) w / 2;
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.outWidth = getMeasuredWidth();
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), thumbImage, options);
                int outH = (int) (bitmap.getHeight() * getMeasuredWidth() / bitmap.getWidth());
                mRadius = outH / 2;
            }
            if (progressSize == 0 || progressSize > w) {
                sLeft = w * 0.3f;
                sRight = w * 0.7f;
            } else {
                sLeft = (float) ((double) (w - progressSize) / (double) 2);
                sRight = (float) ((double) (w - progressSize) / (double) 2) + progressSize;
            }
            sTop = 0;
            sBottom = h;
            sWidth = sRight - sLeft;
            sHeight = sBottom - sTop;
            x = (float) w / 2;
            y = (1 - (float) progress / (float) maxCount) * sHeight;
        }

        drawBackground(canvas);
        drawCircle(canvas);
        paint.reset();
    }

    private void drawBackground(Canvas canvas) {
        if (seekBarOrientation == 0) {
            RectF rectBlackBgPast = new RectF(sLeft, sTop, x, sBottom);
            linearGradient = new LinearGradient(sLeft, sTop, x, sBottom, pastColorArray, null, Shader.TileMode.MIRROR);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(linearGradient);
            canvas.drawRoundRect(rectBlackBgPast, sHeight / 2, sHeight / 2, paint);

            RectF rectBlackBgFuture = new RectF(x, sTop, sWidth, sBottom);
            linearGradient = new LinearGradient(x, sTop, sWidth, sBottom, futureColorArray, null, Shader.TileMode.MIRROR);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(linearGradient);
            canvas.drawRoundRect(rectBlackBgFuture, sHeight / 2, sHeight / 2, paint);
        } else {
            RectF rectBlackBgPast = new RectF(sLeft, y, sRight, sBottom);
            linearGradient = new LinearGradient(sLeft, y, sWidth, sHeight - y, pastColorArray, null, Shader.TileMode.MIRROR);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(linearGradient);
            canvas.drawRoundRect(rectBlackBgPast, sWidth / 2, sWidth / 2, paint);

            RectF rectBlackBgFuture = new RectF(sLeft, sTop, sRight, y);
            linearGradient = new LinearGradient(sLeft, sTop, sWidth, y, futureColorArray, null, Shader.TileMode.MIRROR);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(linearGradient);
            canvas.drawRoundRect(rectBlackBgFuture, sWidth / 2, sWidth / 2, paint);
        }
    }

    private void drawCircle(Canvas canvas) {
        Paint thumbPaint = new Paint();
        thumbPaint.setAntiAlias(true);
        thumbPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setColor(thumbColor);
        if (seekBarOrientation == 0) {
            x = x < mRadius ? mRadius : x;
            x = x > sWidth - mRadius ? sWidth - mRadius : x;
            if (thumbImage == 0) {
                canvas.drawCircle(x, y, mRadius, thumbPaint);
                if (!StringUtils.isEmpty(progressText)) {
                    //绘制文本
                    thumbPaint.setColor(thumbTextColor);
                    thumbPaint.setTextSize(thumbTextSize);
                    float textWidth = thumbPaint.measureText(progressText);
                    float textX = x - textWidth / 2;
                    Paint.FontMetrics fontMetrics = thumbPaint.getFontMetrics();
                    float dy = (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
                    float textY = y + dy;
                    canvas.drawText(progressText, textX, textY, thumbPaint);
                }
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.outHeight = getMeasuredHeight();
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), thumbImage, options);
                int outW = (int) (getMeasuredHeight() * bitmap.getWidth() / bitmap.getHeight());
                Rect rect = new Rect((int) (x + outW / 2), 0, (int) (x - outW / 2), getMeasuredHeight());
                canvas.drawBitmap(bitmap, null, rect, null);
            }
        } else {
            y = y < mRadius ? mRadius : y;
            y = y > sHeight - mRadius ? sHeight - mRadius : y;
            if (thumbImage == 0) {
                canvas.drawCircle(x, y, mRadius, thumbPaint);
                if (!StringUtils.isEmpty(progressText)) {
                    //绘制文本
                    thumbPaint.setColor(thumbTextColor);
                    thumbPaint.setTextSize(thumbTextSize);
                    float textWidth = thumbPaint.measureText(progressText);
                    float textX = x - textWidth / 2;
                    Paint.FontMetrics fontMetrics = thumbPaint.getFontMetrics();
                    float dy = (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
                    float textY = y + dy;
                    canvas.drawText(progressText, textX, textY, thumbPaint);
                }
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.outWidth = getMeasuredWidth();
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), thumbImage, options);
                int outH = (int) (bitmap.getHeight() * getMeasuredWidth() / bitmap.getWidth());
                Rect rect = new Rect(0, (int) (y - outH / 2), getMeasuredWidth(), (int) (y + outH / 2));
                canvas.drawBitmap(bitmap, null, rect, null);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        LogUtils.d("变焦条====》"+dragAble);
        if (!dragAble) {
            return true;
        }
        if (seekBarOrientation == 0) {
            this.x = event.getX();
            setProgress((int) (x / sWidth * maxCount));
        } else {
            this.y = event.getY();
            setProgress((int) ((sHeight - y) / sHeight * maxCount));
        }
//        Log.e("progress", progress + "");

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onSlideProgress(MotionEvent.ACTION_DOWN);
                break;
            case MotionEvent.ACTION_UP:
                onSlideProgress(MotionEvent.ACTION_UP);
                break;
            case MotionEvent.ACTION_MOVE:
                onSlideProgress(MotionEvent.ACTION_MOVE);
                break;

        }

        return true;
    }

    public interface OnSlideChangeListener {
        void OnSlideChangeListener(View view, int progress);

        void onSlideStopTouch(View view, int progress);
    }

    public void setOnSlideChangeListener(OnSlideChangeListener onStateChangeListener) {
        this.onSlideChangeListener = onStateChangeListener;
    }

    public void onlySetProgress(int progress) {
        this.progress = progress;
        if (progress < minCount) {
            this.progress = minCount;
        }
        if (progress > maxCount) {
            this.progress = maxCount;
        }

        invalidate();
    }
    private void setProgress(int progress) {
        this.progress = progress;
        if (progress < minCount) {
            this.progress = minCount;
        }
        if (progress > maxCount) {
            this.progress = maxCount;
        }
        if (onSlideChangeListener != null) {
            onSlideChangeListener.OnSlideChangeListener(this, this.progress);
        }
        invalidate();
    }

    /**
     * 如果progress改变，可直接按照自己需求设置text
     */
    public void setProgressText(String text) {
        this.progressText = text;
        invalidate();
    }


    public void setMax(int maxCount) {
        this.maxCount = maxCount;
    }

    public int getMax() {
        return maxCount;
    }

    public void setMin(int minCount) {
        this.minCount = minCount;
    }

    public int getMin() {
        return minCount;
    }

    public int getProgress() {
        return progress;
    }

    public void onSlideProgress(int event) {
        switch (event) {
            case MotionEvent.ACTION_UP:
                if (onSlideChangeListener != null) {
                    onSlideChangeListener.onSlideStopTouch(this, progress);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                this.invalidate();
                break;
            case MotionEvent.ACTION_DOWN:
                this.invalidate();
                break;
        }

    }
}