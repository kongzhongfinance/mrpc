package com.kongzhong.mrpc.server;

import com.kongzhong.mrpc.client.Referers;
import com.kongzhong.mrpc.demo.service.PayService;
import com.kongzhong.mrpc.trace.config.TraceClientAutoConfigure;
import com.kongzhong.mrpc.trace.interceptor.TraceClientInterceptor;
import com.kongzhong.mrpc.trace.interceptor.TraceServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;

/**
 * @author biezhi
 *         2017/5/15
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.kongzhong.mrpc.server")
public class OtherServerApplication {

    @Bean
    public Referers referers(){
        return new Referers().add(PayService.class);
    }

    @Bean
    public TraceServerInterceptor serverTraceInterceptor(){
        return new TraceServerInterceptor();
    }

    @Bean
    public TraceClientInterceptor clientTraceInterceptor() {
        TraceClientAutoConfigure traceClientAutoConfigure = new TraceClientAutoConfigure();
        traceClientAutoConfigure.setEnable(true);
        traceClientAutoConfigure.setUrl("127.0.0.1:9092");
        traceClientAutoConfigure.setOwner("biezhi");
        traceClientAutoConfigure.setName("other-server");
        return new TraceClientInterceptor(traceClientAutoConfigure);
    }

    public static void main(String[] args) {
        SpringApplication.run(OtherServerApplication.class, args);
    }

}
