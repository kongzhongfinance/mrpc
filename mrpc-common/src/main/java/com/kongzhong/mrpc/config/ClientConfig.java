package com.kongzhong.mrpc.config;

import com.kongzhong.mrpc.client.cluster.HaStrategy;
import com.kongzhong.mrpc.enums.LbStrategyEnum;
import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 客户端公共配置
 *
 * @author biezhi
 *         20/06/2017
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(callSuper = true)
public class ClientConfig {

    private String appId;
    private HaStrategy haStrategy;
    private RpcSerialize rpcSerialize;
    private LbStrategyEnum lbStrategy;
    private TransportEnum transport;

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

}
