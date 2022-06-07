package com.feipai.flypai.beans;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.feipai.flypai.R;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.base.BaseEntity;
import com.feipai.flypai.utils.global.FileUtils;
import com.feipai.flypai.utils.global.StringUtils;

import java.io.File;

public class MusicItemBean extends BaseEntity implements MultiItemEntity, Comparable<MusicItemBean> {

    private String name;

    private String path;

    private int index;

    private String defPicPath;

    public MusicItemBean(String name, String picPath, String musicPath) {
        setName(name);
        setDefPicPath(picPath);
        setPath(musicPath);
    }

    public MusicItemBean(String name, String musicPath) {
        setName(name);
        setDefPicPath("musicsbg/music_bg_01.png");
        setPath(musicPath);
        setIndex(name);
    }

    /**
     * 根据名称来区分下标
     *
     * @param name xxx10.png
     */
    public void setIndex(String name) {
        this.index = StringUtils.findNumbler(name);
    }

    public String getPath() {
        return path;
    }

    public String getAssetsPath() {
        return "file:///android_asset/" + path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    @Override
    public int getItemType() {
        return 0;
    }

    public String getDefPicPath() {
        return defPicPath;
    }

    public void setDefPicPath(String defPicPath) {
        this.defPicPath = "file:///android_asset/" + defPicPath;
    }


    public void setName(String name) {
        this.name = name;
        setIndex(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getTime() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MusicItemBean)) {
            throw new ClassCastException("is not sky file");
        }
        MusicItemBean s = (MusicItemBean) o;
        return this.name.equals(s.name) && this.path == s.path;
    }


    @Override
    public int compareTo(MusicItemBean bean) {
        return Integer.valueOf(this.index).compareTo(bean.index);
    }


    @Override
    public String toString() {
        return "MusicItemBean{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", index=" + index +
                '}';
    }
}
