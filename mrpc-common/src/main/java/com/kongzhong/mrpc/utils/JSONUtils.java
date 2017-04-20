package com.kongzhong.mrpc.utils;

import com.alibaba.fastjson.JSON;

/**
 * @author biezhi
 *         2017/4/20
 */
public class JSONUtils {

    public static String toJSONString(Object object) {
        return JSON.toJSONString(object);
    }

}