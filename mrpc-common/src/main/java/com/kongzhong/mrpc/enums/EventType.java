package com.kongzhong.mrpc.enums;

/**
 * 事件类型
 * <p>
 * Created by biezhi on 03/07/2017.
 */
public enum EventType {

    SERVER_ONLINE("服务端启动后"),
    SERVER_OFFLINE("停止下线时");

    private String desc;

    EventType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
