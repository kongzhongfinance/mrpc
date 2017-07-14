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

    private static final LoadBalance roundRobinLoadBalance    = new RoundRobinStrategy();
    private static final LoadBalance randomLoadBalance        = new RandomStrategy();
    private static final LoadBalance callLeastStrategy        = new CallLeastStrategy();
    private static final LoadBalance weightRoundRobinStrategy = new WeightRoundRobinStrategy();
    private static final LoadBalance weightRandomStrategy     = new WeightRandomStrategy();

    public static LoadBalance getLoadBalance(@NonNull LbStrategyEnum lbStrategyEnum) {
        switch (lbStrategyEnum) {
            case ROUND:
                return roundRobinLoadBalance;
            case WEIGHT_ROUND:
                return weightRoundRobinStrategy;
            case WEIGHT_RANDOM:
                return weightRandomStrategy;
            case RANDOM:
                return randomLoadBalance;
            case CALLLEAST:
                return callLeastStrategy;
            default:
                throw new RpcException(String.format("No haStrategy [%s]", lbStrategyEnum.name()));
        }
    }

}
