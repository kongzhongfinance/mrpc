package com.kongzhong.mrpc.server;

import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.enums.RegistryEnum;
import com.kongzhong.mrpc.model.NoInterface;
import com.kongzhong.mrpc.registry.ServiceRegistry;
import com.kongzhong.mrpc.spring.utils.AopTargetUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Map;

@Slf4j
@Data
public class BootRpcServer extends SimpleRpcServer implements BeanFactoryAware, EnvironmentAware {

    private ConfigurableBeanFactory configurableBeanFactory;

    public BootRpcServer() {
        super();
    }

    public BootRpcServer(String serverAddress) {
        super(serverAddress);
    }

    public BootRpcServer(String serverAddress, ServiceRegistry serviceRegistry) {
        super(serverAddress, serviceRegistry);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
        ListableBeanFactory listableBeanFactory = (ListableBeanFactory) beanFactory;
        Map<String, Object> serviceBeanMap = listableBeanFactory.getBeansWithAnnotation(RpcService.class);
        try {

            if (null != serviceBeanMap && !serviceBeanMap.isEmpty()) {
                for (Object serviceBean : serviceBeanMap.values()) {
                    Object realBean = AopTargetUtils.getTarget(serviceBean);
                    RpcService rpcService = realBean.getClass().getAnnotation(RpcService.class);
                    String serviceName = rpcService.value().getName();
                    String version = rpcService.version();
                    String name = rpcService.name();

                    if (StringUtils.isNotEmpty(name)) {
                        serviceName = name;
                    } else {
                        if (NoInterface.class.getName().equals(serviceName)) {
                            Class<?>[] intes = realBean.getClass().getInterfaces();
                            if (null == intes || intes.length != 1) {
                                serviceName = realBean.getClass().getName();
                            } else {
                                serviceName = intes[0].getName();
                            }
                        }
                    }

                    if (StringUtils.isNotEmpty(version)) {
                        serviceName += "_" + version;
                    }
                    rpcMapping.addHandler(serviceName, realBean);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        log.debug("Set Environment");

        this.serverAddress = environment.getProperty("mrpc.server.address", "127.0.0.1:5066");
        this.transport = environment.getProperty("mrpc.transport", "tcp");

        // 注册中心
        String registry = environment.getProperty("mrpc.registry", RegistryEnum.DEFAULT.getName());
        if (RegistryEnum.ZOOKEEPER.getName().equals(registry)) {
            String zkAddr = environment.getProperty("mrpc.zk.addr", "127.0.0.1:2181");
            log.info("zk address: {}", zkAddr);
            String interfaceName = "com.kongzhong.mrpc.registry.ServiceRegistry";
            try {
                Object zookeeperServiceRegistry = Class.forName("com.kongzhong.mrpc.registry.ZookeeperServiceRegistry").getConstructor(String.class).newInstance(zkAddr);
                ServiceRegistry serviceRegistry = (ServiceRegistry) zookeeperServiceRegistry;
                this.setServiceRegistry(serviceRegistry);
                configurableBeanFactory.registerSingleton(interfaceName, serviceRegistry);
            } catch (Exception e) {
                log.error("", e);
            }
        }
        this.startServer();
    }
}