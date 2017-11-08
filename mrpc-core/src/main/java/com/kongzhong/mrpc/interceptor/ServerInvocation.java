package com.kongzhong.mrpc.interceptor;

import com.kongzhong.mrpc.model.RpcRequest;
import lombok.Data;
import org.springframework.cglib.reflect.FastMethod;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * 拦截器执行器
 * <p>
 * Created by biezhi on 2016/12/23.
 */
@Data
public class ServerInvocation implements Invocation {

    private Object target;
    private RpcRequest request;
    private FastMethod fastMethod;
    private Object[] parameters;

    //拦截器
    private List<RpcServerInterceptor> interceptors;

    //当前Interceptor索引值，初始值：-1，范围：0-interceptor.size()-1
    private int currentIndex = -1;

    public ServerInvocation(FastMethod fastMethod, Object target, Object[] parameters, RpcRequest request, List<RpcServerInterceptor> interceptors) {
        this.fastMethod = fastMethod;
        this.target = target;
        this.request = request;
        this.parameters = parameters;
        this.interceptors = interceptors;
    }

    @Override
    public Object next() throws Exception {
        if (this.currentIndex == this.interceptors.size() - 1) {
            try {
                return fastMethod.invoke(this.target, this.parameters);
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