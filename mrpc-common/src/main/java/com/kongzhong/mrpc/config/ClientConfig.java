package com.kongzhong.mrpc.config;

import com.kongzhong.mrpc.enums.HaStrategyEnum;
import com.kongzhong.mrpc.enums.LbStrategyEnum;
import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * 客户端公共配置
 *
 * @author biezhi
 *         20/06/2017
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@ManagedResource(description = "客户端配置")
public class ClientConfig {

    private String appId;
    private HaStrategyEnum haStrategy = HaStrategyEnum.FAILOVER;
    private RpcSerialize rpcSerialize;
    private LbStrategyEnum lbStrategy = LbStrategyEnum.RANDOM;
    private TransportEnum transport = TransportEnum.TCP;

    // 跳过服务绑定
    private Boolean skipBind;

    // 客户端服务调用超时，单位/毫秒
    private int waitTimeout = 10_000;

    // 快速失效重试次数
    private int failOverRetry = 3;

    // 重试间隔，单位/毫秒 默认每3秒重连一次
    private int retryInterval = 3000;

    // 重试次数，默认10次
    private int retryCount = 10;

    private static final class ClientCommonConfigHolder {

        private static final ClientConfig INSTANCE = new ClientConfig();
    }

    public static ClientConfig me() {
        return ClientCommonConfigHolder.INSTANCE;
    }

    @ManagedAttribute(description = "获取客户端方法调用超时")
    public int getWaitTimeout() {
        return waitTimeout;
    }

    @ManagedAttribute(description = "设置客户端方法调用超时", defaultValue = "10000")
    public void setWaitTimeout(int waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    @ManagedAttribute(description = "获取客户端断线重连次数")
    public int getRetryCount() {
        return retryCount;
    }

    @ManagedAttribute(description = "设置客户端断线重连次数", defaultValue = "10")
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    @ManagedAttribute(description = "获取客户端断线重连间隔")
    public int getRetryInterval() {
        return retryInterval;
    }

    @ManagedAttribute(description = "设置客户端断线重连间隔", defaultValue = "3000")
    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

}
