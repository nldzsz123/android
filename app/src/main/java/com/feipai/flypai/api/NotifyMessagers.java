package com.feipai.flypai.api;

import java.util.HashSet;
import java.util.Set;

public class NotifyMessagers {

    private static NotifyMessagers mInstance;
    private Set<NotifyMessage> mMsgQueue = new HashSet<NotifyMessage>();

    private NotifyMessagers() {

    }

    public static NotifyMessagers getInstance() {
        if (mInstance == null) {
            synchronized (NotifyMessagers.class) {
                if (mInstance == null) {
                    mInstance = new NotifyMessagers();
                }
            }
        }
        return mInstance;
    }

    /**
     * 添加通知消息读取列队
     *
     * @param m
     */
    public void addNotifyMessageQueue(NotifyMessage m) {
        mMsgQueue.add(m);
    }

    /**
     * 发送通知消息 到列队
     */
    public void sendNotifyMessage(Object obj) {
        notifyMessageQueue(obj);
    }

    /**
     * 通知列队更新数据
     */
    private void notifyMessageQueue(Object obj) {
        for (NotifyMessage m : mMsgQueue) {
            m.sendMessage(obj);
        }
    }
}
