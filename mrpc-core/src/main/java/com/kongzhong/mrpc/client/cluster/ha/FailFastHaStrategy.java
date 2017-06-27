package com.kongzhong.mrpc.client.cluster.ha;

import com.kongzhong.mrpc.client.RpcProcessor;
import com.kongzhong.mrpc.client.cluster.HaStrategy;
import com.kongzhong.mrpc.client.cluster.LoadBalance;
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
        RpcProcessor invoker = loadBalance.getInvoker(request.getClassName());
        try {
            return invoker.processor(request);
        } catch (Exception e) {
            if (e instanceof ServiceException) {
                throw (Exception) e.getCause();
            } else {
                throw e;
            }
        }
    }

}