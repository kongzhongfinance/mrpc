package com.kongzhong.mrpc.springboot.server;

import com.kongzhong.mrpc.Const;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ServerEnvironmentCondition implements Condition {

    public final boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();
        return env.containsProperty(Const.SERVER_ADDRESS);
    }

}