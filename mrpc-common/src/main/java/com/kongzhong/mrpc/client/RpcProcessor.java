package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.model.RpcRequest;

/**
 * @author biezhi
 *         20/06/2017
 */
@FunctionalInterface
public interface RpcProcessor {

    Object processor(RpcRequest request) throws Exception;

}
