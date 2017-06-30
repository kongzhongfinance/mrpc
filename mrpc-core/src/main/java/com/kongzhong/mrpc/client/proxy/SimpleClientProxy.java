package com.kongzhong.mrpc.client.proxy;

import com.google.common.reflect.AbstractInvocationHandler;
import com.kongzhong.mrpc.annotation.Command;
import com.kongzhong.mrpc.client.cluster.HaStrategy;
import com.kongzhong.mrpc.client.cluster.LoadBalance;
import com.kongzhong.mrpc.client.cluster.ha.HighAvailableFactory;
import com.kongzhong.mrpc.client.cluster.loadblance.SimpleLoadBalance;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.enums.HaStrategyEnum;
import com.kongzhong.mrpc.enums.LbStrategyEnum;
import com.kongzhong.mrpc.exception.SystemException;
import com.kongzhong.mrpc.interceptor.ClientInvocation;
import com.kongzhong.mrpc.interceptor.InterceptorChain;
import com.kongzhong.mrpc.interceptor.Invocation;
import com.kongzhong.mrpc.interceptor.RpcClientInterceptor;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;

import static com.kongzhong.mrpc.Const.CLIENT_INTERCEPTOR_PREFIX;

/**
 * 默认的客户端代理
 *
 * @author biezhi
 *         2017/4/28
 */
@Slf4j
public class SimpleClientProxy<T> extends AbstractInvocationHandler {

    // 负载均衡器
    protected LoadBalance loadBalance;

    // 是否有客户端拦截器
    protected boolean hasInterceptors;

    // 客户端拦截器列表
    protected List<RpcClientInterceptor> interceptors;

    // 拦截器链
    protected InterceptorChain interceptorChain = new InterceptorChain();

    private String appId;

    public SimpleClientProxy(List<RpcClientInterceptor> interceptors) {
        this.appId = ClientConfig.me().getAppId();

        LbStrategyEnum lbStrategy = ClientConfig.me().getLbStrategy();
        if (null == lbStrategy) {
            throw new SystemException("LoadBalance strategy not is null.");
        }

        this.interceptors = interceptors;
        this.loadBalance = new SimpleLoadBalance(lbStrategy);

        if (null != interceptors && !interceptors.isEmpty()) {
            hasInterceptors = true;
            int pos = interceptors.size();
            log.info("Add interceptors {}", interceptors.toString());
            for (RpcClientInterceptor rpcInteceptor : interceptors) {
                interceptorChain.addLast(CLIENT_INTERCEPTOR_PREFIX + (pos--), rpcInteceptor);
            }
        }
    }

    @Override
    protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Exception {
        RpcRequest request = RpcRequest.builder()
                .appId(appId)
                .requestId(StringUtils.getUUID())
                .methodName(method.getName())
                .className(method.getDeclaringClass().getName())
                .parameterTypes(method.getParameterTypes())
                .parameters(args)
                .returnType(method.getReturnType())
                .waitTimeout(this.getWaitTimeout(method))
                .timestamp(System.currentTimeMillis())
                .build();


        HaStrategy haStrategy = HighAvailableFactory.getHaStrategy(this.getHaStrategy(method));

        if (!hasInterceptors) {
            return haStrategy.call(request, loadBalance);
        }

        Invocation invocation = new ClientInvocation(haStrategy, loadBalance, request, interceptors);
        Object result = invocation.next();
        return result;
    }

    private HaStrategyEnum getHaStrategy(Method method) {
        HaStrategyEnum haStrategyEnum = ClientConfig.me().getHaStrategy();
        Command command = method.getAnnotation(Command.class);
        if (null != command) {
            haStrategyEnum = command.haStrategy();
        }
        return haStrategyEnum;
    }

    private int getWaitTimeout(Method method) {
        Command command = method.getAnnotation(Command.class);
        int timeout = ClientConfig.me().getWaitTimeout();
        if (null != command) {
            return command.waitTimeout();
        }
        return timeout;
    }
}
