package com.kongzhong.mrpc.client.proxy;

import com.google.common.reflect.AbstractInvocationHandler;
import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.annotation.Command;
import com.kongzhong.mrpc.annotation.Comment;
import com.kongzhong.mrpc.client.LocalServiceNodeTable;
import com.kongzhong.mrpc.client.cluster.HaStrategy;
import com.kongzhong.mrpc.client.cluster.LoadBalance;
import com.kongzhong.mrpc.client.cluster.ha.HighAvailableFactory;
import com.kongzhong.mrpc.client.cluster.loadblance.LoadBalanceFactory;
import com.kongzhong.mrpc.client.invoke.ClientInvocation;
import com.kongzhong.mrpc.client.invoke.RpcInvoker;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.embedded.ConfigServiceImpl;
import com.kongzhong.mrpc.enums.HaStrategyEnum;
import com.kongzhong.mrpc.enums.LbStrategyEnum;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.exception.SystemException;
import com.kongzhong.mrpc.interceptor.InterceptorChain;
import com.kongzhong.mrpc.interceptor.Invocation;
import com.kongzhong.mrpc.interceptor.RpcClientInterceptor;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;

import static com.kongzhong.mrpc.Const.CLIENT_INTERCEPTOR_PREFIX;

/**
 * 默认的客户端代理
 *
 * @author biezhi
 * 2017/4/28
 */
@Slf4j
public class SimpleClientProxy extends AbstractInvocationHandler {

    /**
     * 负载均衡器
     */
    private LoadBalance loadBalance;

    /**
     * 是否有客户端拦截器
     */
    private boolean hasInterceptors;

    /**
     * 客户端拦截器列表
     */
    private List<RpcClientInterceptor> interceptors;

    /**
     * 代理接口超时时长
     */
    private Integer waitTimeout;

    /**
     * 配置文件全局APPID，标识一个应用
     */
    private String appId;

    public SimpleClientProxy(Integer waitTimeout, List<RpcClientInterceptor> interceptors) {
        this.waitTimeout = waitTimeout;
        this.appId = ClientConfig.me().getAppId();

        LbStrategyEnum lbStrategy = ClientConfig.me().getLbStrategy();
        if (null == lbStrategy) {
            throw new SystemException("LoadBalance strategy not is null.");
        }

        this.interceptors = interceptors;
        this.loadBalance = LoadBalanceFactory.getLoadBalance(lbStrategy);

        if (null != interceptors && !interceptors.isEmpty()) {
            hasInterceptors = true;
            int pos = interceptors.size();
            log.info("Add interceptor {}", interceptors.toString());
            for (RpcClientInterceptor rpcClientInterceptor : interceptors) {
                InterceptorChain interceptorChain = new InterceptorChain();
                interceptorChain.addLast(CLIENT_INTERCEPTOR_PREFIX + (pos--), rpcClientInterceptor);
            }
        }
    }


    public SimpleClientProxy(List<RpcClientInterceptor> interceptors) {
        this.appId = ClientConfig.me().getAppId();

        LbStrategyEnum lbStrategy = ClientConfig.me().getLbStrategy();
        if (null == lbStrategy) {
            throw new SystemException("LoadBalance strategy not is null.");
        }

        this.interceptors = interceptors;
        this.loadBalance = LoadBalanceFactory.getLoadBalance(lbStrategy);

        if (null != interceptors && !interceptors.isEmpty()) {
            hasInterceptors = true;
            int pos = interceptors.size();
            log.info("Add interceptor {}", interceptors.toString());
            for (RpcClientInterceptor rpcClientInterceptor : interceptors) {
                InterceptorChain interceptorChain = new InterceptorChain();
                interceptorChain.addLast(CLIENT_INTERCEPTOR_PREFIX + (pos--), rpcClientInterceptor);
            }
        }
    }

    @Override
    protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {

        String appId = this.getAppId(method.getDeclaringClass());

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
                .fallbackType(this.getFallbackType(method))
                .fallbackMethod(this.getFallbackMethod(method))
                .build();

        setContext(request, method);

        HaStrategy haStrategy = HighAvailableFactory.getHaStrategy(this.getHaStrategy(method));
        if (!hasInterceptors) {
            return haStrategy.call(request, loadBalance);
        }

        SimpleClientHandler clientHandler = loadBalance.next(appId, request.getClassName());
        if (null == clientHandler) {
            log.warn("Local service mappings: {}", LocalServiceNodeTable.SERVICE_MAPPINGS);
            throw new RpcException("Service [" + request.getClassName() + "] not found.");
        }

        RpcInvoker rpcInvoker = new RpcInvoker(request, clientHandler);
        Invocation invocation = new ClientInvocation(rpcInvoker, interceptors);
        return invocation.next();
    }

    /**
     * 获取该方法的高可用策略
     *
     * @param method 调用的方法
     * @return 返回该方法的高可用策略
     */
    private HaStrategyEnum getHaStrategy(Method method) {
        HaStrategyEnum haStrategyEnum = ClientConfig.me().getHaStrategy();
        Command        command        = method.getAnnotation(Command.class);
        if (null != command) {
            haStrategyEnum = command.haStrategy();
        }
        return haStrategyEnum;
    }

    /**
     * 获取该方法的调用超时
     *
     * @param serviceType 调用的方法
     * @return 返回该方法的超时时长
     */
    private String getAppId(Class<?> serviceType) {
        Command command = serviceType.getAnnotation(Command.class);
        if (null != command && StringUtils.isNotEmpty(command.appId())) {
            return command.appId();
        }
        return this.appId;
    }

    /**
     * 获取该方法的调用超时
     *
     * @param method 调用的方法
     * @return 返回该方法的超时时长
     */
    private Integer getWaitTimeout(Method method) {
        Integer timeout = ConfigServiceImpl.me().getMethodWaitTimeout(method.getName());
        if (null != timeout) {
            return timeout;
        }
        Command command = method.getAnnotation(Command.class);
        timeout = this.waitTimeout != null ? this.waitTimeout : ClientConfig.me().getWaitTimeout();
        if (null != command) {
            return command.waitTimeout();
        }
        return timeout;
    }

    private void setContext(RpcRequest request, Method method) {
        Comment comment = method.getDeclaringClass().getAnnotation(Comment.class);
        if (null != comment) {
            if (StringUtils.isNotEmpty(comment.name())) {
                request.addContext(Const.SERVER_NAME, comment.name());
            }
            if (comment.owners().length > 0) {
                request.addContext(Const.SERVER_OWNER, String.join(",", comment.owners()));
            }
        }
    }

    private String getFallbackType(Method method) {
        Command command = method.getAnnotation(Command.class);
        if (null != command && !"".equals(command.fallbackType())) {
            return command.fallbackType();
        }
        return null;
    }

    private String getFallbackMethod(Method method) {
        Command command = method.getAnnotation(Command.class);
        if (null != command && !"".equals(command.fallbackMethod())) {
            return command.fallbackMethod();
        }
        return method.getName();
    }

}