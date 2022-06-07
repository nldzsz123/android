package com.feipai.flypai.beans;

import com.feipai.flypai.base.BaseEntity;


/**
 * Created by YangLin on 2017-12-11.
 */

public class ABCmdBean extends BaseEntity {

    /**
     * msg_id : 2
     * param : 3840x2160 30P 16:9
     * token : 1
     * type : video_resolution
     */

    private int msg_id;
    private String param;
    private int token;
    private String type;

    public int getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(int msg_id) {
        this.msg_id = msg_id;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public int getToken() {
        return token;
    }

    public void setToken(int token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    @Override
    public String toString() {
        return "ABCmdBean{" +
                "msg_id=" + msg_id +
                ", param='" + param + '\'' +
                ", token=" + token +
                ", type='" + type + '\'' +
                '}';
    }
}
