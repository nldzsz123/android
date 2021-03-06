package com.feipai.flypai.ui.view.cardpageview;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;


import com.zhy.autolayout.utils.AutoUtils;

import java.util.Set;
import java.util.TreeSet;


/**
 * <p>卡片Transformer</p>
 * <p>card Transformer</p>
 */
public class CardPageTransformer implements ViewPager.PageTransformer {
    private Build mBuild;
    private String TAG = getClass().getSimpleName();


    private CardPageTransformer(Build build) {
        mBuild = build;
    }

    public static Build getBuild() {
        return new Build();
    }

    @SuppressLint("NewApi")
    public void transformPage(View page, float position) {
        //判断方向  Judge the direction
        if (mBuild.mOrientation == PageTransformerConfig.HORIZONTAL) {
            //水平方向  Horizontal
            transformHorizontal(page, position);
        } else {
//            throw new RuntimeException("now only support horizontal");
            transformHorizontal(page, position);
        }


    }

    /**
     * <p>水平方向</p>
     * <p>Horizontal</p>
     *
     * @param page
     * @param position
     */
    private void transformHorizontal(View page, float position) {
        if (position <= 0.0f) {//被滑动的那页  The page that was sliding
            page.setTranslationX(0f);
            Log.e(TAG, "transformHorizontal: " + position);

            //-----------------------动画 animation start

            // 动画类型不为none才执行动画  Animation type is not 'none'
            if (!mBuild.mAnimationType.contains(PageTransformerConfig.NONE)) {
                //旋转 //Rotation
                if (mBuild.mAnimationType.contains(PageTransformerConfig.ROTATION)) {
                    //旋转角度  Rotation angle   -45° * 0.1 = -4.5°
                    float targetRotation = mBuild.mRotation * Math.abs(position);
                    // 判断当前的阀值是否到达了指定位置
                    if (Math.abs(targetRotation) > Math.abs(mBuild.mRotation) * mBuild.mOverloadRate) {
                        targetRotation = mBuild.mRotation;
                    }
                    page.setRotation(targetRotation);
                    //X轴偏移 xAxis offset li:  300/3 * -0.1 = -10
                    page.setTranslationX((page.getWidth() / 3f * position));
                }

                //透明度 alpha
                if (mBuild.mAnimationType.contains(PageTransformerConfig.ALPHA)) {

                    //设置透明度  set alpha
                    float targetAlpha = mBuild.mAlpha - mBuild.mAlpha * Math.abs(position);
                    // 增加计算一个容错率，
                    if (targetAlpha > -mBuild.mOverloadRate) mBuild.mAlpha = 1.0f;
                    page.setAlpha(targetAlpha);
                }
            }
            //-----------------------动画 animation end

//            int childCount = mBuild.mViewPager.getChildCount();


            //回调自定义动画 callback customize animation
            if (mBuild.onPageTransformerListener != null) {
                //回调
                mBuild.onPageTransformerListener.onPageTransformerListener(page, position);
            }

            //打开点击事件
            page.setClickable(true);

        } else if (position <= mBuild.mMaxShowPage || mBuild.mViewPager == null) {    //只显示3张卡片

            setHorizontalTransformPager(page, position);
            //屏蔽点击事件
            page.setClickable(false);
        } else {
            page.setTranslationX(0f);
            page.setTranslationY(3f);
        }
    }

    /**
     * 设置禁止页面的样式
     * <p>set view type，set card style</p>
     *
     * @param page
     * @param position
     */
    private void setHorizontalTransformPager(View page, float position) {
        //缩放比例
        float xScale = (page.getWidth() - mBuild.mXScaleOffset * position) / (float) (page.getWidth());
        float yScale = (page.getHeight() - mBuild.mYScaleOffset * position) / (float) (page.getHeight());

        page.setScaleX(xScale);
        page.setScaleY(yScale);
        // why ? because the offset is to small,
        // 我感觉 它不够高，所以增加了比例
        float proportion = 1.5f;
        //开始判断类型 //type
        switch (mBuild.mViewType) {
            //底部 bottom
            case PageTransformerConfig.BOTTOM:
                page.setTranslationX((-page.getWidth() * position));
                page.setTranslationY((mBuild.mTranslationOffset * proportion) * position);
                break;
            //底左  bottom left
            case PageTransformerConfig.BOTTOM_LEFT:
                page.setTranslationX((-page.getWidth() * position) + ((mBuild.mTranslationOffset * proportion) * position));
                page.setTranslationY((mBuild.mTranslationOffset * proportion) * position);
                break;
            //底右  bottom right
            case PageTransformerConfig.BOTTOM_RIGHT:
                page.setTranslationX((-page.getWidth() * position) - ((mBuild.mTranslationOffset * proportion) * position));
                page.setTranslationY((mBuild.mTranslationOffset * proportion) * position);

                break;
            //右边  right
            case PageTransformerConfig.RIGHT:
                page.setTranslationX((-page.getWidth() * position) - ((mBuild.mTranslationOffset * proportion) * position));
                page.setTranslationY(0);
                break;
            //左边 left
            case PageTransformerConfig.LEFT:
                page.setTranslationX((-page.getWidth() * position) + ((mBuild.mTranslationOffset * proportion) * position));
                page.setTranslationY(0);
                break;
            //上面 top
            case PageTransformerConfig.TOP:
                page.setTranslationX((-page.getWidth() * position));
                page.setTranslationY(-((mBuild.mTranslationOffset * proportion) * position));
                break;
            //上左 top left
            case PageTransformerConfig.TOP_LEFT:
                page.setTranslationX((-page.getWidth() * position) + ((mBuild.mTranslationOffset * proportion) * position));
                page.setTranslationY(-((mBuild.mTranslationOffset * proportion) * position));
                break;
            //上右 top right
            case PageTransformerConfig.TOP_RIGHT:
                page.setTranslationX((-page.getWidth() * position) - ((mBuild.mTranslationOffset * proportion) * position));
                page.setTranslationY(-((mBuild.mTranslationOffset * proportion) * position));
                break;


        }


    }

