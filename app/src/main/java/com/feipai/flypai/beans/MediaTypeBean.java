package com.feipai.flypai.beans;

import com.feipai.flypai.base.BaseEntity;
import com.feipai.flypai.utils.global.TimeUtils;
import com.feipai.flypai.utils.languageutils.LanguageUtil;

public class MediaTypeBean extends BaseEntity {
    /**
     * 类型名
     */
    String typeName;
    /**
     * 最后时间
     */
    long lastTime;
    /**
     * 总文件个数
     */
    int totalSize = 0;

    int typeBg;

    public MediaTypeBean(String typeName, int typeBg) {
        this.typeName = typeName;
        this.typeBg = typeBg;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getLastTime() {
        if (lastTime > 0) {
            String time = TimeUtils.millis2String(lastTime, "yyyy年MM月dd日");
            if (LanguageUtil.isEnglish()) {
                time = TimeUtils.millis2String(lastTime, "yyyy-MM-dd");
            }
            return time;
        }
        return null;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public int getTypeBg() {
        return typeBg;
    }

    public void setTypeBg(int typeBg) {
        this.typeBg = typeBg;
    }
}
