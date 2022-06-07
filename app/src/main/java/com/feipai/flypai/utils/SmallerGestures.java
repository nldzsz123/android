package com.feipai.flypai.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.constraint.ConstraintSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.feipai.flypai.ui.view.Camera.MapViewLayout;
import com.feipai.flypai.ui.view.Camera.VideoView;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.ScreenUtils;
import com.feipai.flypai.utils.global.SizeUtils;

/**
 * 手势监听
 * <p>
 * 执行地图和视频播放切换的位置调换，通过动画改变布局
 */
public class SmallerGestures extends GestureDetector.SimpleOnGestureListener {

    private MapViewLayout mMap;
    private VideoView mVideo;

    private GestureCallback mCallback;

    private boolean animationFinish;
    private int orgLeft;
    private int orgtop;
    private int orgbot;
    private int orgrit;
    private int leftbottomId;
    private int fullScreenid;
    int scrrenwidth;
    int scrrenheight;

    ValueAnimator valueAnimator;

    @Override

    public boolean onSingleTapUp(MotionEvent e) {

        MLog.log("单击了左下角窗口");
        animationFinish = false;

        if (mCallback != null) mCallback.setParentChildViewVisible(View.GONE);

        orgLeft = mMap.getLeft();
        orgtop = mMap.getTop();
        orgbot = mMap.getBottom();
        orgrit = mMap.getRight();
        leftbottomId = mMap.getId();
        fullScreenid = mVideo.getId();
        if (mMap.isSmall()) {        // 图传将切换到左下角
            leftbottomId = mVideo.getId();
            fullScreenid = mMap.getId();
        } else {
            orgLeft = mVideo.getLeft();
            orgtop = mVideo.getTop();
            orgbot = mVideo.getBottom();
            orgrit = mVideo.getRight();
        }
        if (valueAnimator != null) {
            valueAnimator.start();
        }
        return true;
    }

    public void bindView(Context context, MapViewLayout one, VideoView two, GestureCallback callback) {
        this.mCallback = callback;
        mMap = one;
        mMap.addTapGesture(context, this);
        mVideo = two;
        initData(context, this);
    }

    private void initData(Context context, SmallerGestures tapSmallGesture) {
        mVideo.addTapGesture(context, tapSmallGesture);
        scrrenwidth = ScreenUtils.getScreenWidth();
        scrrenheight = ScreenUtils.getScreenHeight();
        valueAnimator = ValueAnimator.ofFloat(0, 100);
        valueAnimator.setTarget(mMap);
        valueAnimator.setDuration(500);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();

                int newLeft = (int) (orgLeft * ((100.0 - value) / 100.0f));
                int newtop = (int) ((orgtop) * ((100.0 - value) / 100.0f));
                int newrigt = (int) ((scrrenwidth - orgrit) * ((100.0 - value) / 100.0f));
                int newbot = (int) ((scrrenheight - orgbot) * ((100.0 - value) / 100.0f));

                // 左下角窗口放到最大
                ConstraintSet set = new ConstraintSet();
                set.connect(fullScreenid, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, newLeft);
                set.connect(fullScreenid, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, newrigt);
                set.connect(fullScreenid, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, newtop);
                set.connect(fullScreenid, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, newbot);
//                LogUtils.d("这两个ID===>" + fullScreenid + "||" + leftbottomId + "||" + mMap.getId() + "||" + mVideo.getId());
                if (mCallback != null) mCallback.applyToParent(fullScreenid == mMap.getId(), set);

                if (value >= 100 && !animationFinish) {
                    animationFinish = true;
//                    MLog.log("动画结束");

                    set.connect(leftbottomId, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
                    set.connect(leftbottomId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);
                    set.constrainWidth(leftbottomId, SizeUtils.dp2px(136));
                    set.constrainHeight(leftbottomId, SizeUtils.dp2px(75));
                    set.setMargin(leftbottomId, ConstraintSet.START, SizeUtils.dp2px(11));
                    set.setMargin(leftbottomId, ConstraintSet.BOTTOM, SizeUtils.dp2px(6));
                    if (mCallback != null)
                        mCallback.applyToParent(leftbottomId == mVideo.getId(), set);

                    // 图传出在左下角小窗口
                    if (mMap.isSmall()) {
                        mVideo.bringToFront();
                        mMap.setSmall(false);
                        mVideo.setSmall(true);

                        // 移除地图的手势
                        mMap.removeTapGesture();
                        // 添加图传窗口的手势
//                        mVideo.addTapGesture(context, tapSmallGesture);
                    } else {
                        mMap.bringToFront();
                        mMap.setSmall(true);
                        mVideo.setSmall(false);

                        // 移除图传的手势
//                        mVideo.removeTapGesture();
                        // 添加地图窗口的手势
                        mMap.addTapGesture(context, tapSmallGesture);
                    }

                    if (mCallback != null) mCallback.setParentChildViewVisible(View.VISIBLE);
                }
            }
        });
    }

    /**
     * 操作回调可以自行添加
     */
    public interface GestureCallback {
        /**
         * @param mapTofull 地图变大
         */
        void applyToParent(boolean mapTofull, ConstraintSet set);

        void setParentChildViewVisible(int visibility);
    }

}
