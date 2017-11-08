package com.kongzhong.mrpc.client.invoke;

import com.kongzhong.mrpc.interceptor.Invocation;
import com.kongzhong.mrpc.interceptor.RpcInterceptor;
import com.kongzhong.mrpc.interceptor.RpcClientInterceptor;
import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * 拦截器执行器
 * <p>
 * Created by biezhi on 2016/12/23.
 */
@Data
public class ClientInvocation implements Invocation {

    //拦截器
    private List<RpcClientInterceptor> interceptors;

    //当前Interceptor索引值，初始值：-1，范围：0-interceptor.size()-1
    private int currentIndex = -1;

    private RpcInvoker rpcInvoker;

    public ClientInvocation(RpcInvoker rpcInvoker, List<RpcClientInterceptor> interceptors) {
        this.rpcInvoker = rpcInvoker;
        this.interceptors = interceptors;
    }

    @Override
    public Object next() throws Exception {
        if (this.currentIndex == this.interceptors.size() - 1) {
            try {
                return rpcInvoker.invoke();
            } catch (Exception e) {
                if (e instanceof InvocationTargetException) {
                    throw (Exception) e.getCause();
                }
                throw e;
            }
        } else {
            RpcInterceptor interceptor = this.interceptors.get(++this.currentIndex);
            return interceptor.execute(this);
        }
    }

}