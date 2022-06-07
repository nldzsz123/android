package com.feipai.flypai.ui.view.Camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.TextureMapView;

/**
 * Created by YangLin on 2018-02-01.
 */

public class GaoDeMapView extends TextureMapView {
    private Context context;
    private boolean isDispatch;

    public GaoDeMapView(Context context) {
        super(context);
    }

    public GaoDeMapView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public GaoDeMapView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    public GaoDeMapView(Context context, AMapOptions aMapOptions) {
        super(context, aMapOptions);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        //view加载完成时回调
        this.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ViewGroup child = (ViewGroup) getChildAt(0);//地图框架
                        // child.getChildAt(0).setVisibility(View.VISIBLE);//地图
                        if (child != null) {
                            if (child.getChildAt(2) != null)
                                child.getChildAt(2).setVisibility(View.GONE);//logo
                            if (child.getChildAt(5) != null)
                                child.getChildAt(5).setVisibility(View.GONE);//缩放按钮
                            if (child.getChildAt(6) != null)
                                child.getChildAt(6).setVisibility(View.GONE);//定位按钮
//                         child.getChildAt(7).setVisibility(View.VISIBLE);//指南针
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