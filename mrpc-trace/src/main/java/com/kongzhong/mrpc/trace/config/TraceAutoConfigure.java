package com.kongzhong.mrpc.trace.config;

import com.kongzhong.mrpc.trace.interceptor.TraceClientInterceptor;
import com.kongzhong.mrpc.trace.interceptor.TraceServerInterceptor;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

/**
 * @author biezhi
 * @date 2017/11/22
 */
@Data
@Slf4j
@ConfigurationProperties("mrpc.trace")
@ConditionalOnExpression("'${mrpc.trace.enable}'=='true'")
public class TraceAutoConfigure {

    /**
     * Enable the trace or not
     */
    private Boolean enable = Boolean.FALSE;

    /**
     * zipkin url, e.g: http://localhost:9411
     */
    private String url;

    /**
     * kafka topic name
     */
    private String topic = "zipkin";

    /**
     * The app name
     */
    private String name;

    /**
     * The app owner
     */
    private String owner;

    @PostConstruct
    public void init() {
        log.info("[config] TraceAutoConfigure 加载完成 {}", this.toString());
    }

    @Bean
    public TraceClientInterceptor traceClientInterceptor(@Autowired Environment environment) {
        log.info("加载 TraceClientInterceptor");
        String enable = environment.getProperty("mrpc.trace.enable");
        if (StringUtils.isEmpty(enable)) {
            if (StringUtils.isNotEmpty(url)) {
                return new TraceClientInterceptor(this);
            }
            return null;
        }
        TraceAutoConfigure traceAutoConfigure = parse(environment);
        return new TraceClientInterceptor(traceAutoConfigure);
    }

    @Bean
    public TraceServerInterceptor traceServerInterceptor(@Autowired Environment environment){
        log.info("加载 TraceServerInterceptor");
        String enable = environment.getProperty("mrpc.trace.enable");
        if (StringUtils.isEmpty(enable)) {
            if (StringUtils.isNotEmpty(url)) {
                return new TraceServerInterceptor(this);
            }
            return null;
        }
        TraceAutoConfigure traceAutoConfigure = parse(environment);
        return new TraceServerInterceptor(traceAutoConfigure);
    }

    public static TraceAutoConfigure parse(Environment environment) {
        String enable = environment.getProperty("mrpc.trace.enable");
        log.info("mrpc.trace.enable={}", enable);
        if (StringUtils.isEmpty(enable)) {
            return null;
        }
        String url   = environment.getProperty("mrpc.trace.url");
        String topic = environment.getProperty("mrpc.trace.topic");
        String owner = environment.getProperty("mrpc.trace.owner");
        String name  = environment.getProperty("mrpc.trace.name");

        TraceAutoConfigure traceAutoConfigure = new TraceAutoConfigure();
        traceAutoConfigure.setEnable(Boolean.valueOf(enable));
        traceAutoConfigure.setUrl(url);
        if (null != topic) {
            traceAutoConfigure.setTopic(topic);
        }
        traceAutoConfigure.setOwner(owner);
        traceAutoConfigure.setName(name);
        log.info("TraceAutoConfigure.parse ={}",traceAutoConfigure);
        return traceAutoConfigure;
    }
}