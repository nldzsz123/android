package com.feipai.flypai.beans;

import android.support.annotation.Nullable;

import com.feipai.flypai.base.BaseEntity;
import com.zaihuishou.expandablerecycleradapter.model.ExpandableListItem;

import java.util.List;

public class FunctionBean extends BaseEntity implements ExpandableListItem {
    private boolean mExpand = false;
    public String name;
    public int img = -1;
    public boolean isHintImgShow;
    public List<FunctionChildBean> mChild;

    @Override
    public String getItemName() {
        return name;
    }

    //    public FunctionBean(String name,int Img,boolean isHintImgShow, List<FunctionChildBean> mChild){
//
//    }
    @Override
    public List<?> getChildItemList() {
        return mChild;
    }

    @Override
    public boolean isExpanded() {
        return mExpand;
    }

    @Override
    public void setExpanded(boolean isExpanded) {
        mExpand = isExpanded;
    }

    @Override
    public String toString() {
        return "Department{" +
                "mExpand=" + mExpand +
                ", name='" + name + '\'' +
                ", isHintImgShow='" + isHintImgShow + '\'' +
                ", mEmployees=" + mChild +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof FunctionBean) {
            FunctionBean fb = (FunctionBean) obj;
            if (fb.getItemName() != null && getItemName() != null) {
                return fb.getItemName().equals(getItemName());
            }
        }
        return super.equals(obj);
    }
}
