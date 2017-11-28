package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.demo.service.UserService;
import com.kongzhong.mrpc.trace.config.TraceClientAutoConfigure;
import com.kongzhong.mrpc.trace.interceptor.TraceClientInterceptor;
import com.kongzhong.mrpc.trace.interceptor.TraceFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import java.util.Collections;

/**
 * @author biezhi
 * 2017/5/15
 */
@Slf4j
@SpringBootApplication
public class BootClientApplication {

    @Bean
    public Referers referers() {
        return new Referers().add(UserService.class);
    }

    @Bean
    @ConditionalOnClass(TraceClientInterceptor.class)
    public FilterRegistrationBean traceFilter(@Autowired TraceClientInterceptor traceClientInterceptor) {

        TraceFilter traceFilter = new TraceFilter(traceClientInterceptor.getTraceClientAutoConfigure(), traceClientInterceptor.getAgent());

        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean();
        servletRegistrationBean.setName("TraceFilter");

        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(traceFilter, servletRegistrationBean);
        filterRegistrationBean.setName("TraceFilter");
        filterRegistrationBean.setUrlPatterns(Collections.singletonList("/*"));
        return filterRegistrationBean;
    }

    @Bean
    public TraceClientInterceptor clientTraceInterceptor() {
        TraceClientAutoConfigure traceClientAutoConfigure = new TraceClientAutoConfigure();
        traceClientAutoConfigure.setEnable(true);
        traceClientAutoConfigure.setUrl("127.0.0.1:9092");
        traceClientAutoConfigure.setOwner("biezhi");
        traceClientAutoConfigure.setName("web-client");
        return new TraceClientInterceptor(traceClientAutoConfigure);
    }

    public static void main(String[] args) {
        SpringApplication.run(BootClientApplication.class, args);
    }
}
