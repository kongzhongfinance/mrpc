package com.kongzhong.mrpc.annotation;

import com.kongzhong.mrpc.enums.MediaType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author biezhi
 *         2017/4/20
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GET {

    String value();

    MediaType contentType() default MediaType.JSON;

}
