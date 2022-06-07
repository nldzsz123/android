package com.feipai.flypai.base.basedialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.feipai.flypai.R;
import com.zhy.autolayout.utils.AutoUtils;

public class AlertController {
    private BaseDialog mDialog;
    private Window mWindow;

    private DialogViewHelper mHelper;

    public void setHelper(DialogViewHelper mHelper) {
        this.mHelper = mHelper;
    }

    public DialogViewHelper getHelper() {
        return mHelper;
    }

    public AlertController(BaseDialog dialog, Window window) {
        this.mDialog = dialog;
        this.mWindow = window;
    }

    public BaseDialog getDialog() {
        return mDialog;
    }

    public Window getWindow() {
        return mWindow;
    }


    public static class AlertParams {

        public Context mContext;

        public int mThemeResId;
        public boolean mCancelable = true;
        public DialogInterface.OnCancelListener mOnCanceListener;
        public DialogInterface.OnDismissListener mOnDismissListener;
        public DialogInterface.OnKeyListener mOnKeyListener;
        public View mView;
        public int mViewLayoutResId;
        //存放文字
        public SparseArray<CharSequence> mTextArray = new SparseArray<>();
        //存放点击事件
        public SparseArray<View.OnClickListener> mClickArray = new SparseArray<>();
        public int mWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
        public int mHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        public int mX = 0;
        public int mY = 0;
        public boolean isNotTouchModal;//设置是否拦截window之后的touch事件

        public int mAnimation = 0;
        public int mGravity = Gravity.CENTER;
        public int mRoundConer = 0;


        public AlertParams(Context context, int themeResId) {
            this.mContext = context;
            this.mThemeResId = themeResId;
        }

        /**
         * 绑定和设置参数
         *
         * @param mAlert
         */
        public void apply(AlertController mAlert) {
            //1.设置布局
            DialogViewHelper helper = null;
            if (mViewLayoutResId != 0) {
                helper = new DialogViewHelper(mContext, mViewLayoutResId);
            }
            if (mView != null) {
                helper = new DialogViewHelper();
                helper.setContentView(mView);
            }
            if (helper == null) {
                throw new IllegalArgumentException("请设置布局setContentView()");
            }

            //给dialog设置布局
            View view = helper.getContentView();
            AutoUtils.auto(view);
            mAlert.getDialog().setContentView(view);

            //设置Controller辅助类
            mAlert.setHelper(helper);
            // 2.设置文本
            int textArraySize = mTextArray.size();
            for (int i = 0; i < textArraySize; i++) {
                helper.setText(mTextArray.keyAt(i), mTextArray.valueAt(i));
            }
            //3.设置点击事件
            int clickArraySize = mClickArray.size();
            for (int i = 0; i < clickArraySize; i++) {
                helper.setOnclickListener(mClickArray.keyAt(i), mClickArray.valueAt(i));
            }
            //4.配置宽高，底部弹出，全屏，默认动画等等
            Window window = mAlert.getWindow();
            window.setGravity(mGravity);
            if (mRoundConer != 0) {
                GradientDrawable drawable = new GradientDrawable();
                //设置圆角大小
                drawable.setCornerRadius(mRoundConer);
                window.setBackgroundDrawable(drawable);
            }
            if (mAnimation != 0) {
                window.setWindowAnimations(mAnimation);
            }
            if (isNotTouchModal) {
                window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            }
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = mWidth;
            params.height = mHeight;
            if (mGravity != Gravity.CENTER) {
                //非居中显示,设置具体的X,Y;
                params.x = mX;
                params.y = mY;
            }
            window.setAttributes(params);

        }
    }
}