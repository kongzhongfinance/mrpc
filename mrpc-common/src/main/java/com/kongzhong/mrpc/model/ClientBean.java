package com.kongzhong.mrpc.model;

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
    private String serviceName;
    private Class<?> serviceClass;
    private String registry = "default";
    private String directAddress;
    private int waitTimeout;

    public ClientBean(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
        this.serviceName = serviceClass.getName();
    }
}