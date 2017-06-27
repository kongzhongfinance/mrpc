package com.kongzhong.mrpc.exception;

/**
 * RPC初始化异常
 *
 * @author biezhi
 *         2017/4/20
 */
public class InitializeException extends RuntimeException {

    public InitializeException() {
    }

    public InitializeException(String message) {
        super(message);
    }

    public InitializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InitializeException(Throwable cause) {
        super(cause);
    }
}
