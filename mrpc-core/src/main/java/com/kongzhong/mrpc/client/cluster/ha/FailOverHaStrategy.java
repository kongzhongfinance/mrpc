package com.kongzhong.mrpc.client.cluster.ha;

import com.kongzhong.mrpc.client.cluster.HaStrategy;
import com.kongzhong.mrpc.client.cluster.LoadBalance;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.exception.ConnectException;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.exception.ServiceException;
import com.kongzhong.mrpc.exception.SystemException;
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
        int rc    = ClientConfig.me().getFailOverRetry();
        int nodes = loadBalance.handlers(request.getClassName()).size();
        if (nodes == 1 || rc < 0) {
            rc = 0;
        }
        for (int i = 0; i <= rc; i++) {
            try {
                return invoke(request, loadBalance);
            } catch (Exception e) {
                if (e instanceof ConnectException) {
                    log.debug("{}", e.getMessage());
                    if (i >= rc) {
                        log.error("Connection error", e);
                        throw e;
                    }
                    TimeUnit.MILLISECONDS.sleep(100);
                    log.debug("Failover retry [{}]", i + 1);
                } else {
                    throw e;
                }
            }
        }
        throw new RpcException("Failover processor should not come here!");
    }

}