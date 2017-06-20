package com.kongzhong.mrpc.exception;

/**
 * Http请求异常
 *
 * @author biezhi
 *         2017/4/24
 */
public class HttpException extends Exception {

    public HttpException() {
    }

    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpException(Throwable cause) {
        super(cause);
    }
}
