package com.kongzhong.mrpc.demo.model;

/**
 * @author biezhi
 *         2017/5/23
 */
public class Result<T> {

    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
