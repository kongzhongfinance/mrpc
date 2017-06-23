package com.kongzhong.mrpc.springboot.server;

import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.interceptor.RpcServerInteceptor;
import com.kongzhong.mrpc.model.NoInterface;
import com.kongzhong.mrpc.model.ServiceBean;
import com.kongzhong.mrpc.server.RpcMapping;
import com.kongzhong.mrpc.spring.utils.AopTargetUtils;
import com.kongzhong.mrpc.utils.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Bean初始化拦截
 *
 * @author biezhi
 *         2017/5/13
 */
public class InitBean implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(InitBean.class);

    public RpcMapping rpcMapping;

    public InitBean(RpcMapping rpcMapping) {
        this.rpcMapping = rpcMapping;
    }

    @Override
    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
        return o;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> service = bean.getClass();
        boolean hasInterface = ReflectUtils.hasInterface(service, RpcServerInteceptor.class);
        if (hasInterface) {
            rpcMapping.addInterceptor((RpcServerInteceptor) bean);
        }
        RpcService rpcService = AnnotationUtils.findAnnotation(service, RpcService.class);
        try {
            if (null == rpcService) {
                return bean;
            }
            Object realBean = AopTargetUtils.getTarget(bean);
            String serviceName = rpcService.value().getName();
            if (NoInterface.class.getName().equals(serviceName)) {
                Class<?>[] intes = realBean.getClass().getInterfaces();
                if (null == intes || intes.length != 1) {
                    serviceName = realBean.getClass().getName();
                } else {
                    serviceName = intes[0].getName();
                }
            }

            ServiceBean serviceBean = new ServiceBean();
            serviceBean.setBean(realBean);
            serviceBean.setBeanName(beanName);
            serviceBean.setServiceName(serviceName);

            rpcMapping.addServiceBean(serviceBean);

        } catch (Exception e) {
            log.error("Init bean error", e);
        }
        return bean;
    }

}
