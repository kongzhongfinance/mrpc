package com.kongzhong.mrpc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * 响应
 */
@Data
@ToString
public class RpcResponse implements Serializable {

    private String requestId;
    private Object result;
    private String exception;
    private String returnType;

    public RpcResponse() {

    }

}