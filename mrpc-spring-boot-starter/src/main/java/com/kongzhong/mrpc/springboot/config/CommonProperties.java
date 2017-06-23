package com.kongzhong.mrpc.springboot.config;

import com.google.common.collect.Maps;
import com.kongzhong.mrpc.config.NettyConfig;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 公共配置
 *
 * @author biezhi
 *         21/06/2017
 */
@ConfigurationProperties("mrpc")
@Data
@ToString
public class CommonProperties {

    /**
     * 注册中心
     * <p>
     * mrpc.registry[default].type=zookeeper
     * mrpc.registry[default].address=127.0.0.1:2181
     */
    private Map<String, Map<String, String>> registry;

    /**
     * 自定义配置
     * <p>
     * mrpc.custom[userService][address]=5088
     * mrpc.custom[userService][registry]=my_zk
     * <p>
     * mrpc.custom[payService][direct]=true
     */
    private Map<String, Map<String, String>> custom;

    /**
     * 是否是测试环境, 为 "true" 时服务进程不会挂起
     */
    private String test;

    /**
     * netty配置
     */
    private NettyConfig netty;

}
