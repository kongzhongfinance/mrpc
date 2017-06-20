package com.kongzhong.mrpc.interceptor;

import com.kongzhong.mrpc.client.cluster.HaStrategy;
import com.kongzhong.mrpc.client.cluster.LoadBalance;
import com.kongzhong.mrpc.model.RpcRequest;
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

    private RpcRequest request;
    private HaStrategy haStrategy;
    private LoadBalance loadBalance;

    //拦截器
    private List<RpcClientInteceptor> interceptors;

    //当前Interceptor索引值，初始值：-1，范围：0-interceptors.size()-1
    private int currentIndex = -1;

    public ClientInvocation(HaStrategy haStrategy, LoadBalance loadBalance, RpcRequest request, List<RpcClientInteceptor> interceptors) {
        this.haStrategy = haStrategy;
        this.loadBalance = loadBalance;
        this.request = request;
        this.interceptors = interceptors;
    }

    @Override
    public RpcRequest rpcRequest() {
        return request;
    }

    @Override
    public Object next() throws Exception {
        if (this.currentIndex == this.interceptors.size() - 1) {
            try {
                return haStrategy.call(request, loadBalance);
            } catch (Exception e) {
                if (e instanceof InvocationTargetException) throw (Exception) e.getCause();
                throw e;
            }
        } else {
            RpcInteceptor interceptor = this.interceptors.get(++this.currentIndex);
            return interceptor.execute(this);
        }
    }

}