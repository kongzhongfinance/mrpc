package com.kongzhong.mrpc.springboot.client;

import com.kongzhong.mrpc.Const;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ClientEnvironmentCondition implements Condition {

    public final boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();
        boolean flag = env.containsProperty(Const.DIRECT_ADDRESS_STYLE1_CLIENT) ||
                env.containsProperty(Const.DIRECT_ADDRESS_STYLE2_CLIENT) ||
                env.containsProperty("mrpc.registry[default].type");
        return flag;
    }

}