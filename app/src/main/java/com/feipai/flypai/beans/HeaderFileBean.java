package com.feipai.flypai.beans;

import com.chad.library.adapter.base.entity.AbstractExpandableItem;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.feipai.flypai.app.ConstantFields;

import java.io.Serializable;

public class HeaderFileBean<T> extends AbstractExpandableItem<T> implements MultiItemEntity {

    private String titleName;
    private String countSize;

    @Override
    public String getName() {
        return titleName;
    }

    @Override
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

    public String getCountText() {
        if (getSubItems() != null) {
            if (getSubItems().size() > 0) {
                int count = getSubItems().size();
                for (T t : getSubItems()) {
                    if (t instanceof MultiItemEntity)
                        if (((MultiItemEntity) t).getName().equals(ConstantFields.FILE_TYPE.EMPTY_FILE_NAME)) {
                            count--;
                        }
                }
                if (count > 0) {
                    return "(" + count + ")";
                }
            }
        }
        return null;
    }
}
