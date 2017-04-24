package com.kongzhong.mrpc.metric;

import com.google.common.collect.Maps;
import com.kongzhong.mrpc.common.thread.NamedThreadFactory;
import com.kongzhong.mrpc.interceptor.Invocation;
import com.kongzhong.mrpc.interceptor.RpcInteceptor;
import com.kongzhong.mrpc.model.RpcContext;
import com.kongzhong.mrpc.model.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 监控拦截器
 *
 * @author biezhi
 *         2017/4/24
 */
@Slf4j
public class MetricInterceptor implements RpcInteceptor {

    private static final String START_TIME = "start_time";

    private final static Map<String, Metric> metricsMap = Maps.newConcurrentMap();

    public MetricInterceptor() {
    }

    private final static ScheduledFuture<?> scheduledFuture = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("metric")).scheduleAtFixedRate(() -> {
        metricsMap.forEach((key, value) -> {
            log.info("methodName: {}, current tps: {}, avgTime: {}, maxTime: {}, minTime: {} ",
                    key, value.getAndSet(), value.getTotalTime() / 60, value.getMaxTime(), value.getMinTime());
        });
    }, 0, 1, TimeUnit.SECONDS);

    private void before() {
        RpcContext.attr(START_TIME, String.valueOf(System.currentTimeMillis()));
    }

    private void after(RpcRequest request) {
        Long startTime = Long.valueOf(RpcContext.attr(START_TIME));
        long expend = System.currentTimeMillis() - startTime;
        String key = Thread.currentThread().getName() + ":" + request.getClassName() + "." + request.getMethodName();
        Metric metric = metricsMap.get(key);
        if (null == metric) {
            synchronized (this) {
                metric = metricsMap.get(key);
                if (null == metric) {
                    metric = new Metric();
                    metricsMap.put(key, metric);
                }
            }
        }
        // 消耗
        metric.incrementAndGetTPS();
        metric.expendTime(expend);
    }

    @Override
    public Object execute(Invocation invocation) throws Exception {
        this.before();
        Object bean = invocation.next();
        this.after(invocation.getRequest());
        return bean;
    }

}
