package com.kongzhong.mrpc.utils;

import com.kongzhong.mrpc.exception.SerializeException;
import com.kongzhong.mrpc.serialize.JacksonSerialize;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Type;

/**
 * JSON工具类
 *
 * @author biezhi
 *         2017/4/20
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JSONUtils {

    private static final JacksonSerialize jsonSerialize = new JacksonSerialize();

    public static String toJSONString(Object object) throws SerializeException {
        return jsonSerialize.toJSONString(object);
    }

    public static String toJSONString(Object object, boolean pretty) throws SerializeException {
        return jsonSerialize.toJSONString(object, pretty);
    }

    public static <T> T parseObject(String json, Class<T> type) throws SerializeException {
        return jsonSerialize.parseObject(json, type);
    }

    public static <T> T parseObject(String json, Type type) throws SerializeException {
        return jsonSerialize.parseObject(json, type);
    }
}