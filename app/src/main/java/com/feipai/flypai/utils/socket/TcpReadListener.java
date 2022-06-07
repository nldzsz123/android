package com.feipai.flypai.utils.socket;

public interface TcpReadListener {
    void close();

    void connecSuccess();

    void connectFail();

}
