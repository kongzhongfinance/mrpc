package com.kongzhong.mrpc.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * RPC响应对象
 */
@Data
@ToString
public class RpcResponse implements Serializable {

    private String requestId;
    private Object result;
    private Boolean success = false;
    private String exception;
    private String returnType;

    public RpcResponse() {

    }
}