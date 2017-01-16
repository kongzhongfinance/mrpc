package junicorn.mrpc.client.proxy;

import junicorn.mrpc.async.RpcFuture;
import junicorn.mrpc.loadbalance.Strategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ObjectProxy extends AbstractProxy implements InvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectProxy.class);

    public ObjectProxy(Strategy strategy) {
        super(strategy);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return super.invoke(method, args);
    }

    @Override
    public <T> T createBean(Class<T> type) throws Exception {
        return null;
    }

    @Override
    public RpcFuture call(String funcName, Object... args) {
        return null;
    }

}
