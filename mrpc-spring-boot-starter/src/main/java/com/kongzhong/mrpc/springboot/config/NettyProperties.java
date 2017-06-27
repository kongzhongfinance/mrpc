package com.kongzhong.mrpc.springboot.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * netty配置
 *
 * @author biezhi
 *         21/06/2017
 */
@ConfigurationProperties("mrpc.netty")
@Data
@ToString
public class NettyProperties {

    // 客户端连接超时，默认3秒，超过后断开
    private int connTimeout = 3000;

    // 业务线程池数
    private int businessThreadPoolSize = 16;
    private int backlog;
    private boolean keepalive;
    private int lowWaterMark = 32 * 1024;
    private int highWaterMark = 64 * 1024;

}
