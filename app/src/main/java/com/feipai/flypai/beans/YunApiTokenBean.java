package com.feipai.flypai.beans;

import com.feipai.flypai.base.BaseEntity;


/**
 * {"status":0,"msg":"\u624b\u673a\u53f7\u683c\u5f0f\u4e0d\u6b63\u786e"}
 */
public class YunApiTokenBean extends BaseEntity {
    String token;
    String prefix;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return "YunApiTokenBean{" +
                "token='" + token + '\'' +
                ", prefix='" + prefix + '\'' +
                '}';
    }
}
