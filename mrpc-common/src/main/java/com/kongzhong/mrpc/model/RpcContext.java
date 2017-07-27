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

    private final static FastThreadLocal<RpcRequest> requestFastThreadLocal = new FastThreadLocal<>();

    private final static ThreadLocal<Map<String, String>> attachmentLocal = new ThreadLocal<>();

    private RpcRequest rpcRequest;

    public static RpcRequest getRpcRequest() {
        return requestFastThreadLocal.get();
    }

    public static void setAttachments(Map<String, String> attachments) {
        attachmentLocal.remove();
        attachmentLocal.set(attachments);
    }

    public static String getAttachments(String key) {
        Map<String, String> attachments = attachmentLocal.get();
        if (null == attachments) {
            attachmentLocal.set(new HashMap<>());
        }
        return attachmentLocal.get().get(key);
    }

    public static void setRpcRequest(RpcRequest rpcRequest) {
        requestFastThreadLocal.set(rpcRequest);
    }

    public static void remove() {
        requestFastThreadLocal.remove();
    }

    public static RpcContext get() {
        return null;
    }
}
