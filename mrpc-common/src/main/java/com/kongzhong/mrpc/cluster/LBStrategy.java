package com.kongzhong.mrpc.cluster;

/**
 * 负载均衡策略
 * <p>
 * Created by biezhi on 2016/12/30.
 */
public enum LBStrategy {

    POLL("轮询"),
    RANDOM("随机"),
    LAST("最新节点");

    private final String desc;

    LBStrategy(String desc) {
        this.desc = desc;
    }

}