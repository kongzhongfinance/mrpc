package com.kongzhong.mrpc.utils;

import java.util.concurrent.TimeUnit;

/**
 * @author biezhi
 * @date 2017/7/27
 */
public class TimeUtils {

    public static long currentMicros() {
        return System.currentTimeMillis() * 1000;
    }

    public static String currentMicrosString() {
        return currentMicros() + "";
    }

    public static void sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
