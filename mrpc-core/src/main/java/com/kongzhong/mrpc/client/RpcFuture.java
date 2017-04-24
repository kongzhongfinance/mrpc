package com.kongzhong.mrpc.client;


import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;

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

    public Object get() throws InterruptedException {
        return this.get(10);
    }

    public Object get(int seconds) throws InterruptedException {
        try {
            lock.lock();
            finish.await(seconds, TimeUnit.SECONDS);
            return response != null ? response.getResult() : null;
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