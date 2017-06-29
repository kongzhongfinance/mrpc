package com.kongzhong.mrpc.enums;

/**
 * 服务节点存活状态
 *
 * @author biezhi
 *         29/06/2017
 */
public enum NodeAliveStateEnum {

    ALIVE("节点存活"),
    DEAD("节点挂掉"),
    CONNECTING("连接中");

    private final String state;

    NodeAliveStateEnum(String state) {
        this.state = state;
    }

}
