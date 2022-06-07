package com.feipai.flypai.utils.socket;

import com.MAVLinkPacket;
import com.Messages.MAVLinkMessage;

public abstract class MavlinkObserver<T> {

    //可以重写，具体可由子类实现
    public abstract void onRead(T t);

    public void close() {

    }

    public void connecSuccess() {
    }

    public void connectFail() {
    }


}
