package com.feipai.flypai.yuntaiupdate;

/**
 * Created by feipai1 on 2018/8/13.
 */

public class ReceivePacket {
    public final static int CMD_HANDSHACK = 0xe1;
    public final static int CMD_ERASE_FLASH = 0xe2;
    public final static int CMD_SEND_PERCENT = 0xe3;
    public final static int CMD_SEND_FILE_DATA = 0xe4;
    public final static int CMD_GET_FILE_CRC32 = 0xe5;
    public final static int CMD_RUN_FIRMWARE = 0xe6;
    public final static int CMD_GET_FILE_MD5 = 0xe7;
    public final static int MAX_PACKET_DATA_LEN = 16;

    private static int s_iState = 0, s_iCurDataLen = 0, s_packetLen = 0;
    private Packet mPacket;


    public class Packet {
        public Packet() {
            abyData = new byte[MAX_PACKET_DATA_LEN];
        }

        int byHead;
        public int byCmd;
        int wDataLen;
        byte[] abyData;
        int wCrc16;
        public int packetLen;
    }


    public static boolean CheckCmdValid(int byCmd) {
        switch (byCmd) {
            case CMD_HANDSHACK:
            case CMD_ERASE_FLASH:
            case CMD_SEND_PERCENT:
            case CMD_SEND_FILE_DATA:
            case CMD_GET_FILE_CRC32:
            case CMD_RUN_FIRMWARE:
            case CMD_GET_FILE_MD5:
                return true;
            default:
                return false;
        }
    }

    public void resetState() {
        s_iState = 0;
        s_iCurDataLen = 0;
        s_packetLen = 0;
        mPacket = null;
    }

    public Packet ReceiveStateMachine(byte byIn)
    {
        switch (s_iState) {
            case 0:
                if (0x02 == byIn) {
                    mPacket = new Packet();
                    s_iState++;
                    mPacket.byHead = byIn & 0x00ff;
                    s_packetLen = 1;
                }
                break;
            case 1:
                s_packetLen++;
                int cmd = byIn & 0x00ff;
                if (CheckCmdValid(cmd)) {
                    mPacket.byCmd = byIn & 0x00ff;;
                    s_iState++;
                }
                else {
                    s_iState = 0;
                }
                break;
            case 2:
                s_packetLen++;
                s_iState++;
                mPacket.wDataLen = byIn&0xff;
                mPacket.wDataLen <<= 8;
                break;
            case 3:
                s_packetLen++;
                mPacket.wDataLen += byIn & 0xff;
                if (mPacket.wDataLen > MAX_PACKET_DATA_LEN) {
                    s_iState++;
                    s_iCurDataLen = 0;
                    mPacket.wDataLen = MAX_PACKET_DATA_LEN;
                } else if (0 == mPacket.wDataLen) {
                    s_iState += 2;
                    s_iCurDataLen = 0;
                }
                else {
                    s_iState++;
                    s_iCurDataLen = 0;
                }
                break;
            case 4:
                s_packetLen++;
                mPacket.abyData[s_iCurDataLen] = byIn;
                s_iCurDataLen++;
                if (s_iCurDataLen == mPacket.wDataLen) {
                    s_iState++;
                }
                break;
            case 5:
                s_packetLen++;
                mPacket.wCrc16 = byIn & 0xff;
                mPacket.wCrc16 <<= 8;
                s_iState++;
                break;
            case 6:
                s_packetLen++;
                mPacket.wCrc16 += byIn & 0xff;
                mPacket.packetLen = s_packetLen;
                s_iState = 0;
                return mPacket;
            default:
                return null;
        }
        return null;
    }
}
