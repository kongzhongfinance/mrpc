package com.kongzhong.mrpc.config;

import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.registry.DefaultDiscovery;
import com.kongzhong.mrpc.registry.DefaultRegistry;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.registry.ServiceRegistry;
import com.kongzhong.mrpc.serialize.KyroSerialize;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import lombok.NoArgsConstructor;

/**
 * 默认配置类
 *
 * @author biezhi
 *         2017/4/26
 */
@NoArgsConstructor
public class DefaultConfig {

    private static final RpcSerialize DEFAULT_SERIALIZE = new KyroSerialize();

    /**
     * 默认序列化类型
     *
     * @return
     */
    public static RpcSerialize serialize() {
        return DEFAULT_SERIALIZE;
    }

    /**
     * 默认传输协议
     *
     * @return
     */
    public static String transport() {
        return TransportEnum.TCP.name();
    }

    /**
     * 默认的服务发现
     *
     * @return
     */
    public static ServiceDiscovery discovery() {
        return new DefaultDiscovery();
    }

    /**
     * 默认的服务注册
     *
     * @return
     */
    public static ServiceRegistry registry() {
        return new DefaultRegistry();
    }

    /**
     * 默认服务调用超时重试次数
     *
     * @return
     */
    public static int serviceRecryCount() {
        return 3;
    }

    /**
     * 服务调用超时时间，单位/秒
     *
     * @return
     */
    public static int serviceTimeout() {
        return 10;
    }
}
