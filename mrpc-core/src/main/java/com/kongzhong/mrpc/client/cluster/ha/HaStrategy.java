package com.kongzhong.mrpc.client.cluster.ha;

import com.kongzhong.mrpc.client.cluster.loadblance.LoadBalance;
import com.kongzhong.mrpc.model.RpcRequest;

/**
 * @author biezhi
 *         2017/4/24
 */
public interface HaStrategy {

    Object call(RpcRequest request, LoadBalance loadBalance) throws Exception;

}