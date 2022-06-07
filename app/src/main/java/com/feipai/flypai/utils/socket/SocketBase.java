package com.feipai.flypai.utils.socket;

import java.util.ArrayList;

public class SocketBase {

    private ArrayList<SocketBaseMsg> mCommands;

    protected SocketBase() {
        mCommands = new ArrayList<>();
    }

    protected void sendCommand(SocketBaseMsg msg) {
        if (msg == null) {
            return;
        }

        SocketManager manager = SocketManager.getCmdInstance();
        if (!manager.isCanSendDataTo7878()) {
            mCommands.add(msg);
        }

        manager.sendCameraCommandData(msg);
    }

    protected void clearCommand() {
        mCommands.clear();
    }
}
