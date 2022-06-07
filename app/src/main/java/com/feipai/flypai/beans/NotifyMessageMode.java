package com.feipai.flypai.beans;

public class NotifyMessageMode {

    /**
     * 消息接收者
     */
    public int receiver;
    /**
     * 被操作的文件id
     */
    public int id;
    /**
     * 操作文件意图
     */
    public int actionType;

    /**
     * 操作的文件名
     */
    public String fileName;

    public Object object;

}
