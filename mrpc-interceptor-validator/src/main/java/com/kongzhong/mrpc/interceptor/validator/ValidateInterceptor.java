package com.kongzhong.mrpc.interceptor.validator;


import com.kongzhong.mrpc.interceptor.validator.exception.ValidateException;
import com.kongzhong.mrpc.interceptor.RpcServerInterceptor;
import com.kongzhong.mrpc.interceptor.ServerInvocation;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Created by IFT8 on 2017/5/27.
 * 验证拦截器
 */
@Slf4j
public class ValidateInterceptor implements RpcServerInterceptor {
    //默认
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Override
    public Object execute(ServerInvocation invocation) throws Exception {
        Object[] parameters = invocation.getParameters();
        for (Object arg : parameters) {
            validate(arg);
        }
        return invocation.next();
    }

    /**
     * 验证(支持复合对象，集合)
     *
     * @param object 参数
     * @param groups 组
     */
    private void validate(Object object, Class<?>... groups) throws ValidateException {
        if (object instanceof Collection) {
            for (Object o : (Collection) object) {
                validateSingle(o, groups);
            }
        }
        validateSingle(object, groups);
    }

    /**
     * 单个验证
     */
    private void validateSingle(Object object, Class<?>... groups) throws ValidateException {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
        if (!constraintViolations.isEmpty()) {
            Optional<ConstraintViolation<Object>> constraintViolation = constraintViolations.stream().findFirst();
            if (constraintViolation.isPresent()) {
                throw new ValidateException(constraintViolation.get().getMessage());
            } else {
                throw new ValidateException("缺少参数");
            }
        }
    }
}
