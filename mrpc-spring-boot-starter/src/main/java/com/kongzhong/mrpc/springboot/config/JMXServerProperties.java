package com.kongzhong.mrpc.springboot.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("mrpc.server.jmx-monitor")
@Data
@ToString
public class JMXServerProperties {

    private boolean enabled;
    private String url;

}
