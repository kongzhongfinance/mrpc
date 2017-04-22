package com.kongzhong.mrpc.registry;

/**
 * 服务发现接口
 */
public interface ServiceDiscovery {

    void discover();

    void stop();
}