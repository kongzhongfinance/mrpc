package com.kongzhong.mrpc.enums;

/**
 * HA策略
 *
 * <p>
 * Created by biezhi on 2016/12/30.
 */
public enum HaStrategyEnum {

    FAILFAST("快速失败"),
    FAILOVER("失效切换");

    private final String desc;

    HaStrategyEnum(String desc) {
        this.desc = desc;
    }

}