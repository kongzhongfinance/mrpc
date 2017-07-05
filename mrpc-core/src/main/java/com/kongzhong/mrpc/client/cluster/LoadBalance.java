package com.kongzhong.mrpc.client.cluster;

import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;

/**
 * 负载均衡接口
 * <p>
 * Created by biezhi on 2016/12/30.
 */
public interface LoadBalance {

    /**
     * 根据服务查询连接通道
     *
     * @param serviceName
     * @return
     */
    SimpleClientHandler next(String serviceName) throws Exception;

}