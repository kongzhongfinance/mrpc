package com.kongzhong.mrpc.model;

import lombok.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * RPC请求对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RpcRequest implements Serializable {

    private String     appId;
    private String     requestId;
    private String     className;
    private String     methodName;
    private Class<?>[] parameterTypes;
    private Object[]   parameters;
    private Class<?>   returnType;
    private String     fallbackType;
    private String     fallbackMethod;
    private long       timestamp;
    private int        waitTimeout;
    @Builder.Default
    private Map<String, String> context = new HashMap<>();

    public void addContext(String key, String value){
        context.putIfAbsent(key, value);
    }
}