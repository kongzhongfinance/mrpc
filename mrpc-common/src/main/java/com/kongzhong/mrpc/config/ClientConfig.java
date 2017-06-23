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

    // 客户端服务调用超时，单位/秒
    private int waitTimeout = 10;

    // 快速失效重试次数
    private int failOverRetry = 3;

    private static final class ClientCommonConfigHolder {
        private static final ClientConfig INSTANCE = new ClientConfig();
    }

    public static ClientConfig me() {
        return ClientCommonConfigHolder.INSTANCE;
    }

}
