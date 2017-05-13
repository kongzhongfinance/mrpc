package com.kongzhong.mrpc.metric;

import com.kongzhong.mrpc.interceptor.Invocation;
import com.kongzhong.mrpc.interceptor.RpcInteceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * 监控拦截器
 *
 * @author biezhi
 *         2017/4/24
 */
@Slf4j
public class MetricInterceptor implements RpcInteceptor {

    public MetricInterceptor() {
    }

    @Override
    public Object execute(Invocation invocation) throws Exception {

        String method = invocation.getFastMethod().getJavaMethod().toString();

        Object bean = invocation.next();

        return bean;
    }

}
