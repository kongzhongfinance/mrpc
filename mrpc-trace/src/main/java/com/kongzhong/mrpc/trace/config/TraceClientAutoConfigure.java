package com.kongzhong.mrpc.trace.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Conditional;

/**
 * @author biezhi
 * @date 2017/11/22
 */
@Data
//@Conditional(TraceClientEnvironmentCondition.class)
@ConfigurationProperties("mrpc.client.trace")
public class TraceClientAutoConfigure {

    /**
     * Enable the trace or not
     */
    private Boolean enable = Boolean.FALSE;

    /**
     * zipkin url, e.g: http://localhost:9411
     */
    private String url;

    /**
     * The app name
     */
    private String name;

    /**
     * The app owner
     */
    private String owner;

}
