package com.kongzhong.mrpc.annotation;

import com.kongzhong.mrpc.model.NoInterface;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC服务方法注解，标注在服务实现方法上
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Command {

    /**
     * 服务调用超时时间，单位/秒
     *
     * @return
     */
    int waitTimeout() default 10;

    /**
     * 重试次数，默认3次
     *
     * @return
     */
    int retryNumber() default 3;
}