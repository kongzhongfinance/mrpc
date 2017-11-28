package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.demo.service.UserService;
import com.kongzhong.mrpc.trace.interceptor.TraceClientInterceptor;
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

    public static void main(String[] args) {
        SpringApplication.run(BootClientApplication.class, args);
    }
}
