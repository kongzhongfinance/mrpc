package com.kongzhong.mrpc.client.cluster.ha;

import com.kongzhong.mrpc.client.cluster.loadblance.LoadBalance;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;

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