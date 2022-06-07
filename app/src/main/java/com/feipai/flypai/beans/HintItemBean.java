package com.feipai.flypai.beans;

import com.feipai.flypai.base.BaseEntity;

public class HintItemBean extends BaseEntity {

    /**
     * 类型，区分电池，返航等不同类型提醒
     */
    public int type;
    /**
     * 下标
     */
    public int index;
    /**
     * 文字提示
     */
    private String hintText;

    private int textColor;
    /**
     * 背景色
     */
    private int bgRes;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public String getHintText() {
        return hintText;
    }

    public void setHintText(String hintText) {
        this.hintText = hintText;
    }

    public int getBgRes() {
        return bgRes;
    }

    public void setBgRes(int bgRes) {
        this.bgRes = bgRes;
    }

    public boolean equals(Object obj) {
        if (obj instanceof HintItemBean) {
            HintItemBean itemBean = (HintItemBean) obj;
            return itemBean.getType() == type;
        }
        return super.equals(obj);
    }
}
