package junicorn.mrpc.client.proxy;

import junicorn.mrpc.async.RpcFuture;
import junicorn.mrpc.client.RpcClient;
import junicorn.mrpc.loadbalance.Strategy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Jdk代理实现
 */
public class JdkProxy extends AbstractProxy implements InvocationHandler {

    public JdkProxy(RpcClient rpcClient) {
        super(rpcClient);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return super.invoke(method, args);
    }

    @Override
    public <T> T createBean(Class<T> type) throws Exception {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, this);
    }

    @Override
    public RpcFuture call(String funcName, Object... args) {
        return null;
    }
}
