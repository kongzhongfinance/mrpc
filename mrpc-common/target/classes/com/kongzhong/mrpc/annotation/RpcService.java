package com.kongzhong.mrpc.annotation;

import com.kongzhong.mrpc.model.NoInterface;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC服务注解，标注在服务实现类上
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcService {

    /**
     * 服务实现的接口
     *
     * @return
     */
    Class<?> value() default NoInterface.class;

    /**
     * 当前服务版本号
     *
     * @return
     */
//    String version() default "0.0.1";

    /**
     * 服务所属APPID（服务分组）
     *
     * @return
     */
    String appId() default "";

}