package com.kongzhong.mrpc.exception;

/**
 * RPC顶层异常
 *
 * @author biezhi
 *         2017/4/20
 */
public class RpcException extends Exception {

    public RpcException() {
    }

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
