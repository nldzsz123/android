package com.feipai.flypai.utils.socket;

public interface MavlinkSocketReadListener extends TcpReadListener {
    void read(int requestCode, int bufferSize, byte[] buffer);


}
