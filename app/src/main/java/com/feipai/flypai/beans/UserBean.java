package com.feipai.flypai.beans;

import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.utils.daoutils.DBClient;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.CloseableWrappedIterable;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.EagerForeignCollection;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;


@DatabaseTable(tableName = ConstantFields.DB_TABLE_NAME.USER_INFO)
public class UserBean {

    public static final String COLUMNNAME_ID = "_id";
    public static final String COLUMNNAME_PHONE_NUMB = "phoneNumb";

    @DatabaseField(id = true, columnName = COLUMNNAME_ID, canBeNull = false)
    private int userId = 0;
    @DatabaseField(columnName = COLUMNNAME_PHONE_NUMB)
    private String phoneNumb;
    @DatabaseField
    private String countryCode;
    @DatabaseField
    private String accessToken;
    @ForeignCollectionField(eager = true)
    private ForeignCollection<PlaneInfo> planes;

    //为保证家的位置与飞机的位置唯一，所以存在用户信息里，作为唯一识别
    @DatabaseField
    private double homeLat = 0;
    @DatabaseField
    private double homeLng = 0;
    @DatabaseField
    private double planeLat = 0;
    @DatabaseField
    private double planeLng = 0;


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPhoneNumb() {
        return phoneNumb;
    }

    public void setPhoneNumb(String phoneNumb) {
        this.phoneNumb = phoneNumb;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public ForeignCollection<PlaneInfo> getPlanes() {
        return planes;
    }


    public void setPlanes(ForeignCollection<PlaneInfo> planes) {
        this.planes = planes;
    }

    public double getHomeLat() {
        return homeLat;
    }

    public void setHomeLat(double homeLat) {
        this.homeLat = homeLat;
    }

    public double getHomeLng() {
        return homeLng;
    }

    public void setHomeLng(double homeLng) {
        this.homeLng = homeLng;
    }

    public double getPlaneLat() {
        return planeLat;
    }

    public void setPlaneLat(double planeLat) {
        this.planeLat = planeLat;
    }

    public double getPlaneLng() {
        return planeLng;
    }

    public void setPlaneLng(double planeLng) {
        this.planeLng = planeLng;
    }

    @Override
    public String toString() {
        return "UserBean{" +
                "phoneNumb='" + phoneNumb + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", planes=" + planes +
                ", homeLat=" + homeLat +
                ", homeLng=" + homeLng +
                ", planeLat=" + planeLat +
                ", planeLng=" + planeLng +
                '}';
    }
}
