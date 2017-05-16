package com.kongzhong.mrpc.config;

import com.kongzhong.mrpc.client.cluster.ha.HaStrategy;
import com.kongzhong.mrpc.client.cluster.loadblance.LBStrategy;
import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author biezhi
 *         2017/4/22
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientConfig {

    private String serverAddr;

    private TransportEnum transport;

    private RpcSerialize rpcSerialize;

    private String appId = "default";

    private boolean isHttp;

    private LBStrategy lbStrategy;

    private HaStrategy haStrategy;

    private int retryCount = 3;

    private List<Class<?>> referers;

    private static final ClientConfig conf = new ClientConfig();

    public static ClientConfig me() {
        return conf;
    }
}
