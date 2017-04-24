package com.kongzhong.mrpc.interceptor;

import com.kongzhong.mrpc.model.RpcRequest;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 拦截器执行器
 * <p>
 * Created by biezhi on 2016/12/23.
 */
@Data
public class Invocation {

    private Object target;
    private RpcRequest request;
    private Method method;
    private Object[] parameters;

    //拦截器
    private List<RpcInteceptor> interceptors;

    //当前Interceptor索引值，初始值：-1，范围：0-interceptors.size()-1
    private int currentIndex = -1;

    public Invocation(Object target, Object[] parameters, RpcRequest request, List<RpcInteceptor> interceptors) {
        this.target = target;
        this.request = request;
        this.method = request.getMethod();
        this.parameters = parameters;
        this.interceptors = interceptors;
    }

    public Object next() throws Exception {
        if (this.currentIndex == this.interceptors.size() - 1) {
            return method.invoke(target, parameters);
        } else {
            RpcInteceptor interceptor = this.interceptors.get(++this.currentIndex);
            return interceptor.execute(this);
        }
    }

}