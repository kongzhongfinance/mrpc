package com.kongzhong.mrpc.server;

import com.kongzhong.mrpc.client.Referers;
import com.kongzhong.mrpc.demo.service.PayService;
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
        return new TraceClientInterceptor();
    }

    public static void main(String[] args) {
        SpringApplication.run(OtherServerApplication.class, args);
    }

}
