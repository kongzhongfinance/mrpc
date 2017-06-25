package com.kongzhong.mrpc.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.exception.SystemException;
import com.kongzhong.mrpc.interceptor.RpcServerInteceptor;
import com.kongzhong.mrpc.model.NoInterface;
import com.kongzhong.mrpc.model.ServiceBean;
import com.kongzhong.mrpc.spring.utils.AopTargetUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * RPC映射关系存储
 *
 * @author biezhi
 *         2017/4/24
 */
@Data
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RpcMapping {

    private Map<String, ServiceBean> serviceBeanMap = Maps.newConcurrentMap();
    private List<RpcServerInteceptor> inteceptors = Lists.newArrayList();

    /**
     * 添加一个服务Bean
     *
     * @param bean
     * @param beanName
     */
    public void addServiceBean(Object bean, String beanName) {
        RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
        try {
            if (null == rpcService) {
                return;
            }
            Object realBean = AopTargetUtils.getTarget(bean);
            String serviceName = rpcService.value().getName();
            String appId = rpcService.appId();
            String registry = rpcService.registry();
            String address = rpcService.address();
            String elasticIp = rpcService.elasticIp();

            if (NoInterface.class.getName().equals(serviceName)) {
                Class<?>[] intes = realBean.getClass().getInterfaces();
                if (null == intes || intes.length != 1) {
                    serviceName = realBean.getClass().getName();
                } else {
                    serviceName = intes[0].getName();
                }
            }

            beanName = StringUtils.isNotEmpty(rpcService.name()) ? rpcService.name() : beanName;

            ServiceBean serviceBean = new ServiceBean();
            serviceBean.setAppId(appId);
            serviceBean.setBean(realBean);
            serviceBean.setBeanName(beanName);
            serviceBean.setServiceName(serviceName);
            serviceBean.setRegistry(registry);
            serviceBean.setAddress(address);
            serviceBean.setElasticIp(elasticIp);
            this.addServiceBean(serviceBean);
        } catch (Exception e) {
            log.error("Add service bean [" + beanName + "] error", e);
        }
    }

    public RpcMapping addServiceBean(ServiceBean serviceBean) {
        if (null == serviceBean) {
            throw new SystemException("Service bean not is null");
        }
        serviceBeanMap.put(serviceBean.getServiceName(), serviceBean);
        return this;
    }

    /**
     * 添加一个服务端拦截器
     *
     * @param rpcServerInteceptor
     */
    public void addInterceptor(RpcServerInteceptor rpcServerInteceptor) {
        if (null == rpcServerInteceptor) {
            throw new SystemException("RpcServerInteceptor bean not is null");
        }
        log.info("add interceptor [{}]", rpcServerInteceptor);
        this.inteceptors.add(rpcServerInteceptor);
    }

    /**
     * 添加一组服务端拦截器
     *
     * @param rpcServerInteceptors
     */
    public void addInterceptors(List<RpcServerInteceptor> rpcServerInteceptors) {
        if (null == rpcServerInteceptors) {
            throw new SystemException("RpcServerInteceptors bean not is null");
        }
        log.info("add interceptors {}", rpcServerInteceptors.toString());
        this.inteceptors.addAll(rpcServerInteceptors);
    }

    private static final class RpcMappingHolder {
        private static final RpcMapping INSTANCE = new RpcMapping();
    }

    public static RpcMapping me() {
        return RpcMappingHolder.INSTANCE;
    }

}
