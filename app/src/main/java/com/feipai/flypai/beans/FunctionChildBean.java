package com.feipai.flypai.beans;

import com.feipai.flypai.base.BaseEntity;
import com.feipai.flypai.utils.global.StringUtils;
import com.zaihuishou.expandablerecycleradapter.model.ExpandableListItem;

import java.util.List;

public class FunctionChildBean extends BaseEntity implements ExpandableListItem {
    public String name;
    public int index;
    public boolean isNeedUpgrade;
    public String version;

    @Override
    public String getItemName() {
        return name;
    }

    @Override
    public List<?> getChildItemList() {
        return null;
    }

    @Override
    public boolean isExpanded() {
        return false;
    }

    @Override
    public void setExpanded(boolean isExpanded) {

    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        if (!StringUtils.isEmpty(version))
            this.version = version;
    }
}
