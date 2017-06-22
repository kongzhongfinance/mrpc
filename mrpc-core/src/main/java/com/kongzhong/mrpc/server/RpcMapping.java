package com.kongzhong.mrpc.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kongzhong.mrpc.exception.SystemException;
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
        if (null == serviceBean) {
            throw new SystemException("Service bean not is null");
        }
        serviceBeanMap.put(serviceBean.getServiceName(), serviceBean);
        return this;
    }

    public void addInterceptor(RpcServerInteceptor rpcServerInteceptor) {
        if (null == rpcServerInteceptor) {
            throw new SystemException("RpcServerInteceptor bean not is null");
        }
        log.info("add interceptor [{}]", rpcServerInteceptor);
        this.inteceptors.add(rpcServerInteceptor);
    }

    public void addInterceptors(List<RpcServerInteceptor> rpcServerInteceptors) {
        if (null == rpcServerInteceptors) {
            throw new SystemException("RpcServerInteceptors bean not is null");
        }
        log.info("add interceptors {}", rpcServerInteceptors.toString());
        this.inteceptors.addAll(rpcServerInteceptors);
    }

    public static RpcMapping me() {
        return RpcMappingHolder.INSTANCE;
    }

}
