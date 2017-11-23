package com.kongzhong.mrpc.trace.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author biezhi
 * @date 2017/11/22
 */
@Data
@ConfigurationProperties("mrpc.server.trace")
public class TraceServerAutoConfigure {

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
