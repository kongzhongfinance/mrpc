package com.kongzhong.mrpc.springboot.client;

import com.kongzhong.mrpc.interceptor.RpcClientInteceptor;
import com.kongzhong.mrpc.utils.ReflectUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Bean初始化拦截
 *
 * @author biezhi
 *         2017/5/13
 */
public class RpcClientInitBean implements BeanPostProcessor {

    private BootRpcClient bootRpcClient;

    public RpcClientInitBean(BootRpcClient bootRpcClient) {
        this.bootRpcClient = bootRpcClient;
    }

    @Override
    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
        return o;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String s) throws BeansException {
        Class<?> service = bean.getClass();
        boolean hasInterface = ReflectUtils.hasInterface(service, RpcClientInteceptor.class);
        if (hasInterface) {
            bootRpcClient.addInterceptor((RpcClientInteceptor) bean);
        }
        return bean;
    }

}