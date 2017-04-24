package com.kongzhong.mrpc.support.ha;

import com.kongzhong.mrpc.support.loadblance.LoadBalance;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;

/**
 * @author biezhi
 *         2017/4/24
 */
public interface HaStrategy {

    Object call(RpcRequest request, LoadBalance loadBalance);

}
