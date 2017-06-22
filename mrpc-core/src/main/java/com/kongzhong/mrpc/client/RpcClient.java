package com.kongzhong.mrpc.client;

import com.google.common.collect.Maps;
import com.kongzhong.mrpc.enums.RegistryEnum;
import com.kongzhong.mrpc.interceptor.RpcClientInteceptor;
import com.kongzhong.mrpc.model.ClientBean;
import com.kongzhong.mrpc.model.RegistryBean;
import com.kongzhong.mrpc.registry.DefaultDiscovery;
import com.kongzhong.mrpc.registry.DefaultRegistry;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.registry.ServiceRegistry;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

import static com.kongzhong.mrpc.Const.MRPC_CLIENT_DISCOVERY_PREFIX;
import static com.kongzhong.mrpc.Const.MRPC_SERVER_REGISTRY_PREFIX;

/**
 * rpc客户端
 */
@NoArgsConstructor
@Slf4j
public class RpcClient extends SimpleRpcClient implements ApplicationContextAware, InitializingBean {

    private ApplicationContext ctx;

    @Override
    public void afterPropertiesSet() throws Exception {

        Map<String, RegistryBean> registryBeanMap = ctx.getBeansOfType(RegistryBean.class);
        if (null != registryBeanMap) {
            registryBeanMap.values().forEach(registryBean -> serviceDiscoveryMap.put(MRPC_CLIENT_DISCOVERY_PREFIX + registryBean.getName(), parseRegistry(registryBean)));
        }

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

    protected ServiceDiscovery parseRegistry(RegistryBean registryBean) {
        String type = registryBean.getType();
        if (RegistryEnum.DEFAULT.getName().equals(type)) {
            ServiceDiscovery serviceDiscovery = new DefaultDiscovery();
            return serviceDiscovery;
        }
        if (RegistryEnum.ZOOKEEPER.getName().equals(type)) {
            String zkAddr = registryBean.getAddress();
            log.info("RPC server connect zookeeper address: {}", zkAddr);
            try {
                Object zookeeperDiscovery = Class.forName("com.kongzhong.mrpc.discover.ZookeeperServiceDiscovery").getConstructor(String.class).newInstance(zkAddr);
                ServiceDiscovery serviceDiscovery = (ServiceDiscovery) zookeeperDiscovery;
                return serviceDiscovery;
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return null;
    }

}