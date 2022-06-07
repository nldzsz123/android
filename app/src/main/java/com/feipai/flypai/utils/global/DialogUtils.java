package com.feipai.flypai.utils.global;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

/**
 * Dialog相关工具类
 */
public class DialogUtils {

    private DialogUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 设置Dialog的宽度跟屏幕的宽度一样
     *
     * @param dialog 目标Dialog
     * @param alpha  Dialog的透明度
     */
    public static void setDialogSameWidth(Dialog dialog, float alpha) {
        Window window = dialog.getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.FILL_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.alpha = alpha;
        window.setAttributes(params);
    }

    /**
     * 设置Dialog的宽度
     *
     * @param context      上下文
     * @param dialog       目标Dialog
     * @param widthPercent 和屏幕宽度的百分比
     * @param alpha        Dialog的透明度
     */
    public static void setDialogWidthSize(Context context, Dialog dialog, float widthPercent, float alpha) {
        // 设置dialog的大小
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        params.width = (int) (displayMetrics.widthPixels * widthPercent);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.alpha = alpha;
        // 设置Dialog的宽度
        dialog.getWindow().setAttributes(params);
    }

    /**
     * 设置Dialog的宽度
     *
     * @param context       上下文
     * @param dialog        目标Dialog
     * @param widthPercent  和屏幕宽度的百分比
     * @param heightPercent 和屏幕高度的百分比
     * @param alpha         Dialog的透明度
     */
    public static void setDialogWidthHeight(Context context, Dialog dialog, float widthPercent, float heightPercent, float alpha) {
        // 设置dialog的大小
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        params.width = (int) (displayMetrics.widthPixels * widthPercent);
        params.height = (int) (displayMetrics.heightPixels * heightPercent);
        params.alpha = alpha;
        // 设置Dialog的宽度
        dialog.getWindow().setAttributes(params);
    }

    /**
     * 设置Dialog的宽度、高度
     *
     * @param context 上下文
     * @param dialog  目标Dialog
     * @param width   宽度
     * @param height  高度
     * @param alpha   Dialog的透明度
     */
    public static void setDialogWidthSize(Context context, Dialog dialog, int width, int height, float alpha) {
        // 设置dialog的大小
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        params.width = width;
        params.height = height;
        params.alpha = alpha;
        // 设置Dialog的宽度
        dialog.getWindow().setAttributes(params);
    }

}
