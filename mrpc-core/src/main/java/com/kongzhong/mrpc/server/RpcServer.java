package com.kongzhong.mrpc.server;

import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.model.NoInterface;
import com.kongzhong.mrpc.model.ServiceBean;
import com.kongzhong.mrpc.registry.ServiceRegistry;
import com.kongzhong.mrpc.spring.utils.AopTargetUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

@Slf4j
@Data
@NoArgsConstructor
public class RpcServer extends SimpleRpcServer implements ApplicationContextAware, InitializingBean {

    public RpcServer(String serverAddress) {
        super(serverAddress);
    }

    public RpcServer(String serverAddress, ServiceRegistry serviceRegistry) {
        super(serverAddress, serviceRegistry);
    }


    /**
     * ① 设置上下文
     *
     * @param ctx
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        try {

            if (null != serviceBeanMap && !serviceBeanMap.isEmpty()) {
                for (Object target : serviceBeanMap.values()) {
                    Object realBean = AopTargetUtils.getTarget(target);
                    RpcService rpcService = realBean.getClass().getAnnotation(RpcService.class);
                    String serviceName = rpcService.value().getName();
                    if (NoInterface.class.getName().equals(serviceName)) {
                        Class<?>[] intes = realBean.getClass().getInterfaces();
                        if (null == intes || intes.length != 1) {
                            serviceName = realBean.getClass().getName();
                        } else {
                            serviceName = intes[0].getName();
                        }
                    }
                    ServiceBean serviceBean = new ServiceBean();
                    serviceBean.setBean(realBean);
                    serviceBean.setServiceName(serviceName);
                    rpcMapping.addServiceBean(serviceBean);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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


}