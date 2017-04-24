package com.kongzhong.mrpc.config;

import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.cluster.FailStrategy;
import com.kongzhong.mrpc.cluster.LBStrategy;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author biezhi
 *         2017/4/22
 */
@Data
@NoArgsConstructor
public class ClientConfig {

    private String serverAddr;

    private TransportEnum transport;

    private RpcSerialize rpcSerialize;

    private boolean isHttp;

    private LBStrategy lbStrategy = LBStrategy.POLL;

    private FailStrategy failStrategy = FailStrategy.FAILOVER;

    private int retryCount = 3;

    private static final ClientConfig conf = new ClientConfig();

    public static ClientConfig me() {
        return conf;
    }
}
