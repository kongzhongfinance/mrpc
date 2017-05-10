package com.kongzhong.mrpc.enums;

/**
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
