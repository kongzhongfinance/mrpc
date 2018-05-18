package com.kongzhong.mrpc.exception;

/**
 * Rpc调用超时异常
 *
 * @author biezhi
 *         2017/4/20
 */
public class TimeoutException extends RuntimeException {

    public TimeoutException(String message) {
        super(message);
    }

}
