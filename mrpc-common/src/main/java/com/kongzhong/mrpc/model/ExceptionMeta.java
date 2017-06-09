package com.kongzhong.mrpc.model;

import java.io.Serializable;

/**
 * 异常元信息
 *
 * @author biezhi
 *         2017/6/7
 */
public class ExceptionMeta implements Serializable {

    private String type;
    private Object data;

    public ExceptionMeta(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
