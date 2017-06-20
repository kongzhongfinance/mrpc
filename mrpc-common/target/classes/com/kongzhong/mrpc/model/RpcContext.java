package com.kongzhong.mrpc.model;

import io.netty.util.concurrent.FastThreadLocal;
import lombok.Data;
import lombok.ToString;

/**
 * RPC上下文
 *
 * @author biezhi
 *         2017/4/24
 */
@Data
@ToString
public class RpcContext {

    private final static FastThreadLocal<RpcContext> contextMap = new FastThreadLocal<>();

    private RpcRequest rpcRequest;

    private RpcContext() {
    }

    public RpcContext(RpcRequest rpcRequest) {
        this.rpcRequest = rpcRequest;
    }

    public static void set(RpcContext rpcContext) {
        contextMap.set(rpcContext);
    }

    public static RpcContext get() {
        return contextMap.get();
    }

    public RpcRequest getRpcRequest() {
        return rpcRequest;
    }

    public static void remove() {
        contextMap.remove();
    }

}
