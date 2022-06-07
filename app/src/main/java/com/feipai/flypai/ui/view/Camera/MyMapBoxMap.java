package com.feipai.flypai.ui.view.Camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;

public class MyMapBoxMap extends MapView {
    private Context context;
    private boolean isDispatch;

    public MyMapBoxMap(Context context) {
        super(context);
    }

    public MyMapBoxMap(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public MyMapBoxMap(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    public MyMapBoxMap(Context context, MapboxMapOptions options) {
        super(context, options);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        //view加载完成时回调
        this.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        View compassView = getChildAt(1);//指南针
                        if (compassView != null) {
                            compassView.setVisibility(View.GONE);
                        }
                        View logoView = getChildAt(2);//logo
                        if (logoView != null) {
                            logoView.setVisibility(View.GONE);
                        }
                        View view = getChildAt(3);//logo旁边感叹号
                        if (view != null) {
                            view.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isDispatch) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setDispatchTouch(boolean dispatch) {
        this.isDispatch = dispatch;
    }

    public boolean isDispatchTouch() {
        return isDispatch;
    }
}
