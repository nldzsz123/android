package com.zaihuishou.expandablerecycleradapter.viewholder;

import android.support.annotation.CallSuper;
import android.util.Log;
import android.view.View;

import com.zaihuishou.expandablerecycleradapter.model.ExpandableListItem;

/**
 * Keeps track of expanded state and holds callbacks which can be used to
 * trigger expansion-based events.
 * if you want to item can expandable,you must extands this class{@link AbstractExpandableAdapterItem}
 * creater: zaihuishou
 * create time: 7/13/16.
 * email:tanzhiqiang.cathy@gmail.com
 */

public abstract class AbstractExpandableAdapterItem extends AbstractAdapterItem {

    private int itemIndex = -1;
    private ParentListItemExpandCollapseListener mParentListItemExpandCollapseListener;
    private ExpandableListItem mExpandableListItem;

    @CallSuper
    @Override
    public void onUpdateViews(java.lang.Object model, int position) {
        this.itemIndex = position;
        if (model instanceof ExpandableListItem)
            mExpandableListItem = (ExpandableListItem) model;
    }

    public ExpandableListItem getExpandableListItem() {
        return mExpandableListItem;
    }

    public int getItemIndex() {
        return itemIndex;
    }

    /**
     * implementations to be notified of expand/collapse state change events.
     */
    public interface ParentListItemExpandCollapseListener {

        /**
         * Called when a list item is expanded.
         *
         * @param position The index of the item in the list being expanded
         */
        void onParentListItemExpanded(ExpandableListItem item, int position);

        /**
         * Called when a list item is collapsed.
         *
         * @param position The index of the item in the list being collapsed
         */
        void onParentListItemCollapsed(ExpandableListItem item, int position);

        /**
         * 用于单个的点击事件
         */
        void onParentListItemOnlyClick(ExpandableListItem item, int position);

    }

    /**
     * Callback triggered when expansion state is changed, but not during
     * initialization.
     * <p>
     * Useful for implementing animations on expansion.
     *
     * @param expanded true if view is expanded before expansion is toggled,
     *                 false if not
     */
    public abstract void onExpansionToggled(boolean expanded);

    /**
     * Getter for the {@link ParentListItemExpandCollapseListener} implemented in
     *
     * @return The {@link ParentListItemExpandCollapseListener} set in the {@link AbstractExpandableAdapterItem}
     */
    public ParentListItemExpandCollapseListener getParentListItemExpandCollapseListener() {
        return mParentListItemExpandCollapseListener;
    }

    /**
     * Setter for the {@link ParentListItemExpandCollapseListener} implemented in
     *
     * @param parentListItemExpandCollapseListener The {@link ParentListItemExpandCollapseListener} to set on the {@link AbstractExpandableAdapterItem}
     */
    public void setParentListItemExpandCollapseListener(ParentListItemExpandCollapseListener parentListItemExpandCollapseListener) {
        mParentListItemExpandCollapseListener = parentListItemExpandCollapseListener;
    }

    /**
     * Used to determine whether a click in the entire parent {@link View}
     * should trigger row expansion.
     * <p>
     * If you return false, you can call {@link #expandView()} to trigger an
     * expansion in response to a another event or {@link #collapseView()} to
     * trigger a collapse.
     *
     * @return true to set an {@link android.view.View.OnClickListener} on the item view
     */
    public boolean shouldItemViewClickToggleExpansion() {
        return true;
    }

    /**
     * Triggers expansion of the parent.
     */
    protected void expandView(ExpandableListItem item) {
        if (mParentListItemExpandCollapseListener != null) {
            onExpansionToggled(true);
            mParentListItemExpandCollapseListener.onParentListItemExpanded(item, itemIndex);
        }
    }

    /**
     * Triggers collapse of the parent.
     */
    protected void collapseView(ExpandableListItem item) {
        if (mParentListItemExpandCollapseListener != null) {
            onExpansionToggled(false);
            mParentListItemExpandCollapseListener.onParentListItemCollapsed(item, itemIndex);
        }
    }

    protected void onlyClick(ExpandableListItem item) {
        if (mParentListItemExpandCollapseListener != null) {
            onExpansionToggled(false);
            mParentListItemExpandCollapseListener.onParentListItemOnlyClick(item, itemIndex);
        }
    }


    /**
     * expand or unexpand item
     */
    protected void doExpandOrUnexpand(ExpandableListItem item) {
        if (mExpandableListItem != null) {
            if (mExpandableListItem.getChildItemList() != null
                    && !mExpandableListItem.getChildItemList().isEmpty()) {
                if (getExpandableListItem().isExpanded()) {
                    collapseView(item);
                } else {
                    expandView(item);
                }
            } else {
                onlyClick(item);
            }
        }
    }
}
