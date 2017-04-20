package com.kongzhong.mrpc.utils;

import java.util.UUID;

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

}