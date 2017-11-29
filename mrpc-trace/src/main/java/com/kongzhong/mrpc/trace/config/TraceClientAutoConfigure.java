package com.kongzhong.mrpc.trace.config;

import com.kongzhong.mrpc.trace.interceptor.TraceClientInterceptor;
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
@ConfigurationProperties("mrpc.client.trace")
@ConditionalOnExpression("'${mrpc.client.trace.enable}'=='true'")
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
        log.info("[config] TraceClientAutoConfigure 加载完成 {}", this.toString());
    }

    @Bean
    public TraceClientInterceptor clientTraceInterceptor(@Autowired Environment environment) {
        String enable = environment.getProperty("mrpc.client.trace.enable");
        if (StringUtils.isEmpty(enable)) {
            return null;
        }
        String url   = environment.getProperty("mrpc.client.trace.url");
        String topic = environment.getProperty("mrpc.client.trace.topic");
        String owner = environment.getProperty("mrpc.client.trace.owner");
        String name  = environment.getProperty("mrpc.client.trace.name");

        TraceClientAutoConfigure traceClientAutoConfigure = new TraceClientAutoConfigure();
        traceClientAutoConfigure.setEnable(Boolean.valueOf(enable));
        traceClientAutoConfigure.setUrl(url);
        if(null != topic){
            traceClientAutoConfigure.setTopic(topic);
        }
        traceClientAutoConfigure.setOwner(owner);
        traceClientAutoConfigure.setName(name);
        return new TraceClientInterceptor(traceClientAutoConfigure);
    }

}