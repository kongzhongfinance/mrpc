package com.kongzhong.mrpc.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 服务状态表
 * <p>
 * Created by biezhi on 01/07/2017.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class ServiceStatusTable implements Serializable {

    @Getter
    private Set<String> services = new HashSet<>();

    public void addService(ServiceBean serviceBean, int weight) {
        services.add(serviceBean.getServiceName());
    }

    private static final class ServiceStatusTableHolder {
        private static final ServiceStatusTable INSTANCE = new ServiceStatusTable();
    }

    public static ServiceStatusTable me() {
        return ServiceStatusTableHolder.INSTANCE;
    }

}
