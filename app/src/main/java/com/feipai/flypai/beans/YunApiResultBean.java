package com.feipai.flypai.beans;

import com.feipai.flypai.base.BaseEntity;


/**
 * {"status":0,"msg":"\u624b\u673a\u53f7\u683c\u5f0f\u4e0d\u6b63\u786e"}
 */
public class YunApiResultBean extends BaseEntity {
    int status;
    String msg;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "YunApiResultBean{" +
                "status=" + status +
                ", msg='" + msg + '\'' +
                '}';
    }
}
