package com.kongzhong.mrpc.metric;


import com.kongzhong.mrpc.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class MetricsUtils {

    private MetricsClient metricsClient;

    public MetricsUtils(MetricsClient metricsClient) {
        this.metricsClient = metricsClient;
    }

    public void general(Class clazz, String method, String name, String type, long count, long time, Map.Entry<String, String>... tags) {
        if (StringUtils.isEmpty(metricsClient.getAppId()))
            throw new RuntimeException("请在配置文件中设置metrics.appId为程序的名称");
        Map<String, String> tagMap = new HashMap<>();

        String metrics = "[" + metricsClient.getAppId() + "]";
        if (clazz != null) {
            tagMap.put("class", clazz.getName());
            metrics += "_" + "{" + clazz.getName().replaceAll("\\.", "_") + "}";
        }
        if (!StringUtils.isEmpty(method)) {
            tagMap.put("method", method);
            metrics += "_" + method;
        }
        if (!StringUtils.isEmpty(name)) {
            tagMap.put("name", name);
            metrics += "_" + name;
        }
        if (!StringUtils.isEmpty(type)) {
            tagMap.put("type", type);
            metrics += "_" + type;
        }
        for (Map.Entry<String, String> tag : tags) {
            tagMap.put(tag.getKey(), tag.getValue());
        }
        metricsClient.write(metrics, count, time, tagMap);
    }

    public void success(Class clazz, String method, String name, long begin, Map.Entry<String, String>... tags) {
        general(clazz, method, name, "success", 1, System.currentTimeMillis() - begin, tags);
    }

    public void systemFail(Class clazz, String method, String name, long begin, Map.Entry<String, String>... tags) {
        general(clazz, method, name, "systemFail", 1, System.currentTimeMillis() - begin, tags);
    }

    public void serviceFail(Class clazz, String method, String name, long begin, Map.Entry<String, String>... tags) {
        general(clazz, method, name, "serviceFail", 1, System.currentTimeMillis() - begin, tags);
    }

    public void successMultipleTimes(Class clazz, String method, String name, long count, long begin, Map.Entry<String, String>... tags) {
        general(clazz, method, name, "success", count, System.currentTimeMillis() - begin, tags);
    }

    public void systemFailMultipleTimes(Class clazz, String method, String name, long count, long begin, Map.Entry<String, String>... tags) {
        general(clazz, method, name, "systemFail", count, System.currentTimeMillis() - begin, tags);
    }

    public void serviceFailMultipleTimes(Class clazz, String method, String name, long count, long begin, Map.Entry<String, String>... tags) {
        general(clazz, method, name, "serviceFail", count, System.currentTimeMillis() - begin, tags);
    }
}
