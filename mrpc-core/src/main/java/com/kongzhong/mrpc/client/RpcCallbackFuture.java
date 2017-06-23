package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.exception.ServiceException;
import com.kongzhong.mrpc.model.ExceptionMeta;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.utils.ReflectUtils;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
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

    public Object get(int seconds) throws Exception {
        try {
            lock.lock();
            finish.await(seconds, TimeUnit.SECONDS);
            if (null == response) return null;
            if (!response.getSuccess()) {
                throwException();
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

    /**
     * 抛异常
     *
     * @throws Exception
     */
    private void throwException() throws Exception {
        Class<?> expType = Class.forName(response.getReturnType());
        Exception exception = null;
        if (null == response.getResult()) {
            Constructor constructor = ReflectUtils.getConstructor(expType, String.class);
            exception = (Exception) constructor.newInstance(response.getException());
            throw new ServiceException(exception);
        }

        List exceptionResults = (List) response.getResult();
        Class<?>[] types = new Class[exceptionResults.size()];
        Object[] values = new Object[exceptionResults.size()];

        for (int i = 0; i < exceptionResults.size(); i++) {
            Object exceptionResult = exceptionResults.get(i);
            if (exceptionResult instanceof Map) {
                Map map = (Map) exceptionResult;
                Class<?> ftype = ReflectUtils.getClassType(map.get("type").toString());
                types[i] = ftype;
                values[i] = map.get("data");
            } else if (exceptionResult instanceof ExceptionMeta) {
                ExceptionMeta exceptionMeta = (ExceptionMeta) exceptionResult;
                Class<?> ftype = ReflectUtils.getClassType(exceptionMeta.getType());
                types[i] = ftype;
                values[i] = exceptionMeta.getData();
            }
        }

        Constructor constructor = ReflectUtils.getConstructor(expType, types);
        exception = (Exception) constructor.newInstance(values);
        throw new ServiceException(exception);
    }


}