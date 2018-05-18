package com.kongzhong.mrpc.demo.exception;

/**
 * @author biezhi
 * @date 2018/5/15
 */
public class ServerCustomException extends RuntimeException {

    public ServerCustomException() {
    }

    public ServerCustomException(String message) {
        super(message);
    }

}
