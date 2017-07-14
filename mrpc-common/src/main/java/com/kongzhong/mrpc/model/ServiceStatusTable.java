package com.kongzhong.mrpc.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务状态表
 * <p>
 * Created by biezhi on 01/07/2017.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class ServiceStatusTable implements Serializable {

    private Map<String, ServiceStatus> serviceStatsMap = new HashMap<>();

    public void createServiceStatus(ServiceBean serviceBean, int weight) {
        String        serviceName   = serviceBean.getServiceName();
        ServiceStatus serviceStatus = new ServiceStatus();
        BeanUtils.copyProperties(serviceBean, serviceStatus);
        serviceStatus.setWeight(weight);
        serviceStatsMap.put(serviceName, serviceStatus);
    }

    public void addSuccessInvoke(String serviceName) {
        if (serviceStatsMap.containsKey(serviceName)) {
            serviceStatsMap.get(serviceName).addSuccess();
        }
    }

    public void addErrorInvoke(String serviceName) {
        if (serviceStatsMap.containsKey(serviceName)) {
            serviceStatsMap.get(serviceName).addError();
        }
    }

    public void addTimeoutInvoke(String serviceName) {
        if (serviceStatsMap.containsKey(serviceName)) {
            serviceStatsMap.get(serviceName).addTimeout();
        }
    }

    /**
     * 根据服务器地址获取服务器权重
     *
     * @param serverAddress
     * @return
     */
    public int getServerWeight(String serverAddress) {
        ServiceStatus serviceStatus = getServiceStatus(serverAddress);
        if (null != serviceStatus) {
            return serviceStatus.getWeight();
        }
        return 0;
    }

    public void addClient() {
        serviceStatsMap.values().stream()
                .distinct()
                .forEach(ServiceStatus::addClient);
    }

    public void removeClient() {
        serviceStatsMap.values().stream()
                .distinct()
                .forEach(ServiceStatus::removeClient);
    }

    private static final class ServiceStatusTableHolder {
        private static final ServiceStatusTable INSTANCE = new ServiceStatusTable();
    }

    public static ServiceStatusTable me() {
        return ServiceStatusTableHolder.INSTANCE;
    }

    public ServiceStatus getServiceStatus(String serverAddress) {
        return serviceStatsMap.values().stream()
                .filter(serviceStatus -> serverAddress.equals(serviceStatus.getAddress()))
                .findFirst().orElse(null);
    }

    public List<ServiceStatus> getServiceStatus() {
        return new ArrayList<>(serviceStatsMap.values());
    }

}
