package com.feipai.flypai.ui.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.amap.api.maps.model.LatLng;
import com.feipai.flypai.utils.global.FileUtils;

import java.io.File;

/**
 * Created by YangLin on 2018-06-12.
 */

public class PlaneWaypointDbDao {

    private static final String FP_DB = "FLYMEDIA/db";
    private PlaneWaypointDbHelper dbHelper;


    //black_num表名
    private String tableName = "plane_waypoint";


    private static PlaneWaypointDbDao instance;


    public static synchronized PlaneWaypointDbDao getInstance(Context ctx) {

        //就可以判断  如果为空 就创建一个， 如果不为空就还用原来的  这样整个应用程序中就只能获的一个实例
        if (instance == null) {
            instance = new PlaneWaypointDbDao(ctx);

        }
        return instance;
    }

    private PlaneWaypointDbDao(Context ctx) {

        //由于数据库只需要调用一次，所以在单例中建出来
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {//SD卡是否挂载
            dbHelper = new PlaneWaypointDbHelper(ctx, FileUtils.getSdPaths(FP_DB) + File.separator + "plane_waypoint.db", null, 2);
        }
    }

    /**
     * 添加新飞控的飞行数据  至数据库
     *
     * @param wifiName  wifi名，区分飞行器
     * @param longitude 经度
     * @param latitude  纬度
     */
    public void addPlaneWaypoinit(String wifiName, double longitude, double latitude) {

        if (dbHelper != null) {
            //获得一个可写的数据库的一个引用
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("wifi_name", wifiName); // KEY 是列名，vlaue 是该列的值
            values.put("longitude", longitude);// KEY 是列名，vlaue 是该列的值
            values.put("latitude", latitude);// KEY 是列名，vlaue 是该列的值

            // 参数一：表名，参数三，是插入的内容
            // 参数二：只要能保存 values中是有内容的，第二个参数可以忽略
            db.insert(tableName, null, values);
        }

    }

    /**
     * 添加新飞控的飞行数据到指定行  至数据库
     *
     * @param wifiName  wifi名
     * @param longitude 经度
     * @param latitude  纬度
     */

    public void addPlaneWaypoinit(int realeId, String wifiName, double longitude, double latitude) {

        if (dbHelper != null) {
            //获得一个可写的数据库的一个引用
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int id = -1;
            ContentValues values = new ContentValues();
            values.put("wifi_name", wifiName); // KEY 是列名，vlaue 是该列的值
            values.put("longitude", longitude);// KEY 是列名，vlaue 是该列的值
            values.put("latitude", latitude);// KEY 是列名，vlaue 是该列的值
            Cursor cursor = db.query(tableName, null, null,
                    null, null, null, null, realeId + "," + (realeId + 1));
            if (cursor != null && cursor.moveToNext()) {// 如果查到了，移动成功
                if (cursor.getColumnIndex("_id") != -1)
                    id = cursor.getInt(cursor.getColumnIndex("_id"));
            }
            if (cursor != null)
                cursor.close();
            // 参数一：表名，参数三，是插入的内容
            // 参数二：只要能保存 values中是有内容的，第二个参数可以忽略
            if (id == -1) {
                db.insert(tableName, null, values);
            } else {
                updatePlaneWaypoint(realeId, wifiName, longitude, latitude);
            }
        }

    }

    /**
     * 添加新飞控家的位置到指定行  至数据库
     *
     * @param wifiName      wifi名
     * @param homeLongitude 经度
     * @param homeLatitude  纬度
     */

    public void addHomeWaypoinit(int realeId, String wifiName, double homeLongitude, double homeLatitude) {

        if (dbHelper != null) {
            //获得一个可写的数据库的一个引用
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int id = -1;
            ContentValues values = new ContentValues();
            values.put("wifi_name", wifiName); // KEY 是列名，vlaue 是该列的值
            values.put("homeLongitude", homeLongitude);// KEY 是列名，vlaue 是该列的值
            values.put("homeLatitude", homeLatitude);// KEY 是列名，vlaue 是该列的值
            Cursor cursor = db.query(tableName, null, null,
                    null, null, null, null, realeId + "," + (realeId + 1));
            if (cursor != null && cursor.moveToNext()) {// 如果查到了，移动成功
                if (cursor.getColumnIndex("_id") != -1)
                    id = cursor.getInt(cursor.getColumnIndex("_id"));
            }
            if (cursor != null)
                cursor.close();
            // 参数一：表名，参数三，是插入的内容
            // 参数二：只要能保存 values中是有内容的，第二个参数可以忽略
            if (id == -1) {
                db.insert(tableName, null, values);
            } else {
                updateHomeWaypoint(realeId, wifiName, homeLongitude, homeLatitude);
            }
        }
    }

    /**
     * 删除飞行数据
     *
     * @param wifiName wifi名
     */
    public void deletePlaneWaypoint(String wifiName) {
        if (dbHelper != null) {
//      ？?dad?asd?？sad?asdasdasd?
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            //表名  删除的条件
            db.delete(tableName, "wifi_name = ?", new String[]{wifiName});
        }
    }

