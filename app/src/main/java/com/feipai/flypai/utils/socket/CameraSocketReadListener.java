package com.feipai.flypai.utils.socket;

public interface CameraSocketReadListener extends TcpReadListener {
    void read(int bufferSize, Object buffer);
}
