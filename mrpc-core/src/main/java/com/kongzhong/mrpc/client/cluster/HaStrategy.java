package com.kongzhong.mrpc.client.cluster;

import com.kongzhong.mrpc.model.RpcRequest;

/**
 * HA策略
 *
 * @author biezhi
 *         2017/4/24
 */
@FunctionalInterface
public interface HaStrategy {

    Object call(RpcRequest request, LoadBalance loadBalance) throws Exception;

}