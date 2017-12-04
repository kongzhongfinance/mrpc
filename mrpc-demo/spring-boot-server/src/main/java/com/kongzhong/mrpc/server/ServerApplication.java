package com.kongzhong.mrpc.server;

import com.kongzhong.mrpc.client.Referers;
import com.kongzhong.mrpc.demo.service.OtherService;
import com.kongzhong.mrpc.trace.config.TraceClientAutoConfigure;
import com.kongzhong.mrpc.trace.interceptor.TraceClientInterceptor;
import com.kongzhong.mrpc.trace.interceptor.TraceServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author biezhi
 *         2017/5/15
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.kongzhong.mrpc.server.service")
public class ServerApplication {

    @Bean
    public Referers referers(){
        return new Referers().add(OtherService.class);
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
        traceClientAutoConfigure.setName("server");
        return new TraceClientInterceptor(traceClientAutoConfigure);
    }

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}
