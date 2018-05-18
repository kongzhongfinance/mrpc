package com.kongzhong.mrpc.exception;

import lombok.NoArgsConstructor;

/**
 * RPC初始化异常
 *
 * @author biezhi
 *         2017/4/20
 */
@NoArgsConstructor
public class InitializeException extends RuntimeException {

    public InitializeException(String message) {
        super(message);
    }

}
