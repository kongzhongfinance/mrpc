package com.kongzhong.mrpc.mbean;

import com.kongzhong.mrpc.model.ServiceBean;
import com.kongzhong.mrpc.model.ServiceStatus;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.BeanUtils;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务状态表
 * <p>
 * Created by biezhi on 01/07/2017.
 */
@ManagedResource
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class ServiceStatusTable implements Serializable {

    private Map<String, ServiceStatus> serviceStatsMap = new HashMap<>();

    public void createServiceStatus(ServiceBean serviceBean) {
        String serviceName = serviceBean.getServiceName();
        ServiceStatus serviceStatus = new ServiceStatus();
        BeanUtils.copyProperties(serviceBean, serviceStatus);
        serviceStatus.setAlive(true);
        serviceStatsMap.put(serviceName, serviceStatus);
    }

    public void addSuccessInvoke(String serviceName) {
        serviceStatsMap.get(serviceName).addSuccess();
    }

    public void addErrorInvoke(String serviceName) {
        serviceStatsMap.get(serviceName).addError();
    }

    public void addTimeoutInvoke(String serviceName) {
        serviceStatsMap.get(serviceName).addTimeout();
    }

    private static final class ServiceStatusTableHolder {
        private static final ServiceStatusTable INSTANCE = new ServiceStatusTable();
    }

    public static ServiceStatusTable me() {
        return ServiceStatusTableHolder.INSTANCE;
    }

    @ManagedOperation
    public String getServiceStatus(boolean pretty) {
        return JacksonSerialize.toJSONString(serviceStatsMap, pretty);
    }

}
