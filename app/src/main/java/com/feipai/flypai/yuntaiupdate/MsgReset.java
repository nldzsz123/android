package com.feipai.flypai.yuntaiupdate;

/**
 * Created by feipai1 on 2018/8/13.
 */

// 云台复位消息
public class MsgReset extends MsgBase {
    public byte[] dataFromObject() {
        byte[] reset = {(byte) 0xA5, (byte) 0x02, (byte) 0x02, (byte) 0x06, (byte) 0xAF};

        return reset;
    }

    @Override
    public int responseLength() {
        return 0;
    }

    public String toString() {

        return "重置云台 " + byte2hex(dataFromObject());
    }

}
