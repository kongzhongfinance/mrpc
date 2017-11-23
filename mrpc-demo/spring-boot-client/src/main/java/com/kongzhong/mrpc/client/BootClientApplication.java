package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.demo.service.UserService;
import com.kongzhong.mrpc.trace.config.TraceClientAutoConfigure;
import com.kongzhong.mrpc.trace.interceptor.ClientTraceInterceptor;
import com.kongzhong.mrpc.trace.interceptor.TraceFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

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

//    @Bean
//    public FilterRegistrationBean traceFilter() {
//        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean();
//        servletRegistrationBean.setName("TraceFilter");
//        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new TraceFilter(), servletRegistrationBean);
//        filterRegistrationBean.setName("TraceFilter");
//        filterRegistrationBean.setUrlPatterns(Arrays.asList("/*"));
//        return filterRegistrationBean;
//    }

    @Bean
    public ClientTraceInterceptor clientTraceInterceptor() {
        return new ClientTraceInterceptor();
    }

    @Bean
    @ConditionalOnClass(TraceClientAutoConfigure.class)
    public FilterRegistrationBean traceFilter(@Autowired TraceClientAutoConfigure traceClientAutoConfigure) {

        log.info("Client Trace {}", traceClientAutoConfigure);

        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean();
        servletRegistrationBean.setName("TraceFilter");

        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new TraceFilter(traceClientAutoConfigure), servletRegistrationBean);
        filterRegistrationBean.setName("TraceFilter");
        filterRegistrationBean.setUrlPatterns(Collections.singletonList("/*"));
        return filterRegistrationBean;
    }

    public static void main(String[] args) {
        SpringApplication.run(BootClientApplication.class, args);
    }
}
