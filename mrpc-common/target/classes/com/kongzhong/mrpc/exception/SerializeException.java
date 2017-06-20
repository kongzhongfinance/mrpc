package com.kongzhong.mrpc.exception;

/**
 * 序列化异常
 *
 * @author biezhi
 *         2017/4/26
 */
public class SerializeException extends Exception {

    public SerializeException() {
    }

    public SerializeException(String message) {
        super(message);
    }

    public SerializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializeException(Throwable cause) {
        super(cause);
    }
}
