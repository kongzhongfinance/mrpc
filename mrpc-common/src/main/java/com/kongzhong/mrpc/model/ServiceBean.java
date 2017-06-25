package com.kongzhong.mrpc.model;

import lombok.Data;
import lombok.ToString;

/**
 * 服务Bean
 *
 * @author biezhi
 *         20/06/2017
 */
@Data
@ToString
public class ServiceBean {

    private String appId;
    private String registry;
    private String version;
    private String serviceName;
    private Object bean;
    private String beanName;
    private String address;
    private String elasticIp;

}
