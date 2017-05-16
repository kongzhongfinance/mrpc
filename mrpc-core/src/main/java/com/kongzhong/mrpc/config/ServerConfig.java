package com.kongzhong.mrpc.config;

import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author biezhi
 *         2017/4/22
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServerConfig {

    private String host;
    private int port;
    private String appId = "default";
    private TransportEnum transport;
    private RpcSerialize rpcSerialize;

    private static final ServerConfig conf = new ServerConfig();

    public static ServerConfig me() {
        return conf;
    }
}
