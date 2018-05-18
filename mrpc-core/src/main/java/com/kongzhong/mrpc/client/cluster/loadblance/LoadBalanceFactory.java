package com.kongzhong.mrpc.client.cluster.loadblance;

import com.kongzhong.mrpc.client.cluster.LoadBalance;
import com.kongzhong.mrpc.enums.LbStrategyEnum;
import com.kongzhong.mrpc.exception.RpcException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 负载均衡策略静态工厂
 * <p>
 * Created by biezhi on 09/07/2017.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoadBalanceFactory {

    private static final LoadBalance ROUND_ROBIN_STRATEGY        = new RoundRobinStrategy();
    private static final LoadBalance RANDOM_STRATEGY             = new RandomStrategy();
    private static final LoadBalance CALL_LEAST_STRATEGY         = new CallLeastStrategy();
    private static final LoadBalance WEIGHT_ROUND_ROBIN_STRATEGY = new WeightRoundRobinStrategy();
    private static final LoadBalance WEIGHT_RANDOM_STRATEGY      = new WeightRandomStrategy();

    public static LoadBalance getLoadBalance(@NonNull LbStrategyEnum lbStrategyEnum) {
        switch (lbStrategyEnum) {
            case ROUND:
                return ROUND_ROBIN_STRATEGY;
            case WEIGHT_ROUND:
                return WEIGHT_ROUND_ROBIN_STRATEGY;
            case WEIGHT_RANDOM:
                return WEIGHT_RANDOM_STRATEGY;
            case RANDOM:
                return RANDOM_STRATEGY;
            case CALLLEAST:
                return CALL_LEAST_STRATEGY;
            default:
                throw new RpcException(String.format("No haStrategy [%s]", lbStrategyEnum.name()));
        }
    }

}
