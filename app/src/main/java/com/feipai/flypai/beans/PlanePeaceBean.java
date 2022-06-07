package com.feipai.flypai.beans;

import com.feipai.flypai.base.BaseEntity;

public class PlanePeaceBean extends BaseEntity {
    private int status;
    private String end_time;
    private int left_count;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public int getLeft_count() {
        return left_count;
    }

    public void setLeft_count(int left_count) {
        this.left_count = left_count;
    }

    @Override
    public String toString() {
        return "PlanePeaceBean{" +
                "status=" + status +
                ", end_time='" + end_time + '\'' +
                ", left_count=" + left_count +
                '}';
    }
}
