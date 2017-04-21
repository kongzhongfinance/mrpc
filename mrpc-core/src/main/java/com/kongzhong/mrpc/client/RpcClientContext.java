package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.model.ClientBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

/**
 * @author biezhi
 *         2017/4/21
 */
public class RpcClientContext implements ApplicationContextAware, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(RpcClientContext.class);

    private ApplicationContext cxt;

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, ClientBean> clientBeanMap = cxt.getBeansOfType(ClientBean.class);
        RpcClient rpcClient = cxt.getBean(RpcClient.class);

        if (null != rpcClient && clientBeanMap != null && !clientBeanMap.isEmpty()) {
            ConfigurableApplicationContext context = (ConfigurableApplicationContext) cxt;
            DefaultListableBeanFactory dbf = (DefaultListableBeanFactory) context.getBeanFactory();
            for (ClientBean bean : clientBeanMap.values()) {
                String id = bean.getId();
                String interfaceName = bean.getInterfaceName();
                try {
                    Class<?> clazz = Class.forName(interfaceName);
                    Object object = rpcClient.getProxyBean(clazz);
                    dbf.registerSingleton(id, object);
                    log.info("bind rpc service [{}]", interfaceName);
                } catch (Exception e) {
                    log.warn("Not found rpc service [{}] component!", interfaceName);
                }
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        cxt = applicationContext;
    }
}
