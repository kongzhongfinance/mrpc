package junicorn.mrpc.client.proxy;

import junicorn.mrpc.async.RpcFuture;
import junicorn.mrpc.client.RpcClient;
import junicorn.mrpc.loadbalance.Strategy;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class CglibProxy extends AbstractProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(CglibProxy.class);

    public CglibProxy(RpcClient rpcClient){
        super(rpcClient);
    }

    @Override
    public <T> T createBean(Class<T> interfaceClass) throws Exception {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(interfaceClass);
        enhancer.setCallback(new MethodInterceptor() {
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                if(method.getDeclaringClass() == Object.class){
                    return method.invoke(this, objects);
                } else {
                    return CglibProxy.super.invoke(method, objects);
                }
            }
        });
        return interfaceClass.cast(enhancer.create());
    }

    @Override
    public RpcFuture call(String funcName, Object... args) {
        return null;
    }
}
