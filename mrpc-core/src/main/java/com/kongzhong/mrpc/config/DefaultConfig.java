package com.kongzhong.mrpc.config;

import com.kongzhong.mrpc.enums.TransportEnum;
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

    public static final RpcSerialize DEFAULT_SERIALIZE = new KyroSerialize();

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
