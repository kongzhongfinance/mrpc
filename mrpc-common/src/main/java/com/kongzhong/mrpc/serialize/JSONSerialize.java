package com.kongzhong.mrpc.serialize;

import com.kongzhong.mrpc.exception.SerializeException;

import java.lang.reflect.Type;

/**
 * JSON序列化接口
 *
 * @author biezhi
 *         2017/5/11
 */
public interface JSONSerialize {

    /**
     * 对象转json
     *
     * @param object java对象
     * @return
     */
    String toJSONString(Object object) throws SerializeException;

    /**
     * 对象转json，是否格式化输出
     *
     * @param object
     * @param pretty
     * @return
     */
    String toJSONString(Object object, boolean pretty) throws SerializeException;

    /**
     * json转对象
     *
     * @param json json字符串
     * @param type java类型
     * @param <T>  泛型
     * @return
     */
    <T> T parseObject(String json, Class<T> type) throws SerializeException;

    /**
     * json转obj
     */
    <T> T parseObject(String json, Type type) throws SerializeException;
}
