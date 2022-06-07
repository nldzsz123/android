package com.feipai.flypai.base;

import com.Messages.MAVLinkMessage;

public interface BaseMavlinkEntity {

    int getMsgId();

    void setMavlinkMessage(MAVLinkMessage msg);
}
