package com.kongzhong.mrpc.exception;

import lombok.Data;

/**
 * Rpc服务调用异常
 *
 * @author biezhi
 *         2017/4/24
 */
@Data
public class ServiceException extends Exception {

    private String message;
    private String exceptionType;
    private String exceptionMeta;

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String exceptionType, String message, String exceptionMeta) {
        super(message);
        this.exceptionType = exceptionType;
        this.message = message;
        this.exceptionMeta = exceptionMeta;
    }

}
