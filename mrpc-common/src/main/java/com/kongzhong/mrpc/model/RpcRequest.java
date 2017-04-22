package com.kongzhong.mrpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Arrays;

/**
 * 请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RpcRequest {

    private String requestId;
    private String className;
    private String methodName;
    private Class[] parameterTypes;
    private Object[] parameters;
    private Class<?> returnType;


}
