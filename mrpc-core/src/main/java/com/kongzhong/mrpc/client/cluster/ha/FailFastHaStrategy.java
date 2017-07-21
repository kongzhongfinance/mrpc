package com.kongzhong.mrpc.client.cluster.ha;

import com.kongzhong.mrpc.client.cluster.HaStrategy;
import com.kongzhong.mrpc.client.cluster.LoadBalance;
import com.kongzhong.mrpc.model.RpcRequest;

/**
 * 快速失败策略
 *
 * @author biezhi
 *         2017/4/24
 */
public class FailFastHaStrategy implements HaStrategy {

    @Override
    public Object call(RpcRequest request, LoadBalance loadBalance) throws Exception {
        return invoke(request, loadBalance);
    }

}