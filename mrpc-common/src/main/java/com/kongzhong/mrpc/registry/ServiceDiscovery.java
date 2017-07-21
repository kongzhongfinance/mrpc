package com.kongzhong.mrpc.registry;

import com.kongzhong.mrpc.model.ClientBean;

/**
 * 服务发现接口
 */
public interface ServiceDiscovery {

    void discover(ClientBean clientBean) throws Exception;

    void stop();
}