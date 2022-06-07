package com.feipai.flypai.base;

import com.feipai.flypai.api.CameraCommandCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class BaseCameraMsg{

    /**
     * 固定的消息token
     */
    public int token;

    /**
     * 消息id
     */
    public int msgId;

    /**
     * 类型
     */
    public String type;

    /**
     * 参数
     */
    public Object param;

    /**
     * 文件类型
     */
    public String mediaTpye;

    public long size = -1;

    public int rval = -1;

    public int xposition = -1;

    public int yposition = -1;

    public String md5sum;

    public int offset = -1;
    public int speed = -1;
    public int step = -1;

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String oldname;

    public String newname;
    public String srcname;

    public String dstname;

    public void setSrcname(String srcname) {
        this.srcname = srcname;
    }

    public void setDstname(String dstname) {
        this.dstname = dstname;
    }

    private CameraCommandCallback callback;

    public BaseCameraMsg(CameraCommandCallback msgCallBack) {
        this.callback = msgCallBack;
    }

    public CameraCommandCallback getCallback() {
        return callback;
    }

    public BaseCameraMsg() {
    }

    public int getToken() {
        return token;
    }

    public void setToken(int token) {
        this.token = token;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getParam() {
        return param;
    }

    public void setParam(Object param) {
        this.param = param;
    }

    public String getMediaTpye() {
        return mediaTpye;
    }

    public void setMediaTpye(String mediaTpye) {
        this.mediaTpye = mediaTpye;
    }


    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getRval() {
        return rval;
    }

    public void setRval(int rval) {
        this.rval = rval;
    }

    public int getXposition() {
        return xposition;
    }

    public void setXposition(int xposition) {
        this.xposition = xposition;
    }

    public int getYposition() {
        return yposition;
    }

    public void setYposition(int yposition) {
        this.yposition = yposition;
    }

    public String getMd5sum() {
        return md5sum;
    }

    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }


    public String getOldname() {
        return oldname;
    }

    public void setOldname(String oldname) {
        this.oldname = oldname;
    }

    public String getNewname() {
        return newname;
    }

    public void setNewname(String newname) {
        this.newname = newname;
    }

    /**
     * 组装成消息
     */
    public String getJsonMsg() {
        try {
            if (token != -1) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("token", token);
                jsonObject.put("msg_id", msgId);
                if (type != null) jsonObject.put("type", type);
                if (param != null) jsonObject.put("param", param);
                if (mediaTpye != null) jsonObject.put("media_type", mediaTpye);
                if (size != -1) jsonObject.put("size", size);
                if (rval != -1) jsonObject.put("rval", rval);
                if (xposition != -1) jsonObject.put("xposition", xposition);
                if (yposition != -1) jsonObject.put("yposition", yposition);
                if (speed != -1) jsonObject.put("speed", speed);
                if (step != -1) jsonObject.put("step", step);
                if (offset != -1) jsonObject.put("offset", offset);
                if (md5sum != null) jsonObject.put("md5sum", md5sum);
                if (oldname != null) jsonObject.put("oldname", oldname);
                if (newname != null) jsonObject.put("newname", newname);
                if (srcname != null) jsonObject.put("srcname", srcname);
                if (dstname != null) jsonObject.put("dstname", dstname);

                final String result = jsonObject.toString();
//                MLog.log("发送的数据--->" + result);
                return result;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