    /**
     * 改 根据Wifi名更改经纬度
     *
     * @param wifiName
     * @param longitude 精度
     * @param latitude  纬度
     */
    public void updatePlaneWaypoint(String wifiName, double longitude, double latitude) {
        if (dbHelper != null) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("longitude", longitude);
            values.put("latitude", latitude);

            db.update(tableName, values, " wifi_name = ?", new String[]{wifiName});
        }
    }

    /**
     * 改  更改家的位置到指定行经纬度
     *
     * @param realeId   表示当前表格中第几条数据，并非对应的实际ID
     * @param longitude 精度
     * @param latitude  纬度
     */
    public void updatePlaneWaypoint(int realeId, String wifiName, double longitude, double latitude) {
        if (dbHelper != null) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int id = -1;
            ContentValues values = new ContentValues();
            values.put("wifi_name", wifiName);
            values.put("longitude", longitude);
            values.put("latitude", latitude);
            Cursor cursor = db.query(tableName, null, null,
                    null, null, null, null, realeId + "," + (realeId + 1));
            if (cursor != null && cursor.moveToNext()) {// 如果查到了，移动成功
                if (cursor.getColumnIndex("_id") != -1)
                    id = cursor.getInt(cursor.getColumnIndex("_id"));
            }
            if (cursor != null)
                cursor.close();

            db.update(tableName, values, "_id = ?", new String[]{String.valueOf(id)});
        }
    }

    /**
     * 改  更改指定行经纬度
     *
     * @param realeId       表示当前表格中第几条数据，并非对应的实际ID
     * @param homeLongitude 精度
     * @param homeLatitude  纬度
     */
    public void updateHomeWaypoint(int realeId, String wifiName, double homeLongitude, double homeLatitude) {
        if (dbHelper != null) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int id = -1;
            ContentValues values = new ContentValues();
            values.put("wifi_name", wifiName);
            values.put("homeLongitude", homeLongitude);
            values.put("homeLatitude", homeLatitude);
            Cursor cursor = db.query(tableName, null, null,
                    null, null, null, null, realeId + "," + (realeId + 1));
            if (cursor != null && cursor.moveToNext()) {// 如果查到了，移动成功
                if (cursor.getColumnIndex("_id") != -1)
                    id = cursor.getInt(cursor.getColumnIndex("_id"));
            }
            if (cursor != null)
                cursor.close();

            db.update(tableName, values, "_id = ?", new String[]{String.valueOf(id)});
        }
    }


    /**
     * 根据Wifi名获取经纬度
     *
     * @param wifiName
     * @return 如果没有存储，返回null,否则返回地图的
     */
    public LatLng getPlanePointByName(String wifiName) {
        if (dbHelper != null) {
            double longitude = -1;
            double latitude = -1;
            //获得一个可读的数据库的一个引用
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            //查询  表   列   条件
            Cursor cursor = db.query(tableName, null, "wifi_name = ?", new String[]{wifiName}, null, null, null);

            if (cursor != null && cursor.moveToNext()) {// 如果查到了，移动成功
                if (cursor.getColumnIndex("longitude") != -1)
                    longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
                if (cursor.getColumnIndex("latitude") != -1)
                    latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
            }
            if (cursor != null)
                cursor.close();
            if (longitude != -1 && latitude != -1) {
                return new LatLng(latitude, longitude);
            }
        }
        return null;
    }


    /**
     * 根据Wifi名获取飞机的经纬度
     *
     * @param realeId 表示当前表格中第几条数据，并非对应的实际ID
     * @return 如果没有存储，返回null,否则返回地图的
     */
    public LatLng getPlanePointById(int realeId) {
        if (dbHelper != null) {

            double longitude = -1;
            double latitude = -1;
            //获得一个可读的数据库的一个引用
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            Cursor cursor = db.query(tableName, null, null,
                    null, null, null, null, realeId + "," + (realeId + 1));
            if (cursor != null && cursor.moveToNext()) {// 如果查到了，移动成功
                if (cursor.getColumnIndex("longitude") != -1)
                    longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
                if (cursor.getColumnIndex("latitude") != -1)
                    latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
            }
            if (cursor != null)
                cursor.close();
            if (longitude != -1 && latitude != -1) {
                return new LatLng(latitude, longitude);
            }
        }
        return null;
    }

    /**
     * 根据数据位置获取家的经纬度
     *
     * @param realeId 表示当前表格中第几条数据，并非对应的实际ID
     * @return 如果没有存储，返回null,否则返回地图的
     */
    public LatLng getHomePointById(int realeId) {

        if (dbHelper != null) {
            double longitude = -1;
            double latitude = -1;
            //获得一个可读的数据库的一个引用
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            Cursor cursor = db.query(tableName, null, null,
                    null, null, null, null, realeId + "," + (realeId + 1));
            if (cursor != null && cursor.moveToNext()) {// 如果查到了，移动成功
                if (cursor.getColumnIndex("homeLongitude") != -1)
                    longitude = cursor.getDouble(cursor.getColumnIndex("homeLongitude"));
                if (cursor.getColumnIndex("homeLatitude") != -1)
                    latitude = cursor.getDouble(cursor.getColumnIndex("homeLatitude"));
            }
            if (cursor != null)
                cursor.close();
            if (longitude != -1 && latitude != -1) {
                return new LatLng(latitude, longitude);
            }
        }
        return null;
    }


}
