package com.kongzhong.mrpc.registry;

/**
 * 服务发现接口
 */
public interface ServiceDiscovery {

    String discover();

    void stop();
}