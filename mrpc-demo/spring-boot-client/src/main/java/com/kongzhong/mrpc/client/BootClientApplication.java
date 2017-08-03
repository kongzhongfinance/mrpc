package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.demo.service.UserService;
import com.kongzhong.mrpc.ktrace.interceptor.TraceFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

/**
 * @author biezhi
 * 2017/5/15
 */
@SpringBootApplication
public class BootClientApplication {

    @Bean
    public Referers referers() {
        return new Referers().add(UserService.class);
    }

    @Bean
    public FilterRegistrationBean traceFilter() {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean();
        servletRegistrationBean.setName("TraceFilter");
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new TraceFilter(), servletRegistrationBean);
        filterRegistrationBean.setName("TraceFilter");
        filterRegistrationBean.setUrlPatterns(Arrays.asList("/*"));
        return filterRegistrationBean;

    }

    public static void main(String[] args) {
        SpringApplication.run(BootClientApplication.class, args);
    }
}
