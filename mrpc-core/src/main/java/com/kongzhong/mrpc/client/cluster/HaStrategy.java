package com.kongzhong.mrpc.client.cluster;

import com.kongzhong.mrpc.client.invoke.RpcInvoker;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;

/**
 * HA策略
 *
 * @author biezhi
 * 2017/4/24
 */
@FunctionalInterface
public interface HaStrategy {

    Object call(RpcRequest request, LoadBalance loadBalance) throws Exception;

    default Object invoke(RpcRequest request, LoadBalance loadBalance) throws Exception {
        SimpleClientHandler clientHandler = loadBalance.next(request.getAppId(), request.getClassName());
        clientHandler.addHit();
        RpcInvoker rpcInvoker = new RpcInvoker(request, clientHandler);
        return rpcInvoker.invoke();
    }
}