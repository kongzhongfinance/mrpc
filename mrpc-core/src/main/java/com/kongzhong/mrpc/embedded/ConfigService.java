package com.kongzhong.mrpc.embedded;

import java.util.Map;

/**
 * 配置服务
 *
 * @author biezhi
 * @date 2017/7/24
 */
public interface ConfigService {

    /**
     * 获取业务线程池数量
     *
     * @return
     */
    int getBusinessThreadPoolSize();

    /**
     * 设置业务线程池数量
     *
     * @param threadPoolSize
     */
    void setBusinessThreadPoolSize(int threadPoolSize);

    /**
     * 设置客户端全局超时等待时间
     *
     * @param timeout
     */
    void setWaitTimeout(int timeout);

    /**
     * 获取客户端超时等待时间
     *
     * @return
     */
    int getWaitTimeout();

    /**
     * 获取方法超时等待时间
     *
     * @param method
     * @return
     */
    Integer getMethodWaitTimeout(String method);

    /**
     * 设置方法超时等待时间
     *
     * @param method
     * @param waitTimeout
     */
    void setMethodWaitTimeout(String method, Integer waitTimeout);

    /**
     * 获取所有环境变量
     *
     * @return
     */
    Map<String, Object> environments();

    /**
     * 根据key获取环境变量
     *
     * @param key
     * @return
     */
    Object getEnvironment(String key);
}