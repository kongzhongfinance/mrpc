package com.kongzhong.mrpc.metric;

import com.kongzhong.mrpc.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class MetricsUtils {

    private String        appId;
    private MetricsClient metricsClient;

    public MetricsUtils(MetricsClient metricsClient) {
        this.appId = metricsClient.getAppId();
        this.metricsClient = metricsClient;
    }

    public void generalServers(String type, long count, long time, Map.Entry<String, String>... tags) {
        Map<String, String> tagMap = new HashMap<>();

        String metrics = "[RPC_SERVERS]";

        tagMap.put("appId", appId);
        if (StringUtils.isNotEmpty(type)) {
            tagMap.put("type", type);
            metrics += "_" + type;
        }
        for (Map.Entry<String, String> tag : tags) {
            tagMap.put(tag.getKey(), tag.getValue());
        }

        metricsClient.write(metrics, count, time, tagMap);
    }

    public void general(String clazz, String method, String name, String type, long count, long time, Map.Entry<String, String>... tags) {
        Map<String, String> tagMap = new HashMap<>();

        String metrics = "[RPC_SERVER_" + appId + "]";

        tagMap.put("appId", appId);

        if (StringUtils.isNotEmpty(clazz)) {
            tagMap.put("class", clazz);
            metrics += "_" + clazz.replaceAll("\\.", "_");
        }
        if (StringUtils.isNotEmpty(method)) {
            tagMap.put("method", method);
            metrics += "_" + method;
        }
        if (StringUtils.isNotEmpty(name)) {
            tagMap.put("name", name);
            metrics += "_" + name;
        }
        if (StringUtils.isNotEmpty(type)) {
            tagMap.put("type", type);
            metrics += "_" + type;
        }
        for (Map.Entry<String, String> tag : tags) {
            tagMap.put(tag.getKey(), tag.getValue());
        }

        metricsClient.write(metrics, count, time, tagMap);
    }

    /**
     * 执行成功调用
     *
     * @param clazz  调用的类，如果希望measurement名字短一点或者为了统一可以不传
     * @param method 调用的方法名
     * @param name   方法中的逻辑名或处理的不同数据名
     * @param begin  方法执行开始时间
     * @param tags   额外的标签，比如用户ID
     */
    public void success(String clazz, String method, String name, long begin, Map.Entry<String, String>... tags) {
        general(clazz, method, name, "success", 1, System.currentTimeMillis() - begin, tags);
    }

    /**
     * 执行出现系统异常调用
     *
     * @param clazz  调用的类，如果希望measurement名字短一点或者为了统一可以不传
     * @param method 调用的方法名
     * @param name   方法中的逻辑名或处理的不同数据名
     * @param begin  方法执行开始时间
     * @param tags   额外的标签，比如用户ID
     */
    public void systemFail(String clazz, String method, String name, long begin, Map.Entry<String, String>... tags) {
        general(clazz, method, name, "systemFail", 1, System.currentTimeMillis() - begin, tags);
    }

    /**
     * 执行出现业务异常调用
     *
     * @param clazz  调用的类，如果希望measurement名字短一点或者为了统一可以不传
     * @param method 调用的方法名
     * @param name   方法中的逻辑名或处理的不同数据名
     * @param begin  方法执行开始时间
     * @param tags   额外的标签，比如用户ID
     */
    public void serviceFail(String clazz, String method, String name, long begin, Map.Entry<String, String>... tags) {
        general(clazz, method, name, "serviceFail", 1, System.currentTimeMillis() - begin, tags);
    }

    public void successMultipleTimes(String clazz, String method, String name, long count, long begin, Map.Entry<String, String>... tags) {
        general(clazz, method, name, "success", count, System.currentTimeMillis() - begin, tags);
    }

    public void systemFailMultipleTimes(String clazz, String method, String name, long count, long begin, Map.Entry<String, String>... tags) {
        general(clazz, method, name, "systemFail", count, System.currentTimeMillis() - begin, tags);
    }

    public void serviceFailMultipleTimes(String clazz, String method, String name, long count, long begin, Map.Entry<String, String>... tags) {
        general(clazz, method, name, "serviceFail", count, System.currentTimeMillis() - begin, tags);
    }
}
