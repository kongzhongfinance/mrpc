package com.kongzhong.mrpc.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ToString
@ConfigurationProperties(prefix = "mrpc.client")
public class RpcClientProperties {

    private String basePackage;

    private String serverAddr;

}
