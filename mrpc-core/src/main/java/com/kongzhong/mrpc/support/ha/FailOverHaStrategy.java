package com.kongzhong.mrpc.support.ha;

import com.kongzhong.mrpc.client.RpcInvoker;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.support.loadblance.LoadBalance;
import io.netty.util.concurrent.FastThreadLocal;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 失效切换策略
 *
 * @author biezhi
 *         2017/4/24
 */
@Slf4j
public class FailOverHaStrategy implements HaStrategy {

    //确保每个线程持有一份单独的ArrayList<Referer<T>（FailOverHaStrategy，会有单个实例被多个线程调用）
    protected FastThreadLocal<List> referersHolder = new FastThreadLocal<>();

    @Override
    public Object call(RpcRequest request, LoadBalance loadBalance) {
        int rc = getRetryCount(request.getMethod());
        if (rc < 0) {
            rc = 0;
        }
        List<RpcInvoker> handlers = getReferers(loadBalance);
        for (int i = 0; i <= rc; i++) {
            RpcInvoker referer = handlers.get(i % handlers.size());
            try {
                return referer.invoke(request);
            } catch (Throwable e) {
                // 对于业务异常，直接抛出
                if (i >= rc) {
                    throw new RpcException(e);
                }
                log.warn(String.format("FailOverHaStrategy Call false for request:%s error=%s", request, e.getMessage()));
            }
        }
        throw new RpcException("FailOverHaStrategy.invoke should not come here!");
    }


    /**
     * 获取重试次数
     *
     * @param method
     * @return
     */
    private int getRetryCount(Method method) {
        int defaultRetryCount = ClientConfig.me().getRetryCount();
        return defaultRetryCount;
    }

    public List<RpcInvoker> getReferers(LoadBalance loadBalance) {
        return loadBalance.getInvokers();
    }
}
