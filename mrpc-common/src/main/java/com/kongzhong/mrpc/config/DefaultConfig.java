package com.kongzhong.mrpc.config;

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
    public static String serialize() {
        return "kyro";
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
