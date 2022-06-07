package com.feipai.flypai.ui.view.Camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.feipai.flypai.BuildConfig;
import com.feipai.flypai.utils.MLog;
import com.zhy.autolayout.AutoRelativeLayout;

/**
 * Created by xiongli on 2016-08-16.
 */
public class DrawGridView extends AutoRelativeLayout {
    private Canvas myCanvas;
    private float lineX = 0;
    private int horGrid = 100, verGrid = 100;//水平网格和竖直网格
    private int screenW, screenH;//屏幕宽和高
    private boolean initDrawGrid = false;//初始化网格
    //    private int change
    private static Mode mode = Mode.DRAW_GRID;//初始化需要绘制是哪种

    private int startX, startY, endX, endY;//绘制网格线

    int verNum = 0, horNum = 0;
    private Context context;
    private boolean isEnabled = true;

    // 第一个按下的手指的点
    private PointF startPoint = new PointF();
    // 两个按下的手指的触摸点的中点
    private PointF midPoint = new PointF();

    private int touchMode = 1;

    //两触点距离
    private float beforeLenght = 1f;
    // 两触点距离
    private float afterLenght;
    private boolean zoomSucce = false;
    private OnTouchListener listener;
    private Paint lintPaint = new Paint();


    public DrawGridView(Context context) {
        super(context);
        this.context = context;
        setWillNotDraw(false);
        lintPaint.setColor(Color.RED);//颜色
        lintPaint.setStrokeWidth(2);//线宽
        lintPaint.setAntiAlias(true);//抗锯齿
    }

    public DrawGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setWillNotDraw(false);
    }

    public DrawGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.myCanvas = canvas;
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);//颜色
        paint.setStrokeWidth(1);//线宽
        paint.setAntiAlias(true);//抗锯齿
        switch (mode) {
            case DRAW_GRID:
                drawGrid(canvas, paint);
                break;
            case DRAW_DIAGONAL:
                if (initDrawGrid) {
                    canvas.drawLine(startX, startY, endX, endY, paint);
                    canvas.drawLine(startX, endY, endX, startY, paint);
                }
                drawGrid(canvas, paint);
                break;
        }
        if (BuildConfig.DEBUG)
            if (lineX != 0)
                drawLine(canvas, lintPaint, lineX);
    }

    private void drawGrid(Canvas canvas, Paint paint) {
        verNum = (int) (screenH / verGrid) + 1;
        horNum = (int) (screenW / horGrid) + 1;
//        Log.e("yanglin", "网格---" + initDrawGrid);
        if (initDrawGrid) {
            for (int i = 1; i < verNum - 1; i++) {
                canvas.drawLine(0, i * verGrid - 1, screenW, i * verGrid - 1,
                        paint);//绘制横向
//                Log.e("yanglin", "网格---" + 0 + "||" + (i * verGrid - 1) + "||" + screenW + "||" + (i * verGrid - 1));
            }
            for (int i = 1; i < horNum - 1; i++) {
                canvas.drawLine(i * horGrid - 1, 0, i * horGrid - 1, screenH,
                        paint);//绘制垂直
            }
        }
    }

    private void drawLine(Canvas canvas, Paint paint, float x) {
        canvas.drawLine(x, 0, x, screenH,
                paint);//绘制垂直
    }


    /**
     * 设置网格线参数
     * screenW 代表当前view的宽度
     * screenH 代表屏幕的高度：横屏状态
     **/
    public void setInf(int screenW, int screenH) {
        this.screenW = screenW;
        this.screenH = screenH;
        this.verGrid = screenH / 3;
        this.horGrid = screenW / 3;
        initDrawGrid = true;
        this.mode = Mode.DRAW_GRID;
        postInvalidate();
    }

    public void setDiagonal(int l, int t, int r, int b) {
        this.screenH = b;
        this.startX = l;
        this.startY = t;
        this.endX = r;
        this.endY = b;
        this.screenW = endX - startX;
        this.verGrid = screenH / 3;
        this.horGrid = this.screenW / 3;
//        Log.e("yanglin", "网格-----" + screenW + "||" + screenH + "||" + (endY - startY) + "||" + (endX - startX));
        initDrawGrid = true;
        this.mode = Mode.DRAW_DIAGONAL;
        postInvalidate();

    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
    }

    /**
     * 擦除网格线
     **/
    public void clearLine() {
        initDrawGrid = false;
        postInvalidate();
    }

    public enum Mode {
        DRAW_GRID, DRAW_DIAGONAL
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int touchaction = event.getAction() & MotionEvent.ACTION_MASK;
        if (touchaction == MotionEvent.ACTION_DOWN) {
            touchMode = 1;
        } else if (touchaction == MotionEvent.ACTION_POINTER_DOWN) {
            touchMode = 2;
            midPoint = middle(event);
            if (distance(event) > 10f) {
                //放大
                beforeLenght = distance(event);
            }
        } else if (touchaction == MotionEvent.ACTION_UP) {

            // 手指放开事件
            if (touchMode == 1) {
                lineX = event.getX();
                if (isEnabled && listener != null) {
                    listener.singleTouch();
                }
            }
            return false;
        } else if (touchaction == MotionEvent.ACTION_POINTER_UP) {

        } else if (touchaction == MotionEvent.ACTION_MOVE) {
            if (touchMode == 2) {
                if (distance(event) > 10f) {
                    afterLenght = distance(event);
                    float gapLenght = afterLenght - beforeLenght;
                    //如果当前时间两点距离与前一时间两点距离值
                    if (gapLenght == 0) {
                        //表示没变化
                    } else if (Math.abs(gapLenght) > 60f && !zoomSucce) {
                        //变化达到了5f
                        if (gapLenght > 0) {
                            if (listener != null)
                                listener.zoomOut();
                        } else {
                            if (listener != null)
                                listener.zoomIn();
                        }
                        beforeLenght = afterLenght;
                    }
                }
            }
        }

//        if (touchMode == 1) {
//            if (isEnabled && listener != null) {
//                listener.singleTouch();
//            }
////            getParent().requestDisallowInterceptTouchEvent(true);
//            return super.dispatchTouchEvent(event);
//        } else {
        return true;
//        }

    }

    public void setCanClick(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isCanClick() {
        return isEnabled;
    }


    // 计算两个触摸点之间的距离
    private float distance(MotionEvent event) {
        try {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            MLog.log("出现了异常-->" + e.getMessage());
        }
        return 0;
    }

    private boolean mIsDisallowIntercept = false;

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // keep the info about if the innerViews do
        // requestDisallowInterceptTouchEvent
        mIsDisallowIntercept = disallowIntercept;
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }


    // 计算两个触摸点的中心
    private PointF middle(MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        return new PointF(x / 2, y / 2);
    }

    public int getTouchMode() {
        return touchMode;
    }

    public void setOnTouchListener(OnTouchListener listener) {
        this.listener = listener;
    }

    public interface OnTouchListener {
        void zoomIn();

        void zoomOut();

        void singleTouch();

    }
}

