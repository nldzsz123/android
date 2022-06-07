package com.dou361.ijkplayer.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;

/**
 * 动画工具类
 */
public class AnimationUtils {

    private AnimationUtils() {
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
        Animation animation = android.view.animation.AnimationUtils.loadAnimation(context, animId);
        view.startAnimation(animation);
    }

    /**
     * 执行显示View的动画
     *
     * @param context 上下文
     * @param view    需要执行动画的View
     * @param animId  动画资源ID
     */
    public static void performShowViewAnim(Context context, final View view, int animId) {
        Animation animation = android.view.animation.AnimationUtils.loadAnimation(context, animId);
        view.setVisibility(View.VISIBLE);
        view.startAnimation(animation);
    }

    /**
     * 执行隐藏View的动画
     *
     * @param context 上下文
     * @param view    需要执行动画的View
     * @param animId  动画资源ID
     */
    public static void performHideViewAnim(Context context, final View view, int animId) {
        Animation animation = android.view.animation.AnimationUtils.loadAnimation(context, animId);
        view.setVisibility(View.GONE);
        view.startAnimation(animation);
    }

    /**
     * 执行显示View的动画
     */
    public static void performShowViewAnim(Context context, final View view, int animId, final SimpleAnimationListener listener) {
        Animation animation = android.view.animation.AnimationUtils.loadAnimation(context, animId);
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

    /**
     * 执行隐藏View的动画
     */
    public static void performHideViewAnim(Context context, final View view, int animId, final SimpleAnimationListener listener) {
        Animation animation = android.view.animation.AnimationUtils.loadAnimation(context, animId);
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
