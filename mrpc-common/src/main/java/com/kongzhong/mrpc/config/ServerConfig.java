package com.kongzhong.mrpc.config;

import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务配置
 *
 * @author biezhi
 *         2017/4/22
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServerConfig {

    // 服务绑定ip:port
    private String address;

    // 外网弹性ip:port，不清楚不用填写
    private String elasticIp;

    private String appId = "default";

    private TransportEnum transport;

    private RpcSerialize rpcSerialize;

    private static final ServerConfig conf = new ServerConfig();

    public static ServerConfig me() {
        return conf;
    }
}
