package com.kongzhong.mrpc.model;

import java.io.Serializable;

public class Message<T> implements Serializable {

    private Header header;
    private T content;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }
}
