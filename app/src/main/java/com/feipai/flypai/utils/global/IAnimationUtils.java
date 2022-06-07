package com.feipai.flypai.utils.global;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * 动画工具类
 */
public class IAnimationUtils {

    private IAnimationUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * View执行一个动画
     *
     * @param context 上下文
     * @param view    需要执行动画的View
     * @param animId  动画资源ID
     */
    public static void performAnim(Context context, final View view, int animId) {
        if (view != null) {
            Animation animation = AnimationUtils.loadAnimation(context, animId);
            view.startAnimation(animation);
        }
    }

    /**
     * 执行显示View的动画
     *
     * @param context 上下文
     * @param view    需要执行动画的View
     * @param animId  动画资源ID
     */
    public static void performShowViewAnim(Context context, final View view, int animId) {
        if (view != null && view.getVisibility() != View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(context, animId);
            view.setVisibility(View.VISIBLE);
            view.startAnimation(animation);
        }
    }

    /**
     * 执行隐藏View的动画
     *
     * @param context 上下文
     * @param view    需要执行动画的View
     * @param animId  动画资源ID
     */
    public static void performHideViewAnim(Context context, final View view, int animId) {
        if (view != null && view.getVisibility() == View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(context, animId);
            view.setVisibility(View.GONE);
            view.startAnimation(animation);
        }
    }

    /**
     * 执行隐藏View的动画
     *
     * @param context 上下文
     * @param view    需要执行动画的View
     * @param animId  动画资源ID
     */
    public static void performHideViewAnim(Context context, final View view, int animId, boolean isGone) {
        if (view != null && view.getVisibility() == View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(context, animId);
            view.setVisibility(isGone ? View.GONE : View.INVISIBLE);
            view.startAnimation(animation);
        }
    }

    /**
     * 执行隐藏View的动画
     *
     * @param context 上下文
     * @param view    需要执行动画的View
     * @param animId  动画资源ID
     */
    public static void performHideViewAnim(Context context, final View view, int animId, int visible) {
        if (view != null && view.getVisibility() == View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(context, animId);
            view.setVisibility(visible);
            view.startAnimation(animation);
        }
    }

    /**
     * 执行显示View的动画
     */
    public static void performShowViewAnim(Context context, final View view, int animId, final SimpleAnimationListener listener) {
        if (view != null && view.getVisibility() != View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(context, animId);
            view.setVisibility(View.VISIBLE);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    if (listener != null) {
                        listener.onAnimationStart(animation);
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (listener != null) {
                        listener.onAnimationEnd(animation);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    if (listener != null) {
                        listener.onAnimationRepeat(animation);
                    }
                }
            });
            view.startAnimation(animation);
        }
    }

    /**
     * 执行隐藏View的动画
     */
    public static void performHideViewAnim(Context context, final View view, int animId, final SimpleAnimationListener listener) {
        if (view != null && view.getVisibility() == View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(context, animId);
            view.setVisibility(View.GONE);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    if (listener != null) {
                        listener.onAnimationStart(animation);
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (listener != null) {
                        listener.onAnimationEnd(animation);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    if (listener != null) {
                        listener.onAnimationRepeat(animation);
                    }
                }
            });
            view.startAnimation(animation);
        }
    }

    /**
     * 简单动画监听
     */
    public static class SimpleAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

}
