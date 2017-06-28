package com.kongzhong.mrpc.interceptor.validator.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by IFT8 on 2017/6/24.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ValidateException extends RuntimeException {
    private static final long serialVersionUID = -5770807215499382620L;

    private final String errMsg;

    public ValidateException(String errMsg) {
        super(errMsg);
        this.errMsg = errMsg;
    }
}
