package com.feipai.flypai.beans;

import com.feipai.flypai.base.BaseEntity;
import com.feipai.flypai.utils.global.StringUtils;

public class MusicBgBean extends BaseEntity implements Comparable<MusicBgBean> {
    private int index;
    private String path;
    private String name;

    public MusicBgBean(String name, String path) {
        setName(name);
        setPath(path);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setIndex(name);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setIndex(String name) {
        this.index = StringUtils.findNumbler(name);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MusicBgBean)) {
            throw new ClassCastException("is not sky file");
        }
        MusicBgBean s = (MusicBgBean) o;
        return this.name.equals(s.name) && this.path == s.path;
    }


    @Override
    public int compareTo(MusicBgBean bean) {
        return Integer.valueOf(this.index).compareTo(bean.index);
    }

    @Override
    public String toString() {
        return "MusicBgBean{" +
                "index=" + index +
                ", path='" + path + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
