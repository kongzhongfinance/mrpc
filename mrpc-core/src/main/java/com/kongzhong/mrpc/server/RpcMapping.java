package com.kongzhong.mrpc.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kongzhong.mrpc.interceptor.RpcServerInteceptor;
import com.kongzhong.mrpc.model.ServiceBean;
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
@NoArgsConstructor
public class RpcMapping {

    private Map<String, ServiceBean> serviceBeanMap = Maps.newConcurrentMap();
    private List<RpcServerInteceptor> inteceptors = Lists.newArrayList();

    private static final class RpcMappingHolder {
        private static final RpcMapping INSTANCE = new RpcMapping();
    }

    public RpcMapping addServiceBean(ServiceBean serviceBean) {
        serviceBeanMap.put(serviceBean.getServiceName(), serviceBean);
        return this;
    }

    public void addInterceptor(RpcServerInteceptor inteceptor) {
        if (null != inteceptor) {
            log.info("add interceptor [{}]", inteceptor);
            this.inteceptors.add(inteceptor);
        }
    }

    public void addInterceptors(List<RpcServerInteceptor> inteceptors) {
        if (null != inteceptors && !inteceptors.isEmpty()) {
            log.info("add interceptors {}", inteceptors.toString());
            this.inteceptors.addAll(inteceptors);
        }
    }

    public static RpcMapping me() {
        return RpcMappingHolder.INSTANCE;
    }

}
