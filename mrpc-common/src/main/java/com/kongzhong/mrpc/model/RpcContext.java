package com.kongzhong.mrpc.model;

import io.netty.util.concurrent.FastThreadLocal;
import lombok.Data;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * RPC上下文
 *
 * @author biezhi
 * 2017/4/24
 */
@Data
@ToString
public class RpcContext {

    private final static FastThreadLocal<RpcRequest> REQUEST_FAST_THREAD_LOCAL = new FastThreadLocal<>();

    private final static ThreadLocal<Map<String, String>> ATTACHMENT_LOCAL = new ThreadLocal<>();

    private RpcRequest rpcRequest;

    public static RpcRequest getRpcRequest() {
        return REQUEST_FAST_THREAD_LOCAL.get();
    }

    public static void setAttachments(Map<String, String> attachments) {
        ATTACHMENT_LOCAL.remove();
        ATTACHMENT_LOCAL.set(attachments);
    }

    public static String getAttachments(String key) {
        Map<String, String> attachments = ATTACHMENT_LOCAL.get();
        if (null == attachments) {
            ATTACHMENT_LOCAL.set(new HashMap<>());
        }
        return ATTACHMENT_LOCAL.get().get(key);
    }

    public static void setRpcRequest(RpcRequest rpcRequest) {
        REQUEST_FAST_THREAD_LOCAL.set(rpcRequest);
    }

    public static void remove() {
        REQUEST_FAST_THREAD_LOCAL.remove();
    }

    public static RpcContext get() {
        return null;
    }
}
