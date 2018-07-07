package com.kongzhong.mrpc.config;

import com.kongzhong.mrpc.serialize.RpcSerialize;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 服务端公共配置
 *
 * @author biezhi
 * 20/06/2017
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class ServerConfig {

    private String       appId;
    private String       elasticIp;
    private RpcSerialize rpcSerialize;

    private static final class ServerConfigHolder {
        private static final ServerConfig INSTANCE = new ServerConfig();
    }

    public static ServerConfig me() {
        return ServerConfigHolder.INSTANCE;
    }

}
