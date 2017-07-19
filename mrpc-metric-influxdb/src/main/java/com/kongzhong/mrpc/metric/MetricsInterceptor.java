package com.kongzhong.mrpc.metric;

import com.kongzhong.mrpc.exception.SystemException;
import com.kongzhong.mrpc.interceptor.RpcServerInterceptor;
import com.kongzhong.mrpc.interceptor.ServerInvocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 监控拦截器
 *
 * @author biezhi
 *         2017/4/24
 */
@EnableConfigurationProperties(MetricsProperties.class)
@Slf4j
public class MetricsInterceptor implements RpcServerInterceptor {

    private MetricsClient metricsClient;
    private MetricsUtils  metricsUtils;

    @Resource
    private MetricsProperties metricsProperties;

    public MetricsInterceptor() {
    }

    public MetricsInterceptor(MetricsClient metricsClient) {
        this.metricsClient = metricsClient;
        this.initMetricsUtils();
    }

    @PostConstruct
    public void postConstr() {
        if (null != metricsProperties) {
            log.info("{}", metricsProperties);
            this.metricsClient = new MetricsClient(metricsProperties);
            this.initMetricsUtils();
        }
    }

    private void initMetricsUtils() {
        this.metricsClient.init();
        this.metricsUtils = new MetricsUtils(metricsClient);
    }

    @Override
    public Object execute(ServerInvocation invocation) throws Exception {
        Class<?> clazz  = invocation.getTarget().getClass();
        String   method = invocation.getFastMethod().getName();
        long     begin  = System.currentTimeMillis();
        try {
            Object bean = invocation.next();
            metricsUtils.success(clazz, method, metricsClient.getName(), begin);
            return bean;
        } catch (Exception e) {
            if (e instanceof SystemException) {
                metricsUtils.systemFail(clazz, method, metricsClient.getName(), begin);
            } else {
                metricsUtils.serviceFail(clazz, method, metricsClient.getName(), begin);
            }
            throw e;
        }
    }

}
