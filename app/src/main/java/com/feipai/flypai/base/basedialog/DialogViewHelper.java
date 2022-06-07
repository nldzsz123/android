package com.feipai.flypai.base.basedialog;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.feipai.flypai.ui.view.CircleProgressBar;
import com.feipai.flypai.utils.global.LogUtils;
import com.zhy.autolayout.utils.AutoUtils;

import java.lang.ref.WeakReference;

public class DialogViewHelper {

    private View mContentView = null;

    private SparseArray<WeakReference<View>> mViews;

    public DialogViewHelper() {
        mViews = new SparseArray<>();
    }

    public DialogViewHelper(Context mContext, int mViewLayoutResId) {
        this();
        mContentView = View.inflate(mContext, mViewLayoutResId, null);
    }

    public void setContentView(View contentView) {
        this.mContentView = contentView;
    }

    /**
     * 根据id获取每一个view
     *
     * @param viewId
     * @param <T>
     * @return
     */
    public <T extends View> T getView(int viewId) {
        WeakReference<View> viewWeakReference = mViews.get(viewId);
        View view = null;
        if (viewWeakReference != null) {
            view = viewWeakReference.get();
        }
        if (view == null) {
            view = mContentView.findViewById(viewId);
            mViews.put(viewId, new WeakReference<View>(view));
        }
        return (T) view;
    }

    public void setText(int viewId, CharSequence charSequence) {
        TextView tv = getView(viewId);
        if (tv != null) {
            tv.setText(charSequence);
        }
    }

    public void setTextSize(int viewId, float size) {
        TextView tv = getView(viewId);
        if (tv != null) {
            tv.setTextSize(size);
        }
    }

    public void setTextColor(int viewId, int colorId) {
        TextView tv = getView(viewId);
        if (tv != null) {
            tv.setTextColor(colorId);
        }
    }

    public void setImageResource(int viewId, int drawable) {
        ImageView img = getView(viewId);
        if (img != null) {
            img.setImageResource(drawable);
        }
    }

    public void setCircleProgress(int viewId, int progress) {
        CircleProgressBar cp = getView(viewId);
        if (cp != null) {
            cp.setProgress(progress);
        }
    }

    public void setProgress(int viewId, int progress) {
        ProgressBar progressBar = getView(viewId);
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }
    }

    public int getProgress(int viewId) {
        ProgressBar progressBar = getView(viewId);
        if (progressBar != null) {
            return progressBar.getProgress();
        }
        return 0;
    }

    public void setProgressMax(int viewId, int max) {
        ProgressBar progressBar = getView(viewId);
        if (progressBar != null) {
            progressBar.setMax(max);
        }
    }

    public void setProgressWithCenter(int viewId, int progress, String center) {
        CircleProgressBar cp = getView(viewId);
        if (cp != null) {
            LogUtils.d("刷新一组数据啦" + center);
            cp.setProgressWithCenter(progress, center);
        }
    }

    public void setViewVisibility(int viewId, int visibility) {
        View view = getView(viewId);
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    public void setViewEnabled(int viewId, boolean isEnabled) {
        View view = getView(viewId);
        if (view != null) {
            view.setEnabled(isEnabled);
        }
    }

    public void setViewAlpha(int viewId, float alpha) {
        View view = getView(viewId);
        if (view != null) {
            view.setAlpha(alpha);
        }
    }

    public int getViewVisibility(int viewId) {
        View view = getView(viewId);
        if (view != null) {
            return view.getVisibility();
        }
        return View.GONE;
    }

    public void setOnclickListener(int viewId, View.OnClickListener listener) {
        View view = mContentView.findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    public View getContentView() {
        return mContentView;
    }


}