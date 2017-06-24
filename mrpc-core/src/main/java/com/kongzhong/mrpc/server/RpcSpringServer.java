package com.kongzhong.mrpc.server;

import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.enums.RegistryEnum;
import com.kongzhong.mrpc.exception.SystemException;
import com.kongzhong.mrpc.interceptor.RpcServerInteceptor;
import com.kongzhong.mrpc.model.RegistryBean;
import com.kongzhong.mrpc.model.ServiceBean;
import com.kongzhong.mrpc.registry.DefaultRegistry;
import com.kongzhong.mrpc.registry.ServiceRegistry;
import com.kongzhong.mrpc.spring.utils.AopTargetUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

import static com.kongzhong.mrpc.Const.MRPC_SERVER_REGISTRY_PREFIX;

/**
 * RPC服务端Spring实现
 *
 * @author biezhi
 *         2017/4/24
 */
@Slf4j
@Data
@NoArgsConstructor
public class RpcSpringServer extends SimpleRpcServer implements ApplicationContextAware, InitializingBean {

    /**
     * ① 设置上下文
     *
     * @param ctx
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {

        // 注册中心
        Map<String, RegistryBean> registryBeanMap = ctx.getBeansOfType(RegistryBean.class);
        if (null != registryBeanMap) {
            registryBeanMap.values().forEach(registryBean -> serviceRegistryMap.put(registryBean.getName(), parseRegistry(registryBean)));
        }

        if (StringUtils.isNotEmpty(this.interceptors)) {
            String[] inters = this.interceptors.split(",");
            for (String interceptorName : inters) {
                RpcServerInteceptor rpcServerInteceptor = (RpcServerInteceptor) ctx.getBean(interceptorName);
                rpcMapping.addInterceptor(rpcServerInteceptor);
            }
        }

        Map<String, Object> rpcServiceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        if (null != rpcServiceBeanMap && !rpcServiceBeanMap.isEmpty()) {
            rpcServiceBeanMap.forEach((beanName, target) -> {
                Object realBean = null;
                try {
                    realBean = AopTargetUtils.getTarget(target);
                } catch (Exception e) {
                    log.error("Get bean target error", e);
                }
                rpcMapping.addServiceBean(realBean, beanName);
            });
        }

        // 服务
        Map<String, ServiceBean> serviceBeanMap = ctx.getBeansOfType(ServiceBean.class);
        if (serviceBeanMap != null && !serviceBeanMap.isEmpty()) {
            serviceBeanMap.values().forEach(serviceBean -> {
                if (null == serviceBean.getBean()) {
                    Object bean = ctx.getBean(serviceBean.getBeanName());
                    if (null == bean) {
                        throw new SystemException(String.format("Not found bean [%s]", serviceBean.getBeanName()));
                    }
                    serviceBean.setBean(bean);
                    rpcMapping.addServiceBean(serviceBean);
                }
            });
        }
    }

    /**
     * ② 后置操作
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        this.startServer();
    }

    protected ServiceRegistry parseRegistry(RegistryBean registryBean) {
        String type = registryBean.getType();
        if (RegistryEnum.DEFAULT.getName().equals(type)) {
            ServiceRegistry serviceRegistry = new DefaultRegistry();
            return serviceRegistry;
        }
        if (RegistryEnum.ZOOKEEPER.getName().equals(type)) {
            String zkAddr = registryBean.getAddress();
            log.info("RPC server connect zookeeper address: {}", zkAddr);
            try {
                Object zookeeperServiceRegistry = Class.forName("com.kongzhong.mrpc.registry.ZookeeperServiceRegistry").getConstructor(String.class).newInstance(zkAddr);
                ServiceRegistry serviceRegistry = (ServiceRegistry) zookeeperServiceRegistry;
                return serviceRegistry;
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return null;
    }


}