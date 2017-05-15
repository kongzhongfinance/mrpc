package com.kongzhong.mrpc.enums;

/**
 * 支持的注册中心类型
 *
 * @author biezhi
 *         2017/5/10
 */
public enum RegistryEnum {

    DEFAULT("default"), ZOOKEEPER("zookeeper"), CONSUL("consul");

    private String name;

    RegistryEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
