package com.feipai.flypai.utils.global;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.beans.FileBean;

import java.util.Comparator;

public class ComparatorUtil implements Comparator<MultiItemEntity> {

    @Override
    public int compare(MultiItemEntity entity1, MultiItemEntity entity2) {
        if (entity1 instanceof FileBean && entity2 instanceof FileBean) {
            FileBean bean1 = (FileBean) entity1;
            FileBean bean2 = (FileBean) entity2;
            if (bean1.getTime() - bean2.getTime() > 0) {
                return 1;
            } else {
                return -1;
            }
        } else {
            if (entity1.getItemType() == ConstantFields.FILE_TYPE.TYPE_DIR) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}