package com.kongzhong.mrpc.serialize;

/**
 * 序列化接口
 *
 * Created by biezhi on 2016/11/6.
 */
public interface RpcSerialize {

    /**
     * 消息长度
     */
    int MESSAGE_LENGTH = 4;

    /**
     * 将对象序列化为byte数组
     *
     * @param obj
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> byte[] serialize(T obj) throws Exception;

    /**
     * 反序列化数据为Class类型
     *
     * @param data
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> T deserialize(byte[] data, Class<T> clazz) throws Exception;

}