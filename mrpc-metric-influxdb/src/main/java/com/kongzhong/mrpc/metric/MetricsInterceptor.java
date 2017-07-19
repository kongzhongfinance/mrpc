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

    private boolean       classLevel    = true;
    private MetricsClient metricsClient = null;
    private MetricsUtils  metricsUtils  = null;

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
            this.classLevel = metricsProperties.getParticle().equalsIgnoreCase(ParticleLevel.CLASS.name());
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
            if (classLevel) {
                method = null;
            }
            try {
                metricsUtils.success(clazz, method, "", begin);
            } catch (Exception e) {
                log.error("Metrics调用失败", e);
            }
            return bean;
        } catch (Exception e) {
            if (classLevel) {
                method = null;
            }
            try {
                if (e instanceof SystemException) {
                    metricsUtils.systemFail(clazz, method, "", begin);
                } else {
                    metricsUtils.serviceFail(clazz, method, "", begin);
                }
            } catch (Exception e2) {
                log.error("Metrics调用失败", e2);
            }
            throw e;
        }
    }

}
