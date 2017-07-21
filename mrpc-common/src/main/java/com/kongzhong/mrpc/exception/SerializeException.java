package com.kongzhong.mrpc.exception;

import lombok.NoArgsConstructor;

/**
 * 序列化异常
 *
 * @author biezhi
 *         2017/4/26
 */
@NoArgsConstructor
public class SerializeException extends Exception {

    public SerializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializeException(Throwable cause) {
        super(cause);
    }
}
