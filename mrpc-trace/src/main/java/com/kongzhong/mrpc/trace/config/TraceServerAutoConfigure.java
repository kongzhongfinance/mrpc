package com.kongzhong.mrpc.trace.config;

import com.kongzhong.mrpc.trace.interceptor.TraceServerInterceptor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;

/**
 * @author biezhi
 * @date 2017/11/22
 */
@Data
@Slf4j
@ConfigurationProperties("mrpc.server.trace")
@ConditionalOnExpression("'${mrpc.server.trace.enable}'=='true'")
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

    @PostConstruct
    public void init(){
        log.info("[config] TraceServerAutoConfigure 加载完成 {}", this.toString());
    }

    @Bean
    public TraceServerInterceptor serverTraceInterceptor(){
        return new TraceServerInterceptor();
    }

}
