package com.kongzhong.mrpc.model;

import lombok.Data;
import lombok.ToString;

/**
 * 注册中心Bean
 */
@Data
@ToString
public class RegistryBean {

    private String name;
    private String type;
    private String address;

}