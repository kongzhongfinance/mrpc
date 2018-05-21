package com.kongzhong.mrpc.metric;

import com.kongzhong.mrpc.exception.SystemException;
import com.kongzhong.mrpc.interceptor.RpcServerInterceptor;
import com.kongzhong.mrpc.interceptor.ServerInvocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 监控拦截器
 *
 * @author biezhi
 * 2017/4/24
 */
@EnableConfigurationProperties(MetricsProperties.class)
@Slf4j
public class MetricsInterceptor implements RpcServerInterceptor {

    private MetricsClient metricsClient = null;
    private MetricsUtils  metricsUtils  = null;

    @Resource
    private MetricsProperties metricsProperties;

    @Value("${common.appId}")
    private String appId;

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
            this.metricsClient = new MetricsClient(metricsProperties, appId);
            this.initMetricsUtils();
        }
    }

    private void initMetricsUtils() {
        this.metricsClient.init();
        this.metricsUtils = new MetricsUtils(metricsClient);
    }

    @Override
    public Object execute(ServerInvocation invocation) throws Throwable {
        String className = invocation.getRequest().getClassName();
        long   begin     = System.currentTimeMillis();
        try {
            Object bean = invocation.next();
            try {
                metricsUtils.generalServers("success", 1, System.currentTimeMillis() - begin);
                metricsUtils.success(className, null, "", begin);
            } catch (Exception e) {
                log.error("Metrics调用失败", e);
            }
            return bean;
        } catch (Exception e) {
            try {
                if (e instanceof SystemException) {
                    metricsUtils.generalServers("systemFail", 1, System.currentTimeMillis() - begin);
                    metricsUtils.systemFail(className, null, "", begin);
                } else {
                    metricsUtils.generalServers("serviceFail", 1, System.currentTimeMillis() - begin);
                    metricsUtils.serviceFail(className, null, "", begin);
                }
            } catch (Exception e2) {
                log.error("Metrics调用失败", e2);
            }
            throw e;
        }
    }

}
