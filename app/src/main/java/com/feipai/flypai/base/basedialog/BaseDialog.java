package com.feipai.flypai.base.basedialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.StyleRes;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.feipai.flypai.R;
import com.feipai.flypai.utils.global.LogUtils;

public class BaseDialog extends Dialog {

    protected AlertController mAlert;

    public BaseDialog(Context context) {
        super(context);
        init();
    }

    public BaseDialog(Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    public void init() {
        mAlert = new AlertController(this, getWindow());
    }

    public DialogViewHelper getDgHelper() {
        return mAlert.getHelper();
    }

    /**
     * 设置点击监听
     *
     * @param viewId
     * @param listener
     */
    public void setOnclickListener(int viewId, View.OnClickListener listener) {
        mAlert.getHelper().setOnclickListener(viewId, listener);
    }

    /**
     * 设置文字
     *
     * @param viewId
     * @param text
     */
    public void setText(int viewId, CharSequence text) {
        mAlert.getHelper().setText(viewId, text);
    }

    /**
     * 设置文字大小
     *
     * @param viewId
     * @param size
     */
    public void setTextSize(int viewId, float size) {
        mAlert.getHelper().setTextSize(viewId, size);
    }

    /**
     * 设置文字颜色
     *
     * @param viewId
     * @param colorId
     */
    public void setTextColor(int viewId, int colorId) {
        mAlert.getHelper().setTextColor(viewId, colorId);
    }

    /**
     * 设置图片
     *
     * @param viewId
     * @param drawable
     */
    public void setImageResource(int viewId, int drawable) {
        mAlert.getHelper().setImageResource(viewId, drawable);
    }

    /**
     * 设置普通进度条
     *
     * @param viewId
     * @param progress
     */

    public void setProgress(int viewId, int progress) {
        mAlert.getHelper().setProgress(viewId, progress);
    }

    public int getProgress(int viewId) {
        return mAlert.getHelper().getProgress(viewId);
    }

    public void setProgressMax(int viewId, int max) {
        mAlert.getHelper().setProgressMax(viewId, max);
    }

    /**
     * 特殊:设置圆形进度
     *
     * @param viewId
     * @param progress
     */
    public void setCircleProgress(int viewId, int progress) {
        mAlert.getHelper().setCircleProgress(viewId, progress);
    }

    public void setProgressWithCenterText(int viewId, int progress, String centerText) {
        mAlert.getHelper().setProgressWithCenter(viewId, progress, centerText);
    }

    /**
     * 设置View隐藏与显示
     *
     * @param viewId
     * @param visibility
     */
    public void setViewVisibility(int viewId, int visibility) {
        mAlert.getHelper().setViewVisibility(viewId, visibility);
    }

    /**
     * 设置View是否可点击
     */
    public void setViewEnabled(int viewId, boolean isEnabled) {
        mAlert.getHelper().setViewEnabled(viewId, isEnabled);
    }

    /**
     * 设置view的透明度
     */
    public void setViewAlpha(int viewId, float alpha) {
        mAlert.getHelper().setViewAlpha(viewId, alpha);
    }

    public int getViewVisibility(int viewId) {
        return mAlert.getHelper().getViewVisibility(viewId);
    }

    /**
     * 根据id获取view
     *
     * @param viewId
     */
    public <T extends View> T getView(int viewId) {
        return (T) mAlert.getHelper().getView(viewId);
    }


    public static class Builder {

        private final AlertController.AlertParams p;


        public Builder(Context context) {
            this(context, R.style.dialog_ios_style);
        }

        public Builder(Context context, @StyleRes int themeResId) {
            p = new AlertController.AlertParams(context, themeResId);
        }

        /**
         * 组装参数
         *
         * @return
         */
        public BaseDialog create() {
            final BaseDialog dialog = new BaseDialog(p.mContext, p.mThemeResId);
            p.apply(dialog.mAlert);
            dialog.setCancelable(p.mCancelable);
            if (p.mCancelable) {
                dialog.setCanceledOnTouchOutside(true);
            }
            dialog.setOnCancelListener(p.mOnCanceListener);
            dialog.setOnDismissListener(p.mOnDismissListener);
            if (p.mOnKeyListener != null) {
                dialog.setOnKeyListener(p.mOnKeyListener);
            }
            return dialog;
        }

        /**
         * 设置布局
         *
         * @param view
         * @return
         */
        public Builder setContentView(View view) {
            p.mView = view;
            p.mViewLayoutResId = 0;
            return this;
        }

        /**
         * 设置布局
         *
         * @param layoutId
         * @return
         */
        public Builder setContentView(int layoutId) {
            p.mView = null;
            p.mViewLayoutResId = layoutId;
            return this;
        }

        /**
         * 设置布局是否允许点击其他位置dismiss
         *
         * @param cancelable
         * @return
         */
        public Builder setCancelable(boolean cancelable) {
            p.mCancelable = cancelable;
            return this;
        }


        /**
         * 设置文字
         *
         * @param text
         * @return
         */
        public Builder setText(int viewId, CharSequence text) {
            p.mTextArray.put(viewId, text);
            return this;
        }

        public Builder setViewV(int viewId, CharSequence text) {
            p.mTextArray.put(viewId, text);
            return this;
        }


        /**
         * 设置点击事件
         *
         * @param listener
         * @return
         */
        public Builder setOnClickListener(int viewId, View.OnClickListener listener) {
            p.mClickArray.put(viewId, listener);
            return this;
        }

        /**
         * 、
         * 全屏
         *
         * @return
         */
        public Builder fullWidth() {
            p.mWidth = ViewGroup.LayoutParams.MATCH_PARENT;
            return this;
        }

        /**
         * 底部弹出
         *
         * @param isAnimation
         * @return
         */
        public Builder fromBottom(boolean isAnimation) {
            if (isAnimation) {
                p.mAnimation = R.style.dialog_from_bottom_anim;
            }
            p.mGravity = Gravity.BOTTOM;
            return this;
        }

        /**
         * 设置宽度
         *
         * @param width
         * @return
         */
        public Builder setWidth(int width) {
            p.mWidth = width;
            return this;
        }

        /**
         * 设置高度
         *
         * @param height
         * @return
         */
        public Builder setHeight(int height) {
            p.mHeight = height;
            return this;
        }

        /**
         * 设置宽高
         *
         * @param width
         * @param height
         * @return
         */
        public Builder setWidthAndHeight(int width, int height) {
            p.mWidth = width;
            p.mHeight = height;
            return this;
        }

        public Builder setParamsXAndY(int gravity, int x, int y) {
            p.mGravity = gravity;
            p.mX = x;
            p.mY = y;
            return this;
        }

        public Builder setNotTouchModal(boolean isNotTouchModal) {
            p.isNotTouchModal = isNotTouchModal;
            return this;
        }


        public Builder addDefaultAnimation() {
            p.mAnimation = R.style.dialog_scale_anim;
            return this;
        }

        /**
         * 添加动画
         *
         * @param styleAnimation
         * @return
         */
        public Builder addAnimation(int styleAnimation) {
            p.mAnimation = styleAnimation;
            return this;
        }

        /**
         * 设置圆角
         *
         * @param
         * @return
         */
        public Builder setRoundCorner(int corner) {
            p.mRoundConer = corner;
            return this;
        }

        /**
         * 显示
         *
         * @return
         */
        public BaseDialog show() {
            BaseDialog dialog = create();

            dialog.show();
            return dialog;
        }
    }

}