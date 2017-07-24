package com.kongzhong.mrpc.admin.model.dto;

import com.kongzhong.mrpc.enums.HaStrategyEnum;
import com.kongzhong.mrpc.enums.LbStrategyEnum;
import lombok.Data;
import lombok.ToString;

/**
 * 客户端公共配置
 *
 * @author biezhi
 *         20/06/2017
 */
@Data
@ToString(callSuper = true)
public class ClientConfig {

    private HaStrategyEnum haStrategy = HaStrategyEnum.FAILOVER;
    private LbStrategyEnum lbStrategy = LbStrategyEnum.RANDOM;

    /**
     * 客户端服务调用超时，单位/毫秒
     */
    private int waitTimeout = 10_000;

    /**
     * 快速失效重试次数
     */
    private int failOverRetry = 3;

    /**
     * 重试间隔，单位/毫秒 默认每3秒重连一次
     */
    private int retryInterval = 3000;

    /**
     * 重试次数，默认10次
     */
    private int retryCount = 10;

    /**
     * 客户端定时ping服务端的频率，单位/毫秒
     */
    private int pingInterval = -1;

}
