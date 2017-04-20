package com.kongzhong.mrpc.spring.annotation;

import com.kongzhong.mrpc.spring.bean.NoInterface;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface MRpcService {

    Class<?> value() default NoInterface.class;

    String version() default "";

    String name() default "";
}