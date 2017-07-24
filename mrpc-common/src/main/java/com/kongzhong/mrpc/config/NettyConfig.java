package com.kongzhong.mrpc.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * netty服务端配置
 *
 * @author biezhi
 *         2017/4/28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NettyConfig {

    // 客户端连接超时，默认3秒，超过后断开
    private int connTimeout = 3000;

    private int backlog;

    private boolean keepalive;

    private int lowWaterMark = 32 * 1024;
    private int highWaterMark = 64 * 1024;

    public NettyConfig(int backlog, boolean keepalive) {
        this.backlog = backlog;
        this.keepalive = keepalive;
    }

}
