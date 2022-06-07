package com.feipai.flypai.yuntaiupdate;

/**
 * Created by feipai1 on 2018/8/13.
 */

public class MsgCon extends MsgBase {
    public long fileMaxleng;

    public MsgCon() {
        this.msgId = UP_CON_REQ;
    }

    public byte[] dataFromObject() {
        int offset = 0;
        int crcLen = 3;
        byte[] crcs = new byte[crcLen];

        setToBytesFromLong(crcs,offset,UP_CON_REQ,1);
        offset += 1;
        setToBytesFromLong(crcs,offset,0x00,2);
        int crc = crc_16(crcs);

        offset = 0;
        byte[] sData = new byte[crcLen+3];
        setToBytesFromLong(sData,offset,UP_STX,1);
        offset += 1;

        copyBytes(sData,crcs,offset,crcLen);
        offset += crcLen;

        setToBytesFromLong(sData,offset,crc>>8&0xff,1);
        offset += 1;

        setToBytesFromLong(sData,offset,crc&0xff,1);

        return sData;
    }

    public static MsgCon ObjectFromReponseData(byte[] data) {
        if (data.length < 8) {

        }
        MsgCon con = new MsgCon();

        con.stx = UP_STX;
        con.msgId = UP_CON_REQ;
        con.leng = getLongFromBytes(data,2,2);
        con.fileMaxleng = getLongFromBytes(data,4,2);;
        con.crc = getLongFromBytes(data,6,2);

        return con;

    }

    public boolean crcIsRight() {
        int offset = 0;
        byte[] crcs = new byte[5];
        setToBytesFromLong(crcs,offset,msgId,1);
        offset += 1;

        setToBytesFromLong(crcs,offset,leng,2);
        offset += 2;

        setToBytesFromLong(crcs,offset,fileMaxleng,2);

        int remoteCrc = crc_16(crcs);

        return (remoteCrc == crc);
    }

    public int responseLength() {
        return 8;
    }

    public String toString() {

        return "握手消息 " + byte2hex(dataFromObject());
    }
}
