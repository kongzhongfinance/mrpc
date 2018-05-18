package com.kongzhong.mrpc.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;

/**
 * 集合工具类
 *
 * @author biezhi
 *         23/06/2017
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CollectionUtils {

    /**
     * 返回一个集合是否为空
     *
     * @param collection
     * @return
     */
    public static boolean isEmpty(Collection collection) {
        return null == collection || collection.isEmpty();
    }

    /**
     * 返回一个集合是否为空
     *
     * @param collection
     * @return
     */
    public static boolean isNotEmpty(Collection collection) {
        return null != collection && !collection.isEmpty();
    }

    /**
     * 返回一个数组是否为空
     *
     * @param arr
     * @param <T>
     * @return
     */
    public static <T> boolean isNotEmpty(T[] arr) {
        return null != arr && arr.length > 0;
    }

    /**
     * 返回一个map是否为空
     *
     * @param map
     * @return
     */
    public static boolean isNotEmpty(Map map) {
        return null != map && !map.isEmpty();
    }

}
