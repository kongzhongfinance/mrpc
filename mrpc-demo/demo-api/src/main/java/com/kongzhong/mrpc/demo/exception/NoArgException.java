package com.kongzhong.mrpc.demo.exception;

import lombok.NoArgsConstructor;

/**
 * Created by biezhi on 10/07/2017.
 */
public class NoArgException extends RuntimeException {

    private int code;
    private String qqqq;

    public NoArgException() {
    }

    public NoArgException(int code, String qqqq) {
        this.code = code;
        this.qqqq = qqqq;
    }

    public int getCode() {
        return code;
    }

    public String getQqqq() {
        return qqqq;
    }

}
