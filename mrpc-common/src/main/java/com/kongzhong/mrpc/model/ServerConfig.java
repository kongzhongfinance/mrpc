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
public class ServerConfig {

    private String host;

    private int port;

    private TransportEnum transport;

    private RpcSerialize rpcSerialize;

    private static final ServerConfig conf = new ServerConfig();

    public static ServerConfig me() {
        return conf;
    }
}
