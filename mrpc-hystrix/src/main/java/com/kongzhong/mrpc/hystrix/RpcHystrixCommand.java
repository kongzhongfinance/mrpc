package com.kongzhong.mrpc.hystrix;

import com.google.common.reflect.Reflection;
import com.kongzhong.mrpc.client.SimpleRpcClient;
import com.kongzhong.mrpc.client.invoke.RpcInvoker;
import com.kongzhong.mrpc.exception.SystemException;
import com.netflix.hystrix.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * @author biezhi
 * @date 2017/7/26
 */
@Slf4j
public class RpcHystrixCommand extends HystrixCommand<Object> {

    private final RpcInvoker invoker;

    private static final int DEFAULT_THREADPOOL_CORE_SIZE = 30;

    public RpcHystrixCommand(RpcInvoker invoker) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(invoker.getRequest().getClassName()))
                .andCommandKey(HystrixCommandKey.Factory.asKey(String.format("%s_%d", invoker.getRequest().getMethodName(),
                        invoker.getRequest().getParameters() == null ? 0 : invoker.getRequest().getParameters().length)))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withCircuitBreakerRequestVolumeThreshold(20)//10秒钟内至少19此请求失败，熔断器才发挥起作用
                        .withCircuitBreakerSleepWindowInMilliseconds(30000)//熔断器中断请求30秒后会进入半打开状态,放部分流量过去重试
                        .withCircuitBreakerErrorThresholdPercentage(50)//错误率达到50开启熔断保护
                        .withExecutionTimeoutEnabled(false))//使用rpc的超时，禁用这里的超时
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(getThreadPoolCoreSize())));//线程池为30
        this.invoker = invoker;
    }

    /**
     * 获取线程池大小
     *
     * @return
     */
    private static int getThreadPoolCoreSize() {
//        if (url != null) {
//            int size = url.getParameter("ThreadPoolCoreSize", DEFAULT_THREADPOOL_CORE_SIZE);
//            if (logger.isDebugEnabled()) {
//                logger.debug("ThreadPoolCoreSize:" + size);
//            }
//            return size;
//        }
        return DEFAULT_THREADPOOL_CORE_SIZE;

    }

    @Override
    protected Object run() throws Exception {
        return invoker.invoke();
    }

    @Override
    protected Object getFallback() {
        try {
            Class<?> type = Class.forName(invoker.getRequest().getFallbackType());
            Object   bean = SimpleRpcClient.getBean(type);
            Method method =   type.getMethod(invoker.getRequest().getFallbackMethod(), invoker.getRequest().getParameterTypes());
            return method.invoke(bean, invoker.getRequest().getParameters());
        } catch (Exception e){
            throw new SystemException(e);
        }
    }
}
