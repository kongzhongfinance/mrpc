package junicorn.mrpc.spring.annotation;

import junicorn.mrpc.spring.bean.NoInterface;
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

}