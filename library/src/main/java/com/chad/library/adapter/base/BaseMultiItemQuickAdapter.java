package com.chad.library.adapter.base;

import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.ViewGroup;

import com.chad.library.adapter.base.entity.IExpandable;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.chad.library.adapter.base.util.ListUtils;

import java.util.Collection;
import java.util.List;

import static java.util.Collections.addAll;

/**
 * https://github.com/CymChad/BaseRecyclerViewAdapterHelper
 */
public abstract class BaseMultiItemQuickAdapter<T extends MultiItemEntity, K extends BaseViewHolder> extends BaseQuickAdapter<T, K> {

    /**
     * layouts indexed with their types
     */
    private SparseIntArray layouts;

    private static final int DEFAULT_VIEW_TYPE = -0xff;
    public static final int TYPE_NOT_FOUND = -404;

    protected static final String EMPTY_ITME_NAME = "empty";

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data A new list is created out of this one to avoid mutable list
     */
    public BaseMultiItemQuickAdapter(List<T> data) {
        super(data);
    }

    @Override
    protected int getDefItemViewType(int position) {
        T item = mData.get(position);
        if (item != null) {
            return item.getItemType();
        }
        return DEFAULT_VIEW_TYPE;
    }

    protected void setDefaultViewTypeLayout(@LayoutRes int layoutResId) {
        addItemType(DEFAULT_VIEW_TYPE, layoutResId);
    }

    @Override
    protected K onCreateDefViewHolder(ViewGroup parent, int viewType) {
        return createBaseViewHolder(parent, getLayoutId(viewType));
    }

    private int getLayoutId(int viewType) {
        return layouts.get(viewType, TYPE_NOT_FOUND);
    }

    protected void addItemType(int type, @LayoutRes int layoutResId) {
        if (layouts == null) {
            layouts = new SparseIntArray();
        }
        layouts.put(type, layoutResId);
    }


    @Override
    public void remove(@IntRange(from = 0L) int position) {
//        if (mData == null
//                || position < 0
//                || position >= mData.size()) return;
//
//        T entity = mData.get(position);
//        if (entity instanceof IExpandable) {
//            removeAllChild((IExpandable) entity, position);
//        }
//        removeDataFromParent(entity);
        if (position < mData.size())
            super.remove(position);
    }

    public void removeItemAtPosition(@IntRange(from = 0L) int position) {
        super.remove(position);
    }

    @Override
    public void removeHeaderItem(@IntRange(from = 0L) int position, T data) {
        if (mData == null
                || position < 0
                || position >= mData.size()) return;

        T entity = mData.get(position);
        if (entity instanceof IExpandable) {
            removeAllChild((IExpandable) entity, position);
        }
        removeHeaderDataFromParent(entity, data);
        super.removeHeaderItem(position, data);
    }

    /**
     * 二级菜单单选
     */
    public void expandChild(T child, int position) {
        int positionAtAll = getParentPositionInAll(position);
        IExpandable parent = (IExpandable) mData.get(positionAtAll);
        List<MultiItemEntity> childList = parent.getSubItems();
        for (MultiItemEntity e : childList) {
            if (e instanceof IExpandable) {
                IExpandable c = (IExpandable) e;
                int po = getItemPosition((T) c);
                if (c.isExpanded()) {
                    collapse(po, false, true);
                } else {
                    if (c.equals(child)) {
                        expand(po, false, true);
                    }
                }
            }
        }
//        notifyDataSetChanged();
    }


    protected void removeHeaderDataFromParent(T child, T data) {
        int position = getParentPosition(child);
        if (position >= 0) {
            IExpandable parent = (IExpandable) mData.get(position);
            List<MultiItemEntity> childList = parent.getSubItems();
            childList.remove(child);
            mData.remove(child);
            if (childList.size() > 0) {
                if (childList.size() % 3 == 2) {//尾部需要填充一个空对象或者移除两个空对象
                    if (childList.size() >= 2) {
                        int lastButOneChildIndex = childList.size() - 2;
                        int lastChildIndex = childList.size() - 1;
                        MultiItemEntity lastButOneChild = childList.get(lastButOneChildIndex);
                        MultiItemEntity lastChild = childList.get(lastChildIndex);
                        if (childList.get(childList.size() - 2).getName().equals(EMPTY_ITME_NAME)) {
                            /**倒数第二个为null了，直接移出*/
                            childList.remove(lastChild);
                            mData.remove(lastChild);
                            childList.remove(lastButOneChild);
                            mData.remove(lastButOneChild);

                        } else {
                            childList.add(data);
                            mData.add(childList.size(), data);
                        }
                    } else {

                    }
                } else if (childList.size() % 3 == 1) {

                } else if (childList.size() % 3 == 0) {

                }
            }

        }
    }


    /**
     * 添加二级菜单item
     */
    public void addExpandableItem(int position, @NonNull T data) {
        if (mData == null || mData.size() <= 0) return;
        if (mData.get(0) instanceof IExpandable) {
            IExpandable parent = (IExpandable) mData.get(0);
            parent.getSubItems().add(data);
            position = parent.getSubItems().indexOf(data) + 1;
//        notifyItemInserted(position + getHeaderLayoutCount());
            if (parent.isExpanded()) {
                //展开即可刷新
                mData.add(position, data);
                notifyItemChanged(position);

//                notifyItemRangeInserted(position + getHeaderLayoutCount(), mData.size());
            }
        }
    }


