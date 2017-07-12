package com.kongzhong.mrpc.exception;

import lombok.Data;

/**
 * Rpc服务调用异常
 *
 * @author biezhi
 *         2017/4/24
 */
@Data
public class RpcServiceException extends RuntimeException {

    private String message;
    private String exceptionType;

    public RpcServiceException(String exceptionType, String message) {
        super(message);
        this.exceptionType = exceptionType;
        this.message = message;
    }

}
