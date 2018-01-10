package com.kongzhong.mrpc.interceptor.validator.config;

import com.kongzhong.mrpc.interceptor.validator.ValidateInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Locale;

/**
 * Created by IFT8 on 2018/1/2.
 */
@Configuration
public class ValidateInterceptorConfig {

    @PostConstruct
    private void init() {
        //让Hibernate Validation的错误消息显示中文
        Locale.setDefault(Locale.CHINA);
    }

    @Bean
    public ValidateInterceptor validateInterceptor() {
        return new ValidateInterceptor();
    }
}
