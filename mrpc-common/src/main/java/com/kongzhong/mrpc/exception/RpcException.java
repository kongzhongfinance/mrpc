package com.kongzhong.mrpc.exception;

import lombok.NoArgsConstructor;

/**
 * RPC顶层异常
 *
 * @author biezhi
 *         2017/4/20
 */
@NoArgsConstructor
public class RpcException extends RuntimeException {

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }
}
