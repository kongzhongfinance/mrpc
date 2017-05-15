package com.kongzhong.mrpc.serialize;

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
    String toJSONString(Object object);

    /**
     * json转对象
     *
     * @param json json字符串
     * @param type java类型
     * @param <T>  泛型
     * @return
     */
    <T> T parseObject(String json, Class<T> type);

}
