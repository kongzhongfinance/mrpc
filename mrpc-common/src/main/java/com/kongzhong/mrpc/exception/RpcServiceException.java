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

    private String code;
    private String message;

    public RpcServiceException() {
    }

    public RpcServiceException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public RpcServiceException(String message) {
        super(message);
        this.message = message;
    }

    public RpcServiceException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public RpcServiceException(Throwable cause) {
        super(cause);
        this.message = cause.getMessage();
    }

}
