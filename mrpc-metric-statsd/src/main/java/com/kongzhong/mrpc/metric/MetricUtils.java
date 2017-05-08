package com.kongzhong.mrpc.metric;

import com.kongzhong.mrpc.exception.ServiceException;
import com.kongzhong.mrpc.utils.StringUtils;

/**
 * kcj
 * 2015年12月15日 上午11:38:38
 */
public class MetricUtils {

    /**
     * 生成成功MetricName
     *
     * @param clazz
     * @param methodName
     * @return
     */
    public static String getSuccessMetricName(Class<?> clazz, String methodName) {
        return getSuccessMetricName(clazz.getName(), methodName);
    }

    /**
     * 生成成功MetricName
     *
     * @param clazz
     * @param methodName
     * @return
     */
    public static String getSuccessMetricName(String className, String methodName) {
        return getMetricName(className, methodName, "success");
    }

    /**
     * 生成系统异常MetricName
     *
     * @param clazz
     * @param methodName
     * @return
     */
    public static String getSysMetricName(Class<?> clazz, String methodName) {
        return getSysMetricName(clazz.getName(), methodName);
    }

    /**
     * 生成系统异常MetricName
     *
     * @param clazz
     * @param methodName
     * @return
     */
    public static String getSysMetricName(String className, String methodName) {
        return getMetricName(className, methodName, "fail");
    }

    /**
     * 生成业务异常MetricName
     *
     * @param clazz
     * @param methodName
     * @return
     */
    public static String getBusMetricName(Class<?> clazz, String methodName) {
        return getBusMetricName(clazz.getName(), methodName);
    }

    /**
     * 生成业务异常MetricName
     *
     * @param clazz
     * @param methodName
     * @return
     */
    public static String getBusMetricName(String className, String methodName) {
        return getMetricName(className, methodName, "fail");
    }

    /**
     * 生成延时异常MetricName
     *
     * @param clazz
     * @param methodName
     * @return
     */
    public static String getTimeMetricName(Class<?> clazz, String methodName) {
        return getTimeMetricName(clazz.getName(), methodName);
    }

    /**
     * 生成延时异常MetricName
     *
     * @param clazz
     * @param methodName
     * @return
     */
    public static String getTimeMetricName(String className, String methodName) {
        return getMetricName(className, methodName, "time_out");
    }

    /**
     * 生成MetricName
     *
     * @param clazz
     * @param methodName
     * @return
     */
    public static String getMetricName(Class<?> clazz, String methodName) {
        return getMetricName(clazz.getName(), methodName);
    }

    /**
     * 生成MetricName
     *
     * @param clazz
     * @param methodName
     * @return
     */
    public static String getMetricName(String className, String methodName) {
        return getMetricName(className, methodName, null);
    }

    /**
     * 生成MetricName
     *
     * @param clazz
     * @param methodName
     * @return
     */
    private static String getMetricName(String className, String methodName, String suffix) {
        String result = className + "-" + methodName;
        if (StringUtils.isNotEmpty(suffix)) {
            return result + suffix;
        }
        return result;
    }

    /**
     * 打成功metric点
     *
     * @param clazz
     * @param methodName
     */
    public static void recordSuccessMetric(String className, String methodName) {
        recordCountMetric(getSuccessMetricName(className, methodName));
    }

    /**
     * 打成功metric点
     *
     * @param clazz
     * @param methodName
     */
    public static void recordSuccessMetric(String metricName) {
        recordCountMetric(metricName + "success");
    }

    /**
     * 打成功metric点
     *
     * @param clazz
     * @param methodName
     */
    public static void recordSuccessMetric(Class<?> clazz, String methodName) {
        recordCountMetric(getSuccessMetricName(clazz, methodName));
    }

    /**
     * 打失败metric点
     *
     * @param clazz
     * @param methodName
     */
    public static void recordSysFailMetric(Class<?> clazz, String methodName) {
        recordSysFailMetric(clazz.getName(), methodName);
    }

    /**
     * 打失败metric点
     *
     * @param clazz
     * @param methodName
     */
    public static void recordSysFailMetric(String className, String methodName) {
        recordCountMetric(getSysMetricName(className, methodName));
    }

    /**
     * 打失败metric点
     *
     * @param clazz
     * @param methodName
     */
    public static void recordSysFailMetric(String metricName) {
        recordCountMetric(metricName + "fail");
    }

    /**
     * 打业务异常metric点
     *
     * @param clazz
     * @param methodName
     */
    public static void recordBusFailMetric(Class<?> clazz, String methodName) {
        recordBusFailMetric(clazz.getName(), methodName);
    }

    /**
     * 打业务异常metric点
     *
     * @param clazz
     * @param methodName
     */
    public static void recordBusFailMetric(String metricName) {
        recordCountMetric(metricName + "fail");
    }

    /**
     * 打业务异常metric点
     *
     * @param clazz
     * @param methodName
     */
    public static void recordBusFailMetric(String className, String methodName) {
        recordCountMetric(getBusMetricName(className, methodName));
    }

    /**
     * 打业务异常metric点
     *
     * @param clazz
     * @param methodName
     */
    public static void recordServiceExceptionMetric(String metricName, ServiceException e) {
        String errorCode = null;
        if (e != null && StringUtils.isNotEmpty(e.getCode())) {
            errorCode = e.getCode();
        } else {
            errorCode = "unknow_bus_metric_error";
        }
        recordCountMetric(metricName + "fail" + "-" + errorCode);
    }

    /**
     * 打系统异常metric点
     *
     * @param clazz
     * @param methodName
     */
    public static void recordSysExceptionMetric(String metricName, Exception e) {
        String errorCode = null;
        if (e != null) {
            errorCode = e.getClass().getSimpleName();
        } else {
            errorCode = "unknow_sys_metric_error";
        }
        recordCountMetric(metricName + "fail" + "-" + errorCode);
    }

    /**
     * 打metric点
     *
     * @param metricName
     */
    public static void recordCountMetric(String metricName) {
        new MetricClient().name(metricName).recordIncrement();
    }

    /**
     * 打失败metric time点
     *
     * @param clazz
     * @param methodName
     */
    public static void recordTimeMetric(Class<?> clazz, String methodName, long start, long end) {
        recordTimeMetric(getTimeMetricName(clazz, methodName), start, end);
    }

    /**
     * 打失败metric time点
     *
     * @param clazz
     * @param methodName
     */
    public static void recordTimeMetric(Class<?> clazz, String methodName, long start) {
        recordTimeMetric(clazz, methodName, start, System.currentTimeMillis());
    }

    /**
     * 打metric time点
     *
     * @param metricName
     * @param start
     * @param end
     */
    public static void recordTimeMetric(String metricName, long start, long end) {
        new MetricClient().name(metricName).recordTimeInMillis(end - start);
    }

    /**
     * 打metric times点
     *
     * @param metricName
     * @param times
     */
    public static void recordTimesMetric(String metricName, long times) {
        new MetricClient().name(metricName).recordTimeInMillis(times);
    }

    /**
     * 打metric time点
     *
     * @param metricName
     * @param start
     */
    public static void recordTimeMetric(String metricName, long start) {
        recordTimeMetric(metricName + "time_out", start, System.currentTimeMillis());
    }

    /**
     * 打执行次数metric点
     */
    public static void recordInvokeCountMetric(String metricName) {
        recordCountMetric(metricName + "invoke_count");
    }
}