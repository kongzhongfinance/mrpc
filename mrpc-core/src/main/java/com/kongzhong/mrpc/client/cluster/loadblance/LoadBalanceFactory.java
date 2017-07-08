package com.kongzhong.mrpc.client.cluster.loadblance;

import com.kongzhong.mrpc.client.Connections;
import com.kongzhong.mrpc.client.cluster.LoadBalance;
import com.kongzhong.mrpc.enums.LbStrategyEnum;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

/**
 * Created by biezhi on 09/07/2017.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoadBalanceFactory {

    private static final LoadBalance roundRobinLoadBalance = new RoundRobinLoadBalance();

    private static final LoadBalance randomLoadBalance = new RandomLoadBalance();

    public static LoadBalance getLoadBalance(@NonNull LbStrategyEnum lbStrategyEnum) {
        switch (lbStrategyEnum) {
            case ROUND:
                return roundRobinLoadBalance;
            case RANDOM:
                return randomLoadBalance;
            default:
                throw new RpcException(String.format("No haStrategy [%s]", lbStrategyEnum.name()));
        }
    }

}
