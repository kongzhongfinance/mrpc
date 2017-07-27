package com.kongzhong.mrpc.utils;

/**
 * @author biezhi
 * @date 2017/7/27
 */
public class TimeUtils {

    public static long currentMicros(){
        return System.currentTimeMillis() * 1000;
    }

    public static String currentMicrosString(){
        return currentMicros() + "";
    }

}
