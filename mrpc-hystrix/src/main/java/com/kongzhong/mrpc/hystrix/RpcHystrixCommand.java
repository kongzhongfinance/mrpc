package com.kongzhong.mrpc.hystrix;

import com.kongzhong.mrpc.client.SimpleRpcClient;
import com.kongzhong.mrpc.client.invoke.RpcInvoker;
import com.kongzhong.mrpc.exception.SystemException;
import com.kongzhong.mrpc.utils.ReflectUtils;
import com.netflix.hystrix.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * RpcHystrixCommand
 *
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
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(DEFAULT_THREADPOOL_CORE_SIZE)));//线程池为30
        this.invoker = invoker;
    }

    @Override
    protected Object run() throws Exception {
        try {
            return invoker.invoke();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    @Override
    protected Object getFallback() {
        try {
            Class<?> type = ReflectUtils.from(invoker.getRequest().getFallbackType());
            if (null == type) {
                throw new SystemException(String.format("Unable to construct [%s]", invoker.getRequest().getFallbackType()));
            }
            Object bean = SimpleRpcClient.getBean(type);
            if (null == bean) {
                throw new SystemException(String.format("Can't find bean [%s]", invoker.getRequest().getFallbackType()));
            }
            Method method = type.getMethod(invoker.getRequest().getFallbackMethod(), invoker.getRequest().getParameterTypes());
            if (null == method) {
                throw new SystemException(String.format("Can't find method [%s]", invoker.getRequest().getFallbackMethod()));
            }
            return method.invoke(bean, invoker.getRequest().getParameters());
        } catch (Exception e) {
            throw new SystemException("No fallback available.");
        }
    }
}
