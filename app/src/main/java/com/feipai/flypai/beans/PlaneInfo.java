package com.feipai.flypai.beans;


import com.amap.api.maps.model.LatLng;
import com.feipai.flypai.app.ConstantFields;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = ConstantFields.DB_TABLE_NAME.PLANE_INFO)
public class PlaneInfo {
    public final static String COLUMNNAME_ID = "_id";
    public final static String COLUMNNAME_USER_ID = "user_id";
    @DatabaseField(generatedId = true)
    private int id = 0;
    @DatabaseField(columnName = COLUMNNAME_ID)
    private String wifiName;
    @DatabaseField
    private String flyControlSerialNumber;
    @DatabaseField(canBeNull = true, foreignColumnName = UserBean.COLUMNNAME_PHONE_NUMB, foreign = true, columnName = COLUMNNAME_USER_ID, foreignAutoRefresh = true)
    private UserBean user;
    @DatabaseField(defaultValue = "-1")
    private int acked;
    @DatabaseField(defaultValue = "0")
    private String end_time;
    @DatabaseField(defaultValue = "0")
    private int left_count;
    @DatabaseField(defaultValue = "0")//上传服务器激活状态,0未上传，1已上传
    private int updateAcked;



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public String getFlyControlSerialNumber() {
        return flyControlSerialNumber;
    }

    public void setFlyControlSerialNumber(String flyControlSerialNumber) {
        this.flyControlSerialNumber = flyControlSerialNumber;
    }


    public UserBean getUser_id() {
        return user;
    }

    public void setUser_id(UserBean user_id) {
        this.user = user_id;
    }

    public int getAcked() {
        return acked;
    }

    public void setAcked(int acked) {
        this.acked = acked;
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

    public int getUpdateAcked() {
        return updateAcked;
    }

    public void setUpdateAcked(int updateAcked) {
        this.updateAcked = updateAcked;
    }

    @Override
    public String toString() {
        return "PlaneInfo{" +
                "id=" + id +
                ", wifiName='" + wifiName + '\'' +
                ", flyControlSerialNumber='" + flyControlSerialNumber + '\'' +
                ", user=" + user +
                ", acked=" + acked +
                ", end_time='" + end_time + '\'' +
                ", left_count=" + left_count +
                '}';
    }
}
