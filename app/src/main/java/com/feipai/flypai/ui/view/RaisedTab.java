package com.feipai.flypai.ui.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipai.flypai.R;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.zhy.autolayout.AutoRelativeLayout;
import com.zhy.autolayout.utils.AutoUtils;

public class RaisedTab extends AutoRelativeLayout {


    private int mCurTabPosition = -1;
    private Context mContext;

    private TextView mTextview;

    private View mUnderLine;

    /**
     * 内部最小高度
     */
    private int mMinHeiht;

    /**
     * 最大容器
     */
    private AutoRelativeLayout mParentContainer;

    private LayoutParams mParentParams;


    private AutoRelativeLayout mChildContainer;

    private LayoutParams mChildContainerParams;


    public RaisedTab(Context context, int minHight, CharSequence tabText) {
        this(context, null, minHight, tabText);
    }

    public RaisedTab(Context context, AttributeSet attrs, int minHight, CharSequence tabText) {
        this(context, attrs, 0, minHight, tabText);
    }

    public RaisedTab(Context context, AttributeSet attrs, int minHight, int defStyleAttr, CharSequence tabText) {
        super(context, attrs, defStyleAttr, minHight);
        init(context, minHight, tabText);
    }

    private void init(Context context, int minHight, CharSequence tabText) {
        this.mContext = context;
        this.mMinHeiht = minHight;

        mParentContainer = new AutoRelativeLayout(context);
        mParentParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mParentContainer.setLayoutParams(mParentParams);
        mParentContainer.setBackground(ResourceUtils.getDrawabe(R.drawable.raised_tab_bg));

        //添加位于底部的容器
        mChildContainer = new AutoRelativeLayout(context);
        mChildContainerParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AutoUtils.getPercentHeightSize(mMinHeiht));
        mChildContainerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);//居于底部
        mChildContainer.setLayoutParams(mChildContainerParams);


        mTextview = new TextView(context);
        mTextview.setText(tabText);
        AutoRelativeLayout.LayoutParams tvParams = new AutoRelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mTextview.setTextSize(23);
        tvParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mTextview.setTextColor(ResourceUtils.getColor(R.color.color_333333));
        mTextview.setLayoutParams(tvParams);
        mChildContainer.addView(mTextview);


        mUnderLine = new View(context);
        RelativeLayout.LayoutParams lineParams = new RelativeLayout.LayoutParams(AutoUtils.getPercentWidthSize(45),
                AutoUtils.getPercentHeightSize(3));
        lineParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mUnderLine.setBackgroundColor(ResourceUtils.getColor(R.color.color_transparent));
        mUnderLine.setLayoutParams(lineParams);
        mChildContainer.addView(mUnderLine);


        mParentContainer.addView(mChildContainer);
        addView(mParentContainer);

    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            mChildContainerParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mChildContainer.setLayoutParams(mChildContainerParams);
            mTextview.setTextColor(ResourceUtils.getColor(R.color.color_4097e1));
            mTextview.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            mUnderLine.setBackgroundColor(ResourceUtils.getColor(R.color.color_4097e1));
        } else {
            mChildContainerParams.height = AutoUtils.getPercentHeightSize(mMinHeiht);
            mChildContainer.setLayoutParams(mChildContainerParams);
            mTextview.setTextColor(ResourceUtils.getColor(R.color.color_333333));
            mTextview.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            mUnderLine.setBackgroundColor(ResourceUtils.getColor(R.color.color_transparent));
        }
    }

    public void setTabPosition(int position) {
        mCurTabPosition = position;
        if (position == 0) {
            setSelected(true);
        }
    }


    public int getTabPosition() {
        return mCurTabPosition;
    }
}
