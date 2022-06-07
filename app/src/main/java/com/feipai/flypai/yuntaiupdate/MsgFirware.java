package com.feipai.flypai.yuntaiupdate;

/**
 * Created by feipai1 on 2018/8/13.
 */

public class MsgFirware extends MsgBase {
    // 请求用的
    public long fwSeq;
    public byte[] fwData;

    // 回应用的
    public long fwReq;
    public long response;

    public MsgFirware() {
        this.msgId = UP_SNDFW_REQ;
    }

    public byte[] dataFromObject() {
        int offset = 0;
        int crcLen = 5+fwData.length;
        byte[] crcs = new byte[crcLen];

        setToBytesFromLong(crcs,offset,UP_SNDFW_REQ,1);
        offset += 1;

        int len = fwData.length + 2;
        setToBytesFromLong(crcs,offset,len,2);
        offset += 2;

        setToBytesFromLong(crcs,offset,fwSeq,2);
        offset += 2;

        copyBytes(crcs,fwData,offset,fwData.length);
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

    public static MsgFirware ObjectFromReponseData(byte[] data) {
        if (data.length < 9) {
            return null;
        }
        MsgFirware con = new MsgFirware();

        con.stx = UP_STX;
        con.msgId = UP_SNDFW_REQ;
        con.leng = getLongFromBytes(data,2,2);
        con.fwReq = getLongFromBytes(data,4,2);
        con.response = getLongFromBytes(data,6,1);
        con.crc = getLongFromBytes(data,7,2);

        return con;

    }

    public boolean crcIsRight() {
        int offset = 0;
        byte[] crcs = new byte[6];
        setToBytesFromLong(crcs,offset,msgId,1);
        offset += 1;

        setToBytesFromLong(crcs,offset,leng,2);
        offset += 2;

        setToBytesFromLong(crcs,offset,fwReq,2);
        offset += 2;

        setToBytesFromLong(crcs,offset,response,1);

        int remoteCrc = crc_16(crcs);

        return (remoteCrc == crc);
    }

    public int responseLength() {
        return 9;
    }

    public String toString() {

        return "发送固件 " + byte2hex(dataFromObject());
    }
}
