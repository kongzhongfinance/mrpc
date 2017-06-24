package com.kongzhong.mrpc.client.cluster;

import com.kongzhong.mrpc.client.RpcProcessor;

/**
 * 负载均衡接口
 *
 * Created by biezhi on 2016/12/30.
 */
public interface LoadBalance {

    /**
     * 根据服务查询调用执行器
     *
     * @param serviceName
     * @return
     */
    RpcProcessor getInvoker(String serviceName) throws Exception;

}