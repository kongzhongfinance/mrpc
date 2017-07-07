package com.kongzhong.mrpc.springboot.config;

import jdk.nashorn.internal.runtime.linker.Bootstrap;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * mrpc客户端端配置
 *
 * @author biezhi
 *         2017/5/13
 */
@ConfigurationProperties("mrpc.client")
@Data
@ToString
public class RpcClientProperties {

    // 服务端传输协议，默认tcp
    private String transport;

    // 服务所属appId
    private String appId;

    // 直连服务地址
    private String directAddress;

    // 高可用策略
    private String haStrategy;

    // 负载均衡策略
    private String lbStrategy;

    // 序列化组件，默认kyro
    private String serialize;

    // 跳过服务绑定
    private Boolean skipBind;

    // 客户端连接超时时间，单位/毫秒 默认10秒
    private int waitTimeout = 10_000;

    // 客户端ping间隔
    private int pingInterval = -1;

    // FailOver重试次数
    private int failOverRetry = 3;

    // 重试间隔，单位/毫秒 默认每3秒重连一次
    private int retryInterval = 3000;

    // 重试次数，默认20次
    private int retryCount = 10;

}