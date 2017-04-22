package com.kongzhong.mrpc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 响应
 */
@Data
@NoArgsConstructor
@ToString
public class RpcResponse {

    private String requestId;
    private Object result;
    private Throwable exception;
    private String returnType;

}