package com.kongzhong.mrpc.config;

import com.google.common.collect.Maps;
import com.kongzhong.mrpc.enums.HaStrategyEnum;
import com.kongzhong.mrpc.enums.LbStrategyEnum;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

/**
 * 客户端公共配置
 *
 * @author biezhi
 *         20/06/2017
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
public class ClientConfig {

    private String appId;
    private HaStrategyEnum haStrategy = HaStrategyEnum.FAILOVER;
    private RpcSerialize rpcSerialize;
    private LbStrategyEnum lbStrategy = LbStrategyEnum.RANDOM;
    private Map<String, ServiceDiscovery> serviceDiscoveryMap = Maps.newHashMap();

    /**
     * 跳过服务绑定
     */
    private Boolean skipBind;

    /**
     * 客户端服务调用超时，单位/毫秒
     */
    private int waitTimeout = 10_000;

    /**
     * 快速失效重试次数
     */
    private int failOverRetry = 3;

    /**
     * 重试间隔，单位/毫秒 默认每3秒重连一次
     */
    private int retryInterval = 3000;

    /**
     * 重试次数，默认10次
     */
    private int retryCount = 10;

    /**
     * 客户端定时ping服务端的频率，单位/毫秒
     */
    private int pingInterval = -1;

    private static final class ClientConfigHolder {
        private static final ClientConfig INSTANCE = new ClientConfig();
    }

    public static ClientConfig me() {
        return ClientConfigHolder.INSTANCE;
    }

    public ServiceDiscovery getServiceDiscovery(String serviceName){
        return serviceDiscoveryMap.get(serviceName);
    }
}
