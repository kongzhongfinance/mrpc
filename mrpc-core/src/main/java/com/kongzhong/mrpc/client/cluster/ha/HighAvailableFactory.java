package com.kongzhong.mrpc.client.cluster.ha;

import com.kongzhong.mrpc.client.cluster.HaStrategy;
import com.kongzhong.mrpc.enums.HaStrategyEnum;
import com.kongzhong.mrpc.exception.RpcException;
import lombok.NonNull;

/**
 * @author biezhi
 *         29/06/2017
 */
public class HighAvailableFactory {

    private static final FailFastHaStrategy FAIL_FAST_HA_STRATEGY = new FailFastHaStrategy();
    private static final FailOverHaStrategy FAIL_OVER_HA_STRATEGY = new FailOverHaStrategy();

    public static HaStrategy getHaStrategy(@NonNull HaStrategyEnum hastrategy) {
        if (hastrategy.equals(HaStrategyEnum.FAILFAST)) {
            return FAIL_FAST_HA_STRATEGY;
        }
        if (hastrategy.equals(HaStrategyEnum.FAILOVER)) {
            return FAIL_OVER_HA_STRATEGY;
        }
        throw new RpcException(String.format("No haStrategy [%s]", hastrategy.name()));
    }
}
