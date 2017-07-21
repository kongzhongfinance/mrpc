package com.kongzhong.mrpc.exception;

import lombok.NoArgsConstructor;

/**
 * Rpc调用超时异常
 *
 * @author biezhi
 *         2017/4/20
 */
@NoArgsConstructor
public class TimeoutException extends Exception {

    public TimeoutException(String message) {
        super(message);
    }

}
