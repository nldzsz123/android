package com.feipai.flypai.utils.socket;

public interface CommandSendInterface {

    // 发送相机命令
    void sendCameraCommand(String jsonMsg);
    // 发送飞机命令
    void sendDroneCommand(byte[] msgBytes);
}
