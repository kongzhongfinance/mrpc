package com.kongzhong.mrpc.enums;

/**
 * 负载均衡策略
 * <p>
 * <p>
 * Created by biezhi on 2016/12/30.
 */
public enum LbStrategyEnum {

    ROUND("轮询"),
    WEIGHT_ROUND("加权轮询"),
    WEIGHT_RANDOM("加权随机"),
    RANDOM("随机"),
    CALLLEAST("最少调用");

    private final String desc;

    LbStrategyEnum(String desc) {
        this.desc = desc;
    }

}