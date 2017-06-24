package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.exception.ServiceException;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.utils.ReflectUtils;

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
public class RpcCallbackFuture {

    private RpcRequest request;
    private RpcResponse response;
    private Lock lock = new ReentrantLock();
    private Condition finish = lock.newCondition();

    public RpcCallbackFuture(RpcRequest request) {
        this.request = request;
    }

    public Object get() throws Exception {
        return this.get(request.getWaitTimeout());
    }

    public Object get(int millisconds) throws Exception {
        try {
            lock.lock();
            finish.await(millisconds, TimeUnit.MILLISECONDS);
            if (null == response) return null;
            if (!response.getSuccess()) {
                Class<?> expType = ReflectUtils.from(response.getReturnType());
                Exception exception = (Exception) JacksonSerialize.parseObject(response.getException(), expType);
                throw new ServiceException(exception);
            }
            return response.getResult();
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