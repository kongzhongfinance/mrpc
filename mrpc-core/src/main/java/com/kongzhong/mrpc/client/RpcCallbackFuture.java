package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.exception.SystemException;
import com.kongzhong.mrpc.exception.TimeoutException;
import com.kongzhong.mrpc.model.RpcContext;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.utils.ReflectUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * RPC客户端回调
 *
 * @author biezhi
 * 2017/4/29
 */
@Slf4j
public class RpcCallbackFuture {

    private RpcRequest     request;
    private RpcResponse    response;
    private CountDownLatch latch;
    private long           beginTime;

    public RpcCallbackFuture(RpcRequest request) {
        this.request = request;
        this.latch = new CountDownLatch(1);
        this.beginTime = System.currentTimeMillis();
    }

    public Object get(int milliseconds) throws Throwable {
        if (latch.await(milliseconds, TimeUnit.MILLISECONDS)) {
            if (null != response) {
                Map<String, String> context = response.getContext();
                // TODO: 兼容期，过后删除
                if (null != context) {
                    context.put(Const.SERVER_HOST, this.request.getContext().get(Const.SERVER_HOST));
                    context.put(Const.SERVER_PORT, this.request.getContext().get(Const.SERVER_PORT));
                    RpcContext.setAttachments(context);
                }
                if (response.getSuccess()) {
                    return response.getResult();
                } else {
                    Object object = null;
                    try {
                        Class<?> expType = ReflectUtils.from(response.getReturnType());
                        object = JacksonSerialize.parseObject(response.getException(), expType);
                    } catch (ClassNotFoundException e) {
                        if (StringUtils.isNotEmpty(context.get(Const.SERVER_EXCEPTION))) {
                            object = JacksonSerialize.parseObject(response.getException(), SystemException.class);
                        } else {
                            object = e;
                        }
                    }
                    if (object instanceof Exception) {
                        throw (Exception) object;
                    }
                    if (object instanceof Throwable) {
                        throw (Throwable) object;
                    }
                }
            }
        } else {
            long waitTime = System.currentTimeMillis() - beginTime;
            log.warn("{}.{}() timeout", request.getClassName(), request.getMethodName());
            log.warn("RequestId: {}", request.getRequestId());
            log.warn("Invoke time: {}ms", waitTime);
            String msg = String.format("[Request %s.%s()] timeout", request.getClassName(), request.getMethodName());
            throw new TimeoutException(msg);
        }
        return null;
    }

    public void done(RpcResponse response) {
        this.response = response;
        latch.countDown();
    }

}