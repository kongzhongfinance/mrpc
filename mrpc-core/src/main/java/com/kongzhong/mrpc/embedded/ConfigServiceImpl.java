package com.kongzhong.mrpc.embedded;

import com.google.common.collect.Maps;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.config.ServerConfig;
import com.kongzhong.mrpc.server.SimpleRpcServer;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置服务
 *
 * @author biezhi
 * @date 2017/7/24
 */
public class ConfigServiceImpl implements ConfigService {

    private Map<String, Integer> methodWaitTimeout = Maps.newHashMap();

    /*---------------------服务端:START----------------------*/
    @Override
    public int getBusinessThreadPoolSize() {
        return ServerConfig.me().getBusinessThreadPoolSize();
    }

    @Override
    public void setBusinessThreadPoolSize(int threadPoolSize) {
        ServerConfig.me().setBusinessThreadPoolSize(threadPoolSize);
        SimpleRpcServer.setListeningExecutorService(threadPoolSize);
    }
    /*---------------------服务端:END------------------------*/

    /*---------------------客户端:START----------------------*/

    @Override
    public void setWaitTimeout(int timeout) {
        ClientConfig.me().setWaitTimeout(timeout);
    }

    @Override
    public int getWaitTimeout() {
        return ClientConfig.me().getWaitTimeout();
    }

    @Override
    public Integer getMethodWaitTimeout(String method) {
        return methodWaitTimeout.get(method);
    }

    @Override
    public void setMethodWaitTimeout(String method, Integer waitTimeout) {
        methodWaitTimeout.put(method, waitTimeout);
    }

    /*---------------------客户端:ENT------------------------*/

    private static final class ConfigServiceHolder {
        private static final ConfigServiceImpl INSTANCE = new ConfigServiceImpl();
    }

    public static ConfigServiceImpl me() {
        return ConfigServiceHolder.INSTANCE;
    }

    /**
     * 获取所有环境变量
     *
     * @return
     */
    public Map<String, Object> environments() {
        return new HashMap<>();
    }

    /**
     * 根据key获取环境变量
     *
     * @param key
     * @return
     */
    public Object getEnvironment(String key) {
        return "";
    }

}