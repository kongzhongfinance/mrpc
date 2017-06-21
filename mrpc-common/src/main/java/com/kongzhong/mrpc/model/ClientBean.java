package com.kongzhong.mrpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 客户端引用Bean
 */
@Data
@ToString
public class ClientBean {

    private String id;
    private String serviceName;
    private Class<?> serviceClass;
    private String registry = "default";
    private String directAddress;

    public ClientBean() {
    }

    public ClientBean(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
        this.serviceName = serviceClass.getName();
    }
}