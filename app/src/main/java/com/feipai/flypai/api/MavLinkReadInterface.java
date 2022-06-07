package com.feipai.flypai.api;

import com.MAVLinkPacket;
import com.feipai.flypai.beans.MavlinkBean;

public interface MavLinkReadInterface extends ReadTCPDataInterface<MAVLinkPacket> {

    @Override
    void onReadData(MAVLinkPacket mavData);
}
