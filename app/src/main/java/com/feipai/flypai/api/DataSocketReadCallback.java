package com.feipai.flypai.api;


public abstract class DataSocketReadCallback<T> {

    public void onReadData(byte[] msg) {

    }

    public void onReadThumb(int length, T t) {

    }

    public void onReadUpdataProgress(int progress) {

    }

    public void onUnpackZipResult(boolean isSuccess) {

    }

    public void onErrorCallback(int msgId, int errorCode) {

    }


}