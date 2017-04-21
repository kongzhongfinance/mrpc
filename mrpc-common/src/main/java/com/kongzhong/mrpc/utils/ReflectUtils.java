package com.kongzhong.mrpc.utils;

import java.io.Serializable;

/**
 * @author biezhi
 *         2017/4/21
 */
public class ReflectUtils {

    public static boolean isImpl(Class<?> type) {
        if (!type.isInterface() &&
                type.getInterfaces().length > 0 &&
                !type.getInterfaces()[0].equals(Serializable.class)) {
            return true;
        }
        return false;
    }

    public static Class<?> getInterface(Class<?> type) {
        if (isImpl(type)) {
            return type.getInterfaces()[0];
        }
        return null;
    }

}
