package com.kongzhong.mrpc.config;

import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 服务端公共配置
 *
 * @author biezhi
 *         20/06/2017
 */
@Data
@ToString(callSuper = true)
public class ServerCommonConfig {

    private String appId;
    private String elasticIp;
    private RpcSerialize rpcSerialize;
    private TransportEnum transport;

    private ServerCommonConfig() {
    }

    private static final class ClientCommonConfigHolder {
        private static final ServerCommonConfig INSTANCE = new ServerCommonConfig();
    }

    public static ServerCommonConfig me() {
        return ClientCommonConfigHolder.INSTANCE;
    }

}
