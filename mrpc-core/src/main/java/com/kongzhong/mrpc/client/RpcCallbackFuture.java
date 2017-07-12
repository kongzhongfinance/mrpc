package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.exception.ServiceException;
import com.kongzhong.mrpc.exception.TimeoutException;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.utils.ReflectUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * RPC客户端回调
 *
 * @author biezhi
 *         2017/4/29
 */
@Slf4j
public class RpcCallbackFuture {

    private RpcRequest  request;
    private RpcResponse response;
    private Lock      lock      = new ReentrantLock();
    private Condition finish    = lock.newCondition();
    private long      startTime = System.currentTimeMillis();

    public RpcCallbackFuture(RpcRequest request) {
        this.request = request;
    }

    public Object get() throws Exception {
        return this.get(request.getWaitTimeout());
    }

    public Object get(int milliseconds) throws Exception {
        try {
            lock.lock();
            finish.await(milliseconds, TimeUnit.MILLISECONDS);

            if (null != response && response.getSuccess()) {
                return response.getResult();
            }

            // 客户端调用超时
            long time = System.currentTimeMillis() - startTime;
            if (time > milliseconds) {
                String msg = String.format("[Request %s.%s()] timeout", request.getClassName(), request.getMethodName());
                log.warn(msg + ", {}ms", time);
                throw new TimeoutException(msg);
            }

            if (null != response && !response.getSuccess()) {
                Class<?>  expType   = ReflectUtils.from(response.getReturnType());
                Exception exception = (Exception) JacksonSerialize.parseObject(response.getException(), expType);
                throw exception;
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    public void done(RpcResponse reponse) {
        try {
            lock.lock();
            finish.signal();
            this.response = reponse;
        } finally {
            lock.unlock();
        }
    }

}