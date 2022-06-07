package com.feipai.flypai.yuntaiupdate;

/**
 * Created by feipai1 on 2018/8/13.
 */

public class MsgErase extends MsgBase {

    public long fileLen;
    public long response;

    public MsgErase() {
        this.msgId = UP_ERASE_REQ;
    }

    public byte[] dataFromObject() {
        int offset = 0;
        int crcLen = 7;
        byte[] crcs = new byte[crcLen];

        setToBytesFromLong(crcs, offset, UP_ERASE_REQ, 1);
        offset += 1;
        setToBytesFromLong(crcs, offset, 0x04, 2);
        offset += 2;
        setToBytesFromLong(crcs, offset, fileLen, 4);
        int crc = crc_16(crcs);

        offset = 0;
        byte[] sData = new byte[crcLen + 3];
        setToBytesFromLong(sData, offset, UP_STX, 1);
        offset += 1;

        copyBytes(sData, crcs, offset, crcLen);
        offset += crcLen;

        setToBytesFromLong(sData, offset, crc >> 8 & 0xff, 1);
        offset += 1;

        setToBytesFromLong(sData, offset, crc & 0xff, 1);

        return sData;
    }

    public static MsgErase ObjectFromReponseData(byte[] data) {
        if (data.length < 7) {

        }
        MsgErase con = new MsgErase();

        con.stx = UP_STX;
        con.msgId = UP_ERASE_REQ;
        con.leng = getLongFromBytes(data, 2, 2);
        con.response = getLongFromBytes(data, 4, 1);
        ;
        con.crc = getLongFromBytes(data, 5, 2);

        return con;

    }

    public boolean crcIsRight() {
        int offset = 0;
        byte[] crcs = new byte[4];
        setToBytesFromLong(crcs, offset, msgId, 1);
        offset += 1;

        setToBytesFromLong(crcs, offset, leng, 2);
        offset += 2;

        setToBytesFromLong(crcs, offset, response, 1);

        int remoteCrc = crc_16(crcs);

        return (remoteCrc == crc);
    }

    public int responseLength() {
        return 7;
    }

    public String toString() {

        return "擦除消息 " + byte2hex(dataFromObject());
    }

}