    public void addHeaderBeanItemWithNull(T emptyItem, T data) {
        if (mData == null || mData.size() <= 0) return;
        IExpandable parent = (IExpandable) mData.get(0);
        List<MultiItemEntity> headerData = parent.getSubItems();
        int headerSize = headerData.size();
        if (headerSize % 3 == 0) {
            /**一次就得加3个*/
            if (headerSize > 0) {
                if (headerSize > 1 && headerData.get(headerSize - 2).getName().equals(EMPTY_ITME_NAME)) {
                    Log.d("yanglin", "更新----》倒数第2个");
                    headerData.set(headerSize - 2, data);
                    ListUtils.sort(headerData, false, new String[]{"time"});
                    if (parent.isExpanded()) {
                        setDataAndRotate(headerSize - 1, headerData.indexOf(data) + 1, data, 2);
                    }
                    return;
                } else if (headerData.get(headerSize - 1).getName().equals(EMPTY_ITME_NAME)) {
                    Log.d("yanglin", "更新----》倒数第1个");
                    headerData.set(headerSize - 1, data);
                    ListUtils.sort(headerData, false, new String[]{"time"});
                    if (parent.isExpanded())
                        setDataAndRotate(headerSize, headerData.indexOf(data) + 1, data, 1);
                    return;
                }
            }
            Log.d("yanglin", "添加----》3个");
            addExpandableItem(0, data);
            addExpandableItem(0, emptyItem);
            addExpandableItem(0, emptyItem);
        } else if (headerSize % 3 == 1) {
            /**一次就加入2个*/
            addExpandableItem(0, data);
        } else {
            /**一次就加入1个*/
            addExpandableItem(0, data);
            addExpandableItem(0, emptyItem);
        }

    }

    public int getEmptyCount() {
        int count = 0;
        if (mData != null && mData.size() > 0) {
            for (MultiItemEntity itemEntity : mData) {
                if (itemEntity.getName().equals(EMPTY_ITME_NAME)) {
                    count++;
                }
            }
        }
        return count;
    }


    public int getRealDataSize() {
        int size = mData.size();
        if (size > 0) {
            for (MultiItemEntity itemEntity : mData) {
                if (itemEntity instanceof IExpandable) {
                    size = size - 1;
                    IExpandable parent = (IExpandable) itemEntity;
                    if (!parent.isExpanded()) {
                        List<MultiItemEntity> list = parent.getSubItems();
                        if (list != null && list.size() > 0) {
                            size = size + list.size();
                        }
                    }
                }
            }
        }
        return size;
    }

    @Override
    public void setData(int index, @NonNull T data) {
        if (mData == null || mData.size() <= 0) return;
        super.setData(index, data);
    }

    @Override
    public void setDataAndRotate(int oldIndex, int newIndex, @NonNull T data, int empytCount) {
        if (mData == null) return;
        super.setDataAndRotate(oldIndex, newIndex, data, empytCount);
    }

    public void refreshItemChanged(MultiItemEntity entity) {
        if (mData == null || mData.size() <= 0) return;
        int position = mData.indexOf(entity);
        if (position > 0)
            refreshNotifyItemChanged(position);

    }


    /**
     * 移除父控件时，若父控件处于展开状态，则先移除其所有的子控件
     *
     * @param parent         父控件实体
     * @param parentPosition 父控件位置
     */
    protected void removeAllChild(IExpandable parent, int parentPosition) {
        if (parent.isExpanded()) {
            List<MultiItemEntity> chidChilds = parent.getSubItems();
            if (chidChilds == null || chidChilds.size() == 0) return;

            int childSize = chidChilds.size();
            for (int i = 0; i < childSize; i++) {
                remove(parentPosition + 1);
            }
        }
    }

    /**
     * 移除子控件时，移除父控件实体类中相关子控件数据，避免关闭后再次展开数据重现
     *
     * @param child 子控件实体
     */
    protected void removeDataFromParent(T child) {
        int position = getParentPosition(child);
        if (position >= 0) {
            IExpandable parent = (IExpandable) mData.get(position);
            parent.getSubItems().remove(child);
            notifyItemChanged(position);
        }
    }

    /**
     * 该方法用于 IExpandable 树形列表。
     * 如果不存在 Parent，则 return -1。
     *
     * @param position 所处列表的位置
     * @return 父 position 在数据列表中的位置
     */
    public int getParentPositionInAll(int position) {
        List<T> data = getData();
        MultiItemEntity multiItemEntity = getItem(position);

        if (isExpandable(multiItemEntity)) {
            IExpandable IExpandable = (IExpandable) multiItemEntity;
            for (int i = position - 1; i >= 0; i--) {
                MultiItemEntity entity = data.get(i);
                if (isExpandable(entity) && IExpandable.getLevel() > ((IExpandable) entity).getLevel()) {
                    return i;
                }
            }
        } else {
            for (int i = position - 1; i >= 0; i--) {
                MultiItemEntity entity = data.get(i);
                if (isExpandable(entity)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public boolean isExpandable(MultiItemEntity item) {
        return item != null && item instanceof IExpandable;
    }
}


