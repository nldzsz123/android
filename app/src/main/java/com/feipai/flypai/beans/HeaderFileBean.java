package com.feipai.flypai.beans;

import com.chad.library.adapter.base.entity.AbstractExpandableItem;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.feipai.flypai.app.ConstantFields;

import java.io.Serializable;

public class HeaderFileBean<T> extends AbstractExpandableItem<T> implements MultiItemEntity {

    private String titleName;
    private String countSize;

    public String getName() {
        return titleName;
    }

    public long getTime() {
        return 0;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public int getItemType() {
        return ConstantFields.FILE_TYPE.TYPE_DIR;
    }
}
