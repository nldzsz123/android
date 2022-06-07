package com.feipai.flypai.beans;

import com.feipai.flypai.base.BaseEntity;

public class YunApiMakePanorResultBean extends BaseEntity {
    private String flag;
    private String pid;
    private String view_uuid;
    private String thumb_path;

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getView_uuid() {
        return view_uuid;
    }

    public void setView_uuid(String view_uuid) {
        this.view_uuid = view_uuid;
    }

    public String getThumb_path() {
        return thumb_path;
    }

    public void setThumb_path(String thumb_path) {
        this.thumb_path = thumb_path;
    }

    @Override
    public String toString() {
        return "YunApiMakePanorResultBean{" +
                "flag='" + flag + '\'' +
                ", pid='" + pid + '\'' +
                ", view_uuid='" + view_uuid + '\'' +
                ", thumb_path='" + thumb_path + '\'' +
                '}';
    }
}
