package com.kongzhong.mrpc.model;

import com.kongzhong.mrpc.utils.StringUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 客户端引用Bean
 */
@Data
@NoArgsConstructor
@ToString
public class ClientBean {

    private String id;
    private String appId;
    private String serviceName;
    private Class<?> serviceClass;
    private String registry;
    private String version;
    private String directAddress;
    private int waitTimeout = 10_000;

    public ClientBean(Class<?> serviceClass) {
        this(StringUtils.getSimpleClassName(serviceClass), serviceClass);
    }

    public ClientBean(String id, Class<?> serviceClass) {
        this.id = id;
        this.serviceName = serviceClass.getName();
        this.serviceClass = serviceClass;
    }

}