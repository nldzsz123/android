package com.feipai.flypai.api;


/**
 * 全局消息，通知隐藏页面更新
 */
public abstract class NotifyMessage {

    public void sendMessage(Object obj) {
        readMessage(obj);
    }


    public void readMessage(Object obj) {
    }
}