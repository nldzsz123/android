package com.feipai.flypai.api;

public interface ReadTCPDataInterface<T> {
    /**
     * socket 返回的数据
     */
    public void onReadData(T t);

}
