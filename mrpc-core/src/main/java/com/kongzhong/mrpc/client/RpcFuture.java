package com.kongzhong.mrpc.client;


import com.kongzhong.mrpc.config.DefaultConfig;
import com.kongzhong.mrpc.exception.ServiceException;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.utils.StringUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RpcFuture {

    private RpcRequest request;
    private RpcResponse response;
    private Lock lock = new ReentrantLock();
    private Condition finish = lock.newCondition();

    public RpcFuture(RpcRequest request) {
        this.request = request;
    }

    public Object get() throws Exception {
        return this.get(DefaultConfig.serviceTimeout());
    }

    public Object get(int seconds) throws Exception {
        try {
            lock.lock();
            finish.await(seconds, TimeUnit.SECONDS);
            if (null != response) {
                if (StringUtils.isNotEmpty(response.getException())) {
                    throw new ServiceException(response.getException());
                }
                return response.getResult();
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