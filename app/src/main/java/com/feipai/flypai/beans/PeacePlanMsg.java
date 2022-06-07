package com.feipai.flypai.beans;


/**
 * 0、查询成功
 * 1、token过期或未登录
 * 2、wifi名不合法
 *
 *备注：若status为2，则end_time为空字符串；未0和1，则为购买或者已过期则返回具体时间
 * */

public class PeacePlanMsg {

    /**
     * 0购买 1已过期 2未购买
     * **/
    public int status;
    /**
     * 时间戳(1970年开始)
     * */
    public String end_time;

    /**
     * 安心计划剩余维修次数
     * */
    public int left_count;


    @Override
    public String toString() {
        return "PeacePlanMsg{" +
                "status=" + status +
                ", end_time='" + end_time + '\'' +
                ", left_count=" + left_count +
                '}';
    }
}
