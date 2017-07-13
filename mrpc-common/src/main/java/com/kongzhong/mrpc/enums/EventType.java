package com.kongzhong.mrpc.enums;

/**
 * 事件类型
 * <p>
 * Created by biezhi on 03/07/2017.
 */
public enum EventType {

    SERVER_STARTING("服务端启动"),
    SERVER_STARTED("服务端启动后"),
    SERVER_SERVICE_REGISTER("服务注册后"),
    SERVER_CLIENT_CONNECTED("客户端连接后"),
    SERVER_CLIENT_DISCONNECT("客户端断开连接后"),
    SERVER_ACCEPT("服务端收到请求后"),
    SERVER_PRE_RESPONSE("服务端发送响应前");

    private String desc;

    EventType(String desc) {
        this.desc = desc;
    }

}
