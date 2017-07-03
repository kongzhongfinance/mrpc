package com.kongzhong.mrpc.event;

import com.kongzhong.mrpc.model.RpcContext;

/**
 * 事件监听器
 * <p>
 * Created by biezhi on 03/07/2017.
 */
@FunctionalInterface
public interface EventListener {

    /**
     * 触发事件
     *
     * @param rpcContext
     */
    void trigger(RpcContext rpcContext);

}
