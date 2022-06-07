package com.feipai.flypai.ui.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.feipai.flypai.utils.global.IAnimationUtils;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.zhy.autolayout.AutoLinearLayout;
import com.zhy.autolayout.AutoRelativeLayout;
import com.zhy.autolayout.utils.AutoUtils;

/**
 * 凸起效果的tablayout
 */
public class HumpTab extends AutoRelativeLayout {
    private View mIcon;
    private TextView mTvTitle;
    private LinearLayout mLinearLy;
    private Context mContext;
    private int mTabPosition = -1;

    private int defIcon;
    private int seleIcon;
    private CharSequence text;

//    private ViewGroup.LayoutParams mParams;

    RelativeLayout lLContainer;
    LayoutParams paramsContainer;

    LayoutParams paramsLinly;

    public HumpTab(Context context, @DrawableRes int defIcon, int seleIcon, CharSequence title) {
        this(context, null, defIcon, seleIcon, title);
    }

    public HumpTab(Context context, AttributeSet attrs, int defIcon, int seleIcon, CharSequence title) {
        this(context, attrs, 0, defIcon, seleIcon, title);
    }

    public HumpTab(Context context, AttributeSet attrs, int defStyleAttr, int defIcon, int seleIconn, CharSequence title) {
        super(context, attrs, defStyleAttr);
        init(context, defIcon, seleIconn, title);
    }

    private void init(Context context, int defIcon, int seleIcon, CharSequence title) {
        mContext = context;
        this.defIcon = defIcon;
        this.seleIcon = seleIcon;
        this.text = title;
        TypedArray typedArray = context.obtainStyledAttributes(new int[]{R.attr.selectableItemBackgroundBorderless});
        Drawable drawable = typedArray.getDrawable(0);
        setBackgroundDrawable(drawable);
        typedArray.recycle();

        lLContainer = new RelativeLayout(context);

        paramsContainer = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        paramsContainer.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lLContainer.setLayoutParams(paramsContainer);
        lLContainer.setBackgroundColor(ResourceUtils.getColor(R.color.color_transparent));

        mLinearLy = new LinearLayout(context);
        mLinearLy.setOrientation(LinearLayout.VERTICAL);
        mLinearLy.setGravity(Gravity.CENTER);
        paramsLinly = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AutoUtils.getPercentHeightSize(80));
        paramsLinly.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mLinearLy.setLayoutParams(paramsLinly);
        mLinearLy.setBackgroundResource(R.drawable.humptab_bg);


        mTvTitle = new TextView(context);
        mTvTitle.setText(text);
        LinearLayout.LayoutParams paramsTv = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mTvTitle.setTextSize(13);
        AutoUtils.autoTextSize(mTvTitle);
        mTvTitle.setTextColor(ContextCompat.getColor(context, R.color.color_333333));
        mTvTitle.setLayoutParams(paramsTv);
        mLinearLy.addView(mTvTitle);


        mIcon = new View(context);
        LayoutParams params = new LayoutParams(AutoUtils.getPercentWidthSize(80), AutoUtils.getPercentHeightSize(5));
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mIcon.setBackgroundColor(mContext.getResources().getColor(defIcon));
        mIcon.setLayoutParams(params);

        lLContainer.addView(mLinearLy);
        lLContainer.addView(mIcon);

        addView(lLContainer);

    }


    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            new ViewSizeChangeAnimation(mLinearLy, lLContainer.getHeight(), 0).startAnimation(300);
            mIcon.setBackgroundColor(ContextCompat.getColor(mContext, seleIcon));
            mTvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.color_4097e1));
        } else {
            new ViewSizeChangeAnimation(mLinearLy, AutoUtils.getPercentHeightSize(80), 0).startAnimation(300);
            mIcon.setBackgroundColor(ContextCompat.getColor(mContext, defIcon));
            mTvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.color_333333));
        }
        requestLayout();
    }


    public void setTabPosition(int position) {
        mTabPosition = position;
        Log.d("yanglin", "选中的--->" + position);
        if (position == 0) {
            setSelected(true);
        }
    }


    public int getTabPosition() {
        return mTabPosition;
    }
}
