package com.feipai.flypai.utils.daoutils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.beans.PanoramicShareBean;
import com.feipai.flypai.beans.PlaneInfo;
import com.feipai.flypai.beans.UserBean;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Database HELPER class used to manage the creation and upgrading of your
 * database. This class also usually provides the DAOs used by the other
 * classes.
 * 固定表结构数据库工具类
 *
 * @author hongsir
 */

public class DBHelper extends OrmLiteSqliteOpenHelper {

    /**
     * data base name
     * ss.db
     */
    private static String DATABASE_NAME = "flypie.db";
    /**
     * increase the database version
     */
    private static final int DATABASE_VERSION = 12;

    /**
     * 缓存泛型Dao
     */
    private static Map daoMap = new HashMap<Class, Dao>();

    /***/
    private static final AtomicInteger USAGE_COUNTER = new AtomicInteger(0);

    /**
     * DBHelper实例
     */
    private static DBHelper HELPER = null;

    public static Map<Class, String> allTable = new HashMap();

    static {
        allTable.put(UserBean.class, ConstantFields.DB_TABLE_NAME.USER_INFO);
        allTable.put(PlaneInfo.class, ConstantFields.DB_TABLE_NAME.PLANE_INFO);
        allTable.put(PanoramicShareBean.class, ConstantFields.DB_TABLE_NAME.YUN_URL);
    }

    public static int getDatabaseVersion() {
        return DATABASE_VERSION;
    }

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * 单例模式
     *
     * @param context 上下文
     * @return 返回一个实例
     */
    public static synchronized DBHelper getHelper(Context context) {
        if (HELPER == null) {
            HELPER = new DBHelper(context);
        }
        USAGE_COUNTER.incrementAndGet();
        return HELPER;
    }

    /**
     * This is called when the database is first created. Usually you should
     * call createTable statements here to create the tables that will store
     * your data.
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            Log.i(DBHelper.class.getName(), "onCreate");
            for (Class clazz : allTable.keySet()) {
                TableUtils.createTableIfNotExists(connectionSource, clazz);
            }
            long millis = System.currentTimeMillis();
            Log.i(DBHelper.class.getName(),
                    "created new entries in onCreate: " + millis);
        } catch (SQLException e) {
            Log.e(DBHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * This is called when your application is upgraded and it has a higher
     * version number. This allows you to adjust the various data to match the
     * new version number.
     */

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
                          int oldVersion, int newVersion) {
        try {
            Log.i(DBHelper.class.getName(), "onUpgrade");
            for (Class clazz : allTable.keySet()) {
                TableUtils.dropTable(connectionSource, clazz, true);
            }
            // after we drop the old databases, we create the new ones
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.e(DBHelper.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 返回一个Dao
     *
     * @param clazz 需要操作的数据的class
     * @param <D>   定义返回的类型
     * @param <T>   定义返回的类型
     * @return 返回一个Dao
     */
    public <D extends Dao<T, Integer>, T> D getCacheDao(Class<T> clazz) {
        if (!daoMap.containsKey(clazz)) {
            try {
                Dao<T, Integer> dao = getDao(clazz);
                dao.setObjectCache(true);
                daoMap.put(clazz, dao);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return (D) daoMap.get(clazz);
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */

    @Override
    public void close() {
        if (USAGE_COUNTER.decrementAndGet() == 0) {
            super.close();
            HELPER = null;
            daoMap.clear();
            daoMap = null;
        }
    }

}

