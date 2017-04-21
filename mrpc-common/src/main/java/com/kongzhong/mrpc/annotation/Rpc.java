package com.kongzhong.mrpc.annotation;

import com.kongzhong.mrpc.enums.SerializeEnum;
import com.kongzhong.mrpc.enums.TransportEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于SpringBoot配置
 *
 * @author biezhi
 *         2017/4/20
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Rpc {

    int port() default 5066;

    SerializeEnum serialize() default SerializeEnum.PROTOSTUFF;

    TransportEnum transefer() default TransportEnum.TPC;

}