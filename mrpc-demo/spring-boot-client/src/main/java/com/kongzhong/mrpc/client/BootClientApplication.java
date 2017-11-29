package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.demo.service.UserService;
import com.kongzhong.mrpc.trace.interceptor.TraceClientInterceptor;
import com.kongzhong.mrpc.trace.utils.Exclusions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.servlet.DispatcherType;

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
    public FilterRegistrationBean filterRegistrationBean(@Autowired TraceClientInterceptor traceClientInterceptor) {
        com.kongzhong.mrpc.trace.interceptor.TraceFilter traceFilter = new com.kongzhong.mrpc.trace.interceptor.TraceFilter(traceClientInterceptor.getTraceClientAutoConfigure(), traceClientInterceptor.getAgent());
        FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
        filterRegistration.setFilter(traceFilter);
        filterRegistration.addUrlPatterns("/*");
        filterRegistration.addInitParameter("exclusions", Exclusions.defaultExclusions().toString());
        // filterRegistration.setAsyncSupported(true);
        filterRegistration.setDispatcherTypes(DispatcherType.REQUEST);
        return filterRegistration;
    }

    public static void main(String[] args) {
        SpringApplication.run(BootClientApplication.class, args);
    }
}
