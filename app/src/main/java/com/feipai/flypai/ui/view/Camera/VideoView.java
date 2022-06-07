package com.feipai.flypai.ui.view.Camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.wifi.ScanResult;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.feipai.flypai.BuildConfig;
import com.feipai.flypai.R;
import com.feipai.flypai.api.RxLoopObserver;
import com.feipai.flypai.api.RxLoopSchedulers;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.ui.activity.CameraActivity;
import com.feipai.flypai.ui.view.MoveImageView;
import com.feipai.flypai.utils.MLog;
import com.feipai.flypai.utils.SmallerGestures;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.NetworkUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.ScreenUtils;
import com.feipai.flypai.utils.global.Utils;
import com.feipai.flypai.utils.global.WifiConnectUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class VideoView extends RelativeLayout {
    @BindView(R.id.spot_metering_img)
    ImageView spotMeteringImg;
    @BindView(R.id.matrix_img)
    ImageView matrixImg;
    @BindView(R.id.line)
    ImageView lineImg;

    // 是否小窗口
    private boolean isSmall;

    public int meteringwith = 9;
    public int meteringHeight = 5;

    private int downX, downY;
    private float lastX = 0;
    private float lastY = 0;

    private BaseView mBv;
    private int time = 0;
    RxLoopObserver observable;
    private OnTouchCallback onTouchCallback;

    @BindView(R.id.camera_glsurfface)
    public GLSurfaceView videoView;

    public VideoView(Context context) {
        super(context);
        initView(context);
    }

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.camera_videoview, this, true);
        /**如果真是使用注解,这个在Activity注册比较好吧？*/
        ButterKnife.bind(this);
        setSmall(false);
        lineImg.setVisibility(Utils.isDebug ? VISIBLE : INVISIBLE);
    }

    public void setOnTouchCallback(BaseView baseView, OnTouchCallback callback) {
        this.mBv = baseView;
        this.onTouchCallback = callback;
    }

    public boolean isSmall() {
        return isSmall;
    }

    public void setSmall(boolean small) {
        isSmall = small;

        // 改变背景色
        if (isSmall) {
            setBackgroundResource(R.color.color_black);
        } else {
            setVideoViewShow(true);
            setBackgroundResource(R.drawable.camera_back_grand);
        }
    }

    // 添加单击手势
    public void addTapGesture(Context context, SmallerGestures onTapSmallGesture) {
        if (context == null || onTapSmallGesture == null) {
            return;
        }

        // 添加地图窗口的手势
        GestureDetector tapDetector = new GestureDetector(context, onTapSmallGesture);
        setClickable(true);
        setFocusable(true);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isSmall) {
                    setVideoViewShow(false);
                    return tapDetector.onTouchEvent(event);
                } else {
                    LogUtils.d("移动点测光");
                    onTOuchMoveView(event);
                    return true;
                }
            }
        });
    }

    // 移除前面的单击手势
    public void removeTapGesture() {
        setOnTouchListener(null);
    }


    public void setVideoViewShow(boolean isShow) {
        videoView.setVisibility(isShow ? VISIBLE : INVISIBLE);
    }

    /**
     * 根据手势移动点测光view
     **/
    private void onTOuchMoveView(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getRawX();
                downY = (int) event.getRawY();
                time = 0;
                matrixImg.setVisibility(INVISIBLE);
                if (observable != null)
                    observable.disposeDisposables();
                moveViewByLayout(spotMeteringImg, downX, downY);
                break;
            case MotionEvent.ACTION_UP:
                timerSpotMeterVisible();
                break;
        }
    }

    /**
     * 点测光隐藏
     */
    private void timerSpotMeterVisible() {
        if (observable == null)
            observable = new RxLoopObserver<Integer>() {
                @Override
                public void onNext(Integer t) {
                    super.onNext(t);
                    if (t > 3) {
                        this.disposeDisposables();
                        time = 0;
                        spotMeteringImg.setVisibility(INVISIBLE);
                        matrixImg.setVisibility(INVISIBLE);
                    }
                }
            };
        RxLoopSchedulers.composeLoop(mBv, 0, 1000, new Function() {
            @Override
            public Integer apply(Object o) throws Exception {
                time++;
                return time;
            }
        }).subscribe(observable);
    }

    /**
     * 通过layout方法，移动view
     * 优点：对view所在的布局，要求不苛刻，不要是RelativeLayout，而且可以修改view的大小
     *
     * @param view
     * @param rawX
     * @param rawY
     */
    private int maxX = 1920, minX = 0;
    private int maxY = 1080, minY = 0;

    public void moveViewByLayout(View view, int rawX, int rawY) {
        //记录左上点的位置
        int viewW = view.getWidth();
        int viewH = view.getHeight();
        int l = rawX - viewW / 2;
        int t = rawY - viewH / 2;

        //检测左右边界
        if (l < minX) {
            l = minX;
        } else if (l > maxX - viewW) {
            l = maxX - viewW;
        }

        //检测上下边界
        if (t < minY) {
            t = minY;
        } else if (t > maxY - viewH) {
            t = maxY - viewH;
        }

        //记录右下点的位置
        int r = l + viewW;
        int b = t + viewH;
        LogUtils.d("当前==" + rawX + "||" + (lastX - viewW / 4) + "||" + (lastX + viewW / 4)
                + "||" + rawY + "||" + (lastY - viewH / 4) + "||" + (lastY + viewH / 4));
        if (spotMeteringImg.getVisibility() == VISIBLE && lastX > 0 && lastY > 0
                && (rawX > lastX - viewW / 2)
                && (rawX < lastX + viewW / 2)
                && (rawY > lastY - viewH / 2)
                && (rawY < lastY + viewH / 2)) {
            time = 0;
            matrixImg.setVisibility(VISIBLE);
            if (onTouchCallback != null) {
                onTouchCallback.spotMetering(false, 0, 0);
//                onShowLinstener.sendSpotMeter((int) (((float) x / (float) screenWidth) * 9), (int) (((float) y / (float) screenHight) * 5));
            }
        } else {
            //记录最后的X,Y坐标
            view.layout(l, t, r, b);
            if (Utils.isDebug)
                lineImg.layout(l + (viewW / 2) - 2, 0, l + (viewW / 2), ScreenUtils.getScreenHeight());
            lastX = rawX;
            lastY = rawY;
            if (onTouchCallback != null) {
                onTouchCallback.spotMetering(true, (int) (((float) (l + viewW / 2 - minX) / (float) (maxX - minX)) * meteringwith),
                        (int) (((float) (t + viewH / 2 - minY) / (float) (maxY - minY)) * meteringHeight));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void showSpotMetering(boolean show) {
        spotMeteringImg.setVisibility(show ? VISIBLE : INVISIBLE);
    }

    public void limitScope(int width, int height) {
        //高度满屏为720尺寸，明显有缩放
        int parentWidth = this.getWidth();
        int parentHeight = this.getHeight();
        float scale = (float) height / (float) parentHeight;
        width = (int) ((float) width / scale);
        minX = (parentWidth - width) / 2;
        maxX = (parentWidth - width) / 2 + width;
        maxY = parentHeight;
        LogUtils.d("限定X,Y===>" + minX + "||" + maxX + "||" + minY + "||" + maxY);
    }

    public interface OnTouchCallback {
        void spotMetering(boolean isSpotOpen, int x, int y);
    }


}
