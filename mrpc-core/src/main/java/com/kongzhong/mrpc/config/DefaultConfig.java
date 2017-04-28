package com.kongzhong.mrpc.config;

import com.kongzhong.mrpc.cluster.ha.FailOverHaStrategy;
import com.kongzhong.mrpc.cluster.ha.HaStrategy;
import com.kongzhong.mrpc.cluster.loadblance.LBStrategy;
import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.registry.DefaultRegistry;
import com.kongzhong.mrpc.registry.ServiceRegistry;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.utils.ReflectUtils;
import lombok.NoArgsConstructor;

/**
 * 默认配置类
 *
 * @author biezhi
 *         2017/4/26
 */
@NoArgsConstructor
public class DefaultConfig {

    /**
     * 默认序列化类型
     *
     * @return
     */
    public static RpcSerialize serialize() {
        return ReflectUtils.newInstance("com.kongzhong.mrpc.serialize.KyroSerialize", RpcSerialize.class);
    }

    /**
     * 默认的netty服务端配置
     *
     * @return
     */
    public static NettyConfig nettyServerConfig() {
        return new NettyConfig(128, true);
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

    /**
     * 默认的负载均衡策略
     *
     * @return
     */
    public static LBStrategy lbStrategy() {
        return LBStrategy.ROUND;
    }

    /**
     * 默认高可用策略
     *
     * @return
     */
    public static HaStrategy haStrategy() {
        return new FailOverHaStrategy();
    }
}
