package com.kongzhong.mrpc.client.proxy;

import com.google.common.reflect.AbstractInvocationHandler;
import com.kongzhong.mrpc.client.cluster.ha.HaStrategy;
import com.kongzhong.mrpc.client.cluster.loadblance.LoadBalance;
import com.kongzhong.mrpc.client.cluster.loadblance.SimpleLoadBalance;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.utils.StringUtils;

import java.lang.reflect.Method;

/**
 * @author biezhi
 *         2017/4/28
 */
public class SimpleClientProxy<T> extends AbstractInvocationHandler {

    /**
     * 负载均衡器
     */
    protected LoadBalance loadBalance = new SimpleLoadBalance();

    /**
     * HA策略
     */
    protected HaStrategy haStrategy = ClientConfig.me().getHaStrategy();

    @Override
    protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Exception {
        RpcRequest request = new RpcRequest(StringUtils.getUUID(),
                method.getDeclaringClass().getName(), method.getName(),
                method.getParameterTypes(), args, method.getReturnType());
        return haStrategy.call(request, loadBalance);
    }

}
