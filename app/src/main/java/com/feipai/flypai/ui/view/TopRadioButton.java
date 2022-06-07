package com.feipai.flypai.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.feipai.flypai.R;
import com.feipai.flypai.utils.global.LogUtils;
import com.zhy.autolayout.utils.AutoUtils;

public class TopRadioButton extends android.support.v7.widget.AppCompatRadioButton {

    private int mDrawableWidth;
    private int mDrawableHeight;
    private Drawable mDrawableTop, mDrawableBottom, mDrawableLeft, mDrawableRight;
    private int mDrawableMarginTop, mDrawableMarginBottom, mDrawableMarginLeft, mDrawableMarginRight;

    public TopRadioButton(Context context) {
        super(context);
    }

    public TopRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TopRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RadioButton);
        mDrawableWidth =  AutoUtils.getPercentWidthSize((int) ta.getDimension(R.styleable.RadioButton_drawable_width, 0));
        mDrawableHeight = (int) ta.getDimension(R.styleable.RadioButton_drawable_height, 0);
        mDrawableMarginTop = (int) ta.getDimension(R.styleable.RadioButton_drawable_marginTop, 0);
        mDrawableMarginBottom = (int) ta.getDimension(R.styleable.RadioButton_drawable_marginBottom, 0);
        mDrawableMarginRight = (int) ta.getDimension(R.styleable.RadioButton_drawable_marginRight, 0);
        mDrawableMarginLeft = (int) ta.getDimension(R.styleable.RadioButton_drawable_marginLeft, 0);
        setCompoundDrawablePadding(getCompoundDrawablePadding() + mDrawableMarginTop);
        LogUtils.v("TAG", getCompoundDrawables().length + "");

        mDrawableLeft = getCompoundDrawables()[0];
        mDrawableTop = getCompoundDrawables()[1];
        mDrawableRight = getCompoundDrawables()[2];
        mDrawableBottom = getCompoundDrawables()[3];

        if (mDrawableLeft != null & mDrawableWidth > 0 & mDrawableHeight > 0) {
            mDrawableLeft.setBounds(0, mDrawableMarginTop, mDrawableWidth, mDrawableHeight + mDrawableMarginTop);
            setCompoundDrawables(mDrawableLeft, null, null, null);
        }
        if (mDrawableTop != null & mDrawableWidth > 0 & mDrawableHeight > 0) {
            mDrawableTop.setBounds(0, mDrawableMarginTop, mDrawableWidth, mDrawableHeight + mDrawableMarginTop);
            setCompoundDrawables(null, mDrawableTop, null, null);
        }
        if (mDrawableRight != null & mDrawableWidth > 0 & mDrawableHeight > 0) {
            mDrawableRight.setBounds(0, mDrawableMarginTop, mDrawableWidth, mDrawableHeight + mDrawableMarginTop);
            setCompoundDrawables(null, null, mDrawableRight, null);
        }
        if (mDrawableBottom != null & mDrawableWidth > 0 & mDrawableHeight > 0) {
            mDrawableBottom.setBounds(0, mDrawableMarginTop, mDrawableWidth, mDrawableHeight + mDrawableMarginTop);
            setCompoundDrawables(null, null, null, mDrawableBottom);
        }
        ta.recycle();
    }
}
