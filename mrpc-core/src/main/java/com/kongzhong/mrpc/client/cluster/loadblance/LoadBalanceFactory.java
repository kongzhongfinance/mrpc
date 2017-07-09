package com.kongzhong.mrpc.client.cluster.loadblance;

import com.kongzhong.mrpc.client.cluster.LoadBalance;
import com.kongzhong.mrpc.enums.LbStrategyEnum;
import com.kongzhong.mrpc.exception.RpcException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Created by biezhi on 09/07/2017.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoadBalanceFactory {

    private static final LoadBalance roundRobinLoadBalance = new RoundRobinStrategy();

    private static final LoadBalance randomLoadBalance = new RandomStrategy();

    private static final LoadBalance callLeastStrategy = new CallLeastStrategy();

    public static LoadBalance getLoadBalance(@NonNull LbStrategyEnum lbStrategyEnum) {
        switch (lbStrategyEnum) {
            case ROUND:
                return roundRobinLoadBalance;
            case RANDOM:
                return randomLoadBalance;
            case CALLLEAST:
                return callLeastStrategy;
            default:
                throw new RpcException(String.format("No haStrategy [%s]", lbStrategyEnum.name()));
        }
    }

}
