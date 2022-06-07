package com.feipai.flypai.beans;


import com.feipai.flypai.base.BaseEntity;

/**
 * code:0：登录成功/注册成功
 * 1：手机号码格式不对
 * 3：短信验证码过期
 * 4：短信验证码未填写
 * 5：服务器异常
 * 468：短信验证码不对
 * newUser:1 新注册用户 0已注册用户
 * access_token:32位字符串，暂时未实现，值为feipai6
 **/
public class LoginCodeMsg extends BaseEntity {
    public String access_token;
    public String newUser;

    @Override
    public String toString() {
        return "LoginCodeMsg{" +
                "access_token='" + access_token + '\'' +
                ", newUser='" + newUser + '\'' +
                '}';
    }
}
