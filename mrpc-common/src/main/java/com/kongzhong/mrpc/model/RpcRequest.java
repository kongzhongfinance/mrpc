package com.kongzhong.mrpc.model;

import lombok.*;

import java.io.Serializable;

/**
 * RPC请求对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RpcRequest implements Serializable {

    private String appId;
    private String requestId;
    private String className;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
    private Class<?> returnType;
    private long timestamp;
    private int waitTimeout;

}