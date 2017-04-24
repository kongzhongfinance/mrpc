package com.kongzhong.mrpc.model;

import com.google.common.collect.Maps;
import com.kongzhong.mrpc.exception.RpcException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.FastThreadLocal;
import lombok.Data;
import lombok.ToString;

import java.util.Map;

/**
 * @author biezhi
 *         2017/4/24
 */
@Data
@ToString
public class RpcContext {

    private final static FastThreadLocal<RpcContext> contextMap = new FastThreadLocal<>();

    private Map<String, String> attrs = Maps.newConcurrentMap();

    private ChannelHandlerContext ctx;

    private RpcContext() {
    }

    public static RpcContext get() {
        RpcContext rpcContext = contextMap.get();
        if (rpcContext == null) {
            throw new RpcException("local thread context is null");
        }
        return rpcContext;
    }

    public static void set() {
        contextMap.set(new RpcContext());
    }

    public static void remove() {
        contextMap.remove();
    }

    public static void attr(String key, String value) {
        get().attrs.put(key, value);
    }

    public static String attr(String key) {
        return get().attrs.get(key);
    }

}
