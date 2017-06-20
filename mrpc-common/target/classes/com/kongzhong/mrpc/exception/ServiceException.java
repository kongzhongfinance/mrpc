package com.kongzhong.mrpc.exception;

/**
 * 业务异常
 *
 * @author biezhi
 *         2017/4/24
 */
public class ServiceException extends RuntimeException {

    private String code;

    public ServiceException() {
    }

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
