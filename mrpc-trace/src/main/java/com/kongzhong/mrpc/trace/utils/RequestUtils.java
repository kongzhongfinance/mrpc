package com.kongzhong.mrpc.trace.utils;

/**
 * @author biezhi
 * @date 2017/12/4
 */
public class RequestUtils {

    public static String getServerName(String className, String methodName){
        int pos = className.lastIndexOf(".");
        return String.valueOf(className.substring(pos + 1) + "." + methodName).toLowerCase();
    }

}