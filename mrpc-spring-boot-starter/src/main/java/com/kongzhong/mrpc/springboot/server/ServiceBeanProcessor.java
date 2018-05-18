package com.kongzhong.mrpc.springboot.server;

import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.interceptor.RpcServerInterceptor;
import com.kongzhong.mrpc.server.RpcMapping;
import com.kongzhong.mrpc.spring.utils.AopTargetUtils;
import com.kongzhong.mrpc.utils.ReflectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Bean初始化拦截
 *
 * @author biezhi
 *         2017/5/13
 */
@Slf4j
public class ServiceBeanProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext ctx;
    public RpcMapping rpcMapping;

    public ServiceBeanProcessor(RpcMapping rpcMapping) {
        this.rpcMapping = rpcMapping;
    }

    @Override
    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
        return o;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Object realBean = null;
        try {
            realBean = AopTargetUtils.getTarget(bean);
        } catch (Exception e) {
            log.error("Get bean target error", e);
        }
        Class<?> service = realBean.getClass();
        boolean hasInterface = ReflectUtils.hasInterface(service, RpcServerInterceptor.class);
        if (hasInterface) {
            rpcMapping.addInterceptor((RpcServerInterceptor) realBean);
        }

        RpcService rpcService = AnnotationUtils.findAnnotation(service, RpcService.class);
        if (null == rpcService) {
            return bean;
        }
        rpcMapping.addServiceBean(ctx.getBean(beanName), beanName);
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }
}
