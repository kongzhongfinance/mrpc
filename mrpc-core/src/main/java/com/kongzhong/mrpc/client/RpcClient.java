package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.interceptor.RpcClientInteceptor;
import com.kongzhong.mrpc.model.ClientBean;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

/**
 * rpc客户端
 */
@NoArgsConstructor
@Slf4j
public class RpcClient extends SimpleRpcClient implements ApplicationContextAware, InitializingBean {

    private ApplicationContext ctx;

    @Override
    public void afterPropertiesSet() throws Exception {

        Map<String, RpcClientInteceptor> rpcClientInteceptorMap = ctx.getBeansOfType(RpcClientInteceptor.class);
        if (null != rpcClientInteceptorMap) {
            rpcClientInteceptorMap.values().forEach(super::addInterceptor);
        }

        Map<String, ClientBean> clientBeanMap = ctx.getBeansOfType(ClientBean.class);

        ConfigurableApplicationContext context = (ConfigurableApplicationContext) ctx;
        DefaultListableBeanFactory dbf = (DefaultListableBeanFactory) context.getBeanFactory();

        if (clientBeanMap != null && !clientBeanMap.isEmpty()) {
            clientBeanMap.values().forEach(bean -> {
                String id = bean.getId();
                String interfaceName = bean.getServiceName();
                try {
                    Class<?> clazz = Class.forName(interfaceName);
                    Object object = super.getProxyBean(clazz);
                    dbf.registerSingleton(id, object);
                    log.info("Bind rpc service [{}]", interfaceName);
                } catch (Exception e) {
                    log.warn("Not found rpc service [{}] component!", interfaceName);
                }
            });
        }
        super.init();
        referers.forEach(referer -> super.initReferer(referer, dbf));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }

}