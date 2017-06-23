package com.kongzhong.mrpc.client.cluster.ha;

import com.kongzhong.mrpc.client.RpcInvoker;
import com.kongzhong.mrpc.client.cluster.HaStrategy;
import com.kongzhong.mrpc.client.cluster.LoadBalance;
import com.kongzhong.mrpc.config.ClientCommonConfig;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.exception.ServiceException;
import com.kongzhong.mrpc.model.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 失效切换策略
 *
 * @author biezhi
 *         2017/4/24
 */
@Slf4j
public class FailOverHaStrategy implements HaStrategy {

    @Override
    public Object call(RpcRequest request, LoadBalance loadBalance) throws Exception {
        int rc = ClientCommonConfig.me().getFailOverRetry();
        if (rc < 0) {
            rc = 0;
        }
        String serviceName = request.getClassName();
        for (int i = 0; i <= rc; i++) {
            try {
                RpcInvoker rpcInvoker = loadBalance.getInvoker(serviceName);
                return rpcInvoker.invoke(request);
            } catch (Exception e) {
                if (e instanceof ServiceException) {
                    throw (Exception) e.getCause();
                } else if (e instanceof RpcException) {
                    if (i >= rc) {
                        log.error("", e);
                        throw e;
                    }
                    TimeUnit.MILLISECONDS.sleep(100);
                    log.debug("Client retry [{}]", i + 1);
                } else {
                    log.warn(String.format("FailOverHaStrategy Call false for request:%s error=%s", request, e.getMessage()));
                }
            }
        }
        throw new RpcException("FailOverHaStrategy.invoke should not come here!");
    }

}