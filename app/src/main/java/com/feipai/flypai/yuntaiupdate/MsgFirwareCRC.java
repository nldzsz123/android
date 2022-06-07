package com.feipai.flypai.yuntaiupdate;

/**
 * Created by feipai1 on 2018/8/13.
 */

public class MsgFirwareCRC extends MsgBase {
    public long fwlen;
    public long response;
    public long fwcrc;


    public MsgFirwareCRC() {
        this.msgId = UP_GETCRC_REQ;
    }

    public byte[] dataFromObject() {
        int offset = 0;
        int crcLen = 3;
        byte[] crcs = new byte[crcLen];

        setToBytesFromLong(crcs,offset,UP_GETCRC_REQ,1);
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

    public static MsgFirwareCRC ObjectFromReponseData(byte[] data) {
        if (data.length < 15) {

        }
        MsgFirwareCRC con = new MsgFirwareCRC();

        con.stx = UP_STX;
        con.msgId = UP_GETCRC_REQ;
        con.leng = getLongFromBytes(data,2,2);
        con.response = getLongFromBytes(data,4,1);;
        con.fwlen = getLongFromBytes(data,5,4);
        con.fwcrc = getLongFromBytes(data,9,4);
        con.crc = getLongFromBytes(data,13,2);

        return con;

    }

    public boolean crcIsRight() {
        int offset = 0;
        byte[] crcs = new byte[12];
        setToBytesFromLong(crcs,offset,msgId,1);
        offset += 1;

        setToBytesFromLong(crcs,offset,leng,2);
        offset += 2;

        setToBytesFromLong(crcs,offset,response,1);
        offset += 1;

        setToBytesFromLong(crcs,offset,fwlen,4);
        offset += 4;

        setToBytesFromLong(crcs,offset,fwcrc,4);

        int remoteCrc = crc_16(crcs);

        return (remoteCrc == crc);
    }

    public int responseLength() {
        return 15;
    }

    public String toString() {

        return "获取固件CRC " + byte2hex(dataFromObject());
    }
}
