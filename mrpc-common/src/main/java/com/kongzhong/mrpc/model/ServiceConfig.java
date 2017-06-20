package com.kongzhong.mrpc.model;

import com.google.common.collect.Lists;
import com.kongzhong.mrpc.interceptor.RpcInteceptor;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author biezhi
 *         20/06/2017
 */
@Data
@ToString
public class ServiceConfig {

    private String id;
    private String appId;
    private String address;
    private String transport;
    private String serialize;
    private String serviceRegistry;
    private List<RpcInteceptor> inteceptors = Lists.newArrayList();

}
