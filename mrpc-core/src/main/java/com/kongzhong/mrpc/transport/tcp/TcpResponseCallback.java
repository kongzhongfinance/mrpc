package com.kongzhong.mrpc.transport.tcp;

import com.google.common.base.Throwables;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.exception.ServiceException;
import com.kongzhong.mrpc.interceptor.Invocation;
import com.kongzhong.mrpc.model.RpcContext;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.transport.SimpleResponseCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Tcp响应回调处理
 */
public class TcpResponseCallback extends SimpleResponseCallback<Boolean> {

    public static final Logger log = LoggerFactory.getLogger(TcpResponseCallback.class);

    public TcpResponseCallback(RpcRequest request, RpcResponse response, Map<String, Object> handlerMap) {
        super(request, response, handlerMap);
    }

    @Override
    public Boolean call() throws Exception {
        response.setRequestId(request.getRequestId());
        try {
            Object result = handle(request);
            response.setResult(result);
            return Boolean.TRUE;
        } catch (Exception e) {
            Throwable t = e instanceof InvocationTargetException ? e.getCause() : e;
            response.setException(Throwables.getStackTraceAsString(t));
            log.error("rpc server invoke error", t);
            return Boolean.FALSE;
        } finally {
            RpcContext.remove();
        }
    }

}