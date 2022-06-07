package com.feipai.flypai.ui.adapter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by YangLin on 2018-06-12.
 */

public class PlaneWaypointDbHelper extends SQLiteOpenHelper {

    private String tableName = "plane_waypoint";

    public PlaneWaypointDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    /**
     * 第一次运行时，创建数据库，调用此方法
     */
    public void onCreate(SQLiteDatabase db) {
        //建表：自增长的主键 ， WIFI名(wifi_name) ， 经度(longitude) ，纬度(latitude)
        db.execSQL("create table if not exists " + tableName + "(_id integer primary key autoincrement, wifi_name varchar(20) , longitude real , latitude real , homeLongitude real , homeLatitude real)");
    }

    @Override
    /**
     * 当数据库版本不一样，升级数据库时，调用此方法
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        String sql = "Alter table " + tableName + " add column " + "homeLongitude real";
        db.execSQL(sql);
        String sq2 = "Alter table " + tableName + " add column " + "homeLatitude real";
        db.execSQL(sq2);

    }
}
