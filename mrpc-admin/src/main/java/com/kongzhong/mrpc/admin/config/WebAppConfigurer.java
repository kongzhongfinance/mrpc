package com.kongzhong.mrpc.admin.config;

import com.kongzhong.mrpc.admin.interceptor.ApiInterceptor;
import com.kongzhong.mrpc.admin.interceptor.AdminInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebAppConfigurer extends WebMvcConfigurerAdapter {

    @Bean
    public AdminInterceptor baseInterceptor() {
        return new AdminInterceptor();
    }

    @Bean
    public ApiInterceptor apiInterceptor() {
        return new ApiInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 多个拦截器组成一个拦截器链
        // addPathPatterns 用于添加拦截规则
        // excludePathPatterns 用户排除拦截
        registry.addInterceptor(baseInterceptor()).addPathPatterns("/admin/**");
        registry.addInterceptor(apiInterceptor()).addPathPatterns("/api/**");
        super.addInterceptors(registry);
    }

}