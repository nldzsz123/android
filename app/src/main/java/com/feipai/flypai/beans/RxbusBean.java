package com.feipai.flypai.beans;

import com.feipai.flypai.base.BaseEntity;

public class RxbusBean extends BaseEntity {
    public String TAG;
    public Object object;

    public RxbusBean(String tag, Object o) {
        this.TAG = tag;
        this.object = o;
    }
}
