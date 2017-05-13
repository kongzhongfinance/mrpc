package com.kongzhong.mrpc.springboot.server;

import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.model.NoInterface;
import com.kongzhong.mrpc.server.RpcMapping;
import com.kongzhong.mrpc.utils.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * @author biezhi
 *         2017/5/13
 */
public class InitBean implements BeanPostProcessor {

    public RpcMapping rpcMapping;

    public InitBean(RpcMapping rpcMapping) {
        this.rpcMapping = rpcMapping;
    }

    @Override
    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
        return o;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String s) throws BeansException {

        Class<?> service = bean.getClass();
        RpcService rpcService = AnnotationUtils.findAnnotation(service, RpcService.class);
        if (null == rpcService) {
            return bean;
        }

        String serviceName = rpcService.value().getName();
        String version = rpcService.version();
        String name = rpcService.name();

        if (StringUtils.isNotEmpty(name)) {
            serviceName = name;
        } else {
            if (NoInterface.class.getName().equals(serviceName)) {
                Class<?>[] intes = bean.getClass().getInterfaces();
                if (null == intes || intes.length != 1) {
                    serviceName = bean.getClass().getName();
                } else {
                    serviceName = intes[0].getName();
                }
            }
        }

        if (StringUtils.isNotEmpty(version)) {
            serviceName += "_" + version;
        }
        rpcMapping.addHandler(serviceName, bean);
        return bean;
    }

}
