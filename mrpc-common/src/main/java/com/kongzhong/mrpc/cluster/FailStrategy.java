package com.kongzhong.mrpc.cluster;

/**
 * 容错策略
 * <p>
 * Created by biezhi on 2016/12/30.
 */
public enum FailStrategy {

    FAILFAST("快速失败"),
    FAILOVER("失效切换");

    private final String desc;

    FailStrategy(String desc) {
        this.desc = desc;
    }

}