package com.kongzhong.mrpc.metric;

import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.interceptor.Invocation;
import com.kongzhong.mrpc.interceptor.RpcInteceptor;
import com.kongzhong.mrpc.model.RpcContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 监控拦截器
 *
 * @author biezhi
 *         2017/4/24
 */
@Slf4j
public class MetricInterceptor implements RpcInteceptor {

    private MetricsClient metricsClient;
    private MetricsUtils metricsUtils;

    @Getter
    @Setter
    private String name = "";

    public MetricInterceptor(MetricsClient metricsClient) {
        this.metricsClient = metricsClient;
        this.metricsClient.init();
        this.metricsUtils = new MetricsUtils(metricsClient);
    }

    @Override
    public Object execute(Invocation invocation) throws Exception {
        Class<?> clazz = invocation.getTarget().getClass();
        String method = invocation.getFastMethod().getJavaMethod().toString();
        String appId = RpcContext.get().getRpcRequest().getAppId();
        long begin = System.currentTimeMillis();
        try {
            Object bean = invocation.next();
            metricsUtils.success(clazz, method, name, begin);
            return bean;
        } catch (Exception e) {
            if (e instanceof RpcException) {
                metricsUtils.systemFail(clazz, method, name, begin);
            } else {
                metricsUtils.serviceFail(clazz, method, name, begin);
            }
            throw e;
        }
    }

}
