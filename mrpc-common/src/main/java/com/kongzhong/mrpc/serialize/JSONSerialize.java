package com.kongzhong.mrpc.serialize;

/**
 * @author biezhi
 *         2017/5/11
 */
public interface JSONSerialize {

    String toJSONString(Object object);

    <T> T parseObject(String json, Class<T> type);

}
