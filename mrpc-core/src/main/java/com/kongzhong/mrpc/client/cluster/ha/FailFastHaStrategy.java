package com.kongzhong.mrpc.client.cluster.ha;

import com.kongzhong.mrpc.client.RpcInvoker;
import com.kongzhong.mrpc.client.cluster.loadblance.LoadBalance;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.exception.ServiceException;
import com.kongzhong.mrpc.model.RpcRequest;

/**
 * 快速失败策略
 *
 * @author biezhi
 *         2017/4/24
 */
public class FailFastHaStrategy implements HaStrategy {

    @Override
    public Object call(RpcRequest request, LoadBalance loadBalance) throws Exception {
        RpcInvoker invoker = loadBalance.getInvoker(request.getClassName());
        try {
            return invoker.invoke(request);
        } catch (Throwable e) {
            if (e instanceof ServiceException) {
                throw (Exception) e.getCause();
            }
            throw new RpcException(e);
        }
    }

}