package com.kongzhong.mrpc.exception;

/**
 * RPC超时异常
 *
 * @author biezhi
 *         2017/4/20
 */
public class TimeoutException extends ConnectException {

    public TimeoutException() {
    }

    public TimeoutException(String message) {
        super(message);
    }

    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public TimeoutException(Throwable cause) {
        super(cause);
    }
}
