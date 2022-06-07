package com.feipai.flypai.beans;

import com.feipai.flypai.base.BaseEntity;


/**
 * {"status":0,"msg":"\u624b\u673a\u53f7\u683c\u5f0f\u4e0d\u6b63\u786e"}
 */
public class YunApiUpdateBgMusicResultBean extends BaseEntity {
    int code;
    String msg;
    int result;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "YunApiUpdateBgMusicResultBean{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", result=" + result +
                '}';
    }
}
