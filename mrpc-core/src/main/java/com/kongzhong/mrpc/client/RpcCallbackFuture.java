package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.exception.TimeoutException;
import com.kongzhong.mrpc.model.RpcContext;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;
import com.kongzhong.mrpc.utils.ReflectUtils;
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

    public Object get(int milliseconds) throws Exception {
        if (latch.await(milliseconds, TimeUnit.MILLISECONDS)) {
            if (null != response) {
                Map<String, String> context = response.getContext();
                context.put(Const.SERVER_HOST, this.request.getContext().get(Const.SERVER_HOST));
                context.put(Const.SERVER_PORT, this.request.getContext().get(Const.SERVER_PORT));
                RpcContext.setAttachments(context);
                if (response.getSuccess()) {
                    return response.getResult();
                } else {
                    Class<?>  expType   = ReflectUtils.from(response.getReturnType());
                    Exception exception = (Exception) JacksonSerialize.parseObject(response.getException(), expType);
                    throw exception;
                }
            }
        } else {
            long waitTime = System.currentTimeMillis() - beginTime;
            if (waitTime > milliseconds && SimpleClientHandler.callbackFutureMap.containsKey(request.getRequestId())) {
                String msg = String.format("[Request %s.%s()] timeout", request.getClassName(), request.getMethodName());
                log.warn("{}.{}() timeout", request.getClassName(), request.getMethodName());
                log.warn("RequestId: {}", request.getRequestId());
                log.warn("Invoke time: {}ms", waitTime);
                throw new TimeoutException(msg);
            }
        }
        return null;
    }

    public void done(RpcResponse response) {
        this.response = response;
        latch.countDown();
    }

}