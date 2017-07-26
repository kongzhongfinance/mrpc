package com.kongzhong.mrpc.annotation;

import com.kongzhong.mrpc.enums.HaStrategyEnum;
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
     * 服务调用超时时间，单位/毫秒
     *
     * @return
     */
    int waitTimeout() default 10_000;

    /**
     * 高可用策略，默认为失效切换
     *
     * @return
     */
    HaStrategyEnum haStrategy() default HaStrategyEnum.FAILOVER;

    /**
     * 服务降级后调用的Class
     *
     * @return
     */
    String fallbackType() default "";

    /**
     * 服务降级后调用的方法名称，默认为服务方法名
     *
     * @return
     */
    String fallbackMethod() default "";

}