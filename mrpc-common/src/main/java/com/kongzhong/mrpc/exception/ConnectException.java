package com.kongzhong.mrpc.exception;

/**
 * RPC连接异常
 *
 * @author biezhi
 *         2017/4/20
 */
public class ConnectException extends RpcException {

    public ConnectException() {
    }

    public ConnectException(String message) {
        super(message);
    }

    public ConnectException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectException(Throwable cause) {
        super(cause);
    }
}
