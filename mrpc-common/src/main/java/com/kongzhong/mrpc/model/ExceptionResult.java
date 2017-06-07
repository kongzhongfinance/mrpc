package com.kongzhong.mrpc.model;

import java.io.Serializable;

/**
 * @author biezhi
 *         2017/6/7
 */
public class ExceptionResult implements Serializable {

    private String stackInfo;
    private Class<?> exceptionType;

    public ExceptionResult() {

    }

    public ExceptionResult(String stackInfo, Class<?> exceptionType) {
        this.stackInfo = stackInfo;
        this.exceptionType = exceptionType;
    }

    public String getStackInfo() {
        return stackInfo;
    }

    public void setStackInfo(String stackInfo) {
        this.stackInfo = stackInfo;
    }

    public Class<?> getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(Class<?> exceptionType) {
        this.exceptionType = exceptionType;
    }
}
