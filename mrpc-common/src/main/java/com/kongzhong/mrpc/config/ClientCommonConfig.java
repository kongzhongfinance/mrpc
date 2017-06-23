package com.kongzhong.mrpc.config;

import com.kongzhong.mrpc.client.cluster.HaStrategy;
import com.kongzhong.mrpc.enums.LbStrategyEnum;
import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.serialize.RpcSerialize;
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
@NoArgsConstructor
@ToString(callSuper = true)
public class ClientCommonConfig {

    private String appId;
    private HaStrategy haStrategy;
    private RpcSerialize rpcSerialize;
    private LbStrategyEnum lbStrategy;
    private TransportEnum transport;
    private int waitTimeout = 10;
    private int failOverRetry = 3;

    private static final class ClientCommonConfigHolder {
        private static final ClientCommonConfig INSTANCE = new ClientCommonConfig();
    }

    public static ClientCommonConfig me() {
        return ClientCommonConfigHolder.INSTANCE;
    }

}
