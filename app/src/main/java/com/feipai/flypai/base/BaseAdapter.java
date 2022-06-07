package com.feipai.flypai.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.feipai.flypai.utils.global.LogUtils;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.List;

/**
 * Created by pengsihai@yy.com on 2016/11/15.
 */

public abstract class BaseAdapter<T> extends RecyclerView.Adapter<BaseRecyclerViewHolder> {

    protected Context context;
    protected ItemDataListener itemDataListener;
    protected ItemOnClickListener itemOnClickListener;
    protected ItemOnLongClickListener itemOnLongClickListener;
    private View pullToRefreshHeaderView, headerView, loadMoreFooterView;

    /**
     * 外部recyclerview自行添加了头部
     */
    private boolean isAddHeaderFromRecy;

    protected int layoutId;
    protected boolean isParent;
    protected List<T> datas;
    public static final int TYPE_HEADER = 0;  //正常头部
    public static final int TYPE_PULL_TO_REFRESH_HEADER = 1;  //下拉刷新头部
    public static final int TYPE_NORMAL = 2;  //正常数据
    public static final int TYPE_FOOTER = 3;  //上拉footer

    public BaseAdapter(Context context, int layoutId, List<T> datas, boolean isParent) {
        this.isParent = isParent;
        this.context = context;
        this.layoutId = layoutId;
        this.datas = datas;
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    //添加下拉刷新头
    public void addPullToRefreshHeaderView(View addPullToRefreshHeaderView) {
        if (headerView != null) return;  //如果已经先添加了headerView，就不能增加下拉头了
        if (pullToRefreshHeaderView != null || addPullToRefreshHeaderView == null) {
            return;
        }
        this.pullToRefreshHeaderView = addPullToRefreshHeaderView;
        notifyItemInserted(0);
    }

    //添加头部布局（非下拉头），仅限一个
    public void addHeaderView(View addHeaderView) {
        if (addHeaderView == null || headerView != null) {
            return;
        }
        this.headerView = addHeaderView;
        notifyItemInserted(pullToRefreshHeaderView == null ? 0 : 1);
    }

    /**
     * 从外界recyclerview直接添加了header
     */
    public void addHeaderViewFormRecyclerView(boolean isAdd) {
        this.isAddHeaderFromRecy = isAdd;
    }

    //添加footeer
    public void addLoadMoreFooterView(View addLoadMoreFooterView) {
        if (loadMoreFooterView != null || addLoadMoreFooterView == null) {
            return;
        }
        this.loadMoreFooterView = addLoadMoreFooterView;
        notifyItemInserted(getItemCount() - 1);
    }

    public View getPullToRefreshHeaderView() {
        return pullToRefreshHeaderView;
    }

    public View getLoadMoreFooterView() {
        return loadMoreFooterView;
    }

    @Override
    public int getItemViewType(int position) {
        if (loadMoreFooterView != null && position == getItemCount() - 1) {
            return TYPE_FOOTER;
        }
        if (pullToRefreshHeaderView == null && headerView == null) {
            return TYPE_NORMAL;
        }
        if (pullToRefreshHeaderView == null && headerView != null) {
            if (position == 0) {
                return TYPE_HEADER;
            }
        }
        if (pullToRefreshHeaderView != null && headerView == null) {
            if (position == 0) {
                return TYPE_PULL_TO_REFRESH_HEADER;
            }
        }
        if (pullToRefreshHeaderView != null && headerView != null) {
            if (position == 0) return TYPE_PULL_TO_REFRESH_HEADER;
            if (position == 1) return TYPE_HEADER;
        }
        return TYPE_NORMAL;
    }

    //获取真实的position（与datalist对应，因为添加了头部，会使得position和data对应不上）
    public int getRealPosition(RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
        if (pullToRefreshHeaderView == null) {
            return headerView == null && !isAddHeaderFromRecy ? position : position - 1;
        } else {
            return headerView == null && !isAddHeaderFromRecy ? position - 1 : position - 2;
        }
    }

    public BaseAdapter<T> setItemDataListener(ItemDataListener listener) {
        itemDataListener = listener;
        return this;
    }

    public BaseAdapter<T> setItemOnClickListener(ItemOnClickListener listener) {
        itemOnClickListener = listener;
        return this;
    }

    public BaseAdapter<T> setItemOnLongClickListener(ItemOnLongClickListener listener) {
        itemOnLongClickListener = listener;
        return this;
    }

    @Override
    public BaseRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (pullToRefreshHeaderView != null && viewType == TYPE_PULL_TO_REFRESH_HEADER) {//如果是下拉头
            DisplayMetrics dm = new DisplayMetrics();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(dm);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dm.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT);
            pullToRefreshHeaderView.setLayoutParams(layoutParams);
            return new BaseRecyclerViewHolder(pullToRefreshHeaderView);
        }
        if (headerView != null && viewType == TYPE_HEADER) {//如果是正常头
            DisplayMetrics dm = new DisplayMetrics();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(dm);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dm.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT);
            headerView.setLayoutParams(layoutParams);
            return new BaseRecyclerViewHolder(headerView);
        }
        if (loadMoreFooterView != null && viewType == TYPE_FOOTER) {
            DisplayMetrics dm = new DisplayMetrics();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(dm);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dm.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT);
            loadMoreFooterView.setLayoutParams(layoutParams);
            return new BaseRecyclerViewHolder(loadMoreFooterView);
        }
        View view;
        if (isParent) view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        else view = LayoutInflater.from(context).inflate(layoutId, null, false);
        AutoUtils.auto(view);
        BaseRecyclerViewHolder holder = new BaseRecyclerViewHolder(view);
        view.setOnClickListener(v -> {
            if (itemOnClickListener != null)
                itemOnClickListener.setItemClick(getRealPosition(holder), datas.get(getRealPosition(holder)));
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (itemOnLongClickListener != null)
                    itemOnLongClickListener.setItemLongClick(holder, datas.get(getRealPosition(holder)));
                return false;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final BaseRecyclerViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER || getItemViewType(position) == TYPE_PULL_TO_REFRESH_HEADER) {//如果是头部，不做数据填充
            return;
        } else if (getItemViewType(position) == TYPE_FOOTER) {
            return;
        } else {
            if (itemDataListener == null) {
                return;
            }
//            if (isAddHeaderFromRecy) {
//                itemDataListener.setItemData(holder, datas.get(position));
//            } else {
            itemDataListener.setItemData(holder, datas.get(getRealPosition(holder)));
//            }
        }
    }

    protected void notifyItemChangeAtPosition(int position) {
        notifyItemChanged(position);
    }


    @Override
    public int getItemCount() {
        if (pullToRefreshHeaderView == null) {
            if (headerView == null) {
                return loadMoreFooterView == null ? datas.size() : datas.size() + 1;
            } else {
                return loadMoreFooterView == null ? datas.size() + 1 : datas.size() + 2;
            }
        } else {
            if (headerView == null) {
                return loadMoreFooterView == null ? datas.size() + 1 : datas.size() + 2;
            } else {
                return loadMoreFooterView == null ? datas.size() + 2 : datas.size() + 3;
            }
        }
    }

    protected List<T> getDatas() {
        return datas;
    }

    public interface ItemDataListener<T> {
        void setItemData(BaseRecyclerViewHolder holder, T t);

    }

    public interface ItemOnClickListener<T> {
        void setItemClick(int position, T t);

    }

    public interface ItemOnLongClickListener<T> {
        void setItemLongClick(BaseRecyclerViewHolder holder, T t);

    }

}