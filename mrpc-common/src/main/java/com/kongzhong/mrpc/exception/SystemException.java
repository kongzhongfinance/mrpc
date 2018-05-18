package com.kongzhong.mrpc.exception;

import lombok.NoArgsConstructor;

/**
 * Runtime异常
 *
 * @author biezhi
 *         2017/4/20
 */
@NoArgsConstructor
public class SystemException extends RpcException {

    public SystemException(String message) {
        super(message);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemException(Throwable cause) {
        super(cause);
    }
}
