package com.kongzhong.mrpc.model;

import lombok.Data;
import lombok.ToString;

/**
 * @author biezhi
 *         20/06/2017
 */
@Data
@ToString
public class ServiceBean {

    private String appId;
    private String registry = "default";
    private String serviceName;
    private Object bean;
    private String address;
    private String elasticIp;
    private boolean direct;

}
