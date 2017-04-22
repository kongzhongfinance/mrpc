package com.kongzhong.mrpc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Arrays;

/**
 * 请求
 */
@Data
@NoArgsConstructor
@ToString
public class RpcRequest {

    private String requestId;
    private String className;
    private String methodName;
    private Class[] parameterTypes;
    private Object[] parameters;
    private Class<?> returnType;

    public RpcRequest(String requestId, String className, String methodName, Class[] parameterTypes, Object[] parameters) {
        this.requestId = requestId.toLowerCase();
        this.className = className;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.parameters = parameters;
    }

}
