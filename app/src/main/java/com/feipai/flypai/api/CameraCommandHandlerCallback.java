package com.feipai.flypai.api;

import com.feipai.flypai.base.BaseEntity;
import com.feipai.flypai.utils.gsonlib.MGson;

import java.lang.reflect.Type;

public abstract class CameraCommandHandlerCallback<V extends BaseEntity> {

    /**
     * 最终成功回调
     */
    public abstract void onDigestResults(V data);

    /**
     * 中转站
     */
    public void onResultData(Type type, String data) {
//        LogUtils.d("中转站接收的数据===>" + data);
        V value = MGson.newGson().fromJson(data, type);
        onDigestResults(value);
    }


}