    /**
     * 建造者模式
     */
    public static class Build {

        /**
         * x缩放偏移量
         */
        private int mXScaleOffset = 16;
        /**
         * y缩放偏移量
         */
        private int mYScaleOffset = 16;
        /**
         * 偏移量
         */
        private int mTranslationOffset = AutoUtils.getPercentHeightSize(12);
        /**
         * 旋转角度
         */
        private float mRotation = -45;
        /**
         * 透明度
         */
        private float mAlpha = 1f;
        /**
         * 视图类型
         */
        private int mViewType = PageTransformerConfig.BOTTOM;
        /**
         * 动画类型
         */
        private Set<Integer> mAnimationType = new TreeSet<>();
        /**
         * 方向
         */
        private int mOrientation = PageTransformerConfig.HORIZONTAL;
        /**
         * 默认显示的页数
         */
        private int mMaxShowPage = 5;
        /**
         * 容错率，为了解决快速滑动的情况下会有折叠的问题
         */
        private float mOverloadRate = 0.75f;
        /**
         * ViewPager
         */
        private ViewPager mViewPager;


        private CardPageTransformerListener onPageTransformerListener;

        public int getOrientation() {
            return mOrientation;
        }

        public Build setOrientation(@PageTransformerConfig.Orientation int mOrientation) {
            this.mOrientation = mOrientation;
            return this;
        }

        public int getMaxShowPage() {
            return mMaxShowPage;
        }


        public ViewPager getViewPager() {
            return mViewPager;
        }

        public int getYScaleOffset() {
            return mYScaleOffset;
        }

        public Build setYScaleOffset(int mScaleOffset) {
            this.mYScaleOffset = mScaleOffset;
            return this;
        }

        public int getXScaleOffset() {
            return mXScaleOffset;
        }

        public Build setXScaleOffset(int mScaleOffset) {
            this.mXScaleOffset = mXScaleOffset;
            return this;
        }

        public int getTranslationOffset() {
            return mTranslationOffset;
        }

        public Build setTranslationOffset(int mTranslationOffset) {
            this.mTranslationOffset = AutoUtils.getPercentHeightSize(mTranslationOffset);
            return this;
        }

        public int getViewType() {
            return mViewType;
        }


        public float getOverloadRate() {
            return mOverloadRate;
        }

        public void setOverloadRate(float overloadRate) {
            this.mOverloadRate = overloadRate;
        }

        /**
         * 设置View的类型 或者说 样式
         *
         * @param mViewType
         * @return
         */
        public Build setViewType(@PageTransformerConfig.ViewType int mViewType) {
            this.mViewType = mViewType;
            return this;
        }

        public Set<Integer> getAnimationType() {
            return mAnimationType;
        }

        /**
         * 增加 动画类型
         *
         * @param mAnimationType
         * @return
         */
        public Build addAnimationType(@PageTransformerConfig.AnimationType int... mAnimationType) {
            for (int type : mAnimationType) {
                this.mAnimationType.add(type);
            }
            return this;
        }


        /**
         * 完成创建
         *
         * @return
         */
        public ViewPager.PageTransformer create() {
            return new CardPageTransformer(this);
        }

        /**
         * 完成创建
         *
         * @return
         */
        public ViewPager.PageTransformer create(ViewPager viewPager) {
            this.mViewPager = viewPager;
            this.mMaxShowPage = viewPager.getOffscreenPageLimit() - 1;
            return new CardPageTransformer(this);
        }

        public float getRotation() {
            return mRotation;
        }

        public float getAlpha() {
            return mAlpha;
        }

        public Build setAlpha(float mAlpha) {
            this.mAlpha = mAlpha;
            return this;
        }

        public Build setRotation(float mRotation) {
            this.mRotation = mRotation;
            return this;
        }

        public Build setOnPageTransformerListener(@NonNull CardPageTransformerListener onPageTransformerListener) {
            this.onPageTransformerListener = onPageTransformerListener;
            return this;
        }
    }
}