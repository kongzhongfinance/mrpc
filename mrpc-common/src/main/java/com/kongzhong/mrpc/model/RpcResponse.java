package com.kongzhong.mrpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * 响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RpcResponse implements Serializable {

    private String requestId;
    private Object result;
    private String exception;
    private String returnType;

}