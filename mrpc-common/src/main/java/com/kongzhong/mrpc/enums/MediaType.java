package com.kongzhong.mrpc.enums;

/**
 * @author biezhi
 *         2017/4/20
 */
public enum MediaType {

    JSON("application/json; charset=UTF-8"),
    TEXT("text/plain; charset=UTF-8"),
    HTML("text/html; charset=UTF-8"),
    XML("application/json; charset=UTF-8");

    private String contentType;

    MediaType(String contentType) {
        this.contentType = contentType;
    }

    public String toString() {
        return this.contentType;
    }

}
