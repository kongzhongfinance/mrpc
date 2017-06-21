package com.kongzhong.mrpc.springboot.config;

import com.kongzhong.mrpc.enums.LBStrategy;
import com.kongzhong.mrpc.enums.TransportEnum;
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
    private String transport = TransportEnum.TCP.name();

    // 服务所属appId
    private String appId = "default";

    /**
     * 直连服务地址
     */
    private String directAddress;

    /**
     * 高可用策略
     */
    private String haStrategy;

    /**
     * 负载均衡策略
     */
    private String lbStrategy = LBStrategy.ROUND.name();

    private String serialize = "kyro";

}