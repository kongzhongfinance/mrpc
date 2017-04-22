package com.kongzhong.mrpc.model;

import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.serialize.RpcSerialize;
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

    private Strategy strategy = Strategy.POLL;

    private static final ClientConfig conf = new ClientConfig();

    public static ClientConfig me() {
        return conf;
    }
}
