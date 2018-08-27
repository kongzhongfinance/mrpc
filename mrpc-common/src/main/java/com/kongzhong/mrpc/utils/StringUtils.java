package com.kongzhong.mrpc.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 字符串工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringUtils {

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        if (str == null || str.trim().length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断字符串是否非空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static String getUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replaceAll("-", "");
    }

    public static String getUUID(String fix) {
        UUID uuid = UUID.randomUUID();
        return fix + "_" + (uuid.toString().replaceAll("-", ""));
    }

    public static String getSimpleClassName(Class<?> clazz) {
        String name = clazz.getName();
        if (name.indexOf('.') != -1) {
            name = name.substring(name.lastIndexOf('.') + 1);
        }
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    public static boolean isLong(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}