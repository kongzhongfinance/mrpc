package com.kongzhong.mrpc.enums;

/**
 * 支持的返回媒体类型
 *
 * @author biezhi
 *         2017/4/20
 */
public enum MediaTypeEnum {

    JSON("application/json; charset=UTF-8"),
    TEXT("text/plain; charset=UTF-8"),
    HTML("text/html; charset=UTF-8"),
    XML("application/json; charset=UTF-8");

    private String contentType;

    MediaTypeEnum(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String toString() {
        return this.contentType;
    }

}
