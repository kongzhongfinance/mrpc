package com.kongzhong.mrpc.enums;

import lombok.Getter;

/**
 * @author biezhi
 * @date 2018/6/7
 */
@Getter
public enum NoticeType {

    SERVER_ONLINE("服务上线"),
    SERVER_HEART("服务心跳");

    private String desc;

    NoticeType(String desc) {
        this.desc = desc;
    }

}
