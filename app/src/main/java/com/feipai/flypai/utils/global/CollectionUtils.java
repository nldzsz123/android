package com.feipai.flypai.utils.global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 集合类的工具类
 */
public final class CollectionUtils {

    private CollectionUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 判断集合是否为空
     *
     * @param collection 集合类型的对象
     * @param <T>        类型参数
     * @return 如果集合对象为空或者集合的size为0就返回true, 否则返回false
     */
    public static <T> boolean isNullOrEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断是否为一个非空的数组
     *
     * @param array 传入的数组
     * @param <T>   数组的类型参数
     * @return 如果数组为null或者数组的长度为0就返回true, 否则返回false
     */
    public static <T> boolean isNotNullOrEmptyArray(T[] array) {
        return !(array == null || array.length == 0);
    }

    /**
     * 合并所有集合
     *
     * @param lists List类型的集合
     * @param <T>   集合的参数类型
     * @return 合并后的集合
     */
    public static <T> List<T> mergeAll(List<T>... lists) {
        List<T> mergedList = new ArrayList<T>();
        for (int i = 0, len = lists.length; i < len; i++) {
            List<T> list = lists[i];
            if (list != null && !list.isEmpty()) {
                for (int j = 0, lenj = list.size(); j < lenj; j++) {
                    T obj = list.get(j);
                    if (obj != null) {
                        mergedList.add(obj);
                    }
                }
            }
        }
        return mergedList;
    }

    /**
     * 用数组创建一个ArrayList
     *
     * @param objs 元素
     * @param <T>  元素的类型
     * @return ArrayList
     */
    public static <T> List<T> asList(T... objs) {
        if (objs == null) {
            return Collections.EMPTY_LIST;
        }
        List<T> list = new ArrayList<T>();
        Collections.addAll(list, objs);
        return list;
    }

    /**
     * 创建简单Map的快捷方法
     *
     * @param key   键
     * @param value 值
     * @param <K>   键的类型
     * @param <V>   值的类型
     * @return Map
     */
    public static <K, V> Map<K, V> makeMap(K key, V value) {
        Map<K, V> map = new HashMap<K, V>();
        map.put(key, value);
        return map;
    }

    /**
     * 创建简单Map的快捷方法
     *
     * @param key1   键1
     * @param value1 值1
     * @param key2   键2
     * @param value2 值2
     * @param <K>    键的类型
     * @param <V>    值的类型
     * @return Map
     */
    public static <K, V> Map<K, V> makeMap(K key1, V value1, K key2, V value2) {
        Map<K, V> map = new HashMap<K, V>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    /**
     * 数组包含
     *
     * @param array 数组
     * @param val   被包含值
     * @param <T>   类型
     * @return 是否包含
     */
    public static <T> boolean contains(T[] array, T val) {
        for (T t : array) {
            if (val.equals(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 用分隔符将字符串数组连接起来，构成一个字符串
     *
     * @param words     字符串数组
     * @param seperator 分隔符
     * @return 返回用分隔符连起来的字符串
     */
    public static String join(Object[] words, String seperator) {
        StringBuilder sb = new StringBuilder();
        if (words != null) {
            for (int i = 0; i < words.length; i++) {
                sb.append(words[i]);
                if (i < words.length - 1) {
                    sb.append(seperator);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 将集合中的元素用分隔符连起来，构成一个字符串
     *
     * @param collection 集合
     * @param seperator  分隔符
     * @return 返回用分隔符连起来的字符串
     */
    public static String join(Collection<?> collection, String seperator) {
        if (isNullOrEmpty(collection)) {
            return "";
        }
        Object[] objs = new Object[collection.size()];
        collection.toArray(objs);
        return join(objs, seperator);
    }

    /**
     * 复制一个数组
     *
     * @param sourceArray 要复制的目标数组
     * @param <T>         要复制的数组泛型类型
     * @return 新的数组
     */
    public static <T> T[] copy(T[] sourceArray) {
        if (sourceArray == null) {
            return null;
        }
        return Arrays.copyOf(sourceArray, sourceArray.length);
    }

    /**
     * Map<String, String>转字符串.
     *
     * @param map Map<String, String>对象
     * @return json字符串
     */
    public static String convertMapToJSONString(Map<String, Object> map) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        for (String key : map.keySet()) {
            stringBuilder.append("\"");
            stringBuilder.append(key);
            stringBuilder.append("\"");
            stringBuilder.append(":");
            stringBuilder.append("\"");
            stringBuilder.append(map.get(key));
            stringBuilder.append("\"");
            stringBuilder.append(",");
        }
        String result = stringBuilder.toString();
        if (result.endsWith(",")) {
            result = result.substring(0, result.length() - 1);
        }
        return result + "}";
    }

}