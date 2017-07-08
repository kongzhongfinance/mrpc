package com.kongzhong.mrpc.demo.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceException extends Exception {
    private static final long serialVersionUID = 6056337532100279861L;
    private final String errorCode;
    private final String errorMessage;

    public ServiceException() {
        super();
        this.errorCode = "DEFAULT";
        this.errorMessage = "";
    }

    public ServiceException(String errorCode, String errorMessage) {
        super(errorMessage);

        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public ServiceException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);

        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}