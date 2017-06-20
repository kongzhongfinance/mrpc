package com.kongzhong.mrpc.registry;

/**
 * 服务注册接口
 */
public interface ServiceRegistry {

    void register(String data);

    void unregister(String data);
}