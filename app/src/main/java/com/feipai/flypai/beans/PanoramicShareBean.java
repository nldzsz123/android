package com.feipai.flypai.beans;

import com.feipai.flypai.app.ConstantFields;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


@DatabaseTable(tableName = ConstantFields.DB_TABLE_NAME.YUN_URL)
public class PanoramicShareBean {

    public static final String COLUMNNAME_ID = "_id";

    @DatabaseField(generatedId = true)
    private int id = 0;
    /**
     * 标题
     */
    @DatabaseField
    private String title;
    /**
     * 描述
     */
    @DatabaseField
    private String describe;
    /**
     * 本地路径
     */
    @DatabaseField(columnName = COLUMNNAME_ID)
    private String localPath;
    @DatabaseField
    private String yunUrl;


    public int getUrlId() {
        return id;
    }

    public void setUrlId(int urlId) {
        this.id = urlId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getYunUrl() {
        return yunUrl;
    }

    public void setYunUrl(String yunUrl) {
        this.yunUrl = yunUrl;
    }

    @Override
    public String toString() {
        return "PanoramicShareBean{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", describe='" + describe + '\'' +
                ", localPath='" + localPath + '\'' +
                ", yunUrl='" + yunUrl + '\'' +
                '}';
    }
}
