package com.kongzhong.mrpc.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.Reflection;
import com.kongzhong.mrpc.cluster.Connections;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.exception.InitializeException;
import com.kongzhong.mrpc.model.ClientBean;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.serialize.ProtostuffSerialize;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.utils.ReflectUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * rpc客户端
 */
@Data
@Slf4j
public class RpcClient extends SimpleRpcClient implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext cxt;

    public RpcClient(String serverAddr) {
        super(serverAddr);
    }

    public RpcClient(ServiceDiscovery serviceDiscovery) {
        super(serviceDiscovery);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, ClientBean> clientBeanMap = cxt.getBeansOfType(ClientBean.class);
        RpcClient rpcClient = cxt.getBean(RpcClient.class);

        ConfigurableApplicationContext context = (ConfigurableApplicationContext) cxt;
        DefaultListableBeanFactory dbf = (DefaultListableBeanFactory) context.getBeanFactory();

        if (null != rpcClient && clientBeanMap != null && !clientBeanMap.isEmpty()) {
            for (ClientBean bean : clientBeanMap.values()) {
                String id = bean.getId();
                String interfaceName = bean.getInterfaceName();
                try {
                    Class<?> clazz = Class.forName(interfaceName);
                    Object object = rpcClient.getProxyBean(clazz);
                    dbf.registerSingleton(id, object);
                    log.info("Bind rpc service [{}]", interfaceName);
                } catch (Exception e) {
                    log.warn("Not found rpc service [{}] component!", interfaceName);
                }
            }
        }

        if (null != referers && !referers.isEmpty()) {
            referers.forEach(clazz -> {
                String interfaceName = clazz.getName();
                try {
                    Object object = rpcClient.getProxyBean(clazz);

                    String simeName = clazz.getSimpleName().substring(0, 1).toLowerCase() + clazz.getSimpleName().substring(1);

                    dbf.registerSingleton(interfaceName, object);
                    dbf.registerSingleton(simeName, object);
                    log.info("Bind rpc service [{}]", interfaceName);
                } catch (Exception e) {
                    log.warn("Not found rpc service [{}] component!", interfaceName);
                }
            });
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        log.info("Initializing rpc client.");
        cxt = applicationContext;
    }

}