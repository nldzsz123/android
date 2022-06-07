package com.feipai.flypai.beans;


/**获取用戶信息
 * 4001：token过期
 *
 * 1：昵称不合法
 * 2：城市名称不对
 * 3：未选择头像
 * 4：上传头像超过300kb
 * 5：非jpg和png图片格式
 * 6：服务器错误1
 * */
public class UpdateUserInfoMsg {
    /**
     * 用户头像地址
     * */
    private String avator;
}
