package com.feipai.flypai.ui.view;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import com.feipai.flypai.R;
import com.feipai.flypai.utils.global.ResourceUtils;
import com.zhy.autolayout.AutoLinearLayout;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.ArrayList;
import java.util.List;

public class HumpTabLayout extends AutoLinearLayout {
    private static final int TRANSLATE_DURATION_MILLIS = 200;

    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private boolean mVisible = true;

    private List<HumpTab> mTabs = new ArrayList<>();

    private AutoLinearLayout mTabLayout;

    private LayoutParams mTabParams;

    private LayoutParams mTabsParams;
    private int mCurrentPosition = 0;
    private OnTabSelectedListener mListener;

    public HumpTabLayout(Context context) {
        this(context, null);
    }

    public HumpTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HumpTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        mTabLayout = new AutoLinearLayout(context);
        mTabLayout.setBackgroundColor(ResourceUtils.getColor(R.color.color_transparent));
        mTabLayout.setOrientation(LinearLayout.HORIZONTAL);
        mTabsParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mTabLayout.setGravity(Gravity.CENTER);
        addView(mTabLayout, mTabsParams);

        mTabParams = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        mTabParams.weight = 1;
    }

    public void addItem(List<HumpTab> tabs) {
        if (mTabLayout.getChildCount() > 0) {
            mTabLayout.removeAllViews();
            mTabs.clear();
            mCurrentPosition = 0;
            mTabsParams.width = AutoUtils.getPercentWidthSize(tabs.size() * 264);
            mTabLayout.setLayoutParams(mTabsParams);
        }
        for (HumpTab tab : tabs) {
            tab.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener == null) return;

                    int pos = tab.getTabPosition();
                    if (mCurrentPosition == pos) {
                        mListener.onTabReselected(pos);
                    } else {
                        mListener.onTabSelected(pos, mCurrentPosition);
                        tab.setSelected(true);
                        mListener.onTabUnselected(mCurrentPosition);
                        mTabs.get(mCurrentPosition).setSelected(false);
                        mCurrentPosition = pos;
                    }
                }
            });
            tab.setTabPosition(mTabLayout.getChildCount());
            tab.setSelected(false);
            tab.setLayoutParams(mTabParams);
            mTabLayout.addView(tab);
            mTabs.add(tab);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

    }

    public void setOnTabSelectedListener(OnTabSelectedListener onTabSelectedListener) {
        mListener = onTabSelectedListener;
    }

    public void setCurrentItem(final int position) {
        mTabLayout.post(new Runnable() {
            @Override
            public void run() {
                mCurrentPosition = position;
                mTabs.get(position).setSelected(true);
            }
        });
    }

    public int getCurrentItemPosition() {
        return mCurrentPosition;
    }

    /**
     * 获取 Tab
     */
    public HumpTab getItem(int index) {
        if (mTabs.size() < index) return null;
        return mTabs.get(index);
    }

    public void setItem(int index, HumpTab newTab) {
        if (mTabs.size() > index) {
            mTabs.set(index, newTab);
//            mTabs.get(index).setSelected();
        }
    }

    public interface OnTabSelectedListener {
        void onTabSelected(int position, int prePosition);

        void onTabUnselected(int position);

        void onTabReselected(int position);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, mCurrentPosition);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        if (mCurrentPosition != ss.position) {
            mTabLayout.getChildAt(mCurrentPosition).setSelected(false);
            mTabLayout.getChildAt(ss.position).setSelected(true);
        }
        mCurrentPosition = ss.position;
    }

    static class SavedState extends BaseSavedState {
        private int position;

        public SavedState(Parcel source) {
            super(source);
            position = source.readInt();
        }

        public SavedState(Parcelable superState, int position) {
            super(superState);
            this.position = position;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(position);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }


    public void hide() {
        hide(true);
    }

    public void show() {
        show(true);
    }

    public void hide(boolean anim) {
        toggle(false, anim, false);
    }

    public void show(boolean anim) {
        toggle(true, anim, false);
    }

    public boolean isVisible() {
        return mVisible;
    }

    private void toggle(final boolean visible, final boolean animate, boolean force) {
        if (mVisible != visible || force) {
            mVisible = visible;
            int height = getHeight();
            if (height == 0 && !force) {
                ViewTreeObserver vto = getViewTreeObserver();
                if (vto.isAlive()) {
                    // view树完成测量并且分配空间而绘制过程还没有开始的时候播放动画。
                    vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            ViewTreeObserver currentVto = getViewTreeObserver();
                            if (currentVto.isAlive()) {
                                currentVto.removeOnPreDrawListener(this);
                            }
                            toggle(visible, animate, true);
                            return true;
                        }
                    });
                    return;
                }
            }
            int translationY = visible ? 0 : height;
            if (animate) {
                animate().setInterpolator(mInterpolator)
                        .setDuration(TRANSLATE_DURATION_MILLIS)
                        .translationY(translationY);
            } else {
                ViewCompat.setTranslationY(this, translationY);
            }
        }
    }
}
