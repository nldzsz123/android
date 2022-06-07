package com.feipai.flypai.utils.daoutils;


import com.feipai.flypai.app.FlyPieApplication;

import java.sql.SQLException;
import java.util.List;

/**
 * 辅助db存储和查询
 *
 * @author Deng Yongdong
 */

public class DBClient {

    /**
     * 添加单条数据到指定表中
     *
     * @param obj 需要插入的对象
     * @return 返回-1表示处理失败 1表示成功
     */
    public static int addObject(Object obj) {
        return addObject(DBHelper.getHelper(FlyPieApplication.getInstance()), obj);
    }

    public static int addObject(List obj) {
        return addObject(DBHelper.getHelper(FlyPieApplication.getInstance()), obj);
    }

    static int count = 0;
    static int count1 = 0;

    /**
     * 添加单条数据到指定表中
     *
     * @param helper 指定数据库
     * @param obj    需要插入的对象
     * @return 返回-1表示处理失败 1表示成功
     */
    public static int addObject(DBHelper helper, Object obj) {
        try {
            Class clazz = obj.getClass();
            helper.getCacheDao(clazz).create(obj);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 1;
    }

    /**
     * 添加多条数据到指定表中
     *
     * @param helper 指定数据库
     * @param objs   需要插入的对象集合
     * @return 返回-1表示处理失败 1表示成功
     */
    public static int addObject(DBHelper helper, final List objs) {
        if (objs != null && objs.size() > 0) {
            for (Object obj : objs) {
                if (addObject(helper, obj) != 1) {
                    return -1;
                }
            }
        }
        return 1;
    }

    public static int updateObject(DBHelper helper, final List objs) {
        if (objs != null && objs.size() > 0) {
            for (Object obj : objs) {
                if (updateObject(helper, obj) != 1) {
                    return -1;
                }
            }
        }
        return 1;
    }

    public static int updateObject(Object obj) {
        return updateObject(DBHelper.getHelper(FlyPieApplication.getInstance()), obj);
    }

    public static int updateObject(DBHelper helper, Object obj) {
        try {
            Class clazz = obj.getClass();
            helper.getCacheDao(clazz).update(obj);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 1;
    }


    /**
     * 根据_id查询指定表数据
     *
     * @param clazz 查询的数据class
     * @param id    id值
     * @param <T>   定义返回的类型
     * @return 返回单条
     */
    public static <T> T findObjById(Class<T> clazz, Object id) {
        return findObjByColumn(clazz, "_id", id);
    }

    /**
     * 根据指定列明查询指定表数据
     *
     * @param clazz      查询的数据class
     * @param columnName 列名
     * @param value      列名对应的值
     * @param <T>        定义返回的类型
     * @return 返回单条
     */
    public static <T> T findObjByColumn(Class<T> clazz, String columnName, Object value) {
        try {
            DBHelper helper = DBHelper.getHelper(FlyPieApplication.getInstance());
            T obj = helper.getCacheDao(clazz).queryBuilder().where().eq(columnName, value).queryForFirst();
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据指定列明查询指定表数据
     *
     * @param clazz      查询的数据class
     * @param columnName 列名
     * @param value      列名对应的值
     * @param <T>        定义返回的类型
     * @return 返回多条
     */
    public static <T> List<T> findObjByColumns(Class<T> clazz, String columnName, Object value) {
        try {
            DBHelper helper = DBHelper.getHelper(FlyPieApplication.getInstance());
            List<T> objs = helper.getCacheDao(clazz).queryBuilder().where().eq(columnName, value).query();
            return objs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据指定列明查询指定表数据并排序
     *
     * @param clazz 查询的数据class
     * @return 返回0表示处理成功
     */
    public static List findObjByColumns(Class clazz, String columnName, Object value, String orderColumnName, boolean ascending) {
        try {
            DBHelper helper = DBHelper.getHelper(FlyPieApplication.getInstance());
            List datas = helper.getCacheDao(clazz).queryBuilder().orderBy(orderColumnName, ascending).where().eq(columnName, value).query();
            return datas;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据id删除指定表的数据
     *
     * @param clazz 删除的数据class
     * @param id    需要删除的数据的id
     * @return 返回0表示处理成功
     */
    public static int delObjById(Class clazz, int id) {
        try {
            DBHelper helper = DBHelper.getHelper(FlyPieApplication.getInstance());
            helper.getCacheDao(clazz).deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 清空指定表
     *
     * @param clazz 删除的数据class
     * @param <T>   定义删除数据的类型
     * @return 返回0表示处理成功
     */
    public static <T> int delTableAll(Class<T> clazz) {
        try {
            DBHelper helper = DBHelper.getHelper(FlyPieApplication.getInstance());
            helper.getDao(clazz).queryRaw("delete from " + helper.allTable.get(clazz));
            helper.getDao(clazz).queryRaw("update sqlite_sequence SET seq = 0 where name ='" + helper.allTable.get(clazz) + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 清空所有表
     */
    public static void emptyDB() {
        count = 0;
        count1 = 0;
        try {
            DBHelper helper = DBHelper.getHelper(FlyPieApplication.getInstance());
            for (Class clazz : helper.allTable.keySet()) {
                delTableAll(clazz);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询指定表中所有的数据
     *
     * @param clazz 查询的数据class
     * @return 返回0表示处理成功
     */
    public static List findObject(Class clazz) {
        try {
            DBHelper helper = DBHelper.getHelper(FlyPieApplication.getInstance());
            List datas = helper.getCacheDao(clazz).queryForAll();
            return datas;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询指定表中所有的数据并排序
     *
     * @param clazz 查询的数据class
     * @return 返回0表示处理成功
     */
    public static List findObjectOrderBy(Class clazz, String columnName, boolean ascending) {
        try {
            DBHelper helper = DBHelper.getHelper(FlyPieApplication.getInstance());
            List datas = helper.getCacheDao(clazz).queryBuilder().orderBy(columnName, ascending).query();
            return datas;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

