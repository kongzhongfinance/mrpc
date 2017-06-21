package com.kongzhong.mrpc.config;

import com.google.common.collect.Lists;
import com.kongzhong.mrpc.interceptor.RpcServerInteceptor;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 全局服务配置
 *
 * @author biezhi
 *         20/06/2017
 */
@Data
@ToString
public class GlobalConfig {

    private String appId;
    private String transport;
    private String serialize;
    private List<RpcServerInteceptor> inteceptors = Lists.newArrayList();

}
