package com.kongzhong.mrpc.exception;

import lombok.NoArgsConstructor;

/**
 * RPC连接异常
 *
 * @author biezhi
 *         2017/4/20
 */
@NoArgsConstructor
public class ConnectException extends RpcException {

    public ConnectException(String message) {
        super(message);
    }

    public ConnectException(String message, Throwable cause) {
        super(message, cause);
    }

}
