package com.kongzhong.mrpc.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.exception.SystemException;
import com.kongzhong.mrpc.interceptor.RpcServerInterceptor;
import com.kongzhong.mrpc.model.NoInterface;
import com.kongzhong.mrpc.model.ServiceBean;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * RPC映射关系存储
 *
 * @author biezhi
 * 2017/4/24
 */
@Data
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RpcMapping {

    private Map<String, ServiceBean>   serviceBeanMap     = Maps.newConcurrentMap();
    private List<RpcServerInterceptor> serverInterceptors = Lists.newArrayList();

    /**
     * 添加一个服务Bean
     *
     * @param bean     服务Bean对象
     * @param beanName Bean在IOC容器中的名称
     */
    public void addServiceBean(Object bean, String beanName) {
        Class<?>   targetClass = AopUtils.getTargetClass(bean);
        RpcService rpcService  = targetClass.getAnnotation(RpcService.class);
        try {
            if (null == rpcService) {
                return;
            }
//            String serviceName = rpcService.value().getName();
            String appId     = rpcService.appId();
            String registry  = rpcService.registry();
            String address   = rpcService.address();
            String elasticIp = rpcService.elasticIp();

            final String realBeanName = StringUtils.isNotEmpty(rpcService.name()) ? rpcService.name() : beanName;

            if (NoInterface.class.getName().equals(rpcService.value().getName())) {
                Class<?>[] interfaces = targetClass.getInterfaces();
                if (null == interfaces) {
                    String      serviceName = targetClass.getName();
                    ServiceBean serviceBean = new ServiceBean();
                    serviceBean.setAppId(appId);
                    serviceBean.setBean(bean);
                    serviceBean.setBeanName(realBeanName);
                    serviceBean.setServiceName(serviceName);
                    serviceBean.setRegistry(registry);
                    serviceBean.setAddress(address);
                    serviceBean.setElasticIp(elasticIp);
                    this.addServiceBean(serviceBean);
                } else {
                    Stream.of(interfaces).map(Class::getName)
                            .forEach(serviceName -> {
                                ServiceBean serviceBean = new ServiceBean();
                                serviceBean.setAppId(appId);
                                serviceBean.setBean(bean);
                                serviceBean.setBeanName(realBeanName);
                                serviceBean.setServiceName(serviceName);
                                serviceBean.setRegistry(registry);
                                serviceBean.setAddress(address);
                                serviceBean.setElasticIp(elasticIp);
                                this.addServiceBean(serviceBean);
                            });
                }
            }

        } catch (Exception e) {
            log.error("Add service bean [" + beanName + "] error", e);
        }
    }

    void addServiceBean(ServiceBean serviceBean) {
        if (null == serviceBean) {
            throw new SystemException("Service bean not is null");
        }
        serviceBeanMap.put(serviceBean.getServiceName(), serviceBean);
    }

    /**
     * 添加一个服务端拦截器
     *
     * @param rpcServerInterceptor 服务端拦截器
     */
    public void addInterceptor(RpcServerInterceptor rpcServerInterceptor) {
        if (null == rpcServerInterceptor) {
            throw new SystemException("RpcServerInterceptor bean not is null");
        }
        log.info("Add server interceptor [{}]", rpcServerInterceptor);
        this.serverInterceptors.add(rpcServerInterceptor);
    }

    private static final class RpcMappingHolder {
        private static final RpcMapping INSTANCE = new RpcMapping();
    }

    public static RpcMapping me() {
        return RpcMappingHolder.INSTANCE;
    }

}
