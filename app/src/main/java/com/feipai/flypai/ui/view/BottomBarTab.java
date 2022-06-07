package com.feipai.flypai.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipai.flypai.R;


/**
 * Created by YoKeyword on 16/6/3.
 */
public class BottomBarTab extends FrameLayout {
    private ImageView mIcon;
    private TextView mTvTitle;
    private Context mContext;
    private int mTabPosition = -1;

    private View mRedDotView;
    private int defIcon;
    private int seleIcon;

    public BottomBarTab(Context context, @DrawableRes int defIcon, int seleIcon, CharSequence title) {
        this(context, null, defIcon, seleIcon, title);
    }

    public BottomBarTab(Context context, AttributeSet attrs, int defIcon, int seleIcon, CharSequence title) {
        this(context, attrs, 0, defIcon, seleIcon, title);
    }

    public BottomBarTab(Context context, AttributeSet attrs, int defStyleAttr, int defIcon, int seleIconn, CharSequence title) {
        super(context, attrs, defStyleAttr);
        init(context, defIcon, seleIconn, title);
    }

    private void init(Context context, int defIcon, int seleIcon, CharSequence title) {
        mContext = context;
        this.defIcon = defIcon;
        this.seleIcon = seleIcon;

        TypedArray typedArray = context.obtainStyledAttributes(new int[]{R.attr.selectableItemBackgroundBorderless});
        Drawable drawable = typedArray.getDrawable(0);
        setBackgroundDrawable(drawable);
        typedArray.recycle();

        LinearLayout lLContainer = new LinearLayout(context);
        lLContainer.setOrientation(LinearLayout.VERTICAL);
        lLContainer.setGravity(Gravity.CENTER);
        LayoutParams paramsContainer = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsContainer.gravity = Gravity.CENTER;
        lLContainer.setLayoutParams(paramsContainer);

        mIcon = new ImageView(context);
        int size = dip2px(mContext, 32);
        //(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        mIcon.setImageResource(defIcon);
        mIcon.setLayoutParams(params);
//        mIcon.setColorFilter(ContextCompat.getColor(context, R.color.color_000000));
        lLContainer.addView(mIcon);

        mTvTitle = new TextView(context);
        mTvTitle.setText(title);
        LinearLayout.LayoutParams paramsTv = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsTv.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        mTvTitle.setTextSize(11);
        mTvTitle.setTextColor(ContextCompat.getColor(context, R.color.color_333333));
        mTvTitle.setLayoutParams(paramsTv);
        lLContainer.addView(mTvTitle);

        addView(lLContainer);

        int redDotSize = dip2px(mContext, 6);
        mRedDotView = new TextView(context);
        mRedDotView.setBackgroundResource(R.drawable.red_top_hint_img);
        FrameLayout.LayoutParams redDotParam = new FrameLayout.LayoutParams(redDotSize, redDotSize);
        redDotParam.leftMargin = dip2px(mContext, 76);
        redDotParam.bottomMargin = dip2px(mContext, 42);
        mRedDotView.setLayoutParams(redDotParam);
        setRedDotViewVis(GONE);

        addView(mRedDotView);
    }


    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
//            mIcon.setSelected(true);
            mIcon.setImageResource(seleIcon);
//            mIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
            mTvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.color_4097e1));
        } else {
            mIcon.setImageResource(defIcon);
//            mIcon.setSelected(false);
//            mIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.color_000000));
            mTvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.color_333333));
        }
    }


    public void setTabPosition(int position) {
        mTabPosition = position;
        if (position == 0) {
            setSelected(true);
        }
    }

    public void setRedDotViewVis(int visibility) {
        mRedDotView.setVisibility(visibility);
    }


    public int getTabPosition() {
        return mTabPosition;
    }


    private int dip2px(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
